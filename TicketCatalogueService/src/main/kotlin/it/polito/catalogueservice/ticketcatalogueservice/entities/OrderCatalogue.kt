package it.polito.catalogueservice.ticketcatalogueservice.entities

import org.springframework.data.annotation.Id


class OrderCatalogue(

    var status : String = "",

    var totalCost : Double = 0.0,

    var numberOfTickets : Int = 0,

    var userId : Long,

    var ticketId : Long

    ) {

    @Id
    var id: Long = 0L
}