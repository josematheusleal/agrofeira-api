package br.edu.ufape.agrofeira.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Dados para cadastro de um novo Cliente (Consumidor)")
data class ClienteRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    @Schema(description = "Nome completo do cliente", example = "Maria Silva")
    val nome: String,
    @field:Email(message = "E-mail inválido")
    @Schema(description = "E-mail único do cliente", example = "maria@email.com")
    val email: String? = null,
    @Schema(description = "Telefone único", example = "87988887777")
    val telefone: String? = null,
    @field:NotBlank(message = "A senha é obrigatória")
    @field:Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    @Schema(description = "Senha de acesso", example = "senha123")
    val senha: String,
    @Schema(description = "Descrição ou informações adicionais", example = "Cliente que compra toda semana")
    val descricao: String? = null,
    @field:Valid
    @Schema(description = "Dados do endereço (opcional na criação)")
    val endereco: EnderecoRequest? = null,
)

@Schema(description = "Dados para atualização de um Cliente (Consumidor)")
data class ClienteUpdateRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    @Schema(description = "Nome completo do cliente", example = "Maria Silva Mendes")
    val nome: String,
    @field:Email(message = "E-mail inválido")
    @Schema(description = "E-mail único do cliente", example = "maria.mendes@email.com")
    val email: String? = null,
    @Schema(description = "Telefone único", example = "87988887777")
    val telefone: String? = null,
    @Schema(description = "Descrição ou informações adicionais", example = "Cliente assídua")
    val descricao: String? = null,
)
