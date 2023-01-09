package it.polito.paymentservice.controllers

import it.polito.paymentservice.entities.UserDetails
import it.polito.paymentservice.repositories.UserDetailsRepository
import it.polito.paymentservice.services.AppUserDetailsService
import it.polito.paymentservice.services.TransactionService
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionsController(val transactionService: TransactionService, val appUserDetailsService: AppUserDetailsService) {
    data class Response(
        val message: String
    )

    @Autowired
    lateinit var userDetailsRepository: UserDetailsRepository

    @GetMapping("/test")
    fun helloWorld(): Response {

        return Response("Hello Webflux World")
    }
    //Accessible only to users with the customer role
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    suspend fun getTransactions() : ResponseEntity<Map<String, Any>> {
        var returnMap : Map<String, Any>
        var status = HttpStatus.OK

        val prince = SecurityContextHolder.getContext().authentication.principal
        val username = (prince as User).username
        val userId = userDetailsRepository.findByName(username).awaitSingleOrNull()!!.id

        try{
            val transactions = transactionService.findAllById(userId)

            if(transactions != null) {
                returnMap = mapOf("transactions" to transactions.toList())
            }else{
                returnMap = mapOf("message" to "Something went wrong. Please try again later." )
                status = HttpStatus.NOT_FOUND
            }
        } catch (e: Exception) {
            returnMap = mapOf("error" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)
    }

    @PostMapping("/my/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    suspend fun saveProfile(@RequestBody user: UserDetails) : ResponseEntity<Map<String, String>> {

        var returnMap: Map<String, String>
        var status: HttpStatus

        try {
            appUserDetailsService?.saveProfile(user)
            returnMap = mapOf("message" to "User saved.")
            status = HttpStatus.OK
        } catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }

        return ResponseEntity(returnMap, status)
    }
}