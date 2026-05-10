package br.pucpr.restaurante.users.responses

import br.pucpr.restaurante.users.User

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
) {
    constructor(user: User) : this(user.id!!, user.name, user.email)
}
