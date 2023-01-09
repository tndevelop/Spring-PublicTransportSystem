package it.polito.catalogueservice.ticketcatalogueservice.repositories

import it.polito.catalogueservice.ticketcatalogueservice.entities.TicketCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.models.UserDetails
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TicketCatalogueRepository: CoroutineCrudRepository<TicketCatalogue, Long> {

    // Find user by type
    fun findByType(type: String) : Mono<TicketCatalogue>
}