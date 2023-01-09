package it.polito.loginservice.repositories

import it.polito.loginservice.entities.Activation
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface ActivationRepository : CrudRepository<Activation, UUID?>{

    @Transactional
    @Modifying
    @Query("UPDATE Activation a SET a.attemptCounter = a.attemptCounter - 1 WHERE a.id = ?1")
    fun decrementCounter(id: UUID): Int

}
