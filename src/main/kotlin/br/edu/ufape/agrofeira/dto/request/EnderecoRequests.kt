package br.edu.ufape.agrofeira.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.util.*

@Schema(description = "Dados para cadastro ou atualização de endereço")
data class EnderecoRequest(
    @Schema(description = "Nome da rua/logradouro (Preenchido via CEP se vazio)", example = "Rua das Flores")
    val rua: String? = null,
    @field:NotBlank(message = "O número é obrigatório")
    @Schema(description = "Número da residência", example = "123")
    val numero: String,
    @Schema(description = "Complemento (opcional)", example = "Apto 101")
    val complemento: String? = null,
    @Schema(description = "Bairro (Preenchido via CEP se vazio)", example = "Heliópolis")
    val bairro: String? = null,
    @Schema(description = "Nome da cidade (Preenchido via CEP se vazio)", example = "Garanhuns")
    val cidade: String? = null,
    @Schema(description = "Sigla do estado (Preenchido via CEP se vazio)", example = "PE")
    val estado: String? = null,
    @field:NotBlank(message = "O CEP é obrigatório")
    @field:Pattern(regexp = "^\\d{8}$", message = "O CEP deve conter exatamente 8 dígitos numéricos")
    @Schema(description = "CEP sem traço", example = "55290000")
    val cep: String,
    @Schema(description = "ID da Zona de Entrega vinculada", example = "dddd4444-0000-0000-0000-000000000007")
    val zonaEntregaId: UUID? = null,
)
