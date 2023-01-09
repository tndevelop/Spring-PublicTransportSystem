package it.polito.travelerservice.controllers

import com.fasterxml.jackson.databind.util.JSONPObject
import com.google.gson.JsonObject
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import it.polito.travelerservice.models.JWSBody
import it.polito.travelerservice.models.TicketToValidate
import it.polito.travelerservice.models.TurnstileToAuthenticate
import it.polito.travelerservice.models.TransitInfo
import it.polito.travelerservice.services.TicketPurchasedService
import it.polito.travelerservice.services.TurnstileService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import java.rmi.AlreadyBoundException

@Controller
class TurnstileController {


    @Autowired
    val ticketPurchasedService: TicketPurchasedService? = null

    @Autowired
    val turnstileService: TurnstileService? = null

    @Value("\${keyTickets}")
    lateinit var key: String

    @PutMapping("/emb/tickets/validate")
    @PreAuthorize("hasRole('TURNSTILE')")
    fun validateTicket(@RequestBody jwsBody: JWSBody) : ResponseEntity<Map<String, Any>> {
        var returnMap: Map<String, Any>
        var status: HttpStatus



        try {
            val used = ticketPurchasedService?.validateTicket(jwsBody.jws)
            if(used != null && used == true) {
                returnMap = mapOf("correctly validated" to true)
                status = HttpStatus.OK
            }
            else {
                returnMap = mapOf("message" to "Something went wrong. Please try again later.")
                status = HttpStatus.BAD_REQUEST
            }
        }catch (e: AlreadyBoundException){
            returnMap = mapOf("error_message" to "ticket has been validated already")
            status = HttpStatus.BAD_REQUEST
        }catch(e: JwtException){
            returnMap = mapOf("error_message" to "jwt not recognized")
            status = HttpStatus.BAD_REQUEST
        }
        catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }

        return ResponseEntity(returnMap, status)
    }

    @PostMapping("/emb/authenticate")
    @PreAuthorize("hasRole('TURNSTILE')")
    fun authenticateTurnstile(@RequestBody turnstileToAuthenticate: TurnstileToAuthenticate) : ResponseEntity<Map<String, Any>> {
        var returnMap: Map<String, Any>
        var status: HttpStatus


        if (turnstileToAuthenticate.count < 0) {
            returnMap = mapOf("error_message" to "Invalid transit count.")
            status = HttpStatus.BAD_REQUEST
            return ResponseEntity(returnMap, status)
        }

        try {
            val id = turnstileService?.authenticate(turnstileToAuthenticate)

            returnMap = mapOf("turnstileId" to id!!, "secret" to key)
            status = HttpStatus.OK


        }
        catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }

        return ResponseEntity(returnMap, status)
    }

    @PutMapping("/emb/count")
    @PreAuthorize("hasRole('TURNSTILE')")
    fun updateTurnstileCount(@RequestBody transitInfo: TransitInfo) : ResponseEntity<Map<String, Any>> {
        var returnMap: Map<String, Any>
        var status: HttpStatus


        if (transitInfo.passengerId < 0L || transitInfo.turnstileId < 0L) {
            returnMap = mapOf("error_message" to "Invalid passenger or turnstile")
            status = HttpStatus.BAD_REQUEST
            return ResponseEntity(returnMap, status)
        }

        try {
            val count = turnstileService?.addTransit(transitInfo)

            returnMap = mapOf("new count" to count!!)
            status = HttpStatus.OK


        }
        catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }

        return ResponseEntity(returnMap, status)
    }
}