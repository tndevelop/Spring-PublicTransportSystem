package it.polito.catalogueservice.ticketcatalogueservice.entities

import com.fasterxml.jackson.annotation.JsonBackReference
import org.springframework.data.annotation.Id


data class TicketCatalogue(

    var price: Double = 0.0,

    var type: String = "",

    var minimumAge: Int = 0,

    var maximumAge: Int = 200,

    var zones: String = "A",

    // Duration in minutes
    var validityDuration: Long = 60,

) {
    @Id
    var id: Long  = 0L
    
}