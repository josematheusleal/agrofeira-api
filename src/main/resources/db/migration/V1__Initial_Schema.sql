-- ==============================================================================
-- 1. LIMPEZA E CONFIGURAÇÃO INICIAL
-- ==============================================================================
CREATE
    EXTENSION IF NOT EXISTS "pgcrypto";

-- Drop de Tabelas (Ordem reversa de dependência)
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS relatorios CASCADE;
DROP TABLE IF EXISTS repasses CASCADE;
DROP TABLE IF EXISTS pagamentos CASCADE;
DROP TABLE IF EXISTS fila_rateio CASCADE;
DROP TABLE IF EXISTS rateios_resultado CASCADE;
DROP TABLE IF EXISTS itens_pedido CASCADE;
DROP TABLE IF EXISTS pedidos CASCADE;
DROP TABLE IF EXISTS ofertas_estoque CASCADE;
DROP TABLE IF EXISTS feiras CASCADE;
DROP TABLE IF EXISTS enderecos CASCADE;
DROP TABLE IF EXISTS zonas_entrega CASCADE;
DROP TABLE IF EXISTS produtos CASCADE;
DROP TABLE IF EXISTS usuario_perfil CASCADE;
DROP TABLE IF EXISTS perfis CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TABLE IF EXISTS feira_produtos_elegiveis CASCADE;
DROP TABLE IF EXISTS feira_comerciantes_elegiveis CASCADE;

-- Drop de Tipos ENUM
DROP TYPE IF EXISTS enum_status_feira CASCADE;
DROP TYPE IF EXISTS enum_status_pedido CASCADE;
DROP TYPE IF EXISTS enum_tipo_retirada CASCADE;
DROP TYPE IF EXISTS enum_status_pagamento CASCADE;
DROP TYPE IF EXISTS enum_status_processamento CASCADE;

-- ==============================================================================
-- 2. CRIAÇÃO DE DOMÍNIOS (TIPOS ENUM)
-- ==============================================================================
CREATE TYPE enum_status_feira AS ENUM ('RASCUNHO', 'ABERTA', 'ENCERRADA', 'FINALIZADA', 'CANCELADA');
CREATE TYPE enum_status_pedido AS ENUM ('PENDENTE', 'AGUARDANDO_SEPARACAO', 'PRONTO_RETIRADA', 'SAIU_ENTREGA', 'ENTREGUE', 'CANCELADO');
CREATE TYPE enum_tipo_retirada AS ENUM ('LOCAL', 'ENTREGA');
CREATE TYPE enum_status_pagamento AS ENUM ('PENDENTE', 'PAGO', 'ESTORNADO', 'CANCELADO');
CREATE TYPE enum_status_processamento AS ENUM ('PENDENTE', 'CONCLUIDO', 'FALHA');

-- ==============================================================================
-- 3. CRIAÇÃO DAS TABELAS (DDL)
-- ==============================================================================

-- 3.1. Identidade e Acessos
CREATE TABLE perfis
(
    id   SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuarios
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    nome          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) UNIQUE,
    telefone      VARCHAR(20) UNIQUE,
    descricao     TEXT,
    senha_hash    VARCHAR(255) NOT NULL,
    ativo         BOOLEAN               DEFAULT TRUE,
    versao        BIGINT                DEFAULT 0,
    criado_em     TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP    NOT NULL DEFAULT NOW(),
    deletado_em   TIMESTAMP
);

CREATE TABLE usuario_perfil
(
    usuario_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    perfil_id  INT  NOT NULL REFERENCES perfis (id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, perfil_id)
);

-- 3.2. Logística e Endereçamento
CREATE TABLE zonas_entrega
(
    id            UUID PRIMARY KEY                 DEFAULT gen_random_uuid(),
    nome          VARCHAR(100)   NOT NULL          DEFAULT 'ZONA_PADRAO',
    taxa          NUMERIC(10, 2) NOT NULL
        CONSTRAINT chk_zona_taxa CHECK (taxa >= 0) DEFAULT 7.00,
    ativo         BOOLEAN                          DEFAULT TRUE,
    versao        BIGINT                           DEFAULT 0,
    criado_em     TIMESTAMP      NOT NULL          DEFAULT NOW(),
    atualizado_em TIMESTAMP      NOT NULL          DEFAULT NOW(),
    deletado_em   TIMESTAMP
);

