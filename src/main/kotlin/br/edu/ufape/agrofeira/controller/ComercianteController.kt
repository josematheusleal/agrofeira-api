package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.mapper.toDetalhadoDTO
import br.edu.ufape.agrofeira.dto.request.ComercianteRequest
import br.edu.ufape.agrofeira.dto.request.ComercianteUpdateRequest
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.UsuarioDTO
import br.edu.ufape.agrofeira.dto.response.UsuarioDetalhadoDTO
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import br.edu.ufape.agrofeira.service.ComercianteService
import br.edu.ufape.agrofeira.service.EnderecoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/comerciantes")
@Tag(name = "Comerciantes", description = "Gerenciamento de comerciantes e itens elegíveis")
class ComercianteController(
    private val comercianteService: ComercianteService,
    private val enderecoService: EnderecoService,
) {
    @GetMapping
    @IsManagerOrAdmin
    @Operation(summary = "Listar comerciantes")
    fun listar(
        @Parameter(description = "Filtro opcional por nome")
        @RequestParam(required = false) nome: String?,
        @PageableDefault(size = 10, page = 0) pageable: Pageable,
    ): ResponseEntity<ApiResponse<Page<UsuarioDTO>>> {
        val comerciantes = comercianteService.listar(nome, pageable).map { it.toDTO() }
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Comerciantes recuperados com sucesso",
                data = comerciantes,
            ),
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityUtils.isResourceOwner(#id) or hasAnyRole('ADMINISTRADOR', 'GERENCIADOR')")
    @Operation(summary = "Buscar comerciante por ID (Perfil)")
    fun buscarPorId(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<UsuarioDetalhadoDTO>> {
        val comerciante = comercianteService.buscarPorId(id)
        val endereco = enderecoService.buscarPorUsuarioIdOuNulo(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Comerciante recuperado com sucesso",
                data = comerciante.toDetalhadoDTO(endereco),
            ),
        )
    }

    @PostMapping
    @IsManagerOrAdmin
    @Operation(summary = "Cadastrar comerciante")
    fun criar(
        @RequestBody @Valid request: ComercianteRequest,
    ): ResponseEntity<ApiResponse<UsuarioDetalhadoDTO>> {
        val novoComerciante = comercianteService.criar(request)
        val endereco = enderecoService.buscarPorUsuarioIdOuNulo(novoComerciante.id)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ApiResponse(
                    success = true,
                    message = "Comerciante cadastrado com sucesso",
                    data = novoComerciante.toDetalhadoDTO(endereco),
                ),
            )
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityUtils.isResourceOwner(#id) or hasAnyRole('ADMINISTRADOR', 'GERENCIADOR')")
    @Operation(summary = "Atualizar comerciante")
    fun atualizar(
        @PathVariable id: UUID,
        @RequestBody @Valid request: ComercianteUpdateRequest,
    ): ResponseEntity<ApiResponse<UsuarioDetalhadoDTO>> {
        val comercianteAtualizado = comercianteService.atualizar(id, request)
        val endereco = enderecoService.buscarPorUsuarioIdOuNulo(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Comerciante atualizado com sucesso",
                data = comercianteAtualizado.toDetalhadoDTO(endereco),
            ),
        )
    }

    @DeleteMapping("/{id}")
    @IsManagerOrAdmin
    @Operation(summary = "Deletar comerciante")
    fun deletar(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        comercianteService.deletar(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Comerciante deletado com sucesso"))
    }
}
