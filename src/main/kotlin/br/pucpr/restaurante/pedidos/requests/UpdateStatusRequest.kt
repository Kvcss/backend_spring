package br.pucpr.restaurante.pedidos.requests

import jakarta.validation.constraints.NotBlank

data class UpdateStatusRequest(
    @NotBlank
    val status: String?
)
