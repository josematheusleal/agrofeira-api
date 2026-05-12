package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.ZonaEntrega
import br.edu.ufape.agrofeira.dto.request.ZonaEntregaRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.ZonaEntregaRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class ZonaEntregaServiceTest {
    @Mock
    lateinit var repository: ZonaEntregaRepository

    @InjectMocks
    lateinit var service: ZonaEntregaService

    private val id = UUID.randomUUID()
    private lateinit var zona: ZonaEntrega

    @BeforeEach
    fun setUp() {
        zona = ZonaEntrega(id = id, nome = "ZONA_PROXIMA", taxa = BigDecimal("7.00"))
    }

    @Test
    fun `listar deve retornar todas as zonas`() {
        `when`(repository.findAll()).thenReturn(listOf(zona))

        val result = service.listar()

        assertEquals(1, result.size)
        assertEquals("ZONA_PROXIMA", result[0].nome)
    }

    @Test
    fun `buscarPorId deve retornar zona se existir`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(zona))

        val result = service.buscarPorId(id)

        assertEquals(id, result.id)
    }

    @Test
    fun `buscarPorId deve lancar excecao se nao existir`() {
        `when`(repository.findById(id)).thenReturn(Optional.empty())

        assertThrows(ResourceNotFoundException::class.java) {
            service.buscarPorId(id)
        }
    }

    @Test
    fun `criar deve salvar nova zona`() {
        val request = ZonaEntregaRequest("ZONA_AFASTADA", BigDecimal("10.00"))
        `when`(repository.save(any(ZonaEntrega::class.java))).thenAnswer { it.arguments[0] }

        val result = service.criar(request)

        assertNotNull(result)
        assertEquals("ZONA_AFASTADA", result.nome)
        assertEquals(BigDecimal("10.00"), result.taxa)
    }

    @Test
    fun `atualizar deve salvar alteracoes`() {
        val request = ZonaEntregaRequest("ZONA_ALTERADA", BigDecimal("10.00"))
        `when`(repository.findById(id)).thenReturn(Optional.of(zona))
        `when`(repository.save(any(ZonaEntrega::class.java))).thenAnswer { it.arguments[0] }

        val result = service.atualizar(id, request)

        assertEquals("ZONA_ALTERADA", result.nome)
        assertEquals(BigDecimal("10.00"), result.taxa)
    }

    @Test
    fun `deletar deve chamar repository delete`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(zona))

        service.deletar(id)

        verify(repository).delete(zona)
    }
}
