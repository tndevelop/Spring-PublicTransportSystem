package it.polito.paymentservice

import com.google.gson.GsonBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.paymentservice.entities.Transaction
import it.polito.paymentservice.repositories.TransactionRepository
import it.polito.paymentservice.repositories.UserDetailsRepository
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitEntity
import org.springframework.web.reactive.function.client.awaitExchange
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Timestamp
import java.util.*

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionsControllerTests {

    @LocalServerPort
    protected var port: Int = 0

    @Value("\${key}")
    lateinit var key: String

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Autowired
    lateinit var userDetailsRepository: UserDetailsRepository

    val webClient = WebClient.create("http://localhost:$port")

    @BeforeEach
    fun destroyAll() {
        runBlocking {
            transactionRepository.deleteAll()
        }
    }

    @Test
    fun getTransactions() {
        val baseUrl = "http://localhost:$port"

        val date = Timestamp.valueOf("2022-10-02 00:00:00")
        val transaction = Transaction(0, 3.5, 2, 1, date)
        runBlocking {
            transactionRepository.save(transaction)

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

            val response: ResponseEntity<String> = webClient.get()
                    .uri("$baseUrl/transactions")
                    .headers { httpHeadersOnWebClientBeingBuilt ->
                        httpHeadersOnWebClientBeingBuilt.addAll(headers)
                    }
                    .awaitExchange()
                    .awaitEntity()

            Assertions.assertEquals(HttpStatus.OK, response.statusCode)
            val responseObject = JSONObject(response.body)
            val transactions = GsonBuilder().create().fromJson(responseObject.get("transactions").toString(), Array<Transaction>::class.java).toList()
            assert(transactions[0].totalCost == 3.5)
        }
    }

    @Test
    fun getTransactionsNotCustomer() {
        val baseUrl = "http://localhost:$port"

        val date = Timestamp.valueOf("2022-10-02 00:00:00")
        val transaction = Transaction(0, 3.5, 2, 1, date)
        runBlocking {
            transactionRepository.save(transaction)

            val claims = Jwts.claims().setSubject("Name")
            claims["roles"] = arrayListOf("ROLE_USER")

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

            val response: ResponseEntity<String> = webClient.get()
                    .uri("$baseUrl/transactions")
                    .headers { httpHeadersOnWebClientBeingBuilt ->
                        httpHeadersOnWebClientBeingBuilt.addAll(headers)
                    }
                    .awaitExchange()
                    .awaitEntity()

            Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        }
    }

    @Test
    fun getMultiTransactions() {
        val baseUrl = "http://localhost:$port"

        val date = Timestamp.valueOf("2022-10-02 00:00:00")
        val transaction1 = Transaction(0, 3.5, 2, 1, date)
        val transaction2 = Transaction(0, 5.0, 3, 1, date)
        runBlocking {
            transactionRepository.save(transaction1)
            transactionRepository.save(transaction2)

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

            val response: ResponseEntity<String> = webClient.get()
                    .uri("$baseUrl/transactions")
                    .headers { httpHeadersOnWebClientBeingBuilt ->
                        httpHeadersOnWebClientBeingBuilt.addAll(headers)
                    }
                    .awaitExchange()
                    .awaitEntity()

            Assertions.assertEquals(HttpStatus.OK, response.statusCode)
            val responseObject = JSONObject(response.body)
            val transactions = GsonBuilder().create().fromJson(responseObject.get("transactions").toString(), Array<Transaction>::class.java).toList()
            assert(transactions[0].totalCost == 3.5)
            assert(transactions.size == 2)
        }
    }
}