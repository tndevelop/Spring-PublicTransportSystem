package it.polito.travelerservice.models

data class TicketsToBuy (
    val cmd: String,
    val quantity: Int,
    val zones: String,
    val validity: Long, // minutes
    val type: String
    )