package it.polito.loginservice.utils

import it.polito.loginservice.entities.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys

@Component
class JWTUtils {

    @Value("\${key}")
    lateinit var key: String

//    @Value("\${exp}")\
    private val jwtExpirationMs = 60 * 60 * 1000

    fun generateJwtToken(user: User): String
    {
        val claims = Jwts.claims().setSubject(user.username)
        claims["roles"] = arrayListOf(user.role.toString())
        claims["authorities"] = arrayListOf(user.authority.toString())

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + jwtExpirationMs)) //expTime in millisecs
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()
    }
}