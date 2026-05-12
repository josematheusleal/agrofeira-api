package br.edu.ufape.agrofeira.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Dados para cadastro de um novo Comerciante")
data class ComercianteRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    @Schema(description = "Nome completo ou nome da banca do comerciante", example = "Hortifruti do João")
    val nome: String,
    @field:Email(message = "E-mail inválido")
    @Schema(description = "E-mail único do comerciante", example = "joao.hortifruti@email.com")
    val email: String? = null,
    @Schema(description = "Telefone único", example = "87988887777")
    val telefone: String? = null,
    @field:NotBlank(message = "A senha é obrigatória")
    @field:Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    @Schema(description = "Senha de acesso", example = "senha123")
    val senha: String,
    @Schema(description = "Descrição da banca ou especialidade", example = "Produtos orgânicos e frescos")
    val descricao: String? = null,
    @field:Valid
    @Schema(description = "Dados do endereço (opcional na criação)")
    val endereco: EnderecoRequest? = null,
)

@Schema(description = "Dados para atualização de um Comerciante")
data class ComercianteUpdateRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    @Schema(description = "Nome da banca", example = "Hortifruti do João e Maria")
    val nome: String,
    @field:Email(message = "E-mail inválido")
    @Schema(description = "E-mail único", example = "contato@hortifruti.com")
    val email: String? = null,
    @Schema(description = "Telefone único", example = "87988887777")
    val telefone: String? = null,
    @Schema(description = "Descrição atualizada", example = "Agora entregamos em domicílio")
    val descricao: String? = null,
)
