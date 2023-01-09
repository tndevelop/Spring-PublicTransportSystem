package it.polito.travelerservice

import com.google.gson.GsonBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.travelerservice.entities.TicketPurchased
import it.polito.travelerservice.entities.UserDetails
import it.polito.travelerservice.models.TicketsToBuy
import it.polito.travelerservice.repositories.TicketPurchasedRepository
import it.polito.travelerservice.repositories.UserDetailsRepository
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.json.GsonJsonParser
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Timestamp
import java.time.Instant
import java.util.*


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketControllerTests {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var userDetailsRepository : UserDetailsRepository

    @Autowired
    lateinit var ticketPurchasedRepository: TicketPurchasedRepository

    @Value("\${key}")
    lateinit var key: String


    @BeforeEach
    fun destroyAll() {
        userDetailsRepository.deleteAll()
        ticketPurchasedRepository.deleteAll()

        val userDetails = UserDetails("Name")
        userDetailsRepository.save(userDetails)
    }

    @Test
    fun getTickets() {
        val baseUrl = "http://localhost:$port"

        val user = userDetailsRepository.findByName("Name")
        assert(user != null)

        var ticket = TicketPurchased(iat=Timestamp.from(Instant.now()),
                exp= Timestamp.from(Instant.parse("2022-12-12T10:37:30.00Z")), zid="A","", "3h", user)
        ticketPurchasedRepository.save(ticket)

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_CUSTOMER")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 60*60*1000))
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
                "$baseUrl/my/tickets",
                HttpMethod.GET,
                HttpEntity<String>(null, headers),
                String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val responseObject = JSONObject(response.body)
        val tickets = GsonBuilder().create().fromJson(responseObject.get("message").toString(),Array<TicketPurchased>::class.java).toList()
        assert(tickets[0].zid == "A")
    }

    @Test
    fun getTicketsWrongReq() {
        val baseUrl = "http://localhost:$port"

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_CUSTOMER")

        val token = Jwts.builder()
                .setSubject("Name")
            .setClaims(claims)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 60*60*1000))
            .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
            .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
            "$baseUrl/my/tickets",
            HttpMethod.GET,
            HttpEntity<String>("", headers),
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun generateTickets() {
        val baseUrl = "http://localhost:$port"

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_CUSTOMER")

        val token = Jwts.builder()
                .setSubject("Name")
            .setClaims(claims)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 60*60*1000))
            .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
            .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")



        val ticketsToBuyJson = JSONObject()
        ticketsToBuyJson.put("cmd" , "buy_tickets")
        ticketsToBuyJson.put("quantity", 2)
        ticketsToBuyJson.put("zones", "P")
        ticketsToBuyJson.put("validity", 180)
        ticketsToBuyJson.put("type", "3h")
        val response: ResponseEntity<String> = restTemplate.exchange(
            "$baseUrl/my/tickets",
            HttpMethod.POST,
            HttpEntity<String>(ticketsToBuyJson.toString(), headers),
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val responseObject = JSONObject(response.body)
        assert(responseObject.get("message") is JSONArray)
        var tickets = responseObject.get("message") as JSONArray
        assert(tickets[0] is JSONObject)
        var myTicket = tickets[0] as JSONObject
        assert(myTicket.get("zid") == "P")
    }

    @Test
    fun generateTicketsWrongReq() {
        val baseUrl = "http://localhost:$port"

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_CUSTOMER")

        val token = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 60*60*1000))
            .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
            .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val ticketsToBuy = TicketsToBuy("wrong_command", 2, "P", 180, "3h")
        val response: ResponseEntity<String> = restTemplate.exchange(
            "$baseUrl/my/tickets",
            HttpMethod.POST,
            HttpEntity<String>(ticketsToBuy.toString(), headers),
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun validateTicket() {
        val baseUrl = "http://localhost:$port"


        val user = userDetailsRepository.findByName("Name")
        assert(user != null)

        var ticket = TicketPurchased(iat=Timestamp.from(Instant.now()),
            exp= Timestamp.from(Instant.parse("2022-12-12T10:37:30.00Z")), zid="A","", "3h", user)
        ticketPurchasedRepository.save(ticket)

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_TURNSLIDE")

        val token = Jwts.builder()
            .setSubject("Name")
            .setClaims(claims)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 60*60*1000))
            .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
            .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")



        val ticketToValidateJson = JSONObject()
        ticketToValidateJson.put("ticketId" , ticket.sub)
        val response: ResponseEntity<String> = restTemplate.exchange(
            "$baseUrl/my/tickets",
            HttpMethod.POST,
            HttpEntity<String>(ticketToValidateJson.toString(), headers),
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val responseObject = JSONObject(response.body)
        assert(responseObject.get("correctly validated") is Boolean)
        var validated = responseObject.get("correctly validated") as Boolean
        assert(validated == true)
    }
}