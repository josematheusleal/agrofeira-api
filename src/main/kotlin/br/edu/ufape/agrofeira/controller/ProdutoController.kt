package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.dto.mapper.toDTO
import br.edu.ufape.agrofeira.dto.request.ProdutoRequest
import br.edu.ufape.agrofeira.dto.response.ApiResponse
import br.edu.ufape.agrofeira.dto.response.ItensOpcoesDTO
import br.edu.ufape.agrofeira.dto.response.ProdutoDTO
import br.edu.ufape.agrofeira.security.annotations.IsManagerOrAdmin
import br.edu.ufape.agrofeira.service.ProdutoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/itens")
@Tag(name = "Produtos", description = "Gerenciamento do catálogo de produtos")
class ProdutoController(
    private val service: ProdutoService,
) {
    @GetMapping
    @Operation(summary = "Listar todos os produtos", description = "Retorna uma lista paginada de produtos ativos")
    fun listar(
        @Parameter(description = "Filtro opcional por nome")
        @RequestParam(required = false) nome: String?,
        @PageableDefault(size = 10, page = 0) pageable: Pageable,
    ): ResponseEntity<ApiResponse<Page<ProdutoDTO>>> {
        val produtos = service.listar(nome, pageable).map { it.toDTO() }
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Produtos listados com sucesso",
                data = produtos,
            ),
        )
    }

    @GetMapping("/opcoes")
    @Operation(
        summary = "Obter opções de categorias e unidades de medida",
        description = "Retorna as opções para popular selects no frontend",
    )
    fun obterOpcoes(): ResponseEntity<ApiResponse<ItensOpcoesDTO>> {
        val opcoes = service.obterOpcoes()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Opções listadas com sucesso",
                data = opcoes,
            ),
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    fun buscarPorId(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<ProdutoDTO>> {
        val produto = service.buscarPorId(id).toDTO()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Produto encontrado",
                data = produto,
            ),
        )
    }

    @PostMapping
    @IsManagerOrAdmin
    @Operation(summary = "Cadastrar novo produto (Apenas Gerência)")
    fun criar(
        @RequestBody @Valid request: ProdutoRequest,
    ): ResponseEntity<ApiResponse<ProdutoDTO>> {
        val produto = service.criar(request).toDTO()
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                message = "Produto cadastrado com sucesso",
                data = produto,
            ),
        )
    }

    @PutMapping("/{id}")
    @IsManagerOrAdmin
    @Operation(summary = "Atualizar dados de um produto (Apenas Gerência)")
    fun atualizar(
        @PathVariable id: UUID,
        @RequestBody @Valid request: ProdutoRequest,
    ): ResponseEntity<ApiResponse<ProdutoDTO>> {
        val produto = service.atualizar(id, request).toDTO()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Produto atualizado com sucesso",
                data = produto,
            ),
        )
    }

    @DeleteMapping("/{id}")
    @IsManagerOrAdmin
    @Operation(summary = "Deletar um produto (Soft Delete) - Apenas Gerência")
    fun deletar(
        @PathVariable id: UUID,
    ): ResponseEntity<ApiResponse<Unit>> {
        service.deletar(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Produto removido com sucesso",
            ),
        )
    }
}
