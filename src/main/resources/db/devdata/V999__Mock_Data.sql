INSERT INTO usuarios (id, nome, email, telefone, senha_hash, descricao)
VALUES ('aaaa1111-0000-0000-0000-000000000001', 'LMTS Admin', 'lmts@ufape.edu.br', null,
        '$2a$10$MwVBHxu9EH4ScRxjK8nL.OG.9PmLiGhPrRt3Cb7gqcL7bONiK3Rr6', 'Administrador do Sistema'),
       ('aaaa1111-0000-0000-0000-000000000002', 'Jucelino (Gerenciador)', 'jucelino@agro.com', '87911111111',
        '$2a$10$MwVBHxu9EH4ScRxjK8nL.OG.9PmLiGhPrRt3Cb7gqcL7bONiK3Rr6', 'Gerenciador da Feira'),
-- Comerciante APENAS com telefone (Sem E-mail)
       ('bbbb2222-0000-0000-0000-000000000001', 'João Agricultor', NULL, '87922222222',
        '$2a$10$MwVBHxu9EH4ScRxjK8nL.OG.9PmLiGhPrRt3Cb7gqcL7bONiK3Rr6', 'Sítio Boa Esperança'),
-- Comerciante APENAS com e-mail (Sem Telefone)
       ('bbbb2222-0000-0000-0000-000000000002', 'Maria da Horta', 'maria@horta.com', NULL,
        '$2a$10$MwVBHxu9EH4ScRxjK8nL.OG.9PmLiGhPrRt3Cb7gqcL7bONiK3Rr6', 'Hortaliças Orgânicas'),
-- Consumidor com Tudo Preenchido
       ('cccc3333-0000-0000-0000-000000000001', 'Carlos Cliente', 'carlos@email.com', '87944444444',
        '$2a$10$MwVBHxu9EH4ScRxjK8nL.OG.9PmLiGhPrRt3Cb7gqcL7bONiK3Rr6', NULL),
-- Consumidora SEM e-mail e SEM telefone (Cadastro Ultra-Rápido da V1)
       ('cccc3333-0000-0000-0000-000000000002', 'Dona Ana Consumidora', NULL, NULL,
        '$2a$10$MwVBHxu9EH4ScRxjK8nL.OG.9PmLiGhPrRt3Cb7gqcL7bONiK3Rr6', NULL);

INSERT INTO usuario_perfil (usuario_id, perfil_id)
VALUES ('aaaa1111-0000-0000-0000-000000000001', 1),
       ('aaaa1111-0000-0000-0000-000000000002', 2),
       ('bbbb2222-0000-0000-0000-000000000001', 3),
       ('bbbb2222-0000-0000-0000-000000000002', 3),
       ('cccc3333-0000-0000-0000-000000000001', 4),
       ('cccc3333-0000-0000-0000-000000000002', 4);

-- ==============================================================================
-- 5.2. LOGÍSTICA (Zonas e Endereços de Garanhuns)
-- ==============================================================================

INSERT INTO zonas_entrega (id, nome, taxa)
VALUES ('dddd4444-0000-0000-0000-000000000007', 'ZONA_PROXIMA', 7.00),
       ('dddd4444-0000-0000-0000-000000000010', 'ZONA_AFASTADA', 10.00);

-- Endereços (PK é o próprio usuario_id)
INSERT INTO enderecos (usuario_id, zona_entrega_id, rua, numero, bairro, cidade, estado, cep)
VALUES ('cccc3333-0000-0000-0000-000000000001', 'dddd4444-0000-0000-0000-000000000007', 'Rua das Flores', '123',
        'Centro', 'Garanhuns', 'PE', '55290000'),
       ('cccc3333-0000-0000-0000-000000000002', 'dddd4444-0000-0000-0000-000000000010', 'Av. Rui Barbosa', '456',
        'Heliópolis', 'Garanhuns', 'PE', '55291000');

-- ==============================================================================
-- 5.3. CATÁLOGO BASE DE PRODUTOS
-- ==============================================================================

INSERT INTO produtos (id, nome, categoria, unidade_medida, preco_base)
VALUES ('eeee5555-0000-0000-0000-000000000001', 'Tomate Orgânico', 'HORTIFRUTI', 'QUILO', 6.00),
       ('eeee5555-0000-0000-0000-000000000002', 'Alface Crespa', 'HORTIFRUTI', 'UNIDADE', 3.00),
       ('eeee5555-0000-0000-0000-000000000003', 'Queijo Coalho', 'LATICINIOS', 'QUILO', 35.00);


-- ==============================================================================
-- 5.4. FEIRA 1 (CICLO COMPLETO E FINALIZADO NO PASSADO)
-- ==============================================================================

INSERT INTO feiras (id, data_hora, status)
VALUES ('ffff6666-0000-0000-0000-000000000001', '2026-03-10 08:00:00', 'FINALIZADA');

-- Definindo quem e o que pode vender (Elegibilidade)
INSERT INTO feira_produtos_elegiveis (feira_id, produto_id)
VALUES ('ffff6666-0000-0000-0000-000000000001', 'eeee5555-0000-0000-0000-000000000001'); -- Só Tomate

INSERT INTO feira_comerciantes_elegiveis (feira_id, comerciante_id)
VALUES ('ffff6666-0000-0000-0000-000000000001', 'bbbb2222-0000-0000-0000-000000000001'), -- João
       ('ffff6666-0000-0000-0000-000000000001', 'bbbb2222-0000-0000-0000-000000000002');
-- Maria

