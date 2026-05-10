package br.pucpr.restaurante.users.requests

import jakarta.validation.constraints.NotBlank

data class UpdateUserRequest(
    @NotBlank
    val name: String?
)
