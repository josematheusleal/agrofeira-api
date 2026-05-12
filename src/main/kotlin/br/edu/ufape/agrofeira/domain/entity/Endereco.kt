package br.edu.ufape.agrofeira.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "enderecos")
class Endereco(
    @Id
    @Column(name = "usuario_id")
    var usuarioId: UUID? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    var usuario: Usuario? = null,
    var rua: String? = null,
    var numero: String? = null,
    var complemento: String? = null,
    var bairro: String? = null,
    var cidade: String? = null,
    var estado: String? = null,
    var cep: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_entrega_id", nullable = true)
    var zonaEntrega: ZonaEntrega? = null,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Endereco) return false
        return usuarioId != null && usuarioId == other.usuarioId
    }

    override fun hashCode(): Int = 31 // Standard for JPA entities with UUID PK
}
