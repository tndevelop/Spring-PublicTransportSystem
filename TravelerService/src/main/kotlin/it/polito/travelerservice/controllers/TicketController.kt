package it.polito.travelerservice.controllers

import io.github.g0dkar.qrcode.QRCode
import io.jsonwebtoken.JwtException
import it.polito.travelerservice.dtos.ticketPurchasedToDTO
import it.polito.travelerservice.entities.TicketPurchased
import it.polito.travelerservice.models.TicketQRCode
import it.polito.travelerservice.models.TicketsToBuy
import it.polito.travelerservice.repositories.TicketPurchasedRepository
import it.polito.travelerservice.services.AppUserDetailsService
import it.polito.travelerservice.services.TicketPurchasedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayOutputStream

@Controller
class TicketController {

    @Autowired
    val ticketPurchasedRepository: TicketPurchasedRepository? = null

    @Autowired
    val ticketPurchasedService: TicketPurchasedService? = null

    @Autowired
    private var appUserDetailsService: AppUserDetailsService? = null

    //    accessible only by authenticated users with a CUSTOMER role
    @GetMapping("/my/tickets")
    @PreAuthorize("hasRole('CUSTOMER')")
    fun getMyTickets() : ResponseEntity<Map<String, Any>> {

        var returnMap: Map<String, Any>
        var status: HttpStatus

        val username = (SecurityContextHolder.getContext().authentication.principal as User).username

        try {
            val user = appUserDetailsService?.getProfile(username)
            val tickets = ticketPurchasedService?.getTicketsPurchasedByUserId(user?.id!!.toLong())?.map{ u -> u.ticketPurchasedToDTO()}
            if(tickets!= null) { //&& tickets.size != 0) {
                returnMap = mapOf("message" to tickets)
                status = HttpStatus.OK
            }
            else {
                returnMap = mapOf("message" to "Something went wrong. Please try again later.")
                status = HttpStatus.BAD_REQUEST
            }
        } catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }

        return ResponseEntity(returnMap, status)
    }

//    creating and persisting the requested number of tickets for the current principal
    @PostMapping("/my/tickets")
    @PreAuthorize("hasRole('CUSTOMER')")
    fun postMyTickets(@RequestBody ticketsToBuy: TicketsToBuy) : ResponseEntity<Map<String, Any>> {

        var returnMap: Map<String, Any>
        var status: HttpStatus

        if (!ticketsToBuy.cmd.equals("buy_tickets")) {
            returnMap = mapOf("error_message" to "Invalid cmd.")
            status = HttpStatus.BAD_REQUEST
            return ResponseEntity(returnMap, status)
        }

//      PS: for now no restrictions on the number of tickets
        if(ticketsToBuy.quantity <= 0) {
            returnMap = mapOf("error_message" to "Invalid quantity.")
            status = HttpStatus.BAD_REQUEST
            return ResponseEntity(returnMap, status)
        }

        if (ticketsToBuy.zones.isEmpty()) {
            returnMap = mapOf("error_message" to "Invalid zone id.")
            status = HttpStatus.BAD_REQUEST
            return ResponseEntity(returnMap, status)
        }

        try {
            val tickets = ticketPurchasedService?.purchaseTickets(ticketsToBuy)?.map{ t -> t.ticketPurchasedToDTO()}
            if(tickets != null) {
                returnMap = mapOf("message" to tickets)
                status = HttpStatus.OK
            }
            else {
                returnMap = mapOf("message" to "Something went wrong. Please try again later.")
                status = HttpStatus.BAD_REQUEST
            }
        } catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }

        return ResponseEntity(returnMap, status)
    }


    @GetMapping("/my/tickets/qrcode", produces = [MediaType.IMAGE_JPEG_VALUE])
    @PreAuthorize("hasRole('CUSTOMER')")
    fun generateTicketQRCode(@RequestBody ticketQRCode: TicketQRCode) : ResponseEntity<Any> {
        var returnMap: Map<String, Any> = emptyMap()
        var status: HttpStatus = HttpStatus.BAD_REQUEST

        if (ticketQRCode.ticketId < 0L) {
            returnMap = mapOf("error_message" to "Invalid ticket id.")
            status = HttpStatus.BAD_REQUEST
            return ResponseEntity(returnMap, status)
        }

        try {
            val ticket: TicketPurchased = ticketPurchasedRepository!!.findById(ticketQRCode.ticketId).orElse(null)
            if((ticket != null) && !ticket.jws.isNullOrEmpty()) {

                val imageOut = ByteArrayOutputStream()

                QRCode(ticket.jws).render().writeImage(imageOut)

                val imageBytes = imageOut.toByteArray()
                val resource = ByteArrayResource(imageBytes, MediaType.IMAGE_PNG_VALUE)

                return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"qrcode.png\"")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource)
            }
            else {
                returnMap = mapOf("message" to "Something went wrong. Please try again later.")
                status = HttpStatus.BAD_REQUEST
            }
        }
        catch(e: JwtException){
            returnMap = mapOf("error_message" to "jwt not recognized")
            status = HttpStatus.BAD_REQUEST
        }
        catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }

        return ResponseEntity(returnMap, status)
    }
}