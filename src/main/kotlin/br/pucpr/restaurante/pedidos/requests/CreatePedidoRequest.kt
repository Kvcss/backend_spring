package br.pucpr.restaurante.pedidos.requests

import br.pucpr.restaurante.pedidos.Pedido
import jakarta.validation.constraints.NotBlank

data class CreatePedidoRequest(
    @NotBlank
    val cliente: String?,

    val produtoIds: Set<Long> = emptySet(),
) {
    fun toPedido() = Pedido(cliente = cliente!!)
}
