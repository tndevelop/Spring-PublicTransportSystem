package it.polito.catalogueservice.ticketcatalogueservice

import com.google.gson.GsonBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.catalogueservice.ticketcatalogueservice.entities.OrderCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.entities.TicketCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.models.PaymentInformation
import it.polito.catalogueservice.ticketcatalogueservice.models.ShoppingTickets
import it.polito.catalogueservice.ticketcatalogueservice.models.UserDetails
import it.polito.catalogueservice.ticketcatalogueservice.repositories.OrderCatalogueRepository
import it.polito.catalogueservice.ticketcatalogueservice.repositories.TicketCatalogueRepository
import it.polito.catalogueservice.ticketcatalogueservice.repositories.UserDetailsRepository
import kotlinx.coroutines.*
import org.json.JSONArray
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
class ShopControllerTests {

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

    @Autowired
    lateinit var userDetailsRepository : UserDetailsRepository

    val webClient = WebClient.create("http://localhost:$port")


    @BeforeEach
    fun destroyAll() {
        runBlocking{
            ticketCatalogueRepository.deleteAll()
            orderCatalogueRepository.deleteAll()
        }
    }

    // This test requires a user with username "Name" in the traveler service
    @Test
    fun postShop() {

        runBlocking {

            val baseUrl = "http://localhost:$port"

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

			//TODO replace with relevant data
            val paymentInformation = PaymentInformation("a 16 digits card number", "month/day card expires", "CCV code",
                                    "Name")

            val userDetails = UserDetails("Name")
            userDetailsRepository.save(userDetails)

            val tick = ticketCatalogueRepository.save(TicketCatalogue(2.0, "Weekly", 0, 200, "A"))

            val shopTicket = ShoppingTickets(3, tick.id, paymentInformation)

            val response: ResponseEntity<String> = webClient.post()
                .uri("$baseUrl/shop")
               .headers { httpHeadersOnWebClientBeingBuilt ->
                    httpHeadersOnWebClientBeingBuilt.addAll(headers)
                }
                .body(Mono.just(shopTicket), ShoppingTickets::class.java)
                .awaitExchange()
                .awaitEntity()

            Assertions.assertEquals(HttpStatus.OK, response.statusCode)
            val responseObject = JSONObject(response.body)
            val orderid = GsonBuilder().create().fromJson(responseObject.get("message").toString(), String::class.java)
            assert(orderid != null)

        }
    }

}