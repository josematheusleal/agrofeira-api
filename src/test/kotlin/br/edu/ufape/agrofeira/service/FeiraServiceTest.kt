package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Comerciante
import br.edu.ufape.agrofeira.domain.entity.Feira
import br.edu.ufape.agrofeira.domain.entity.Item
import br.edu.ufape.agrofeira.domain.enums.StatusFeira
import br.edu.ufape.agrofeira.domain.repository.ComercianteRepository
import br.edu.ufape.agrofeira.domain.repository.FeiraRepository
import br.edu.ufape.agrofeira.domain.repository.ItemRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class FeiraServiceTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres =
            PostgreSQLContainer("postgres:18-alpine").apply {
                withDatabaseName("testdb")
                withUsername("test")
                withPassword("test")
            }
    }

    @Autowired
    lateinit var feiraService: FeiraService

    @Autowired
    lateinit var feiraRepository: FeiraRepository

    @Autowired
    lateinit var comercianteRepository: ComercianteRepository

    @Autowired
    lateinit var itemRepository: ItemRepository

    @Test
    fun `deve criar uma feira com comerciantes e itens com sucesso`() {
        // Arrange
        val comerciante = comercianteRepository.save(
            Comerciante(nome = "Comerciante Teste", telefone = "8199999999")
        )
        val item = itemRepository.save(
            Item(nome = "Tomate", precoBase = BigDecimal("5.00"), unidadeMedida = "KG")
        )

        val novaFeira = Feira(
            dataHora = LocalDateTime.now().plusDays(7),
            status = StatusFeira.AGENDADA
        )

        // Act
        val feiraCriada = feiraService.criar(
            novaFeira,
            listOf(comerciante.id),
            listOf(item.id)
        )

        // Assert
        assertNotNull(feiraCriada.id)
        assertEquals(StatusFeira.AGENDADA, feiraCriada.status)
        
        val feiraNoBanco = feiraRepository.findById(feiraCriada.id).get()
        assertEquals(1, feiraNoBanco.comerciantes.size)
        assertEquals(comerciante.id, feiraNoBanco.comerciantes[0].comerciante.id)
        assertEquals(1, feiraNoBanco.itens.size)
        assertEquals(item.id, feiraNoBanco.itens[0].item.id)
    }

    @Test
    fun `deve lancar excecao ao criar feira com comerciante inexistente`() {
        val novaFeira = Feira(dataHora = LocalDateTime.now())
        
        assertThrows(RuntimeException::class.java) {
            feiraService.criar(novaFeira, listOf("id-inexistente"), emptyList())
        }
    }
}
