package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Produto
import br.edu.ufape.agrofeira.domain.enums.CategoriaProduto
import br.edu.ufape.agrofeira.domain.enums.UnidadeMedida
import br.edu.ufape.agrofeira.dto.request.ProdutoRequest
import br.edu.ufape.agrofeira.dto.response.ItensOpcoesDTO
import br.edu.ufape.agrofeira.dto.response.OpcaoDTO
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.ProdutoRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ProdutoService(
    private val repository: ProdutoRepository,
) {
    fun listar(
        nome: String? = null,
        pageable: Pageable,
    ): Page<Produto> =
        if (nome.isNullOrBlank()) {
            repository.findAll(pageable)
        } else {
            repository.findByNomeContainingIgnoreCase(nome, pageable)
        }

    fun obterOpcoes(): ItensOpcoesDTO {
        val categorias = CategoriaProduto.entries.map { OpcaoDTO(it.name, it.descricao) }
        val unidadesMedida = UnidadeMedida.entries.map { OpcaoDTO(it.name, it.descricao) }
        return ItensOpcoesDTO(categorias, unidadesMedida)
    }

    fun buscarPorId(id: UUID): Produto =
        repository
            .findById(id)
            .orElseThrow { ResourceNotFoundException("Produto", id.toString()) }

    @Transactional
    fun criar(request: ProdutoRequest): Produto {
        val produto =
            Produto(
                nome = request.nome,
                categoria = request.categoria,
                unidadeMedida = request.unidadeMedida,
                precoBase = request.precoBase,
            )
        return repository.save(produto)
    }

    @Transactional
    fun atualizar(
        id: UUID,
        request: ProdutoRequest,
    ): Produto {
        val produtoExistente = buscarPorId(id)
        val produtoAtualizado =
            produtoExistente.copy(
                nome = request.nome,
                categoria = request.categoria,
                unidadeMedida = request.unidadeMedida,
                precoBase = request.precoBase,
            )
        return repository.save(produtoAtualizado)
    }

    @Transactional
    fun deletar(id: UUID) {
        val produto = buscarPorId(id)
        repository.delete(produto)
    }
}
