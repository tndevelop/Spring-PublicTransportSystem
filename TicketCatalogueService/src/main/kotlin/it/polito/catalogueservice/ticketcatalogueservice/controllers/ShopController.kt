package it.polito.catalogueservice.ticketcatalogueservice.controllers

import com.fasterxml.jackson.databind.util.JSONPObject
import com.google.gson.JsonParser
import it.polito.catalogueservice.ticketcatalogueservice.entities.OrderCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.enum.OrderStatus
import it.polito.catalogueservice.ticketcatalogueservice.models.PaymentDetails
import it.polito.catalogueservice.ticketcatalogueservice.models.ShoppingTickets
import it.polito.catalogueservice.ticketcatalogueservice.models.TicketToAdd
import it.polito.catalogueservice.ticketcatalogueservice.models.UserDetails
import it.polito.catalogueservice.ticketcatalogueservice.services.AppUserDetailsService
import it.polito.catalogueservice.ticketcatalogueservice.services.OrderCatalogueService
import it.polito.catalogueservice.ticketcatalogueservice.services.ShopService
import it.polito.catalogueservice.ticketcatalogueservice.services.TicketCatalogueService
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
//import org.springframework.security.core.context.SecurityContextHolder
//import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ShopController(
        @Value("\${kafka.topics.payment}") val topic: String,
        @Autowired
        private val kafkaTemplate: KafkaTemplate<String, Any>
    ) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired
    var shopService: ShopService? = null

    @Autowired
    lateinit var orderCatalogueService: OrderCatalogueService //? = null

    @Autowired
    lateinit var ticketCatalogueService: TicketCatalogueService

    @Autowired
    lateinit var appUserDetailsService: AppUserDetailsService

//  Only authenticated users can perform this request.
//    IF tickets have age restrictions, ask TravelerService for user information
//    IF he is eligible, continue and save the order in the database with status = PENDING
//      return the pending request to the client and send credit card info to paymentservice
//    when it receives a feedback, update status = PURCHASED
//    + add purchased product to list of acquired tickeets in travelerservice
    @PostMapping("/shop")  //{ticketId} this was deemed not needed since the ticketId is in the body
    @PreAuthorize("hasRole('CUSTOMER')")
    suspend fun shopByTicketId(@RequestBody shoppingTickets: ShoppingTickets) : ResponseEntity<Any> {
        val prince = SecurityContextHolder.getContext().authentication.principal
        val username = (prince as User).username

        //    check if everything is valid
        if(shoppingTickets.numberOfTickets < 1)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Number of tickets is invalid, please try again!")

        if(shoppingTickets.ticketId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ticket id is invalid, please try again!")

        // Check credit card information
       val cardInfoResponse = shoppingTickets.paymentInformation.validInfo()
        if(!cardInfoResponse.equals("valid"))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cardInfoResponse)

        // get the users profile
        val response: String? = shopService?.getProfile(username)
        val responseObject = JsonParser().parse(response).asJsonObject
        val userId = appUserDetailsService.findByName(username)!!.awaitSingleOrNull()!!.id  ///responseObject.get("id").asLong
        // check age restrictions from the profile
        val age = responseObject.get("age").asLong
        val ticketCatalogue = ticketCatalogueService.getTicketById(shoppingTickets.ticketId)
        if (ticketCatalogue == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ticket type does not exists")
        if (age < ticketCatalogue.minimumAge || age > ticketCatalogue.maximumAge)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Age requirements not met")

        // totalCost calculation
        val totalCost = shoppingTickets.numberOfTickets * ticketCatalogue.price

        // save order in db status=PENDING
        val orderCatalogue = OrderCatalogue(
            OrderStatus.PENDING.toString(),
            totalCost,
            shoppingTickets.numberOfTickets,
            userId,
            ticketCatalogue.id
        )
        val pendingOrder = orderCatalogueService.save(orderCatalogue)

        //    send creditcard info to paymentservice
        //    in background
        try {
            val product = PaymentDetails(shoppingTickets.paymentInformation, totalCost.toFloat(), userId,
                    shoppingTickets.numberOfTickets, shoppingTickets.ticketId, pendingOrder.id)
            log.info("Receiving product request")
            log.info("Sending message to Kafka {}", shoppingTickets)
            val message: Message<PaymentDetails> = MessageBuilder
                .withPayload(product)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader("X-Custom-Header", "Custom header here")
                .build()
            kafkaTemplate.send(message)
            log.info("Message sent with success")
            // Return order id
            return ResponseEntity(mapOf("message" to "Order received", "OrderID" to pendingOrder.id ), HttpStatus.OK) //ResponseEntity.ok().build()
        } catch (e: Exception) {
            log.error("Exception: {}",e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error to send message")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }

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