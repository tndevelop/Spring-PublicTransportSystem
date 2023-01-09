package it.polito.catalogueservice.ticketcatalogueservice.models

class TicketToAdd(
    var type: String,
    var price: Double,
    var minimumAge: Int = 0,
    var maximumAge: Int = 200,
    var zones: String = "A",
    var validityDuration: Long = 60,
    var validDaysOfWeek: String = ""
)