package it.polito.travelerservice.repositories

import it.polito.travelerservice.entities.TicketPurchased
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional

interface TicketPurchasedRepository: CrudRepository<TicketPurchased, Long> {

    fun findByUserId(userId: Long): List<TicketPurchased>

    // Set ticket as used
    @Transactional
    @Modifying
    @Query("UPDATE TicketPurchased t SET t.used = :used WHERE t.sub = :ticketId")
    fun consumeTicket(ticketId: Long, used:Boolean=true)

    @Transactional
    @Modifying
    @Query("UPDATE TicketPurchased t SET t.jws = :jws WHERE t.sub = :ticketId")
    fun updateTicketToken(jws: String, ticketId: Long)

    fun findTicketPurchasedBySub(id: Long): TicketPurchased?
}