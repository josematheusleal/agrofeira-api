package br.edu.ufape.agrofeira.dto.mapper

import br.edu.ufape.agrofeira.domain.entity.*
import br.edu.ufape.agrofeira.dto.response.*

fun Usuario.toDTO() =
    UsuarioDTO(
        id = id,
        nome = nome,
        email = email,
        telefone = telefone,
        descricao = descricao,
        perfis = perfis.map { it.nome }.toSet(),
    )

fun Usuario.toDetalhadoDTO(endereco: Endereco? = null) =
    UsuarioDetalhadoDTO(
        id = id,
        nome = nome,
        email = email,
        telefone = telefone,
        descricao = descricao,
        perfis = perfis.map { it.nome }.toSet(),
        endereco = endereco?.toDTO(),
    )

fun Produto.toDTO() =
    ProdutoDTO(
        id = id,
        nome = nome,
        categoria = categoria.name,
        unidadeMedida = unidadeMedida.name,
        precoBase = precoBase,
    )

fun Feira.toDTO() =
    FeiraDTO(
        id = id,
        dataHora = dataHora,
        status = status.name,
        ativa = ativo,
    )

fun Pedido.toDTO(itens: List<ItemPedido>) =
    PedidoDTO(
        id = id,
        feiraId = feira.id,
        consumidorNome = consumidor.nome,
        status = status.name,
        tipoRetirada = tipoRetirada.name,
        valorProdutos = valorProdutos,
        taxaEntrega = taxaEntrega,
        valorTotal = valorTotal,
        itens = itens.map { it.toDTO() },
        criadoEm = criadoEm,
    )

fun ItemPedido.toDTO() =
    ItemPedidoDTO(
        produtoId = produto.id,
        nomeItem = nomeItem,
        unidadeMedida = unidadeMedida.name,
        quantidade = quantidade,
        valorUnitario = valorUnitario,
        valorTotal = valorUnitario.multiply(quantidade),
    )

fun Pagamento.toDTO() =
    PagamentoDTO(
        id = id,
        pedidoId = pedido.id,
        valor = valor,
        metodo = metodo,
        status = status.name,
        pagoEm = pagoEm,
        criadoEm = criadoEm,
    )

fun ZonaEntrega.toDTO() =
    ZonaEntregaDTO(
        id = id,
        nome = nome,
        taxa = taxa,
        ativo = ativo,
    )

fun Endereco.toDTO() =
    EnderecoDTO(
        usuarioId = usuarioId!!,
        rua = rua,
        numero = numero,
        complemento = complemento,
        bairro = bairro,
        cidade = cidade,
        estado = estado,
        cep = cep,
        zonaEntrega = zonaEntrega?.toDTO(),
    )

fun Relatorio.toDTO() =
    RelatorioDTO(
        id = id,
        titulo = titulo,
        tipo = tipo.name,
        conteudo = conteudo,
        criadoEm = criadoEm,
    )
