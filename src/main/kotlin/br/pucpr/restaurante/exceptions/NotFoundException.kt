package br.pucpr.restaurante.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(
    message: String = "Not Found",
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause) {
    constructor(id: Long, cause: Throwable? = null) : this("Not found: $id", cause)
}
