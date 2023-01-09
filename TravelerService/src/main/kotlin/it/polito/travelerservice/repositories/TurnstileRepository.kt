package it.polito.travelerservice.repositories

import it.polito.travelerservice.entities.Turnstile
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface TurnstileRepository : CrudRepository<Turnstile, Long> {

    // Update count
    @Transactional
    @Modifying
    @Query("UPDATE Turnstile t SET t.count = :count WHERE t.id = :id")
    fun updateTurnstileCount(id: Long, count: Int): Int

}