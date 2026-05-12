package br.edu.ufape.agrofeira.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Dados detalhados do endereço")
data class EnderecoDTO(
    @Schema(description = "ID do usuário dono do endereço")
    val usuarioId: UUID,
    @Schema(description = "Rua informada", example = "Rua das Flores")
    val rua: String?,
    @Schema(description = "Número", example = "123")
    val numero: String?,
    @Schema(description = "Complemento", example = "Apto 101")
    val complemento: String?,
    @Schema(description = "Bairro oficial (ViaCep)", example = "Heliópolis")
    val bairro: String?,
    @Schema(description = "Cidade", example = "Garanhuns")
    val cidade: String?,
    @Schema(description = "Estado", example = "PE")
    val estado: String?,
    @Schema(description = "CEP", example = "55290000")
    val cep: String?,
    @Schema(description = "Dados da Zona de Entrega vinculada (Taxa)")
    val zonaEntrega: ZonaEntregaDTO?,
)
