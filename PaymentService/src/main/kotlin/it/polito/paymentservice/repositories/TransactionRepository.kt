package it.polito.paymentservice.repositories

import it.polito.paymentservice.entities.Transaction
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
interface TransactionRepository : CoroutineCrudRepository<Transaction, Long> {

    fun findByUserId(userid: Long) : Flow<Transaction>

    fun findAllByTimestampIsBetween(dateFrom : Timestamp, dateTo: Timestamp) : Flow<Transaction>

    fun findAllByUserIdAndTimestampIsBetween(userid: Long, dateFrom: Timestamp, dateTo: Timestamp) :Flow<Transaction>
}