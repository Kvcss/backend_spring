package br.pucpr.restaurante.exceptions

import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(UNAUTHORIZED)
class UnauthorizedException(
    message: String = "Unauthorized",
    cause: Throwable? = null,
) : IllegalStateException(message, cause)
