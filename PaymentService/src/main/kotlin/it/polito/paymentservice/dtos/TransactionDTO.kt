package it.polito.paymentservice.dtos

import it.polito.paymentservice.entities.Transaction
import java.sql.Timestamp

data class TransactionDTO(
    val id: Long,
    val userId: Long,
    val totalCost: Double,
    val numberOfTickets: Int,
    val ticketId: Int,
    val timestamp: Timestamp
)

fun Transaction.transactionToDTO(): TransactionDTO {
    return TransactionDTO(id, userId, totalCost, numberOfTickets, ticketId, timestamp)
}
