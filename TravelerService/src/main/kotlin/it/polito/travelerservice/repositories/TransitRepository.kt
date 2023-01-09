package it.polito.travelerservice.repositories

import it.polito.travelerservice.entities.Transit
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
interface TransitRepository : CrudRepository<Transit, Long> {

    fun findByPassengerId(passengerid: Long) : List<Transit>

    fun findAllByTransitTimeIsBetween(dateFrom: Timestamp, dateTo: Timestamp): List<Transit>

    fun findAllByPassengerIdAndTransitTimeIsBetween(passengerid: Long, dateFrom: Timestamp, dateTo: Timestamp): List<Transit>

}