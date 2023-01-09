package it.polito.travelerservice.dtos

import it.polito.travelerservice.entities.UserDetails
import java.time.LocalDate
import javax.persistence.Column

data class UserDetailsDTO(
        val id: Long,
        val name: String,
        val address: String,
        val dateOfBirth: LocalDate,
        val telephoneNumber: String
    )

fun UserDetails.userDetailsToDTO(): UserDetailsDTO {
    return UserDetailsDTO(id, name, address, dateOfBirth, telephoneNumber)
}