package br.pucpr.restaurante.pedidos

import br.pucpr.restaurante.exceptions.BadRequestException

enum class PedidoStatus {
    CRIADO, PAGO, ENTREGUE, CANCELADO;

    companion object {
        fun findOrNull(status: String) =
            entries.find { it.name == status.uppercase() }

        fun find(status: String) =
            findOrNull(status) ?: throw BadRequestException("Status inválido: $status")
    }
}
