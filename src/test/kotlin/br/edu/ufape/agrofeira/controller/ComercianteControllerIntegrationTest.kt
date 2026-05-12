package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.Perfil
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.dto.request.ComercianteRequest
import br.edu.ufape.agrofeira.dto.request.ComercianteUpdateRequest
import br.edu.ufape.agrofeira.repository.PerfilRepository
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
import java.util.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["app.scheduling.enabled=false"],
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ComercianteControllerIntegrationTest {
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
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtService: JwtService

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    lateinit var viaCepService: br.edu.ufape.agrofeira.service.ViaCepService

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var adminToken: String
    private lateinit var comercianteToken: String
    private lateinit var comercianteId: UUID
    private lateinit var outroComercianteId: UUID

    @BeforeEach
    fun setup() {
        org.mockito.Mockito.`when`(viaCepService.consultarCep(org.mockito.ArgumentMatchers.anyString())).thenReturn(
            br.edu.ufape.agrofeira.service.ViaCepResponse(
                cep = "55290000",
                bairro = "Heliópolis",
                localidade = "Garanhuns",
                uf = "PE",
            ),
        )

        jdbcTemplate.execute("TRUNCATE TABLE usuario_perfil, usuarios, perfis CASCADE")

        val pAdmin = perfilRepository.save(Perfil(nome = "ADMINISTRADOR"))
        val pComerciante = perfilRepository.save(Perfil(nome = "COMERCIANTE"))

        val admin =
            usuarioRepository.save(
                Usuario(
                    nome = "Admin",
                    email = "admin@email.com",
                    senhaHash = passwordEncoder.encode("senha123")!!,
                    perfis = mutableSetOf(pAdmin),
                ),
            )
        adminToken = jwtService.generateToken("admin@email.com")

        val comerciante =
            usuarioRepository.save(
                Usuario(
                    nome = "Banca do João",
                    email = "joao@banca.com",
                    descricao = "Frutas frescas da região",
                    senhaHash = passwordEncoder.encode("senha123")!!,
                    perfis = mutableSetOf(pComerciante),
                ),
            )
        comercianteId = comerciante.id
        comercianteToken = jwtService.generateToken("joao@banca.com")

        val outro =
            usuarioRepository.save(
                Usuario(
                    nome = "Banca da Maria",
                    email = "maria@banca.com",
                    senhaHash = passwordEncoder.encode("senha123")!!,
                    perfis = mutableSetOf(pComerciante),
                ),
            )
        outroComercianteId = outro.id
    }

    @Test
    fun `listar deve retornar 200 para Admin`() {
        mockMvc
            .perform(
                get("/api/v1/comerciantes")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(2))
    }

    @Test
    fun `listar comerciantes com filtro por nome deve retornar apenas os correspondentes`() {
        mockMvc
            .perform(
                get("/api/v1/comerciantes")
                    .param("nome", "Maria")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.content[0].nome").value("Banca da Maria"))
            .andExpect(jsonPath("$.data.totalElements").value(1))
    }

    @Test
    fun `buscarPorId deve retornar 200 se comerciante busca a si mesmo`() {
        mockMvc
            .perform(
                get("/api/v1/comerciantes/$comercianteId")
                    .header("Authorization", "Bearer $comercianteToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.nome").value("Banca do João"))
            .andExpect(jsonPath("$.data.descricao").value("Frutas frescas da região"))
            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.hasKey("endereco")))
    }

    @Test
    fun `buscarPorId deve retornar 403 se comerciante busca outro`() {
        mockMvc
            .perform(
                get("/api/v1/comerciantes/$outroComercianteId")
                    .header("Authorization", "Bearer $comercianteToken"),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `criar deve retornar 201 para Admin`() {
        val request =
            ComercianteRequest(
                nome = "Banca Nova",
                email = "nova@banca.com",
                senha = "senhaSegura123",
                descricao = "Nova banca de legumes",
            )

        mockMvc
            .perform(
                post("/api/v1/comerciantes")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.nome").value("Banca Nova"))
            .andExpect(jsonPath("$.data.descricao").value("Nova banca de legumes"))
            .andExpect(jsonPath("$.data.endereco").isEmpty)
    }

    @Test
    fun `atualizar deve retornar 200 se for o dono`() {
        val request =
            ComercianteUpdateRequest(
                nome = "Banca João Alterada",
                email = "novo.email@joao.com",
                descricao = "Atualizada com novos produtos",
            )

        mockMvc
            .perform(
                put("/api/v1/comerciantes/$comercianteId")
                    .header("Authorization", "Bearer $comercianteToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.nome").value("Banca João Alterada"))
            .andExpect(jsonPath("$.data.descricao").value("Atualizada com novos produtos"))
    }

    @Test
    fun `deletar deve retornar 200 para Admin e marcar como inativo`() {
        mockMvc
            .perform(
                delete("/api/v1/comerciantes/$comercianteId")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)

        val ativo =
            jdbcTemplate.queryForObject("SELECT ativo FROM usuarios WHERE id = '$comercianteId'", Boolean::class.java)
        assert(ativo == false)
    }

    @Test
    fun `acesso sem token deve retornar 401 Unauthorized`() {
        mockMvc
            .perform(get("/api/v1/comerciantes"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `buscar por id inexistente deve retornar 404`() {
        val idInexistente = UUID.randomUUID()
        mockMvc
            .perform(
                get("/api/v1/comerciantes/$idInexistente")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `criar com dados invalidos deve retornar 400`() {
        val requestInvalido =
            mapOf(
                "nome" to "", // Nome vazio
                "senha" to "123", // Senha curta demais
            )

        mockMvc
            .perform(
                post("/api/v1/comerciantes")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestInvalido)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Erro de validação nos campos"))
    }

    @Test
    fun `criar comerciante com endereco deve salvar ambos atomicamente`() {
        val zonaId =
            jdbcTemplate.queryForObject(
                "INSERT INTO zonas_entrega (nome, taxa, ativo) VALUES ('ZONA_COMERCIANTE', 10.00, true) RETURNING id",
                UUID::class.java,
            )

        val enderecoReq =
            br.edu.ufape.agrofeira.dto.request.EnderecoRequest(
                rua = "Rua do Comércio",
                numero = "500",
                complemento = null,
                cidade = "Garanhuns",
                estado = "PE",
                cep = "55290000",
                zonaEntregaId = zonaId!!,
            )

        val request =
            ComercianteRequest(
                nome = "Comerciante Atômico",
                email = "atomico.com@email.com",
                senha = "senha123",
                endereco = enderecoReq,
            )

        mockMvc
            .perform(
                post("/api/v1/comerciantes")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.nome").value("Comerciante Atômico"))
            .andExpect(jsonPath("$.data.endereco.rua").value("Rua do Comércio"))
    }

    @Test
    fun `comerciante nao pode deletar a si mesmo`() {
        mockMvc
            .perform(
                delete("/api/v1/comerciantes/$comercianteId")
                    .header("Authorization", "Bearer $comercianteToken"),
            ).andExpect(status().isForbidden)
    }
}
