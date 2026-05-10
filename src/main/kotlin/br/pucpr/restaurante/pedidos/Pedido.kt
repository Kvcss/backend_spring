package br.pucpr.restaurante.pedidos

import br.pucpr.restaurante.produtos.Produto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.persistence.Transient
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "Pedido")
class Pedido(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var cliente: String,

    @Column(nullable = false)
    var criadoEm: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PedidoStatus = PedidoStatus.CRIADO,

    @ManyToMany
    @JoinTable(
        name = "PedidoProduto",
        joinColumns = [JoinColumn(name = "idPedido")],
        inverseJoinColumns = [JoinColumn(name = "idProduto")]
    )
    var produtos: MutableSet<Produto> = mutableSetOf(),
) {
    @Transient
    fun total(): BigDecimal =
        produtos.fold(BigDecimal.ZERO) { acc, p -> acc + p.preco }
}
