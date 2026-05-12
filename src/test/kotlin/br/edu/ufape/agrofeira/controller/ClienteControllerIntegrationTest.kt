package br.edu.ufape.agrofeira.controller

import br.edu.ufape.agrofeira.config.JwtService
import br.edu.ufape.agrofeira.domain.entity.Perfil
import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.dto.request.ClienteRequest
import br.edu.ufape.agrofeira.dto.request.ClienteUpdateRequest
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
class ClienteControllerIntegrationTest {
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
    lateinit var jdbcTemplate: org.springframework.jdbc.core.JdbcTemplate

    private lateinit var consumidorToken: String
    private lateinit var consumidorSecundarioToken: String
    private lateinit var adminToken: String

    private lateinit var consumidorId: UUID
    private lateinit var consumidorSecundarioId: UUID

    @BeforeEach
    fun setup() {
        org.mockito.Mockito.`when`(viaCepService.consultarCep(org.mockito.ArgumentMatchers.anyString())).thenReturn(
            br.edu.ufape.agrofeira.service.ViaCepResponse(
                cep = "55290000",
                bairro = "Bairro Atômico",
                localidade = "Garanhuns",
                uf = "PE",
            ),
        )

        jdbcTemplate.execute("TRUNCATE TABLE usuario_perfil, usuarios, perfis CASCADE")

        val perfilAdmin = perfilRepository.save(Perfil(nome = "ADMINISTRADOR"))
        val perfilGerenciador = perfilRepository.save(Perfil(nome = "GERENCIADOR"))
        val perfilConsumidor = perfilRepository.save(Perfil(nome = "CONSUMIDOR"))

        val admin =
            usuarioRepository.save(
                Usuario(
                    nome = "Admin User",
                    email = "admin@email.com",
                    senhaHash = passwordEncoder.encode("senha123")!!,
                    perfis = mutableSetOf(perfilAdmin),
                ),
            )
        adminToken = jwtService.generateToken("admin@email.com")

        val consumidor =
            usuarioRepository.save(
                Usuario(
                    nome = "João Consumidor",
                    email = "joao@email.com",
                    telefone = "87999991111",
                    descricao = "Cliente assíduo",
                    senhaHash = passwordEncoder.encode("senha123")!!,
                    perfis = mutableSetOf(perfilConsumidor),
                ),
            )
        consumidorId = consumidor.id
        consumidorToken = jwtService.generateToken("joao@email.com")

        val consumidorSecundario =
            usuarioRepository.save(
                Usuario(
                    nome = "Maria Consumidora",
                    email = "maria@email.com",
                    senhaHash = passwordEncoder.encode("senha123")!!,
                    perfis = mutableSetOf(perfilConsumidor),
                ),
            )
        consumidorSecundarioId = consumidorSecundario.id
        consumidorSecundarioToken = jwtService.generateToken("maria@email.com")
    }

