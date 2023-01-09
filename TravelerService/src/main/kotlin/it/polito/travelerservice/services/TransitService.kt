package it.polito.travelerservice.services

import it.polito.travelerservice.entities.Transit
import it.polito.travelerservice.repositories.TransitRepository
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDate

@Service
class TransitService(val transitRepository: TransitRepository) {

    fun getAll() : List<Transit> {
        return transitRepository.findAll().toList()
    }

    fun getAllInRange(dateFrom: LocalDate, dateTo: LocalDate) : List<Transit> {
        return transitRepository.findAllByTransitTimeIsBetween(Timestamp.valueOf(dateFrom.atStartOfDay()),
                Timestamp.valueOf(dateTo.atTime(23,59)))
    }

    fun findAllById(userid: Long) : List<Transit>  {
        return transitRepository.findByPassengerId(userid)
    }

    fun findAllByUserIdInRange(userid: Long, dateFrom: LocalDate, dateTo: LocalDate) : List<Transit> {
        return transitRepository.findAllByPassengerIdAndTransitTimeIsBetween(userid,
                Timestamp.valueOf(dateFrom.atStartOfDay()), Timestamp.valueOf(dateTo.atTime(23,59)))
    }

}