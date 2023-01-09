package it.polito.travelerservice.dtos

import it.polito.travelerservice.entities.TicketPurchased
import it.polito.travelerservice.entities.UserDetails
import java.sql.Timestamp

data class TicketPurchasedDTO(
    val sub: Long,
    val iat: Timestamp,
    val exp: Timestamp,
    val zid: String,
    val jws: String,
    var type: String,
    val used: Boolean
    //val user: UserDetails?
)

fun TicketPurchased.ticketPurchasedToDTO(): TicketPurchasedDTO {
    return TicketPurchasedDTO(sub, iat, exp, zid, jws, type, used /*user*/)
}