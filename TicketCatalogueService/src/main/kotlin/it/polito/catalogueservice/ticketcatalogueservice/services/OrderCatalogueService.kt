package it.polito.catalogueservice.ticketcatalogueservice.services

import io.r2dbc.spi.ConnectionFactory
import it.polito.catalogueservice.ticketcatalogueservice.entities.OrderCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.repositories.OrderCatalogueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono

@Service
class OrderCatalogueService(val orderCatalogueRepository : OrderCatalogueRepository,
                            val connectionFactory: ConnectionFactory) {

    fun getAllOrders(): Flow<OrderCatalogue>{

        val orders = orderCatalogueRepository.findAll()

        return orders

    }

    fun getOrdersByUserId(userId: Long): Flow<OrderCatalogue> {
        return orderCatalogueRepository.findByUserId(userId)
    }

    suspend fun getOrderByOrderId(orderId: Long) : OrderCatalogue? {
        return orderCatalogueRepository.findById(orderId)
    }

    suspend fun save(orderCatalogue: OrderCatalogue): OrderCatalogue {
        return orderCatalogueRepository.save(orderCatalogue)
    }

    suspend fun updateOrder(orderId: Int, status: String) : Flow<OrderCatalogue> {

        orderCatalogueRepository.updateOrderStatus(orderId.toLong(), status).toList()

        return emptyFlow()
    }

}