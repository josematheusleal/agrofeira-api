package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Endereco
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.domain.entity.ZonaEntrega
import br.edu.ufape.agrofeira.dto.request.EnderecoRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.EnderecoRepository
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.repository.ZonaEntregaRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class EnderecoServiceTest {
    @Mock
    private lateinit var repository: EnderecoRepository

    @Mock
    private lateinit var usuarioRepository: UsuarioRepository

    @Mock
    private lateinit var zonaEntregaRepository: ZonaEntregaRepository

    @Mock
    private lateinit var viaCepService: ViaCepService

    @InjectMocks
    private lateinit var service: EnderecoService

    private lateinit var usuario: Usuario
    private lateinit var zonaEntrega: ZonaEntrega
    private val usuarioId = UUID.randomUUID()
    private val zonaId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        usuario = Usuario(id = usuarioId, nome = "Teste", senhaHash = "123")
        zonaEntrega = ZonaEntrega(id = zonaId, nome = "ZONA_PROXIMA", taxa = 7.0.toBigDecimal(), ativo = true)
    }

    private fun mockViaCepSucesso() {
        `when`(viaCepService.consultarCep(anyString())).thenReturn(
            ViaCepResponse(
                cep = "55290000",
                bairro = "Heliópolis",
                localidade = "Garanhuns",
                uf = "PE",
            ),
        )
    }

    @Test
    fun `salvarEndereco deve criar novo endereco com sucesso para consumidor`() {
        val request = EnderecoRequest(null, "123", null, null, null, null, "55290000", zonaId)
        val perfilConsumidor =
            br.edu.ufape.agrofeira.domain.entity
                .Perfil(nome = "CONSUMIDOR")
        usuario.perfis.add(perfilConsumidor)

        mockViaCepSucesso()
        `when`(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario))
        `when`(zonaEntregaRepository.findById(zonaId)).thenReturn(Optional.of(zonaEntrega))
        `when`(repository.findById(usuarioId)).thenReturn(Optional.empty())
        `when`(repository.save(any(Endereco::class.java))).thenAnswer { it.arguments[0] }

        val result = service.salvarEndereco(usuarioId, request)

        assertNotNull(result)
        assertEquals("Heliópolis", result.bairro)
        assertEquals(zonaEntrega, result.zonaEntrega)
    }

    @Test
    fun `salvarEndereco deve lancar excecao se CEP nao for de Garanhuns-PE`() {
        val request = EnderecoRequest(null, "123", null, null, null, null, "50000000", zonaId)

        `when`(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario))
        `when`(viaCepService.consultarCep("50000000")).thenReturn(
            ViaCepResponse(localidade = "Recife", uf = "PE"),
        )

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                service.salvarEndereco(usuarioId, request)
            }
        assertEquals("No momento, atendemos apenas o município de Garanhuns-PE", exception.message)
    }

    @Test
    fun `salvarEndereco deve permitir zona nula para comerciante`() {
        val request = EnderecoRequest(null, "123", null, null, null, null, "55290000", null)
        val perfilComerciante =
            br.edu.ufape.agrofeira.domain.entity
                .Perfil(nome = "COMERCIANTE")
        usuario.perfis.add(perfilComerciante)

        mockViaCepSucesso()
        `when`(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario))
        `when`(repository.findById(usuarioId)).thenReturn(Optional.empty())
        `when`(repository.save(any(Endereco::class.java))).thenAnswer { it.arguments[0] }

        val result = service.salvarEndereco(usuarioId, request)

        assertNotNull(result)
        assertNull(result.zonaEntrega)
    }
}
