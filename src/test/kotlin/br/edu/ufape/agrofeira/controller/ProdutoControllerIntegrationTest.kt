package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.Perfil
import br.edu.ufape.agrofeira.domain.entity.Produto
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.domain.enums.CategoriaProduto
import br.edu.ufape.agrofeira.domain.enums.UnidadeMedida
import br.edu.ufape.agrofeira.dto.request.ProdutoRequest
import br.edu.ufape.agrofeira.repository.PerfilRepository
import br.edu.ufape.agrofeira.repository.ProdutoRepository
import br.edu.ufape.agrofeira.repository.UsuarioRepository
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
class ProdutoControllerIntegrationTest {
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
    lateinit var produtoRepository: ProdutoRepository

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    lateinit var perfilRepository: PerfilRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtService: JwtService

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var adminToken: String
    private lateinit var consumidorToken: String
    private lateinit var produtoId: UUID

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("TRUNCATE TABLE usuario_perfil, usuarios, perfis, produtos CASCADE")

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

        val produto =
            produtoRepository.save(
                Produto(
                    nome = "Cenoura",
                    categoria = CategoriaProduto.HORTIFRUTI,
                    unidadeMedida = UnidadeMedida.QUILO,
                    precoBase = BigDecimal("4.50"),
                ),
            )
        produtoId = produto.id
    }

    @Test
    fun `listar produtos deve retornar 200 para qualquer usuario autenticado`() {
        mockMvc
            .perform(
                get("/api/v1/itens")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.totalElements").value(1))
    }

    @Test
    fun `listar produtos com filtro por nome deve retornar apenas os correspondentes`() {
        mockMvc
            .perform(
                get("/api/v1/itens")
                    .param("nome", "Cenou")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.content[0].nome").value("Cenoura"))
            .andExpect(jsonPath("$.data.totalElements").value(1))
    }

    @Test
    fun `buscar por id deve retornar 200 se existir`() {
        mockMvc
            .perform(
                get("/api/v1/itens/$produtoId")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.nome").value("Cenoura"))
    }

    @Test
    fun `obter opcoes deve retornar 200 e listas de dominios`() {
        mockMvc
            .perform(
                get("/api/v1/itens/opcoes")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.categorias").isArray)
            .andExpect(jsonPath("$.data.unidadesMedida").isArray)
            .andExpect(jsonPath("$.data.categorias[0].value").value("HORTIFRUTI"))
            .andExpect(jsonPath("$.data.categorias[0].label").value("Hortifrúti"))
    }

    @Test
    fun `criar produto deve retornar 201 para Admin`() {
        val request =
            ProdutoRequest(
                nome = "Melancia",
                categoria = CategoriaProduto.FRUTAS,
                unidadeMedida = UnidadeMedida.UNIDADE,
                precoBase = BigDecimal("15.00"),
            )

        mockMvc
            .perform(
                post("/api/v1/itens")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.nome").value("Melancia"))
    }

    @Test
    fun `criar produto deve retornar 403 para Consumidor`() {
        val request = ProdutoRequest("Hack", CategoriaProduto.OUTROS, UnidadeMedida.UNIDADE, BigDecimal.ONE)

        mockMvc
            .perform(
                post("/api/v1/itens")
                    .header("Authorization", "Bearer $consumidorToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `atualizar produto deve retornar 200 para Admin`() {
        val request =
            ProdutoRequest(
                nome = "Cenoura Premium",
                categoria = CategoriaProduto.HORTIFRUTI,
                unidadeMedida = UnidadeMedida.QUILO,
                precoBase = BigDecimal("5.50"),
            )

        mockMvc
            .perform(
                put("/api/v1/itens/$produtoId")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.nome").value("Cenoura Premium"))
            .andExpect(jsonPath("$.data.precoBase").value(5.50))
    }

    @Test
    fun `deletar produto deve retornar 200 e aplicar Soft Delete`() {
        mockMvc
            .perform(
                delete("/api/v1/itens/$produtoId")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)

        val ativo =
            jdbcTemplate.queryForObject("SELECT ativo FROM produtos WHERE id = '$produtoId'", Boolean::class.java)
        val deletadoEm =
            jdbcTemplate.queryForObject(
                "SELECT deletado_em FROM produtos WHERE id = '$produtoId'",
                java.sql.Timestamp::class.java,
            )

        assert(ativo == false)
        assert(deletadoEm != null)
    }

    @Test
    fun `criar produto com 3 casas decimais deve retornar 400`() {
        val request =
            ProdutoRequest(
                nome = "Produto Invalido",
                categoria = CategoriaProduto.OUTROS,
                unidadeMedida = UnidadeMedida.UNIDADE,
                precoBase = BigDecimal("1.234"),
            )

        mockMvc
            .perform(
                post("/api/v1/itens")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `criar produto com unidade de medida invalida deve retornar 400`() {
        val body =
            """
            {
                "nome": "Produto X",
                "categoria": "HORTIFRUTI",
                "unidadeMedida": "INVALIDA",
                "precoBase": 10.00
            }
            """.trimIndent()

        mockMvc
            .perform(
                post("/api/v1/itens")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `criar produto com categoria nula deve retornar 400`() {
        val body =
            """
            {
                "nome": "Produto X",
                "unidadeMedida": "QUILO",
                "precoBase": 10.00
            }
            """.trimIndent()

        mockMvc
            .perform(
                post("/api/v1/itens")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body),
            ).andExpect(status().isBadRequest)
    }
}
