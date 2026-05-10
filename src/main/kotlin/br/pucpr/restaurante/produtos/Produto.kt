package br.pucpr.restaurante.produtos

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "Produto")
class Produto(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var nome: String,

    @Column(nullable = false)
    var descricao: String = "",

    @Column(nullable = false)
    var preco: BigDecimal,

    @Column(nullable = false)
    var categoria: String,

    @Column(nullable = false)
    var disponivel: Boolean = true,
)
