package it.polito.catalogueservice.ticketcatalogueservice.services

import it.polito.catalogueservice.ticketcatalogueservice.entities.TicketCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.models.TicketToAdd
import it.polito.catalogueservice.ticketcatalogueservice.repositories.TicketCatalogueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class TicketCatalogueService(var ticketCatalogueRepository: TicketCatalogueRepository) {

    @PreAuthorize("hasRole('ADMIN')")
    suspend fun addTicket(ticketToAdd: TicketToAdd): TicketCatalogue {

        val ticket = TicketCatalogue(
            ticketToAdd.price,
            ticketToAdd.type,
            ticketToAdd.minimumAge,
            ticketToAdd.maximumAge,
            ticketToAdd.zones,
            ticketToAdd.validityDuration
        )

        val newTk = ticketCatalogueRepository.save(ticket)

        return newTk
    }

    @PreAuthorize("hasRole('ADMIN')")
    suspend fun updateTicket(ticketToAdd: TicketToAdd): TicketCatalogue {

        val ticket = TicketCatalogue(
                ticketToAdd.price,
                ticketToAdd.type,
                ticketToAdd.minimumAge,
                ticketToAdd.maximumAge,
                ticketToAdd.zones,
                ticketToAdd.validityDuration
        )

        val oldTk = ticketCatalogueRepository.findByType(ticket.type).awaitSingleOrNull()!!

        ticket.apply { this.id = oldTk.id }

        val newTk = ticketCatalogueRepository.save(ticket)

        return newTk
    }

    fun getAll() : Flow<TicketCatalogue> {
        return ticketCatalogueRepository.findAll()
    }

    suspend fun getTicketById(id: Long) : TicketCatalogue? {
        return ticketCatalogueRepository.findById(id)
    }
}