package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.ZonaEntrega
import br.edu.ufape.agrofeira.dto.request.ZonaEntregaRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.ZonaEntregaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class ZonaEntregaService(
    private val repository: ZonaEntregaRepository,
) {
    fun listar(): List<ZonaEntrega> = repository.findAll()

    fun buscarPorId(id: UUID): ZonaEntrega =
        repository
            .findById(id)
            .orElseThrow { ResourceNotFoundException("Zona de Entrega", id.toString()) }

    @Transactional
    fun criar(request: ZonaEntregaRequest): ZonaEntrega {
        val zona =
            ZonaEntrega(
                nome = request.nome,
                taxa = request.taxa,
            )
        return repository.save(zona)
    }

    @Transactional
    fun atualizar(
        id: UUID,
        request: ZonaEntregaRequest,
    ): ZonaEntrega {
        val zona = buscarPorId(id)
        val zonaAtualizada =
            zona.copy(
                nome = request.nome,
                taxa = request.taxa,
                atualizadoEm = LocalDateTime.now(),
            )
        return repository.save(zonaAtualizada)
    }

    @Transactional
    fun deletar(id: UUID) {
        val zona = buscarPorId(id)
        repository.delete(zona)
    }
}
