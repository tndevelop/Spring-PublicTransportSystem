package it.polito.catalogueservice.ticketcatalogueservice

import it.polito.catalogueservice.ticketcatalogueservice.entities.OrderCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.enum.OrderStatus
import it.polito.catalogueservice.ticketcatalogueservice.repositories.OrderCatalogueRepository
import it.polito.catalogueservice.ticketcatalogueservice.services.OrderCatalogueService
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderRepositoryAndServiceTests {


    @LocalServerPort
    protected var port: Int = 0


    @Autowired
    lateinit var orderCatalogueRepository : OrderCatalogueRepository

    @Autowired
    lateinit var orderCatalogueService : OrderCatalogueService

    @BeforeEach
    fun destroyAll() {
        runBlocking{
            orderCatalogueRepository.deleteAll()
        }
    }

    @Test
    fun testUpdateOrderCatalogue(){
        runBlocking{
            val order = orderCatalogueRepository.save(OrderCatalogue(OrderStatus.PENDING.toString(), 2.0, 1, 0, 0))

            orderCatalogueRepository.updateOrderStatus(order.id, OrderStatus.DONE.toString()).toList()

            val updatedOrder = orderCatalogueRepository.findByUserId(order.userId).toList()[0]

            assert(updatedOrder.status == OrderStatus.DONE.toString())


        }
    }

    @Test
    fun testUpdateOrderCatalogueService(){
        runBlocking{
            val order = orderCatalogueRepository.save(OrderCatalogue(OrderStatus.PENDING.toString(), 2.0, 1, 0, 0))


            orderCatalogueService.updateOrder(order.id.toInt(), OrderStatus.DONE.toString())

            val updatedOrder = orderCatalogueRepository.findByUserId(order.userId).toList()[0]

            assert(updatedOrder.status == OrderStatus.DONE.toString())

        }
    }
}