-- Ofertas Submetidas
INSERT INTO ofertas_estoque (feira_id, comerciante_id, produto_id, quantidade_ofertada, quantidade_reservada)
VALUES ('ffff6666-0000-0000-0000-000000000001', 'bbbb2222-0000-0000-0000-000000000001',
        'eeee5555-0000-0000-0000-000000000001', 20.00, 0), -- João 20kg
       ('ffff6666-0000-0000-0000-000000000001', 'bbbb2222-0000-0000-0000-000000000002',
        'eeee5555-0000-0000-0000-000000000001', 10.00, 0);
-- Maria 10kg

-- Pedido Finalizado de Carlos (10kg Tomate = R$60 + R$5 Frete)
INSERT INTO pedidos (id, feira_id, consumidor_id, status, tipo_retirada, taxa_entrega, valor_produtos, valor_total)
VALUES ('11117777-0000-0000-0000-000000000001', 'ffff6666-0000-0000-0000-000000000001',
        'cccc3333-0000-0000-0000-000000000001', 'ENTREGUE', 'ENTREGA', 5.00, 60.00, 65.00);

INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, valor_unitario, nome_item, unidade_medida)
VALUES ('11117777-0000-0000-0000-000000000001', 'eeee5555-0000-0000-0000-000000000001', 10.00, 6.00, 'Tomate Orgânico',
        'QUILO');

INSERT INTO pagamentos (pedido_id, valor, metodo, status, pago_em)
VALUES ('11117777-0000-0000-0000-000000000001', 65.00, 'PIX', 'PAGO', '2026-03-09 15:30:00');

-- Rateios Feira 1 (Motor de Rateio dividiu 5kg para cada)
INSERT INTO rateios_resultado (id, feira_id, comerciante_id, produto_id, quantidade_sequestrada, valor_bruto_venda,
                               status_processamento)
VALUES ('22228888-0000-0000-0000-000000000001', 'ffff6666-0000-0000-0000-000000000001',
        'bbbb2222-0000-0000-0000-000000000001', 'eeee5555-0000-0000-0000-000000000001', 5.00, 30.00, 'CONCLUIDO'),
       ('22228888-0000-0000-0000-000000000002', 'ffff6666-0000-0000-0000-000000000001',
        'bbbb2222-0000-0000-0000-000000000002', 'eeee5555-0000-0000-0000-000000000001', 5.00, 30.00, 'CONCLUIDO');

-- Repasses Cooperativos (Paga João e Maria, Associação retém 10%)
INSERT INTO repasses (rateio_resultado_id, comerciante_id, valor_bruto, taxa_associacao, valor_liquido, status,
                      repassado_em)
VALUES ('22228888-0000-0000-0000-000000000001', 'bbbb2222-0000-0000-0000-000000000001', 30.00, 3.00, 27.00, 'PAGO',
        '2026-03-12 10:00:00'),
       ('22228888-0000-0000-0000-000000000002', 'bbbb2222-0000-0000-0000-000000000002', 30.00, 3.00, 27.00, 'PAGO',
        '2026-03-12 10:05:00');

-- Fila Rateio (Uma "sobra" de Alface matemática que ficou para a Maria)
INSERT INTO fila_rateio (comerciante_id, produto_id, feira_origem_id, quantidade_deficit, compensado)
VALUES ('bbbb2222-0000-0000-0000-000000000002', 'eeee5555-0000-0000-0000-000000000002',
        'ffff6666-0000-0000-0000-000000000001', 1.00, FALSE);


-- ==============================================================================
-- 5.5. FEIRA 2 (STATUS ABERTA - ACONTECENDO AGORA)
-- ==============================================================================

INSERT INTO feiras (id, data_hora, status)
VALUES ('ffff6666-0000-0000-0000-000000000002', '2026-04-15 08:00:00', 'ABERTA');

-- Elegibilidade Feira 2 (Libera Alface e Queijo. Apenas João participa).
INSERT INTO feira_produtos_elegiveis (feira_id, produto_id)
VALUES ('ffff6666-0000-0000-0000-000000000002', 'eeee5555-0000-0000-0000-000000000002'),
       ('ffff6666-0000-0000-0000-000000000002', 'eeee5555-0000-0000-0000-000000000003');

INSERT INTO feira_comerciantes_elegiveis (feira_id, comerciante_id)
VALUES ('ffff6666-0000-0000-0000-000000000002', 'bbbb2222-0000-0000-0000-000000000001');

-- Ofertas de João na Feira 2 (Com carrinho travando o estoque de Alface!)
INSERT INTO ofertas_estoque (feira_id, comerciante_id, produto_id, quantidade_ofertada, quantidade_reservada)
VALUES ('ffff6666-0000-0000-0000-000000000002', 'bbbb2222-0000-0000-0000-000000000001',
        'eeee5555-0000-0000-0000-000000000002', 30.00, 2.00), -- 30 Alfaces (2 Reservados no Carrinho)
       ('ffff6666-0000-0000-0000-000000000002', 'bbbb2222-0000-0000-0000-000000000001',
        'eeee5555-0000-0000-0000-000000000003', 15.00, 0.00);
-- 15kg Queijo (Nenhuma reserva)

-- Carrinho Aberto da Dona Ana
INSERT INTO pedidos (id, feira_id, consumidor_id, status, tipo_retirada, taxa_entrega, valor_produtos, valor_total)
VALUES ('11117777-0000-0000-0000-000000000002', 'ffff6666-0000-0000-0000-000000000002',
        'cccc3333-0000-0000-0000-000000000002', 'PENDENTE', 'LOCAL', 0.00, 6.00, 6.00);

-- Os 2 Alfaces que estão ativando a reserva lá na oferta
INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, valor_unitario, nome_item, unidade_medida)
VALUES ('11117777-0000-0000-0000-000000000002', 'eeee5555-0000-0000-0000-000000000002', 2.00, 3.00, 'Alface Crespa',
        'UNIDADE');
