package it.polito.catalogueservice.ticketcatalogueservice.kafka.consumer
import it.polito.catalogueservice.ticketcatalogueservice.enum.OrderStatus
import it.polito.catalogueservice.ticketcatalogueservice.models.Test
import it.polito.catalogueservice.ticketcatalogueservice.models.TransactionOutcome
import it.polito.catalogueservice.ticketcatalogueservice.repositories.OrderCatalogueRepository
import it.polito.catalogueservice.ticketcatalogueservice.services.OrderCatalogueService

import kotlinx.coroutines.flow.toList
import it.polito.catalogueservice.ticketcatalogueservice.services.ShopService
import it.polito.catalogueservice.ticketcatalogueservice.services.TicketCatalogueService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component

@Component
class Consumer {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var orderCatalogueService: OrderCatalogueService

    @Autowired
    lateinit var ticketCatalogueService: TicketCatalogueService

    @Autowired
    lateinit var shopService: ShopService

    @KafkaListener(topics = ["\${kafka.topics.result}"], groupId = "ppr")
    fun listenGroupFoo(consumerRecord: ConsumerRecord<Any, Any>, ack: Acknowledgment) {
        logger.info("Message received")
        logger.info("Transaction outcome: {}", (consumerRecord.value() as TransactionOutcome))


        val outcome = (consumerRecord.value() as TransactionOutcome)
        println("outcome: ${outcome.userId}")
        if (outcome.status == "Success") {
            runBlocking {
                launch {
                    // Update the status of the order
                    logger.info("Orderid: ${outcome.orderId}")
                    orderCatalogueService.updateOrder(outcome.orderId.toInt(), OrderStatus.DONE.toString())
                    logger.info("Order status updated")

                    // add tickets to TravellerService - use the post my/tickets end point
                    // Get order details
                    val order = orderCatalogueService.getOrderByOrderId(outcome.orderId)
                    if (order == null) {
                        logger.info("Something went wrong accessing the order")
                    } else {
                        // get  ticket details
                        val ticketCatalogue = ticketCatalogueService.getTicketById(order.ticketId)
                        if (ticketCatalogue == null) {
                            logger.info("Something went wrong accessing the ticket")
                        } else {
                            //val prince = SecurityContextHolder.getContext().authentication.principal
                            //val username = (prince as User).username
                            // Add tickets to users profile
                            shopService.addTickets(outcome.userId, order.numberOfTickets, ticketCatalogue.zones,
                                    ticketCatalogue.validityDuration, ticketCatalogue.type)
                        }
                    }
                }
            }
        } else {
            runBlocking {
                launch {
                    // Cancel order that wasn't successful
                    orderCatalogueService.updateOrder(outcome.orderId.toInt(), OrderStatus.CANCELED.toString())
                }
            }
        }

//        fireback a message to ticketcatalogueservice
        ack.acknowledge()
    }
}