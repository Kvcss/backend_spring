package br.pucpr.restaurante.users

import br.pucpr.restaurante.roles.Role
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.persistence.Transient

@Entity
@Table(name = "UserTable")
class User(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    var password: String = "",
    var name: String = "",

    @ManyToMany
    @JoinTable(
        name = "UserRole",
        joinColumns = [JoinColumn(name = "idUser")],
        inverseJoinColumns = [JoinColumn(name = "idRole")]
    )
    var roles: MutableSet<Role> = mutableSetOf(),
) {
    @Transient
    fun isAdmin() = roles.any { r -> r.name == "ADMIN" }
}