CREATE TABLE enderecos
(
    usuario_id      UUID PRIMARY KEY REFERENCES usuarios (id) ON DELETE CASCADE,
    rua             VARCHAR(255),
    numero          VARCHAR(20),
    complemento     VARCHAR(255),
    bairro          VARCHAR(100),
    cidade          VARCHAR(255),
    estado          VARCHAR(2),
    cep             VARCHAR(10),
    zona_entrega_id UUID      NOT NULL REFERENCES zonas_entrega (id),
    criado_em       TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 3.3. Catálogo e Eventos (Com Auditoria)
CREATE TABLE produtos
(
    id             UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    nome           VARCHAR(255)   NOT NULL,
    categoria      VARCHAR(100),
    unidade_medida VARCHAR(50)    NOT NULL,
    preco_base     NUMERIC(10, 2) NOT NULL
        CONSTRAINT chk_produto_preco CHECK (preco_base >= 0),
    ativo          BOOLEAN                 DEFAULT TRUE,
    versao         BIGINT                  DEFAULT 0,
    criado_em      TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em  TIMESTAMP      NOT NULL DEFAULT NOW(),
    deletado_em    TIMESTAMP
);

CREATE TABLE feiras
(
    id            UUID PRIMARY KEY           DEFAULT gen_random_uuid(),
    data_hora     TIMESTAMP         NOT NULL,
    status        enum_status_feira NOT NULL DEFAULT 'RASCUNHO',
    ativo         BOOLEAN                    DEFAULT TRUE,
    versao        BIGINT                     DEFAULT 0,
    criado_em     TIMESTAMP         NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP         NOT NULL DEFAULT NOW(),
    deletado_em   TIMESTAMP
);

CREATE TABLE ofertas_estoque
(
    id                   UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    feira_id             UUID           NOT NULL REFERENCES feiras (id),
    comerciante_id       UUID           NOT NULL REFERENCES usuarios (id),
    produto_id           UUID           NOT NULL REFERENCES produtos (id),
    quantidade_ofertada  NUMERIC(10, 2) NOT NULL
        CONSTRAINT chk_oferta_qtd CHECK (quantidade_ofertada >= 0),
    quantidade_reservada NUMERIC(10, 2) NOT NULL DEFAULT 0
        CONSTRAINT chk_reserva_qtd CHECK (quantidade_reservada >= 0),
    criado_em            TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em        TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT unq_oferta_feira UNIQUE (feira_id, comerciante_id, produto_id)
);

-- 3.4. Vendas e Transações
CREATE TABLE pedidos
(
    id             UUID PRIMARY KEY            DEFAULT gen_random_uuid(),
    feira_id       UUID               NOT NULL REFERENCES feiras (id),
    consumidor_id  UUID               NOT NULL REFERENCES usuarios (id),
    status         enum_status_pedido NOT NULL DEFAULT 'PENDENTE',
    tipo_retirada  enum_tipo_retirada NOT NULL DEFAULT 'LOCAL',
    taxa_entrega   NUMERIC(10, 2)     NOT NULL DEFAULT 0
        CONSTRAINT chk_pedido_taxa CHECK (taxa_entrega >= 0),
    valor_produtos NUMERIC(10, 2)     NOT NULL DEFAULT 0
        CONSTRAINT chk_pedido_v_produtos CHECK (valor_produtos >= 0),
    valor_total    NUMERIC(10, 2)     NOT NULL DEFAULT 0
        CONSTRAINT chk_pedido_v_total CHECK (valor_total >= 0),
    versao         BIGINT                      DEFAULT 0,
    criado_em      TIMESTAMP          NOT NULL DEFAULT NOW(),
    atualizado_em  TIMESTAMP          NOT NULL DEFAULT NOW(),
    deletado_em    TIMESTAMP
);

CREATE TABLE itens_pedido
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id      UUID           NOT NULL REFERENCES pedidos (id) ON DELETE CASCADE,
    produto_id     UUID           NOT NULL REFERENCES produtos (id),
    quantidade     NUMERIC(10, 2) NOT NULL
        CONSTRAINT chk_item_qtd CHECK (quantidade > 0),
    valor_unitario NUMERIC(10, 2) NOT NULL
        CONSTRAINT chk_item_valor CHECK (valor_unitario >= 0),
    nome_item      VARCHAR(255)   NOT NULL,
    unidade_medida VARCHAR(50)    NOT NULL
);

