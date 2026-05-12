package br.edu.ufape.agrofeira.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

@Schema(description = "Dados para cadastro/atualização de zona de entrega")
data class ZonaEntregaRequest(
    @field:NotBlank(message = "O nome da zona é obrigatório")
    @Schema(description = "Nome identificador da zona", example = "ZONA_PROXIMA")
    val nome: String,
    @field:NotNull(message = "A taxa é obrigatória")
    @field:PositiveOrZero(message = "A taxa não pode ser negativa")
    @Schema(description = "Taxa de entrega para esta zona", example = "7.00")
    val taxa: BigDecimal,
)
