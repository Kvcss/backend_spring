package br.pucpr.restaurante.roles

import org.springframework.stereotype.Service

@Service
class RoleService(val repository: RoleRepository) {
    fun insert(role: Role): Role? {
        if (repository.findByName(role.name) != null) {
            return null
        }
        return repository.save(role)
    }

    fun findAll() = repository.findAll()
}
