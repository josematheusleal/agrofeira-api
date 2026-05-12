package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Produto
import br.edu.ufape.agrofeira.domain.enums.CategoriaProduto
import br.edu.ufape.agrofeira.domain.enums.UnidadeMedida
import br.edu.ufape.agrofeira.dto.request.ProdutoRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.ProdutoRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class ProdutoServiceTest {
    @Mock
    private lateinit var repository: ProdutoRepository

    @InjectMocks
    private lateinit var service: ProdutoService

    private lateinit var produto: Produto
    private val id = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        produto =
            Produto(
                id = id,
                nome = "Tomate",
                categoria = CategoriaProduto.HORTIFRUTI,
                unidadeMedida = UnidadeMedida.QUILO,
                precoBase = BigDecimal("6.00"),
            )
    }

    @Test
    fun `listar deve retornar pagina de produtos quando nome for nulo`() {
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(produto))
        `when`(repository.findAll(pageable)).thenReturn(page)

        val result = service.listar(null, pageable)

        assertEquals(1, result.totalElements)
        assertEquals("Tomate", result.content[0].nome)
    }

    @Test
    fun `listar deve retornar pagina de produtos filtrada por nome`() {
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(produto))
        `when`(repository.findByNomeContainingIgnoreCase("Tomate", pageable)).thenReturn(page)

        val result = service.listar("Tomate", pageable)

        assertEquals(1, result.totalElements)
        assertEquals("Tomate", result.content[0].nome)
    }

    @Test
    fun `buscarPorId deve retornar produto se existir`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(produto))

        val result = service.buscarPorId(id)

        assertNotNull(result)
        assertEquals(id, result.id)
    }

    @Test
    fun `buscarPorId deve lancar excecao se produto nao existir`() {
        `when`(repository.findById(id)).thenReturn(Optional.empty())

        assertThrows(ResourceNotFoundException::class.java) {
            service.buscarPorId(id)
        }
    }

    @Test
    fun `criar deve salvar e retornar novo produto`() {
        val request = ProdutoRequest("Alface", CategoriaProduto.HORTIFRUTI, UnidadeMedida.UNIDADE, BigDecimal("3.00"))
        `when`(repository.save(any(Produto::class.java))).thenAnswer { it.arguments[0] }

        val result = service.criar(request)

        assertNotNull(result)
        assertEquals("Alface", result.nome)
        assertEquals(BigDecimal("3.00"), result.precoBase)
        verify(repository).save(any(Produto::class.java))
    }

    @Test
    fun `atualizar deve salvar alteracoes no produto existente`() {
        val request =
            ProdutoRequest("Tomate Especial", CategoriaProduto.HORTIFRUTI, UnidadeMedida.QUILO, BigDecimal("8.00"))
        `when`(repository.findById(id)).thenReturn(Optional.of(produto))
        `when`(repository.save(any(Produto::class.java))).thenAnswer { it.arguments[0] }

        val result = service.atualizar(id, request)

        assertEquals("Tomate Especial", result.nome)
        assertEquals(BigDecimal("8.00"), result.precoBase)
        verify(repository).save(any(Produto::class.java))
    }

    @Test
    fun `obterOpcoes deve retornar todas as categorias e unidades de medida`() {
        val result = service.obterOpcoes()

        assertNotNull(result)
        assertTrue(result.categorias.isNotEmpty())
        assertTrue(result.unidadesMedida.isNotEmpty())
        assertEquals("HORTIFRUTI", result.categorias[0].value)
        assertEquals("Hortifrúti", result.categorias[0].label)
    }

    @Test
    fun `deletar deve chamar repository delete`() {
        `when`(repository.findById(id)).thenReturn(Optional.of(produto))

        service.deletar(id)

        verify(repository).delete(produto)
    }
}
