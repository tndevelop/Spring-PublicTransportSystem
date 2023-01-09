package it.polito.paymentservice.services

import it.polito.paymentservice.entities.Transaction
import it.polito.paymentservice.repositories.TransactionRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDate

@Service
class TransactionService(val transactionRepository: TransactionRepository) {

    fun getAll() : Flow<Transaction> {
        return transactionRepository.findAll()
    }

    fun getAllInRange(dateFrom: LocalDate, dateTo: LocalDate) : Flow<Transaction> {
        return transactionRepository.findAllByTimestampIsBetween(Timestamp.valueOf(dateFrom.atStartOfDay()),
                Timestamp.valueOf(dateTo.atTime(23,59)))
    }

    fun findAllById(userid: Long) : Flow<Transaction> {
        return transactionRepository.findByUserId(userid)
    }

    fun findAllByUserIdInRange(userid: Long, dateFrom: LocalDate, dateTo: LocalDate) : Flow<Transaction> {
        return transactionRepository.findAllByUserIdAndTimestampIsBetween(userid,
                Timestamp.valueOf(dateFrom.atStartOfDay()), Timestamp.valueOf(dateTo.atTime(23,59)))
    }
}