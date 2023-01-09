package it.polito.catalogueservice.ticketcatalogueservice.services

import com.google.gson.JsonObject
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.catalogueservice.ticketcatalogueservice.repositories.UserDetailsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

@Service
class ShopService (val userDetailsRepository: UserDetailsRepository) {

    //@LocalServerPort
    protected var port: Int = 8081

    //@Value("\${key}")lateinit
    var key = "some valid key, just for testing" //: String

    var restTemplate =  RestTemplate()

    fun getProfile(username: String): String? {

        val baseUrl = "http://localhost:$port"

        val claims = Jwts.claims().setSubject(username)
        claims["roles"] = arrayListOf("ROLE_CUSTOMER")

        val token = Jwts.builder()
            .setSubject(username)
            .setClaims(claims)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //exp in 7 days
            .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
            .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
            "$baseUrl/my/profile",
            HttpMethod.GET,
            HttpEntity<String>(null, headers),
            String::class.java
        )
        println("response ${response.body.toString()}")
        return response.body
    }

    suspend fun addTickets(userId: Long, numTickets: Int, zones: String, validity: Long, type: String): String? {

        val username = userDetailsRepository.findFirstById(userId).awaitSingle().name

        val baseUrl = "http://localhost:$port"

        val claims = Jwts.claims().setSubject(username)
        claims["roles"] = arrayListOf("ROLE_CUSTOMER")

        val token = Jwts.builder()
                .setSubject(username)
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 60*60*1000)) //exp in 1 hour
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val body = JsonObject()
        body.addProperty("cmd","buy_tickets")
        body.addProperty("quantity", numTickets.toString())
        body.addProperty("zones", zones)
        body.addProperty("validity", validity)
        body.addProperty("type", type)
        println(body.toString())

        val response: ResponseEntity<String> = withContext(Dispatchers.IO) {
            restTemplate.exchange(
                    "$baseUrl/my/tickets",
                    HttpMethod.POST,
                    HttpEntity<String>(body.toString(), headers),
                    String::class.java
            )
        }
        println("response ${response.body.toString()}")
        return response.body
    }
}