CREATE TABLE pagamentos
(
    id            UUID PRIMARY KEY               DEFAULT gen_random_uuid(),
    pedido_id     UUID                  NOT NULL REFERENCES pedidos (id),
    valor         NUMERIC(10, 2)        NOT NULL,
    metodo        VARCHAR(50),
    status        enum_status_pagamento NOT NULL DEFAULT 'PENDENTE',
    pago_em       TIMESTAMP,
    ativo         BOOLEAN                        DEFAULT TRUE,
    versao        BIGINT                         DEFAULT 0,
    criado_em     TIMESTAMP             NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP             NOT NULL DEFAULT NOW(),
    deletado_em   TIMESTAMP
);

-- 3.5. Motor de Rateio Cooperativo e Financeiro
CREATE TABLE rateios_resultado
(
    id                     UUID PRIMARY KEY                   DEFAULT gen_random_uuid(),
    feira_id               UUID                      NOT NULL REFERENCES feiras (id),
    comerciante_id         UUID                      NOT NULL REFERENCES usuarios (id),
    produto_id             UUID                      NOT NULL REFERENCES produtos (id),
    quantidade_sequestrada NUMERIC(10, 2)            NOT NULL
        CONSTRAINT chk_rateio_sequestro CHECK (quantidade_sequestrada >= 0),
    valor_bruto_venda      NUMERIC(10, 2)            NOT NULL
        CONSTRAINT chk_rateio_bruto CHECK (valor_bruto_venda >= 0),
    status_processamento   enum_status_processamento NOT NULL DEFAULT 'CONCLUIDO',
    criado_em              TIMESTAMP                 NOT NULL DEFAULT NOW(),
    atualizado_em          TIMESTAMP                 NOT NULL DEFAULT NOW(),
    CONSTRAINT unq_rateio UNIQUE (feira_id, comerciante_id, produto_id)
);

