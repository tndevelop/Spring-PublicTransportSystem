package it.polito.catalogueservice.ticketcatalogueservice.controllers

import it.polito.catalogueservice.ticketcatalogueservice.repositories.UserDetailsRepository
import it.polito.catalogueservice.ticketcatalogueservice.services.OrderCatalogueService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
class OrdersController (val orderCatalogueService: OrderCatalogueService) {

    @Autowired
    lateinit var userDetailsRepository: UserDetailsRepository

    @GetMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    suspend fun getOrders(@PathVariable order_id: Optional<Long>): ResponseEntity<Map<String, Any>> {

        var returnMap: Map<String, Any>
        var status = HttpStatus.OK

        val prince = SecurityContextHolder.getContext().authentication.principal
        val username = (prince as User).username
        val userId = userDetailsRepository.findByName(username).awaitSingleOrNull()!!.id

            try {
                val orders = orderCatalogueService.getOrdersByUserId(userId).toList()

                if (orders != null && orders.isNotEmpty()) {
                    returnMap = mapOf("orders" to orders.toList())
                }else if(orders.isEmpty()){
                    returnMap = mapOf("message" to "No orders found." )
                } else {
                    returnMap = mapOf("message" to "Something went wrong. Please try again later.")
                    status = HttpStatus.NOT_FOUND
                }
            } catch (e: Exception) {
                returnMap = mapOf("error" to e.message!!)
                status = HttpStatus.BAD_REQUEST
            }
            return ResponseEntity(returnMap, status)

    }


    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    suspend fun getOrders(@PathVariable orderId : Long): ResponseEntity<Map<String, Any>> {

        var returnMap : Map<String, Any>
        var status = HttpStatus.OK

        try {
            val order = orderCatalogueService.getOrderByOrderId(orderId as Long)
            if (order != null) {
                returnMap = mapOf("order" to order)
            } else {
                returnMap = mapOf("message" to "Something went wrong. Please try again later.")
                status = HttpStatus.NOT_FOUND
            }
        } catch (e: Exception) {
            returnMap = mapOf("error" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)

    }


}