    @Test
    fun `listar clientes deve retornar 200 para ADMINISTRADOR`() {
        mockMvc
            .perform(
                get("/api/v1/clientes")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
            // Espera achar Joao e Maria
            .andExpect(jsonPath("$.data.totalElements").value(2))
    }

    @Test
    fun `listar clientes com filtro por nome deve retornar apenas os correspondentes`() {
        mockMvc
            .perform(
                get("/api/v1/clientes")
                    .param("nome", "João")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data.content[0].nome").value("João Consumidor"))
            .andExpect(jsonPath("$.data.totalElements").value(1))
    }

    @Test
    fun `listar clientes deve retornar 403 Forbidden para CONSUMIDOR`() {
        mockMvc
            .perform(
                get("/api/v1/clientes")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `buscarPorId deve retornar 200 se o Consumidor busca seu proprio id`() {
        mockMvc
            .perform(
                get("/api/v1/clientes/$consumidorId")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("joao@email.com"))
            .andExpect(jsonPath("$.data.descricao").value("Cliente assíduo"))
            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.hasKey("endereco")))
    }

    @Test
    fun `buscarPorId deve retornar 403 se Consumidor busca id de outro usuario`() {
        mockMvc
            .perform(
                get("/api/v1/clientes/$consumidorSecundarioId")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `buscarPorId deve retornar 200 se Administrador busca qualquer Consumidor`() {
        mockMvc
            .perform(
                get("/api/v1/clientes/$consumidorId")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("joao@email.com"))
    }

    @Test
    fun `atualizar deve retornar 200 se Consumidor edita seu proprio perfil`() {
        val updateRequest =
            ClienteUpdateRequest(
                nome = "João Consumidor Alterado",
                email = "joao.novo@email.com",
                telefone = "87999998888",
                descricao = "Nova descrição",
            )

        mockMvc
            .perform(
                put("/api/v1/clientes/$consumidorId")
                    .header("Authorization", "Bearer $consumidorToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.nome").value("João Consumidor Alterado"))
            .andExpect(jsonPath("$.data.email").value("joao.novo@email.com"))
            .andExpect(jsonPath("$.data.descricao").value("Nova descrição"))
    }

    @Test
    fun `atualizar deve retornar 403 se Consumidor edita outro usuario`() {
        val updateRequest =
            ClienteUpdateRequest(
                nome = "Invasao",
                email = "hacker@email.com",
            )

        mockMvc
            .perform(
                put("/api/v1/clientes/$consumidorSecundarioId")
                    .header("Authorization", "Bearer $consumidorToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            ).andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `criar deve retornar 201 apenas se for Administrador ou Gerenciador`() {
        val novoCliente =
            ClienteRequest(
                nome = "Novo Cliente da Feira",
                email = "novo@email.com",
                senha = "senhaSegura123",
                telefone = "87999992222",
                descricao = "Descrição do novo cliente",
            )

        mockMvc
            .perform(
                post("/api/v1/clientes")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(novoCliente)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.nome").value("Novo Cliente da Feira"))
            .andExpect(jsonPath("$.data.descricao").value("Descrição do novo cliente"))
            .andExpect(jsonPath("$.data.endereco").isEmpty)
    }

    @Test
    fun `criar deve retornar 403 se Consumidor tentar criar novo cliente`() {
        val novoCliente =
            ClienteRequest(
                nome = "Novo",
                email = "novo@email.com",
                senha = "senhaSegura123",
            )

        mockMvc
            .perform(
                post("/api/v1/clientes")
                    .header("Authorization", "Bearer $consumidorToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(novoCliente)),
            ).andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `deletar deve retornar 200 se Administrador deletar Consumidor`() {
        mockMvc
            .perform(
                delete("/api/v1/clientes/$consumidorId")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        // Verifica o Soft Delete via JDBC (ignora filtros do Hibernate)
        val ativo =
            jdbcTemplate.queryForObject("SELECT ativo FROM usuarios WHERE id = '$consumidorId'", Boolean::class.java)
        val deletadoEm =
            jdbcTemplate.queryForObject(
                "SELECT deletado_em FROM usuarios WHERE id = '$consumidorId'",
                java.sql.Timestamp::class.java,
            )

        assert(ativo == false)
        assert(deletadoEm != null)
    }

    @Test
    fun `deletar deve retornar 403 se Consumidor deletar ele mesmo (somente Admin ou Gerente pode)`() {
        mockMvc
            .perform(
                delete("/api/v1/clientes/$consumidorId")
                    .header("Authorization", "Bearer $consumidorToken"),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `criar deve retornar 400 se o email ja estiver cadastrado`() {
        val request =
            ClienteRequest(
                nome = "Maria Silva",
                email = "joao@email.com", // Já existe no setup
                senha = "senha123",
                telefone = "87988887771",
            )

        mockMvc
            .perform(
                post("/api/v1/clientes")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("E-mail já cadastrado"))
    }

    @Test
    fun `criar deve retornar 400 se o telefone ja estiver cadastrado`() {
        val request =
            ClienteRequest(
                nome = "Carlos Eduardo",
                email = "carlos@novo.com",
                senha = "senha123",
                telefone = "87999991111", // Já existe no setup (João)
            )

        mockMvc
            .perform(
                post("/api/v1/clientes")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Telefone já cadastrado"))
    }

    @Test
    fun `criar cliente com endereco deve salvar ambos atomicamente`() {
        val zonaId =
            jdbcTemplate.queryForObject(
                "INSERT INTO zonas_entrega (nome, taxa, ativo) VALUES ('ZONA_TESTE', 7.00, true) RETURNING id",
                UUID::class.java,
            )

        val enderecoReq =
            br.edu.ufape.agrofeira.dto.request.EnderecoRequest(
                rua = "Rua Atômica",
                numero = "100",
                complemento = null,
                cidade = "Garanhuns",
                estado = "PE",
                cep = "55290000",
                zonaEntregaId = zonaId!!,
            )

        val request =
            ClienteRequest(
                nome = "Cliente Atômico",
                email = "atomico@email.com",
                senha = "senha123",
                endereco = enderecoReq,
            )

        mockMvc
            .perform(
                post("/api/v1/clientes")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.nome").value("Cliente Atômico"))
            .andExpect(jsonPath("$.data.endereco.rua").value("Rua Atômica"))
    }
}
