package it.polito.travelerservice.dtos

import it.polito.travelerservice.entities.Transit
import java.sql.Timestamp

data class TransitDTO(
    val transitId: Long,
    val turnstileId : Long,
    val passengerId : Long,
    val transitTime: Timestamp
)

fun Transit.transitToDTO(): TransitDTO {
    return TransitDTO(transitId, turnstileId, passengerId, transitTime)
}