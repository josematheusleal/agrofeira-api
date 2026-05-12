package br.edu.ufape.agrofeira.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Schema(description = "Dados resumidos do usuário")
data class UsuarioDTO(
    val id: UUID,
    val nome: String,
    val email: String?,
    val telefone: String?,
    @Schema(description = "Descrição ou biografia do usuário/comerciante")
    val descricao: String?,
    val perfis: Set<String>,
)

@Schema(description = "Dados detalhados do usuário (incluindo endereço)")
data class UsuarioDetalhadoDTO(
    val id: UUID,
    val nome: String,
    val email: String?,
    val telefone: String?,
    @Schema(description = "Descrição ou biografia do usuário/comerciante")
    val descricao: String?,
    val perfis: Set<String>,
    @Schema(description = "Dados do endereço do usuário")
    val endereco: EnderecoDTO?,
)

@Schema(description = "Dados detalhados do produto")
data class ProdutoDTO(
    val id: UUID,
    val nome: String,
    val categoria: String?,
    val unidadeMedida: String,
    val precoBase: BigDecimal,
)

@Schema(description = "Dados da feira")
data class FeiraDTO(
    val id: UUID,
    val dataHora: LocalDateTime,
    val status: String,
    val ativa: Boolean,
)

@Schema(description = "Dados do pedido")
data class PedidoDTO(
    val id: UUID,
    val feiraId: UUID,
    val consumidorNome: String,
    val status: String,
    val tipoRetirada: String,
    val valorProdutos: BigDecimal,
    val taxaEntrega: BigDecimal,
    val valorTotal: BigDecimal,
    val itens: List<ItemPedidoDTO>,
    val criadoEm: LocalDateTime,
)

@Schema(description = "Item detalhado do pedido (Snapshot)")
data class ItemPedidoDTO(
    val produtoId: UUID,
    val nomeItem: String,
    val unidadeMedida: String,
    val quantidade: BigDecimal,
    val valorUnitario: BigDecimal,
    val valorTotal: BigDecimal,
)

@Schema(description = "Dados do pagamento")
data class PagamentoDTO(
    val id: UUID,
    val pedidoId: UUID,
    val valor: BigDecimal,
    val metodo: String?,
    val status: String,
    val pagoEm: LocalDateTime?,
    val criadoEm: LocalDateTime,
)

@Schema(description = "Dados da zona de entrega")
data class ZonaEntregaDTO(
    val id: UUID,
    val nome: String,
    val taxa: BigDecimal,
    val ativo: Boolean,
)

@Schema(description = "Dados do relatório")
data class RelatorioDTO(
    val id: UUID,
    val titulo: String,
    val tipo: String,
    val conteudo: String?,
    val criadoEm: LocalDateTime,
)

@Schema(description = "Opção de domínio para seleção")
data class OpcaoDTO(
    val value: String,
    val label: String,
)

@Schema(description = "Conjunto de opções para cadastro de produtos")
data class ItensOpcoesDTO(
    val categorias: List<OpcaoDTO>,
    val unidadesMedida: List<OpcaoDTO>,
)
