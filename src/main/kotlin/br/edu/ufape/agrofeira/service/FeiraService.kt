package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.entity.FeiraComercianteEntity
import br.edu.ufape.agrofeira.domain.entity.FeiraItem
import br.edu.ufape.agrofeira.domain.enums.StatusFeira
import br.edu.ufape.agrofeira.domain.repository.ComercianteRepository
import br.edu.ufape.agrofeira.domain.repository.FeiraComercianteRepository
import br.edu.ufape.agrofeira.domain.repository.FeiraRepository
import br.edu.ufape.agrofeira.domain.repository.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class FeiraService(
    private val feiraRepository: FeiraRepository,
    private val feiraComercianteRepository: FeiraComercianteRepository,
    private val comercianteRepository: ComercianteRepository,
    private val itemRepository: ItemRepository,
) {
    fun listarTodas(): List<Feira> = feiraRepository.findAllByOrderByDataHoraDesc()

    fun buscarPorId(id: String): Feira = feiraRepository.findById(id).orElseThrow { RuntimeException("Feira não encontrada") }

    @Transactional
    fun criar(
        feira: Feira,
        comercianteIds: List<String>,
        itemIds: List<String>,
    ): Feira {
        val feiraSalva = feiraRepository.save(feira)

        comercianteIds.forEach { comercianteId ->
            val comerciante =
                comercianteRepository
                    .findById(comercianteId)
                    .orElseThrow { RuntimeException("Comerciante não encontrado") }
            val feiraComerciante = FeiraComercianteEntity(
                feira = feiraSalva,
                comerciante = comerciante,
                totalVendido = BigDecimal.ZERO,
            )
            feiraComercianteRepository.save(feiraComerciante)
            feiraSalva.comerciantes.add(feiraComerciante)
        }

        itemIds.forEach { itemId ->
            val item =
                itemRepository
                    .findById(itemId)
                    .orElseThrow { RuntimeException("Item não encontrado") }
            feiraSalva.itens.add(FeiraItem(feira = feiraSalva, item = item))
        }

        return feiraRepository.save(feiraSalva)
    }

    @Transactional
    fun atualizarStatus(
        id: String,
        status: StatusFeira,
    ): Feira {
        val feira = buscarPorId(id)
        return feiraRepository.save(feira.copy(status = status))
    }

    fun buscarFeiraComerciantePorFeiraEComerciante(
        feiraId: String,
        comercianteId: String,
    ): FeiraComercianteEntity? = feiraComercianteRepository.findByFeiraIdAndComercianteId(feiraId, comercianteId)
}
