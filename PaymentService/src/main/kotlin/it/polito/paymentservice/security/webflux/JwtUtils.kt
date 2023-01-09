package it.polito.paymentservice.security.webflux

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Service
class JwtUtils {

    @Value("\${key}")
    lateinit var key: String

    @Value("\${prefix}")
    lateinit var prefix : String

    /*
        it validates the token received in a
        request, catching all exceptions and returning true only if the token is valid
     */
    fun validateJwt(authToken: String): Boolean {
        if(authToken.isEmpty())
            return false //throw IllegalAccessException("Token should not be empty")
        try {
            val claims: Claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(key.toByteArray()))
                .build()
                 .parseClaimsJws(authToken.replace(prefix, ""))
                .getBody()

            return true
        } catch (e: ExpiredJwtException) {
            return false
        } catch (e: NullPointerException) {
            return false
        }
        catch (e: Exception) {
            return false
        }
    }

    /*
        it relies on the Jwts
        parser to retrieve the username and roles from the token
     */
    fun getDetailsJwt(authToken: String): Map<String, String> {
        if(authToken.isEmpty())
            throw IllegalAccessException("Token should not be empty")
        try {
            val claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(key.toByteArray()))
                    .build()
                    .parseClaimsJws(authToken.replace(prefix, ""))

            val scopes = claims.body["roles"] as ArrayList<String>
            val scope = scopes.get(0)

            val username = claims.body.subject
            val role = scope
            return mapOf("username" to username, "roles" to role)

        } catch (e: ExpiredJwtException) {
            throw AuthenticationServiceException("token expired: please login again")
        }catch (e: Exception) {
            throw Exception(e.message)
        }

    }
}