package br.pucpr.restaurante.roles.responses

import br.pucpr.restaurante.roles.Role

data class RoleResponse(
    val name: String,
    val description: String,
) {
    constructor(role: Role) : this(role.name, role.description)
}
