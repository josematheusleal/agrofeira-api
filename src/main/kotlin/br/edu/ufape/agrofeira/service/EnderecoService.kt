package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Endereco
import br.edu.ufape.agrofeira.dto.request.EnderecoRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.EnderecoRepository
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import br.edu.ufape.agrofeira.repository.ZonaEntregaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class EnderecoService(
    private val repository: EnderecoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val zonaEntregaRepository: ZonaEntregaRepository,
    private val viaCepService: ViaCepService,
) {
    @Transactional(readOnly = true)
    fun buscarPorUsuarioId(usuarioId: UUID): Endereco =
        repository
            .findById(usuarioId)
            .orElseThrow { ResourceNotFoundException("Endereço", usuarioId.toString()) }

    @Transactional(readOnly = true)
    fun buscarPorUsuarioIdOuNulo(usuarioId: UUID): Endereco? = repository.findById(usuarioId).orElse(null)

    @Transactional
    fun salvarEndereco(
        usuarioId: UUID,
        request: EnderecoRequest,
    ): Endereco {
        val usuario =
            usuarioRepository
                .findById(usuarioId)
                .orElseThrow { ResourceNotFoundException("Usuário", usuarioId.toString()) }

        // 1. Validação ViaCep (Garanhuns-PE)
        val dadosCep =
            viaCepService.consultarCep(request.cep)
                ?: throw IllegalArgumentException("CEP não encontrado ou inválido")

        if (dadosCep.localidade != "Garanhuns" || dadosCep.uf != "PE") {
            throw IllegalArgumentException("No momento, atendemos apenas o município de Garanhuns-PE")
        }

        // 2. Validação de Zona de Entrega por Perfil
        val eConsumidor = usuario.perfis.any { it.nome == "CONSUMIDOR" }

        if (eConsumidor && request.zonaEntregaId == null) {
            throw IllegalArgumentException("A Zona de Entrega é obrigatória para Clientes (Consumidores)")
        }

        val zonaEntrega =
            request.zonaEntregaId?.let {
                zonaEntregaRepository
                    .findById(it)
                    .orElseThrow { ResourceNotFoundException("Zona de Entrega", it.toString()) }
            }

        if (zonaEntrega != null && !zonaEntrega.ativo) {
            throw IllegalArgumentException("A Zona de Entrega selecionada está inativa")
        }

        val endereco =
            repository.findById(usuarioId).orElseGet {
                Endereco(usuario = usuario)
            }

        // Auto-preenchimento com dados do ViaCep se vazio no request
        endereco.rua = request.rua ?: dadosCep.logradouro
        endereco.bairro = dadosCep.bairro
        endereco.numero = request.numero
        endereco.complemento = request.complemento
        endereco.cidade = "Garanhuns"
        endereco.estado = "PE"
        endereco.cep = request.cep
        endereco.zonaEntrega = zonaEntrega
        endereco.atualizadoEm = LocalDateTime.now()

        return repository.save(endereco)
    }

    @Transactional
    fun deletarEndereco(usuarioId: UUID) {
        val endereco = buscarPorUsuarioId(usuarioId)
        repository.delete(endereco)
    }
}
