package br.pucpr.restaurante.users.requests

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @NotBlank
    val email: String?,

    @NotBlank
    val password: String?,
)
