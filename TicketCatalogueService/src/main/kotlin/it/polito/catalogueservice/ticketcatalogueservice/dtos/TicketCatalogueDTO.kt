package it.polito.catalogueservice.ticketcatalogueservice.dtos

import it.polito.catalogueservice.ticketcatalogueservice.entities.TicketCatalogue

data class TicketCatalogueDTO(
    val id: Long,
    val price: Double,
    val type: String,
    val minimumAge: Int,
    val maximumAge: Int,
    val zones: String,
    val validityDuration: Long
)

fun TicketCatalogue.ticketCatalogueToDTO(): TicketCatalogueDTO {
    return TicketCatalogueDTO(id, price, type, minimumAge, maximumAge, zones, validityDuration)
}