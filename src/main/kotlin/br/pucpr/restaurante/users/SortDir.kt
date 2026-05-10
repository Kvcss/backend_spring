package br.pucpr.restaurante.users

import br.pucpr.restaurante.exceptions.BadRequestException

enum class SortDir {
    ASC, DESC;

    companion object {
        fun findOrNull(sortDir: String) =
            entries.find { it.name == sortDir.uppercase() }

        fun find(sortDir: String) =
            findOrNull(sortDir) ?: throw BadRequestException("Unknown sort dir: $sortDir")
    }
}