CREATE TABLE fila_rateio
(
    id                 UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    comerciante_id     UUID           NOT NULL REFERENCES usuarios (id),
    produto_id         UUID           NOT NULL REFERENCES produtos (id),
    feira_origem_id    UUID           NOT NULL REFERENCES feiras (id),
    quantidade_deficit NUMERIC(10, 2) NOT NULL
        CONSTRAINT chk_fila_deficit CHECK (quantidade_deficit > 0),
    compensado         BOOLEAN                 DEFAULT FALSE,
    criado_em          TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em      TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE repasses
(
    id                  UUID PRIMARY KEY               DEFAULT gen_random_uuid(),
    rateio_resultado_id UUID                  NOT NULL REFERENCES rateios_resultado (id),
    comerciante_id      UUID                  NOT NULL REFERENCES usuarios (id),
    valor_bruto         NUMERIC(10, 2)        NOT NULL
        CONSTRAINT chk_repasse_bruto CHECK (valor_bruto >= 0),
    taxa_associacao     NUMERIC(10, 2)        NOT NULL DEFAULT 0
        CONSTRAINT chk_repasse_taxa CHECK (taxa_associacao >= 0),
    valor_liquido       NUMERIC(10, 2)        NOT NULL
        CONSTRAINT chk_repasse_liquido CHECK (valor_liquido >= 0),
    status              enum_status_pagamento NOT NULL DEFAULT 'PENDENTE',
    repassado_em        TIMESTAMP,
    criado_em           TIMESTAMP             NOT NULL DEFAULT NOW(),
    atualizado_em       TIMESTAMP             NOT NULL DEFAULT NOW()
);

-- Diz quais Produtos podem ser vendidos nesta feira
CREATE TABLE feira_produtos_elegiveis
(
    feira_id      UUID      NOT NULL REFERENCES feiras (id) ON DELETE CASCADE,
    produto_id    UUID      NOT NULL REFERENCES produtos (id) ON DELETE CASCADE,
    criado_em     TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (feira_id, produto_id)
);

-- Diz quais Comerciantes podem participar desta feira
CREATE TABLE feira_comerciantes_elegiveis
(
    feira_id       UUID      NOT NULL REFERENCES feiras (id) ON DELETE CASCADE,
    comerciante_id UUID      NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    criado_em      TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em  TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (feira_id, comerciante_id)
);

-- 3.6. Relatórios e Auditoria
CREATE TABLE relatorios
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    versao        BIGINT                DEFAULT 0,
    titulo        VARCHAR(255) NOT NULL,
    tipo          VARCHAR(50)  NOT NULL,
    conteudo      TEXT,
    ativo         BOOLEAN               DEFAULT TRUE,
    criado_em     TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP    NOT NULL DEFAULT NOW(),
    deletado_em   TIMESTAMP
);

-- 3.7. Segurança e Sessão
CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY,
    token      VARCHAR(255) UNIQUE NOT NULL,
    usuario_id UUID                NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    expiracao  TIMESTAMP           NOT NULL,
    revogado   BOOLEAN             NOT NULL DEFAULT FALSE,
    criado_em  TIMESTAMP           NOT NULL DEFAULT NOW()
);
CREATE TABLE password_reset_tokens
(
    id         UUID PRIMARY KEY,
    token      VARCHAR(255) UNIQUE NOT NULL,
    usuario_id UUID                NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    expiracao  TIMESTAMP           NOT NULL,
    usado      BOOLEAN             NOT NULL DEFAULT FALSE,
    criado_em  TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- ==============================================================================
-- 4. ÍNDICES DE PERFORMANCE (OTIMIZAÇÃO DE LEITURA)
-- ==============================================================================

-- Navegação rápida em Pedidos
CREATE INDEX idx_pedidos_feira_id ON pedidos (feira_id);
CREATE INDEX idx_pedidos_consumidor_id ON pedidos (consumidor_id);

-- Carregamento de Itens do Pedido
CREATE INDEX idx_itens_pedido_id ON itens_pedido (pedido_id);

-- Relatórios e Agregações da Feira
CREATE INDEX idx_ofertas_feira_id ON ofertas_estoque (feira_id);
CREATE INDEX idx_rateios_feira_id ON rateios_resultado (feira_id);

-- Otimização extrema para a Fila FIFO (Busca parcial focada apenas no que precisa ser pago)
CREATE INDEX idx_fila_rateio_comerciante ON fila_rateio (comerciante_id);
CREATE INDEX idx_fila_rateio_pendentes ON fila_rateio (compensado) WHERE compensado = FALSE;

-- Índices para acelerar buscas de tokens e validação de sessão

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_usuario_id ON refresh_tokens (usuario_id);

-- Índices para acelerar buscas de tokens de recuperação de senha

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens (token);
CREATE INDEX idx_password_reset_tokens_usuario_id ON password_reset_tokens (usuario_id);

-- ==============================================================================
-- 5. DADOS INICIAIS (SEEDING MANDATÓRIO)
-- ==============================================================================

INSERT INTO perfis (id, nome)
VALUES (1, 'ADMINISTRADOR'),
       (2, 'GERENCIADOR'),
       (3, 'COMERCIANTE'),
       (4, 'CONSUMIDOR')
ON CONFLICT (nome) DO NOTHING;

