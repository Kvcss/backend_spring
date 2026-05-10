package br.pucpr.restaurante.exceptions

import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(FORBIDDEN)
class ForbiddenException(
    message: String = "Forbidden",
    cause: Throwable? = null,
) : IllegalStateException(message, cause)
