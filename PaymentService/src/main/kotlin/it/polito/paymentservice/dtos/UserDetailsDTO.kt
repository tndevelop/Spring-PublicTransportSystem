package it.polito.paymentservice.dtos

import it.polito.paymentservice.entities.UserDetails
import java.time.LocalDate

data class UserDetailsDTO(
    val id: Long,
    var name: String,
    var address: String,
    var dateOfBirth: LocalDate,
    var telephoneNumber: String,
    var age: Int
)

fun UserDetails.userDetailsToDTO(): UserDetailsDTO {
    return UserDetailsDTO(id, name, address, dateOfBirth, telephoneNumber, age)
}