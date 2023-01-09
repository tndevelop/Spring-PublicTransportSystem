package it.polito.travelerservice.controllers

import it.polito.travelerservice.dtos.ticketPurchasedToDTO
import it.polito.travelerservice.entities.Transit
import it.polito.travelerservice.services.AppUserDetailsService
import it.polito.travelerservice.services.TicketPurchasedService
import it.polito.travelerservice.services.TransitService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
class AdminController(val transitService: TransitService) {

    @Autowired
    val ticketPurchasedService: TicketPurchasedService? = null

    @Autowired
    private var appUserDetailsService: AppUserDetailsService? = null


    @GetMapping("/admin/travelers")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAdminTravelers() : ResponseEntity<Map<String, Any>> {
        var returnMap : Map<String, Any>
        var status = HttpStatus.OK
        try{
            val users = appUserDetailsService?.getAllUsers()?.map { u -> u.name }

            if(users != null) {
                returnMap = mapOf("users" to users)
            }else{
                returnMap = mapOf("message" to "no user found" )
                status = HttpStatus.NOT_FOUND
            }
        } catch (e: Exception) {
            returnMap = mapOf("error" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)
    }

    @GetMapping("/admin/traveler/{userID}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAdminTravelerProfileByUserID(@PathVariable userID: Int) : ResponseEntity<Map<String, Any>> {

        var returnMap : Map<String, Any>
        var status = HttpStatus.OK
        try{
            val user = appUserDetailsService?.getProfileById(userID.toLong())

            if(user != null) {
                returnMap = mapOf("id" to user.id.toString(), "name" to user.name, "address" to user.address, "date of birth" to user.dateOfBirth.toString(), "telephone" to user.telephoneNumber)
            }else{
                returnMap = mapOf("message" to "user not found" )
                status = HttpStatus.NOT_FOUND
            }
        } catch (e: Exception) {
            returnMap = mapOf("error" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)
    }


    @GetMapping("/admin/traveler/{userID}/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAdminTravelerTicketsByUserID(@PathVariable userID: Long) : ResponseEntity<Map<String, List<Any>>> {

        var returnMap : Map<String, List<Any>>
        var status = HttpStatus.OK
        try{
            val tickets = ticketPurchasedService?.getTicketsPurchasedByUserId(userID)?.map{ t -> t.ticketPurchasedToDTO()}

            if(tickets != null && tickets.size > 0) {
                returnMap = mapOf("tickets" to tickets)
            }else{
                returnMap = mapOf("message" to listOf("no tickets found") )
                status = HttpStatus.NOT_FOUND
            }
        } catch (e: Exception) {
            returnMap = mapOf("error" to listOf(e.message!!) )
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)

    }

    //Accessible only to users with the admin role
    @GetMapping("/admin/transits")
    @PreAuthorize("hasRole('ADMIN')")
    fun getTransits(@RequestParam(required = false) dateRange : String?) : ResponseEntity<Map<String, Any>> {
        var returnMap : Map<String, Any>
        var status = HttpStatus.OK
        try{
            val transits: List<Transit>

            if (dateRange != null) {
                val dates = dateRange.split("-")
                val dateFrom = LocalDate.parse(dates[0], DateTimeFormatter.ofPattern("d/MM/yyyy"))
                val dateTo = LocalDate.parse(dates[1], DateTimeFormatter.ofPattern("d/MM/yyyy"))
                println("$dateFrom to $dateTo" )

                transits = transitService.getAllInRange(dateFrom, dateTo)
            } else {
                transits = transitService.getAll()
            }

            returnMap = mapOf("transits" to transits)

        } catch (e: Exception) {
            returnMap = mapOf("error" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)
    }

    //Accessible only to users with the admin role
    @GetMapping("/admin/transits/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getTransitsByUser(@RequestParam(required = false) dateRange : String?, @PathVariable userId: Int) : ResponseEntity<Map<String, Any>> {
        var returnMap : Map<String, Any>
        var status = HttpStatus.OK
        try{
            val transits: List<Transit>

            if (dateRange != null) {
                val dates = dateRange.split("-")
                val dateFrom = LocalDate.parse(dates[0], DateTimeFormatter.ofPattern("d/MM/yyyy"))
                val dateTo = LocalDate.parse(dates[1], DateTimeFormatter.ofPattern("d/MM/yyyy"))
                println("$dateFrom to $dateTo" )

                transits = transitService.findAllByUserIdInRange(userId.toLong(), dateFrom, dateTo)
            } else {
                transits = transitService.findAllById(userId.toLong())
            }

            returnMap = mapOf("transits" to transits)

        } catch (e: Exception) {
            returnMap = mapOf("error" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)
    }
}