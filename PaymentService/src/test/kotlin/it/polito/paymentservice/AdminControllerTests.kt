package it.polito.paymentservice

import com.google.gson.GsonBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.paymentservice.entities.Transaction
import it.polito.paymentservice.repositories.TransactionRepository
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
class AdminControllerTests {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    val webClient = WebClient.create("http://localhost:$port")

    @Value("\${key}")
    lateinit var key: String

    @BeforeEach
    fun destroyAll() {
        runBlocking {
            transactionRepository.deleteAll()
        }
    }

    @Test
    fun getAllTransactions() {
        val baseUrl = "http://localhost:$port"
        println(baseUrl)
        runBlocking {

            val date = Timestamp.valueOf("2022-10-02 00:00:00")
            val transaction = Transaction(0, 3.5, 2, 1, date)

            transactionRepository.save(transaction)

            val claims = Jwts.claims().setSubject("Name")
            claims["roles"] = arrayListOf("ROLE_ADMIN")

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

            val webClient2 = WebClient.create("http://localhost:$port")

            val response: ResponseEntity<String> = webClient2.get()
                    .uri("$baseUrl/admin/transactions")
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
    fun getAllTransactionsByUser() {
        val baseUrl = "http://localhost:$port"
        println(baseUrl)
        runBlocking {

            val date = Timestamp.valueOf("2022-10-02 00:00:00")
            val userid = 0
            var transaction = Transaction(userid.toLong(), 3.5, 2, 1, date)
            transactionRepository.save(transaction)
            transaction = Transaction(2, 9.8, 2, 1, date)
            transactionRepository.save(transaction)

            val claims = Jwts.claims().setSubject("Name")
            claims["roles"] = arrayListOf("ROLE_ADMIN")

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

            val webClient2 = WebClient.create("http://localhost:$port")

            val response: ResponseEntity<String> = webClient2.get()
                    .uri("$baseUrl/admin/transactions/$userid")
                    .headers { httpHeadersOnWebClientBeingBuilt ->
                        httpHeadersOnWebClientBeingBuilt.addAll(headers)
                    }
                    .awaitExchange()
                    .awaitEntity()

            Assertions.assertEquals(HttpStatus.OK, response.statusCode)
            val responseObject = JSONObject(response.body)
            val transactions = GsonBuilder().create().fromJson(responseObject.get("transactions").toString(), Array<Transaction>::class.java).toList()
            assert(transactions[0].totalCost == 3.5)
            assert(transactions.size == 1)
        }
    }

    @Test
    fun getTransactionsInRange() {
        val baseUrl = "http://localhost:$port"
        println(baseUrl)
        runBlocking {

            val date = Timestamp.valueOf("2022-07-02 00:00:00")
            val transaction = Transaction(0, 3.5, 2, 1, date)
            transactionRepository.save(transaction)

            val date2 = Timestamp.valueOf("2022-07-10 00:00:00")
            val transaction2 = Transaction(1, 6.0, 2, 1, date2)
            transactionRepository.save(transaction2)

            val claims = Jwts.claims().setSubject("Name")
            claims["roles"] = arrayListOf("ROLE_ADMIN")

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

            val webClient2 = WebClient.create("http://localhost:$port")

            val response: ResponseEntity<String> = webClient2.get()
                    .uri{ uriBuilder -> uriBuilder.path("/admin/transactions")
                            .queryParam("dateRange", "30/06/2022-05/07/2022")
                            .build() }
                    .headers { httpHeadersOnWebClientBeingBuilt ->
                        httpHeadersOnWebClientBeingBuilt.addAll(headers)
                    }
                    .awaitExchange()
                    .awaitEntity()

            Assertions.assertEquals(HttpStatus.OK, response.statusCode)
            val responseObject = JSONObject(response.body)
            val transactions = GsonBuilder().create().fromJson(responseObject.get("transactions").toString(), Array<Transaction>::class.java).toList()
            assert(transactions[0].totalCost == 3.5)
            assert(transactions.size == 1)
        }
    }

    @Test
    fun getAllTransactionsByUserInRange() {
        val baseUrl = "http://localhost:$port"
        println(baseUrl)
        runBlocking {

            val date = Timestamp.valueOf("2022-07-02 00:00:00")
            val transaction = Transaction(0, 3.5, 2, 1, date)
            transactionRepository.save(transaction)

            val date2 = Timestamp.valueOf("2022-07-10 00:00:00")
            val transaction2 = Transaction(0, 6.0, 2, 1, date2)
            transactionRepository.save(transaction2)

            val claims = Jwts.claims().setSubject("Name")
            claims["roles"] = arrayListOf("ROLE_ADMIN")

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

            val webClient2 = WebClient.create("http://localhost:$port")

            val response: ResponseEntity<String> = webClient2.get()
                    .uri{ uriBuilder -> uriBuilder.path("/admin/transactions")
                            .queryParam("dateRange", "30/06/2022-05/07/2022")
                            .build() }
                    .headers { httpHeadersOnWebClientBeingBuilt ->
                        httpHeadersOnWebClientBeingBuilt.addAll(headers)
                    }
                    .awaitExchange()
                    .awaitEntity()

            Assertions.assertEquals(HttpStatus.OK, response.statusCode)
            val responseObject = JSONObject(response.body)
            val transactions = GsonBuilder().create().fromJson(responseObject.get("transactions").toString(), Array<Transaction>::class.java).toList()
            assert(transactions[0].totalCost == 3.5)
            assert(transactions.size == 1)
        }
    }

    @Test
    fun getTransactionsNotAdmin() {
        val baseUrl = "http://localhost:$port"

        val date = Timestamp.valueOf("2022-07-02 00:00:00")
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
                    .uri("$baseUrl/admin/transactions")
                    .headers { httpHeadersOnWebClientBeingBuilt ->
                        httpHeadersOnWebClientBeingBuilt.addAll(headers)
                    }
                    .awaitExchange()
                    .awaitEntity()

            Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        }
    }

    @Test
    fun getTransactionsBadToken() {
        val baseUrl = "http://localhost:$port"

        val date = Timestamp.valueOf("2022-07-02 00:00:00")
        val transaction = Transaction(0, 3.5, 2, 1, date)
        runBlocking {
            transactionRepository.save(transaction)

            val claims = Jwts.claims().setSubject("Name")
            claims["roles"] = arrayListOf("ROLE_ADMIN")

            val token = Jwts.builder()
                    .setSubject("Name")
                    .setClaims(claims)
                    .setIssuedAt(Date(System.currentTimeMillis()))
                    .setExpiration(Date(System.currentTimeMillis() - 60*60*1000)) // in the past
                    .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                    .compact()

            val headers = HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            headers.set("Authorization", "Bearer $token")

            val response: ResponseEntity<String> = webClient.get()
                    .uri("$baseUrl/admin/transactions")
                    .headers { httpHeadersOnWebClientBeingBuilt ->
                        httpHeadersOnWebClientBeingBuilt.addAll(headers)
                    }
                    .awaitExchange()
                    .awaitEntity()

            Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        }
    }

}