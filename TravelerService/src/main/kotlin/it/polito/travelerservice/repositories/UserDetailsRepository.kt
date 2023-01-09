package it.polito.travelerservice.repositories

import it.polito.travelerservice.entities.UserDetails
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Repository
interface UserDetailsRepository: CrudRepository<UserDetails, Long> {

    // Find user by username
    fun findByName(name: String) : UserDetails?

    // Update user details for a user
    @Transactional
    @Modifying
    @Query("UPDATE UserDetails u SET u.address = :address, u.dateOfBirth = :dateOfBirth, " +
            "u.name = :name, u.telephoneNumber = :number, u.age = :age WHERE u.id = :id")
    fun updateUserDetails(id: Long, address: String, dateOfBirth: LocalDate, name: String, number: String, age: Int): Int


    fun findUserDetailsById(id: Long) : UserDetails?


}