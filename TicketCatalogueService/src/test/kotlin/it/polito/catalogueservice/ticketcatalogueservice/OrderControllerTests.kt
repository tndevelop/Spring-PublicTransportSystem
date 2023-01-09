package it.polito.catalogueservice.ticketcatalogueservice

import com.google.gson.GsonBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.catalogueservice.ticketcatalogueservice.entities.OrderCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.entities.TicketCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.enum.OrderStatus
import it.polito.catalogueservice.ticketcatalogueservice.repositories.OrderCatalogueRepository
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
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitEntity
import org.springframework.web.reactive.function.client.awaitExchange
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTests {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var orderCatalogueRepository: OrderCatalogueRepository

    @Autowired
    lateinit var ticketCatalogueRepository: TicketCatalogueRepository

    val webClient = WebClient.create("http://localhost:$port")

    @Value("\${key}")
    lateinit var key: String

    @AfterEach
    @BeforeEach
    fun destroyAll() {
        runBlocking {
            orderCatalogueRepository.deleteAll()
        }
    }


    @Test
    fun getOrders() {
        val baseUrl = "http://localhost:$port"

        runBlocking {
            val ticket = ticketCatalogueRepository.save(TicketCatalogue(2.0, "Weekly"))
            val order = OrderCatalogue("PENDING", 3.5, 1, 0L, ticket.id)
            orderCatalogueRepository.save(order)

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

            val response: ResponseEntity<String> = restTemplate.exchange(
                "$baseUrl/orders",
                HttpMethod.GET,
                HttpEntity<String>(null, headers),
                String::class.java
            )

            println(response.body)
            Assertions.assertEquals(HttpStatus.OK, response.statusCode)

            val responseObject = JSONObject(response.body)
            val orders = GsonBuilder().create().fromJson(responseObject.get("orders").toString(),Array<OrderCatalogue>::class.java).toList()
            assert(orders[0].status == "PENDING")
            assert(orders[0].totalCost == 3.5)
            assert(orders[0].numberOfTickets == 1)
        }

    }

    @Test
    fun getOrderById() {
        val baseUrl = "http://localhost:$port"

        runBlocking {
            val ticket = ticketCatalogueRepository.save(TicketCatalogue(2.0, "Weekly"))
            val order = OrderCatalogue("PENDING", 3.5, 1, 0L, ticket.id)
            orderCatalogueRepository.save(order)

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
                .uri("$baseUrl/orders/${order.id}")
                .headers { httpHeadersOnWebClientBeingBuilt ->
                    httpHeadersOnWebClientBeingBuilt.addAll(headers)
                }
                .awaitExchange()
                .awaitEntity()

            println(response.body)
            Assertions.assertEquals(HttpStatus.OK, response.statusCode)

            val responseObject = JSONObject(response.body)
            val finalOrder = GsonBuilder().create().fromJson(responseObject.get("order").toString(),OrderCatalogue::class.java)
            assert(finalOrder.status == "PENDING")
            assert(finalOrder.totalCost == 3.5)
            assert(finalOrder.numberOfTickets == 1)
        }

    }

}