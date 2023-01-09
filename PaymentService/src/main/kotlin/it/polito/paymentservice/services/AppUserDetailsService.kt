package it.polito.paymentservice.services

import it.polito.paymentservice.entities.UserDetails
import it.polito.paymentservice.repositories.UserDetailsRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service(value = "userDService")
class AppUserDetailsService : UserDetailsService{
    @Autowired
    lateinit var userDetailsRepository: UserDetailsRepository

    suspend fun findByName(name: String) : Mono<UserDetails>? {
        return userDetailsRepository.findByName(name)
    }

    suspend fun saveProfile(userDetails: UserDetails) : UserDetails {
        return userDetailsRepository.save(userDetails)
    }

    override fun loadUserByUsername(username: String): org.springframework.security.core.userdetails.UserDetails {

            val authorities = ArrayList<GrantedAuthority>()
            return User(
                    username,
                    "null",
                    authorities
            )
    }

}