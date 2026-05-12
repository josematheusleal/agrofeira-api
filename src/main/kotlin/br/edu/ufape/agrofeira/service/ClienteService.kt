package br.edu.ufape.agrofeira.service

import br.edu.ufape.agrofeira.domain.entity.Usuario
import br.edu.ufape.agrofeira.dto.request.ClienteRequest
import br.edu.ufape.agrofeira.dto.request.ClienteUpdateRequest
import br.edu.ufape.agrofeira.exception.ResourceNotFoundException
import br.edu.ufape.agrofeira.repository.UsuarioRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class ClienteService(
    private val usuarioService: UsuarioService,
    private val usuarioRepository: UsuarioRepository,
    private val enderecoService: EnderecoService,
) {
    fun listar(
        nome: String? = null,
        pageable: Pageable,
    ): Page<Usuario> =
        if (nome.isNullOrBlank()) {
            usuarioRepository.findByPerfilNome("CONSUMIDOR", pageable)
        } else {
            usuarioRepository.findByPerfilNomeAndNomeContainingIgnoreCase("CONSUMIDOR", nome, pageable)
        }

    fun buscarPorId(id: UUID): Usuario {
        val usuario =
            usuarioRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("Cliente", id.toString()) }
        if (usuario.perfis.none { it.nome == "CONSUMIDOR" }) {
            throw ResourceNotFoundException("Cliente", id.toString())
        }
        return usuario
    }

    @Transactional
    fun criar(request: ClienteRequest): Usuario {
        val novoUsuario =
            Usuario(
                nome = request.nome,
                email = request.email,
                telefone = request.telefone,
                senhaHash = request.senha,
                descricao = request.descricao,
            )
        val usuarioSalvo = usuarioService.cadastrar(novoUsuario, setOf("CONSUMIDOR"))

        if (request.endereco != null) {
            enderecoService.salvarEndereco(usuarioSalvo.id, request.endereco)
        }

        return usuarioSalvo
    }

    @Transactional
    fun atualizar(
        id: UUID,
        request: ClienteUpdateRequest,
    ): Usuario {
        val usuario = buscarPorId(id)

        usuario.atualizadoEm = LocalDateTime.now()

        val usuarioAtualizado =
            usuario.copy(
                nome = request.nome,
                email = request.email ?: usuario.email,
                telefone = request.telefone ?: usuario.telefone,
                descricao = request.descricao ?: usuario.descricao,
            )
        // O copy de data class preserva as listas não informadas, mas como JPA tem problemas com set, precisamos usar save
        return usuarioRepository.save(usuarioAtualizado)
    }

    @Transactional
    fun deletar(id: UUID) {
        buscarPorId(id)
        usuarioRepository.deletarLogicamente(id)
    }
}
