package it.polito.catalogueservice.ticketcatalogueservice.repositories

import it.polito.catalogueservice.ticketcatalogueservice.entities.OrderCatalogue
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface OrderCatalogueRepository : CoroutineCrudRepository<OrderCatalogue, Long> {

    fun findByUserId(userId: Long): Flow<OrderCatalogue>

    @Query("UPDATE order_catalogue SET status = :status WHERE id = :orderId")
    fun updateOrderStatus(@Param("orderId")orderId: Long, @Param("status") status: String) : Flow<OrderCatalogue>

}