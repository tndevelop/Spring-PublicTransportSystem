package it.polito.catalogueservice.ticketcatalogueservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TicketCatalogueServiceApplication

fun main(args: Array<String>) {
    runApplication<TicketCatalogueServiceApplication>(*args)
}
