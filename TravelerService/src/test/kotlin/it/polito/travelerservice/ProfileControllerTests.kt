package it.polito.travelerservice

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.travelerservice.entities.UserDetails
import it.polito.travelerservice.repositories.TicketPurchasedRepository
import it.polito.travelerservice.repositories.UserDetailsRepository
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.util.UriComponentsBuilder
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProfileControllerTests {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var userDetailsRepository : UserDetailsRepository

    @Value("\${key}")
    lateinit var key: String

    @BeforeEach
    fun destroyAll() {
        userDetailsRepository.deleteAll()
    }

    @Test
    fun getProfile() {
        val baseUrl = "http://localhost:$port"
        val userDetails = UserDetails("Name")
        userDetailsRepository.save(userDetails)
        val user = userDetailsRepository.findByName("Name")
        assert(user != null)

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_CUSTOMER")

        val token = Jwts.builder()
                .setSubject("Name")
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

        Assertions.assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        val responseObject = JSONObject(response.body)
        assert(responseObject.get("name") == "Name")
    }

    @Test
    fun getProfileNoUser() {
        val baseUrl = "http://localhost:$port"

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_CUSTOMER")

        val token = Jwts.builder()
                .setSubject("Name")
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

        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun updateProfile() {
        val baseUrl = "http://localhost:$port"
        val userDetails = UserDetails("Name")
        userDetailsRepository.save(userDetails)
        val user = userDetailsRepository.findByName("Name")
        assert(user != null)

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_CUSTOMER")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //exp in 7 days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val request = user!!.apply { address = "New address" }
        val response: ResponseEntity<String> = restTemplate.exchange(
                "$baseUrl/my/profile",
                HttpMethod.PUT,
                HttpEntity<UserDetails>(request, headers),
                String::class.java
        )

        Assertions.assertEquals(HttpStatus.ACCEPTED, response.statusCode)

        val userNew = userDetailsRepository.findByName("Name")
        assert(userNew!!.address == "New address")
    }

    @Test
    fun updateProfileWrongUsername() {
        val baseUrl = "http://localhost:$port"
        val userDetails = UserDetails("Name")
        userDetailsRepository.save(userDetails)
        val user = userDetailsRepository.findByName("Name")
        assert(user != null)

        val claims = Jwts.claims().setSubject("Name")
        claims["roles"] = arrayListOf("ROLE_CUSTOMER")

        val token = Jwts.builder()
                .setSubject("Name")
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //exp in 7 days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val request = user!!.apply { address = "New address"; name = "Wrong Name" }
        val response: ResponseEntity<String> = restTemplate.exchange(
                "$baseUrl/my/profile",
                HttpMethod.PUT,
                HttpEntity<UserDetails>(request, headers),
                String::class.java
        )

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}