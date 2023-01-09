package it.polito.paymentservice.entities

import org.springframework.data.annotation.Id
import java.sql.Timestamp
import java.time.LocalDate

class Transaction(
        val userId: Long,
        val totalCost: Double,
        val numberOfTickets: Int,
        val ticketId: Int,
        val timestamp: Timestamp
) {
    @Id
    var id: Long = 0L
}