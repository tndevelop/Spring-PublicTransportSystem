package it.polito.catalogueservice.ticketcatalogueservice.controllers

import it.polito.catalogueservice.ticketcatalogueservice.models.TicketToAdd
import it.polito.catalogueservice.ticketcatalogueservice.services.OrderCatalogueService
import it.polito.catalogueservice.ticketcatalogueservice.services.TicketCatalogueService
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
class AdminCatalogueController {

    @Autowired
    val ticketCatalogueService: TicketCatalogueService? = null

    @Autowired
    lateinit var orderCatalogueService: OrderCatalogueService//? = null

    @PostMapping("/admin/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun postAdminTickets(@RequestBody ticketToAdd: TicketToAdd) : ResponseEntity<Map<String, Any>> {
        var returnMap : Map<String, Any>
        var status = HttpStatus.OK
        try{
            val insertedTicket = ticketCatalogueService?.addTicket(ticketToAdd)

            if(insertedTicket != null) {
                returnMap = mapOf("ticket" to insertedTicket)
            }else{
                returnMap = mapOf("message" to "Something went wrong. Please try again later." )
                status = HttpStatus.BAD_REQUEST
            }
        } catch (e: Exception) {
            returnMap = mapOf("error" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)
    }

    // Used to update existing tickets based on the unique "type"
    @PutMapping("/admin/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun putAdminTickets(@RequestBody ticketToAdd: TicketToAdd) : ResponseEntity<Map<String, Any>> {
        var returnMap : Map<String, Any>
        var status = HttpStatus.OK
        try{
            val updateTicket = ticketCatalogueService?.updateTicket(ticketToAdd)

            if(updateTicket != null) {
                returnMap = mapOf("ticket" to updateTicket)
            }else{
                returnMap = mapOf("message" to "Something went wrong. Please try again later." )
                status = HttpStatus.BAD_REQUEST
            }
        } catch (e: Exception) {
            returnMap = mapOf("error" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)
    }


    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun getAdminOrders() : ResponseEntity<Map<String, Any>> {
        var returnMap : Map<String, Any>
        var status = HttpStatus.OK
        try{
            val orders = orderCatalogueService?.getAllOrders()

            if(orders != null) {
                val orderslist = orders.toList()
                returnMap = mapOf("orders" to orderslist)
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

    @GetMapping("/admin/orders/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun getAdminOrderByUserId(@PathVariable userId: Int) : ResponseEntity<Map<String, Any>> {
        var returnMap : Map<String, Any>
        var status = HttpStatus.OK

        try{
            val orders = orderCatalogueService.getOrdersByUserId(userId.toLong())
            val ordersList = orders.toList()
            if(ordersList != null && !ordersList.isEmpty()) {
                returnMap = mapOf("orders" to ordersList)
            } else if (ordersList?.isEmpty() == true) {
                returnMap = mapOf("message" to "No orders found." )
            } else {
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