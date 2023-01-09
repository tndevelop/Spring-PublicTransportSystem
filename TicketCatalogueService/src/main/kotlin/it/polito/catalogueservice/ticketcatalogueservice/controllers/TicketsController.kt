package it.polito.catalogueservice.ticketcatalogueservice.controllers

import it.polito.catalogueservice.ticketcatalogueservice.entities.TicketCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.repositories.TicketCatalogueRepository
import it.polito.catalogueservice.ticketcatalogueservice.services.TicketCatalogueService
import kotlinx.coroutines.flow.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.exchangeToFlow
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.Principal

@RestController
class TicketsController(val ticketCatalogueService: TicketCatalogueService) {
    data class Response(
        val message: String
    )

    @GetMapping("/test")
    fun helloWorld(): Response {

        return Response("Hello Webflux World")
    }

    /*returns a JSON representation of all available tickets. Those tickets
    are represented as a JSON object consisting of price, ticketId, type ( ordinal or type
    of pass).*/
    // Accessible to all users
    @GetMapping("/tickets")
    suspend fun getTickets() : ResponseEntity<Map<String, Any>> {
        var returnMap : Map<String, Any>
        var status = HttpStatus.OK
        try{
            val tickets = ticketCatalogueService.getAll()

            if(tickets != null) {
                returnMap = mapOf("tickets" to tickets.toList())
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

}