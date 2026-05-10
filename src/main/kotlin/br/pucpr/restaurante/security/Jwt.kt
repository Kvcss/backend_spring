package br.pucpr.restaurante.security

import br.pucpr.restaurante.users.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.jackson.io.JacksonSerializer
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Date

@Component
class Jwt {
    fun createToken(user: User): String =
        UserToken(user).let {
            Jwts.builder().json(JacksonSerializer())
                .signWith(Keys.hmacShaKeyFor(SECRET.toByteArray()))
                .issuedAt(utcNow().toDate())
                .expiration(
                    utcNow().plusHours(
                        if (it.isAdmin) ADMIN_EXPIRE_HOURS else EXPIRE_HOURS
                    ).toDate()
                )
                .issuer(ISSUER)
                .subject("${it.id}")
                .claim(USER_FIELD, it)
                .compact()
        }

    @Suppress("UNCHECKED_CAST")
    fun parseToken(token: String): UserToken? = try {
        val payload = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(SECRET.toByteArray()))
            .build()
            .parseSignedClaims(token)
            .payload
        val claim = payload[USER_FIELD] as? Map<String, Any> ?: return null
        UserToken(
            id = (claim["id"] as Number).toLong(),
            name = claim["name"] as String,
            roles = (claim["roles"] as List<String>).toSet()
        )
    } catch (e: Exception) {
        log.debug("Invalid JWT token: {}", e.message)
        null
    }

    companion object {
        const val SECRET = "eff56a337431c19cafa596c9847859f9525dcd48"
        const val ADMIN_EXPIRE_HOURS = 1L
        const val EXPIRE_HOURS = 48L
        const val ISSUER = "RestauranteAPI"
        const val USER_FIELD = "user"

        private val log = LoggerFactory.getLogger(Jwt::class.java)
        private fun utcNow() = ZonedDateTime.now(ZoneOffset.UTC)
        private fun ZonedDateTime.toDate(): Date = Date.from(this.toInstant())
    }
}
