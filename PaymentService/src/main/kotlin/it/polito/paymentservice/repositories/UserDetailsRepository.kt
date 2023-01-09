package it.polito.paymentservice.repositories

import it.polito.paymentservice.entities.UserDetails
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserDetailsRepository: CoroutineCrudRepository<UserDetails, Long> {

    // Find user by username
    fun findByName(name: String) : Mono<UserDetails>

}