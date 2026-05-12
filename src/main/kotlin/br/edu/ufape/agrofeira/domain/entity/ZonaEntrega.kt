package br.edu.ufape.agrofeira.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "zonas_entrega")
@SQLDelete(sql = "UPDATE zonas_entrega SET deletado_em = NOW(), ativo = false WHERE id = ? AND versao = ?")
@SQLRestriction("deletado_em IS NULL")
data class ZonaEntrega(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Version
    val versao: Long = 0,
    @Column(nullable = false)
    val nome: String = "",
    @Column(nullable = false)
    val taxa: BigDecimal = BigDecimal.ZERO,
    val ativo: Boolean = true,
    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "atualizado_em", nullable = false)
    var atualizadoEm: LocalDateTime = LocalDateTime.now(),
    @Column(name = "deletado_em")
    val deletadoEm: LocalDateTime? = null,
)
