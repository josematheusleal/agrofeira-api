package br.edu.ufape.agrofeira.repository

import br.edu.ufape.agrofeira.domain.entity.Usuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface UsuarioRepository : JpaRepository<Usuario, UUID> {
    fun findByEmail(email: String): Optional<Usuario>

    fun findByTelefone(telefone: String): Optional<Usuario>

    @Query("SELECT u FROM Usuario u WHERE u.email = :identificador OR u.telefone = :identificador")
    fun findByIdentificador(identificador: String): Optional<Usuario>

    @Query("SELECT u FROM Usuario u JOIN u.perfis p WHERE p.nome = :nomePerfil")
    fun findByPerfilNome(
        nomePerfil: String,
        pageable: Pageable,
    ): Page<Usuario>

    @Query("SELECT u FROM Usuario u JOIN u.perfis p WHERE p.nome = :nomePerfil AND LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    fun findByPerfilNomeAndNomeContainingIgnoreCase(
        nomePerfil: String,
        nome: String,
        pageable: Pageable,
    ): Page<Usuario>

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.deletadoEm = CURRENT_TIMESTAMP, u.ativo = false WHERE u.id = :id")
    fun deletarLogicamente(id: UUID)

    @Query(value = "SELECT COUNT(*) > 0 FROM usuarios WHERE email = :email", nativeQuery = true)
    fun existsByEmailIncludingDeleted(
        @Param("email") email: String,
    ): Boolean

    @Query(value = "SELECT COUNT(*) > 0 FROM usuarios WHERE telefone = :telefone", nativeQuery = true)
    fun existsByTelefoneIncludingDeleted(
        @Param("telefone") telefone: String,
    ): Boolean
}
