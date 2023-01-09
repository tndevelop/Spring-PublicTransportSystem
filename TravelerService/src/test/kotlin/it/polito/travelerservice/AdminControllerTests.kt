package it.polito.travelerservice

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.travelerservice.entities.TicketPurchased
import it.polito.travelerservice.entities.Transit
import it.polito.travelerservice.entities.UserDetails
import it.polito.travelerservice.repositories.TicketPurchasedRepository
import it.polito.travelerservice.repositories.TransitRepository
import it.polito.travelerservice.repositories.UserDetailsRepository
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
class AdminControllerTests {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var userDetailsRepository : UserDetailsRepository

    @Autowired
    lateinit var ticketPurchasedRepository: TicketPurchasedRepository

    @Autowired
    lateinit var transitRepository: TransitRepository

    @Value("\${key}")
    lateinit var key: String

    @BeforeEach
    fun destroyAll() {
        userDetailsRepository.deleteAll()
        ticketPurchasedRepository.deleteAll()
        transitRepository.deleteAll()

        val userDetails = UserDetails("Name")
        userDetailsRepository.save(userDetails)

        val user2 = UserDetails("Name2")
        userDetailsRepository.save(user2)

    }

    @Test
    fun getTravelers() {
        val baseUrl = "http://localhost:$port"

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_ADMIN")

        val token = Jwts.builder()
                    .setSubject("Name")
                    .setClaims(claims)
                    .setIssuedAt(Date(System.currentTimeMillis()))
                    .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //expTime in days
                    .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                    .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
            "$baseUrl/admin/travelers",
            HttpMethod.GET,
            HttpEntity<String>(null, headers),
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val responseObject = JSONObject(response.body)
        val users = GsonBuilder().create().fromJson(responseObject.get("users").toString(),Array<String>::class.java).toList()
        assert(users.size == 2)
        val myUser = users[0]
        assert(myUser == "Name" || myUser == "Name2")
    }

    @Test
    fun getTravelersNotAdmin() {
        val baseUrl = "http://localhost:$port"

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_CUSTOMER")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
                "$baseUrl/admin/travelers",
                HttpMethod.GET,
                HttpEntity<String>(null, headers),
                String::class.java
        )

        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun getTravelerByUserId() {
        val baseUrl = "http://localhost:$port"

        val user = userDetailsRepository.findByName("Name")
        val userID = user!!.id

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_ADMIN")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
                "$baseUrl/admin/traveler/$userID/profile",
                HttpMethod.GET,
                HttpEntity<String>(null, headers),
                String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val responseObject = JSONObject(response.body)
        val username = responseObject.get("name")
        assert(username == "Name")
    }

    @Test
    fun getTicketsByUserId() {
        val baseUrl = "http://localhost:$port"

        val user = userDetailsRepository.findByName("Name")
        val userID = user!!.id
        val ticket = TicketPurchased(iat=Timestamp.from(Instant.now()), exp= Timestamp.from(Instant.parse("2022-12-12T10:37:30.00Z")), zid="A","", "3h", user)
        ticketPurchasedRepository.save(ticket)

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_ADMIN")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
            "$baseUrl/admin/traveler/${userID}/tickets",
            HttpMethod.GET,
            HttpEntity<String>(null, headers),
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val responseObject = JSONObject(response.body)
        val tickets = GsonBuilder().create().fromJson(responseObject.get("tickets").toString(),Array<TicketPurchased>::class.java).toList()
        assert(tickets[0].zid == "A")
        assert(tickets.size == 1)
    }

