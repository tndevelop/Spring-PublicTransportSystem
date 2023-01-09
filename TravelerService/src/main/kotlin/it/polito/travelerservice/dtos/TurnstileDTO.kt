package it.polito.travelerservice.dtos

import it.polito.travelerservice.entities.Turnstile

data class TurnstileDTO(
    val id: Long,
    val count : Int
)

fun Turnstile.turnstileToDTO(): TurnstileDTO {
    return TurnstileDTO(id, count)
}