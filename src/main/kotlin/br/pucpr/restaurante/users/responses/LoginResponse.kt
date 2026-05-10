package br.pucpr.restaurante.users.responses

data class LoginResponse(
    val token: String,
    val user: UserResponse
)
