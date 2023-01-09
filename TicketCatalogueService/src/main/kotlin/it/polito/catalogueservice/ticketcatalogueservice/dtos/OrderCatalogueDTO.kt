package it.polito.catalogueservice.ticketcatalogueservice.dtos

import it.polito.catalogueservice.ticketcatalogueservice.entities.OrderCatalogue

data class OrderCatalogueDTO(
    val id: Long,
    val status: String,
    val totalCost: Double,
    val numberOfTickets: Int,
    val userId: Long,
    val ticketId: Long
)

fun OrderCatalogue.orderCatalogueToDTO(): OrderCatalogueDTO {
    return OrderCatalogueDTO(id, status, totalCost, numberOfTickets, userId, ticketId)
}