package br.pucpr.restaurante.pedidos.responses

import br.pucpr.restaurante.pedidos.Pedido
import br.pucpr.restaurante.produtos.responses.ProdutoResponse
import java.math.BigDecimal
import java.time.LocalDateTime

data class PedidoResponse(
    val id: Long,
    val cliente: String,
    val status: String,
    val criadoEm: LocalDateTime,
    val produtos: List<ProdutoResponse>,
    val total: BigDecimal,
) {
    constructor(pedido: Pedido) : this(
        id = pedido.id!!,
        cliente = pedido.cliente,
        status = pedido.status.name,
        criadoEm = pedido.criadoEm,
        produtos = pedido.produtos.map { ProdutoResponse(it) }.sortedBy { it.id },
        total = pedido.total(),
    )
}
