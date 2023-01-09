package it.polito.catalogueservice.ticketcatalogueservice

import com.google.gson.GsonBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.catalogueservice.ticketcatalogueservice.entities.OrderCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.entities.TicketCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.repositories.OrderCatalogueRepository
import it.polito.catalogueservice.ticketcatalogueservice.repositories.TicketCatalogueRepository
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitEntity
import org.springframework.web.reactive.function.client.awaitExchange
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono
import java.util.*

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminControllerTests {

    @LocalServerPort
    protected var port: Int = 0

    @Value("\${key}")
    lateinit var key: String

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var ticketCatalogueRepository: TicketCatalogueRepository


    @Autowired
    lateinit var orderCatalogueRepository : OrderCatalogueRepository

    val webClient = WebClient.create("http://localhost:$port")


    @BeforeEach
    fun destroyAll() {
        runBlocking{
            ticketCatalogueRepository.deleteAll()
            orderCatalogueRepository.deleteAll()
        }
    }

    @Test
    fun postAdminTicket() {

        runBlocking {

            val baseUrl = "http://localhost:$port"

            val claims = Jwts.claims().setSubject("Name")
            claims["roles"] = arrayListOf("ROLE_ADMIN")

            val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

            val headers = HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            headers.set("Authorization", "Bearer $token")

            val ticketToAdd = JSONObject()
            ticketToAdd.put("price", 20.0)
            ticketToAdd.put("type", "Weekly")

            val ticketCat = TicketCatalogue(2.0, "Weekly")

            val response: ResponseEntity<String> = webClient.post()
                .uri("$baseUrl/admin/tickets")
                .headers { httpHeadersOnWebClientBeingBuilt ->
                    httpHeadersOnWebClientBeingBuilt.addAll(headers)
                }
                .body(Mono.just(ticketCat), TicketCatalogue::class.java)
                .awaitExchange()
                .awaitEntity()

            Assertions.assertEquals(HttpStatus.OK, response.statusCode)
            val responseObject = JSONObject(response.body)
            val ticket =
                GsonBuilder().create().fromJson(responseObject.get("ticket").toString(), TicketCatalogue::class.java)
            assert(ticket.price == 2.0)

        }
    }

    @Test
    fun updateTicket() {

        runBlocking {

            val baseUrl = "http://localhost:$port"

            val claims = Jwts.claims().setSubject("Name")
            claims["roles"] = arrayListOf("ROLE_ADMIN")

            val token = Jwts.builder()
                    .setSubject("Name")
                    .setClaims(claims)
                    .setIssuedAt(Date(System.currentTimeMillis()))
                    .setExpiration(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) //expTime in days
                    .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                    .compact()

            val headers = HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            headers.set("Authorization", "Bearer $token")

            val ticketToAdd = JSONObject()
            ticketToAdd.put("price", 20.0)
            ticketToAdd.put("type", "Weekly")

            val ticketCat = TicketCatalogue(2.0, "Weekly")

            val response: ResponseEntity<String> = webClient.post()
                    .uri("$baseUrl/admin/tickets")
                    .headers { httpHeadersOnWebClientBeingBuilt ->
                        httpHeadersOnWebClientBeingBuilt.addAll(headers)
                    }
                    .body(Mono.just(ticketCat), TicketCatalogue::class.java)
                    .awaitExchange()
                    .awaitEntity()

            Assertions.assertEquals(HttpStatus.OK, response.statusCode)
            val responseObject = JSONObject(response.body)
            val ticket =
                    GsonBuilder().create().fromJson(responseObject.get("ticket").toString(), TicketCatalogue::class.java)
            assert(ticket.price == 2.0)

            val updatedTicket = TicketCatalogue(10.0, "Weekly")

            val response2: ResponseEntity<String> = webClient.put()
                    .uri("$baseUrl/admin/tickets")
                    .headers { httpHeadersOnWebClientBeingBuilt ->
                        httpHeadersOnWebClientBeingBuilt.addAll(headers)
                    }
                    .body(Mono.just(updatedTicket), TicketCatalogue::class.java)
                    .awaitExchange()
                    .awaitEntity()

            Assertions.assertEquals(HttpStatus.OK, response.statusCode)
            val responseObject2 = JSONObject(response2.body)
            val ticket2 =
                    GsonBuilder().create().fromJson(responseObject2.get("ticket").toString(), TicketCatalogue::class.java)
            assert(ticket2.price == 10.0)
        }
    }


    @Test
    fun getAdminOrders() {

        runBlocking {

            val baseUrl = "http://localhost:$port"

            val ticket = ticketCatalogueRepository.save(TicketCatalogue(2.0, "Weekly"))
            orderCatalogueRepository.save(OrderCatalogue("", 2.0, 1, 0, ticket.id))

            val claims = Jwts.claims().setSubject("Name")
            claims["roles"] = arrayListOf("ROLE_ADMIN")

            val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

            val headers = HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            headers.set("Authorization", "Bearer $token")

            val ticketToAdd = JSONObject()
            ticketToAdd.put("price", 20.0)
            ticketToAdd.put("type", "Weekly")

            val response: ResponseEntity<String> = webClient.get()
                .uri("$baseUrl/admin/orders")
                 .headers { httpHeadersOnWebClientBeingBuilt ->
                     httpHeadersOnWebClientBeingBuilt.addAll(headers)
                 }
                .awaitExchange()
                .awaitEntity()

            Assertions.assertEquals(HttpStatus.OK, response.statusCode)
            val responseObject = JSONObject(response.body)
            val orders =
                GsonBuilder().create().fromJson(responseObject.get("orders").toString(), Array<OrderCatalogue>::class.java)
            assert(orders[0].totalCost == 2.0)

        }

    }


    @Test
    fun getAdminOrderByUserId() {

        runBlocking {

            val baseUrl = "http://localhost:$port"

            val ticket = ticketCatalogueRepository.save(TicketCatalogue(2.0, "Weekly"))
            orderCatalogueRepository.save(OrderCatalogue("", 2.0, 1, 0, ticket.id))


            val claims = Jwts.claims().setSubject("Name")
            claims["roles"] = arrayListOf("ROLE_ADMIN")

            val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

            val headers = HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            headers.set("Authorization", "Bearer $token")

            val ticketToAdd = JSONObject()
            ticketToAdd.put("price", 20.0)
            ticketToAdd.put("type", "Weekly")

            val response: ResponseEntity<String> = webClient.get()
                .uri("$baseUrl/admin/orders/0")
                 .headers { httpHeadersOnWebClientBeingBuilt ->
                     httpHeadersOnWebClientBeingBuilt.addAll(headers)
                 }
                .awaitExchange()
                .awaitEntity()

            Assertions.assertEquals(HttpStatus.OK, response.statusCode)
            val responseObject = JSONObject(response.body)
            val orders =
                GsonBuilder().create().fromJson(responseObject.get("orders").toString(), Array<OrderCatalogue>::class.java)
            assert(orders[0].totalCost == 2.0)

        }
    }

}