    @Test
    fun getTravelersByUserWrongReq() {
        val baseUrl = "http://localhost:$port"

        //select a non existing ID
        val ids = userDetailsRepository.findAll().map{ u -> u.id }
        var nonExistingId : Long= -1
        var i : Long= 0
        while (nonExistingId == -1 as Number){
            if (ids.contains(i))
                i++
            else
                nonExistingId = i
        }

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_ADMIN")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
            "$baseUrl/admin/traveler/${nonExistingId}/profile",
            HttpMethod.GET,
            HttpEntity<String>("", headers),
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun getMultipleTicketsByUserId() {
        val baseUrl = "http://localhost:$port"

        val user = userDetailsRepository.findAll().elementAt(0)

        ticketPurchasedRepository.save(TicketPurchased(zid="A", user = user))
        ticketPurchasedRepository.save(TicketPurchased(zid="B", user = user))

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_ADMIN")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
            "$baseUrl/admin/traveler/${user.id}/tickets",
            HttpMethod.GET,
            HttpEntity<String>("", headers),
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val responseObject = JSONObject(response.body)
        val tickets = GsonBuilder().create().fromJson(responseObject.get("tickets").toString(),Array<TicketPurchased>::class.java).toList()
        assert(tickets.size == 2)
        val ticket = tickets[0]
        assert(ticket.zid=="A" || ticket.zid=="B")
    }


    @Test
    fun getTicketsByUserIdNoTickets() {
        val baseUrl = "http://localhost:$port"

        val userID = userDetailsRepository.findAll().elementAt(0).id

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_ADMIN")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
            "$baseUrl/admin/traveler/${userID}/tickets",
            HttpMethod.GET,
            HttpEntity<String>("", headers),
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun getAllTransits() {
        val baseUrl = "http://localhost:$port"

        var transit = Transit(0,0, Timestamp.valueOf("2022-10-02 00:00:00"))
        transitRepository.save(transit)
        transit = Transit(1,1, Timestamp.valueOf("2022-10-07 00:00:00"))
        transitRepository.save(transit)

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_ADMIN")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
                "$baseUrl/admin/transits",
                HttpMethod.GET,
                HttpEntity<String>(null, headers),
                String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val responseObject = JSONObject(response.body)
        val transits = GsonBuilder().create().fromJson(responseObject.get("transits").toString(),Array<Transit>::class.java).toList()
        assert(transits.size == 2)
        val transit1 = transits[0]
        assert(transit1.passengerId == 0L || transit1.passengerId == 1L)
    }

    @Test
    fun getAllTransitsByPassenger() {
        val baseUrl = "http://localhost:$port"

        var transit = Transit(0,0, Timestamp.valueOf("2022-10-02 00:00:00"))
        transitRepository.save(transit)
        transit = Transit(1,1, Timestamp.valueOf("2022-10-07 00:00:00"))
        transitRepository.save(transit)

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_ADMIN")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
                "$baseUrl/admin/transits/0",
                HttpMethod.GET,
                HttpEntity<String>(null, headers),
                String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val responseObject = JSONObject(response.body)
        val transits = GsonBuilder().create().fromJson(responseObject.get("transits").toString(),Array<Transit>::class.java).toList()
        assert(transits.size == 1)
        val transit1 = transits[0]
        assert(transit1.passengerId == 0L)
    }

    @Test
    fun getAllTransitsInRange() {
        val baseUrl = "http://localhost:$port"

        var transit = Transit(0,0, Timestamp.valueOf("2022-07-02 00:00:00"))
        transitRepository.save(transit)
        transit = Transit(1,1, Timestamp.valueOf("2022-07-07 00:00:00"))
        transitRepository.save(transit)

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_ADMIN")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
                "$baseUrl/admin/transits?dateRange={dateRange}",
                HttpMethod.GET,
                HttpEntity<String>(null, headers),
                String::class.java,
                "30/06/2022-05/07/2022"
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val responseObject = JSONObject(response.body)
        val transits = GsonBuilder().create().fromJson(responseObject.get("transits").toString(),Array<Transit>::class.java).toList()
        assert(transits.size == 1)
        val transit1 = transits[0]
        assert(transit1.passengerId == 0L)
    }

    @Test
    fun getAllTransitsByPassengerInRange() {
        val baseUrl = "http://localhost:$port"

        var transit = Transit(0,0, Timestamp.valueOf("2022-07-02 00:00:00"))
        transitRepository.save(transit)
        transit = Transit(0,1, Timestamp.valueOf("2022-07-07 00:00:00"))
        transitRepository.save(transit)
        transit = Transit(1,1, Timestamp.valueOf("2022-07-03 00:00:00"))
        transitRepository.save(transit)

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_ADMIN")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val response: ResponseEntity<String> = restTemplate.exchange(
                "$baseUrl/admin/transits/0?dateRange={dateRange}",
                HttpMethod.GET,
                HttpEntity<String>(null, headers),
                String::class.java,
                "30/06/2022-05/07/2022"
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val responseObject = JSONObject(response.body)
        val transits = GsonBuilder().create().fromJson(responseObject.get("transits").toString(),Array<Transit>::class.java).toList()
        assert(transits.size == 1)
        val transit1 = transits[0]
        assert(transit1.passengerId == 0L)
        assert(transit1.transitTime == Timestamp.valueOf("2022-07-02 00:00:00"))
    }
}