package it.polito.catalogueservice.ticketcatalogueservice

import com.google.gson.GsonBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.catalogueservice.ticketcatalogueservice.entities.TicketCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.repositories.TicketCatalogueRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitEntity
import org.springframework.web.reactive.function.client.awaitExchange
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TicketsControllerTests {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var ticketCatalogueRepository: TicketCatalogueRepository

    val webClient = WebClient.create("http://localhost:$port")

    @Value("\${key}")
    lateinit var key: String

    @AfterEach
    @BeforeEach
    fun destroyAll() {
        runBlocking {
            ticketCatalogueRepository.deleteAll()
        }
    }


    @Test
    fun getTicketsCustomer() {
        val baseUrl = "http://localhost:$port"

        runBlocking {
            val ticket = ticketCatalogueRepository.save(TicketCatalogue(2.0, "Weekly"))

            val claims = Jwts.claims().setSubject("Name")
            claims["roles"] = arrayListOf("ROLE_CUSTOMER")

            val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

            val headers = HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            headers.set("Authorization", "Bearer $token")

            val response: ResponseEntity<String> = webClient.get()
                    .uri("$baseUrl/tickets")
                    .headers { httpHeadersOnWebClientBeingBuilt ->
                        httpHeadersOnWebClientBeingBuilt.addAll(headers)
                    }
                    .awaitExchange()
                    .awaitEntity()

            println(response.body)
            Assertions.assertEquals(HttpStatus.OK, response.statusCode)

            val responseObject = JSONObject(response.body)
            val tickets = GsonBuilder().create().fromJson(responseObject.get("tickets").toString(),Array<TicketCatalogue>::class.java).toList()
            assert(tickets[0].type == "Weekly")
            assert(tickets[0].price == 2.0)
        }

    }

    @Test
    fun getTicketsAdmin() {
        val baseUrl = "http://localhost:$port"

        runBlocking {
            val ticket = ticketCatalogueRepository.save(TicketCatalogue(2.0, "Weekly"))

            val claims = Jwts.claims().setSubject("Name")
            claims["roles"] = arrayListOf("ROLE_ADMIN")

            val token = Jwts.builder()
                    .setSubject("Name")
                    .setClaims(claims)
                    .setIssuedAt(Date(System.currentTimeMillis()))
                    .setExpiration(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
                    .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                    .compact()

            val headers = HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            headers.set("Authorization", "Bearer $token")

            val response: ResponseEntity<String> = webClient.get()
                    .uri("$baseUrl/tickets")
                    .headers { httpHeadersOnWebClientBeingBuilt ->
                        httpHeadersOnWebClientBeingBuilt.addAll(headers)
                    }
                    .awaitExchange()
                    .awaitEntity()

            println(response.body)
            Assertions.assertEquals(HttpStatus.OK, response.statusCode)

            val responseObject = JSONObject(response.body)
            val tickets = GsonBuilder().create().fromJson(responseObject.get("tickets").toString(),Array<TicketCatalogue>::class.java).toList()
            assert(tickets[0].type == "Weekly")
            assert(tickets[0].price == 2.0)
        }

    }

    @Test
    fun getTicketsNoAuth() {
        val baseUrl = "http://localhost:$port"

        runBlocking {
            val ticket = TicketCatalogue(3.5, "Day")
            ticketCatalogueRepository.save(ticket)

            val response: ResponseEntity<String> = webClient.get()
                    .uri("$baseUrl/tickets")
                    .awaitExchange()
                    .awaitEntity()

            println(response.body)
            Assertions.assertEquals(HttpStatus.OK, response.statusCode)

            val responseObject = JSONObject(response.body)
            val tickets = GsonBuilder().create().fromJson(responseObject.get("tickets").toString(),Array<TicketCatalogue>::class.java).toList()
            assert(tickets[0].type == "Day")
            assert(tickets[0].price == 3.5)
        }

    }

}