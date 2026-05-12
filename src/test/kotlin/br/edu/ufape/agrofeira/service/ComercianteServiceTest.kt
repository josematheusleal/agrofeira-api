package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Perfil
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.dto.request.ComercianteRequest
import br.edu.ufape.agrofeira.dto.request.ComercianteUpdateRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

@ExtendWith(MockitoExtension::class)
class ComercianteServiceTest {
    @Mock
    private lateinit var usuarioRepository: UsuarioRepository

    @Mock
    private lateinit var usuarioService: UsuarioService

    @Mock
    private lateinit var enderecoService: EnderecoService

    @InjectMocks
    private lateinit var comercianteService: ComercianteService

    private lateinit var comerciante: Usuario
    private lateinit var perfilComerciante: Perfil
    private val id = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        perfilComerciante = Perfil(nome = "COMERCIANTE")
        comerciante =
            Usuario(
                id = id,
                nome = "Banca do João",
                email = "joao@banca.com",
                senhaHash = "123456",
                perfis = mutableSetOf(perfilComerciante),
            )
    }

    private fun anyUsuario(): Usuario {
        any(Usuario::class.java)
        return Usuario(nome = "", senhaHash = "")
    }

    private fun eqSet(value: Set<String>): Set<String> {
        eq(value)
        return value
    }

    @Test
    fun `listar deve retornar uma pagina de comerciantes quando nome for nulo`() {
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(comerciante))

        `when`(usuarioRepository.findByPerfilNome("COMERCIANTE", pageable)).thenReturn(page)

        val result = comercianteService.listar(null, pageable)

        assertEquals(1, result.totalElements)
        assertEquals("Banca do João", result.content[0].nome)
        verify(usuarioRepository).findByPerfilNome("COMERCIANTE", pageable)
    }

    @Test
    fun `listar deve retornar uma pagina de comerciantes filtrada por nome`() {
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(comerciante))

        `when`(
            usuarioRepository.findByPerfilNomeAndNomeContainingIgnoreCase(
                "COMERCIANTE",
                "Banca",
                pageable,
            ),
        ).thenReturn(page)

        val result = comercianteService.listar("Banca", pageable)

        assertEquals(1, result.totalElements)
        assertEquals("Banca do João", result.content[0].nome)
        verify(usuarioRepository).findByPerfilNomeAndNomeContainingIgnoreCase("COMERCIANTE", "Banca", pageable)
    }

    @Test
    fun `buscarPorId deve retornar o comerciante se possuir perfil COMERCIANTE`() {
        `when`(usuarioRepository.findById(id)).thenReturn(Optional.of(comerciante))

        val result = comercianteService.buscarPorId(id)

        assertNotNull(result)
        assertEquals(comerciante.id, result.id)
    }

    @Test
    fun `buscarPorId deve lancar excecao se usuario nao for comerciante`() {
        val perfilConsumidor = Perfil(nome = "CONSUMIDOR")
        val consumidor = comerciante.copy(perfis = mutableSetOf(perfilConsumidor))

        `when`(usuarioRepository.findById(id)).thenReturn(Optional.of(consumidor))

        assertThrows(ResourceNotFoundException::class.java) {
            comercianteService.buscarPorId(id)
        }
    }

    @Test
    fun `criar deve chamar o usuarioService cadastrar e salvar endereco se fornecido`() {
        val enderecoRequest =
            br.edu.ufape.agrofeira.dto.request.EnderecoRequest(
                rua = "Rua",
                numero = "1",
                complemento = "Comp",
                cep = "55290000",
                zonaEntregaId = UUID.randomUUID(),
            )
        val request =
            ComercianteRequest("Banca Nova", "nova@banca.com", "8799999999", "senha123", "desc", enderecoRequest)
        `when`(usuarioService.cadastrar(anyUsuario(), eqSet(setOf("COMERCIANTE")))).thenReturn(comerciante)

        val result = comercianteService.criar(request)

        assertNotNull(result)
        verify(usuarioService).cadastrar(anyUsuario(), eqSet(setOf("COMERCIANTE")))
        verify(enderecoService).salvarEndereco(comerciante.id, enderecoRequest)
    }

    @Test
    fun `criar deve chamar apenas usuarioService cadastrar se endereco for nulo`() {
        val request = ComercianteRequest("Banca Nova", "nova@banca.com", "8799999999", "senha123", "desc", null)
        `when`(usuarioService.cadastrar(anyUsuario(), eqSet(setOf("COMERCIANTE")))).thenReturn(comerciante)

        val result = comercianteService.criar(request)

        assertNotNull(result)
        verify(usuarioService).cadastrar(anyUsuario(), eqSet(setOf("COMERCIANTE")))
        verifyNoInteractions(enderecoService)
    }

    @Test
    fun `atualizar deve salvar usuario com os novos dados de banca`() {
        val request = ComercianteUpdateRequest("Banca Atualizada", "joao.novo@email.com", "8799999888", "desc")

        `when`(usuarioRepository.findById(id)).thenReturn(Optional.of(comerciante))
        `when`(usuarioRepository.save(anyUsuario())).thenAnswer { it.arguments[0] }

        val result = comercianteService.atualizar(id, request)

        assertEquals("Banca Atualizada", result.nome)
        assertEquals("joao.novo@email.com", result.email)
        verify(usuarioRepository).save(anyUsuario())
    }

    @Test
    fun `deletar deve chamar deletarLogicamente do repository`() {
        `when`(usuarioRepository.findById(id)).thenReturn(Optional.of(comerciante))

        comercianteService.deletar(id)

        verify(usuarioRepository).deletarLogicamente(id)
    }
}
