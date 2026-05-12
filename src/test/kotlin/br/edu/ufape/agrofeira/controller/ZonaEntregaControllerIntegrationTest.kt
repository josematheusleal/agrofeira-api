package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.*
import br.edu.ufape.agrofeira.dto.request.ZonaEntregaRequest
import br.edu.ufape.agrofeira.repository.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.util.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["app.scheduling.enabled=false"],
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ZonaEntregaControllerIntegrationTest {
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
    lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    lateinit var perfilRepository: PerfilRepository

    @Autowired
    lateinit var zonaEntregaRepository: ZonaEntregaRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtService: JwtService

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var adminToken: String
    private lateinit var consumidorToken: String
    private lateinit var zonaId: UUID

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("TRUNCATE TABLE usuario_perfil, usuarios, perfis, zonas_entrega CASCADE")

        val pAdmin = perfilRepository.save(Perfil(nome = "ADMINISTRADOR"))
        val pConsumidor = perfilRepository.save(Perfil(nome = "CONSUMIDOR"))

        usuarioRepository.save(
            Usuario(
                nome = "Admin",
                email = "admin@email.com",
                senhaHash = passwordEncoder.encode("senha123")!!,
                perfis = mutableSetOf(pAdmin),
            ),
        )
        adminToken = jwtService.generateToken("admin@email.com")

        usuarioRepository.save(
            Usuario(
                nome = "João",
                email = "joao@email.com",
                senhaHash = passwordEncoder.encode("senha123")!!,
                perfis = mutableSetOf(pConsumidor),
            ),
        )
        consumidorToken = jwtService.generateToken("joao@email.com")

        val zona =
            zonaEntregaRepository.save(
                ZonaEntrega(nome = "ZONA_PROXIMA", taxa = BigDecimal("7.00")),
            )
        zonaId = zona.id
    }

    @Test
    fun `listar zonas deve retornar 200`() {
        mockMvc
            .perform(get("/api/v1/zonas-entrega"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(1))
    }

    @Test
    fun `buscar por id deve retornar 200 se existir`() {
        mockMvc
            .perform(get("/api/v1/zonas-entrega/$zonaId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.nome").value("ZONA_PROXIMA"))
    }

    @Test
    fun `criar zona deve retornar 201 para Admin`() {
        val request = ZonaEntregaRequest("ZONA_NOVA", BigDecimal("8.00"))

        mockMvc
            .perform(
                post("/api/v1/zonas-entrega")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.nome").value("ZONA_NOVA"))
    }

    @Test
    fun `criar zona deve retornar 403 para Consumidor`() {
        val request = ZonaEntregaRequest("PROIBIDA", BigDecimal.ZERO)

        mockMvc
            .perform(
                post("/api/v1/zonas-entrega")
                    .header("Authorization", "Bearer $consumidorToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `atualizar zona deve retornar 200 para Admin`() {
        val request = ZonaEntregaRequest("ZONA_ALTERADA", BigDecimal("12.00"))

        mockMvc
            .perform(
                put("/api/v1/zonas-entrega/$zonaId")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.nome").value("ZONA_ALTERADA"))
            .andExpect(jsonPath("$.data.taxa").value(12.00))
    }

    @Test
    fun `deletar zona deve retornar 200 e aplicar Soft Delete`() {
        mockMvc
            .perform(
                delete("/api/v1/zonas-entrega/$zonaId")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)

        val ativo =
            jdbcTemplate.queryForObject("SELECT ativo FROM zonas_entrega WHERE id = '$zonaId'", Boolean::class.java)
        val deletadoEm =
            jdbcTemplate.queryForObject(
                "SELECT deletado_em FROM zonas_entrega WHERE id = '$zonaId'",
                java.sql.Timestamp::class.java,
            )

        assert(ativo == false)
        assert(deletadoEm != null)
    }
}
