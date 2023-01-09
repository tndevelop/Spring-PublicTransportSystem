package it.polito.travelerservice.services

import it.polito.travelerservice.entities.UserDetails
import it.polito.travelerservice.repositories.UserDetailsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service(value = "userDService")
class AppUserDetailsService : UserDetailsService{
    @Autowired
    lateinit var userDetailsRepository: UserDetailsRepository


    fun getProfile(username: String) : UserDetails? {
        return userDetailsRepository.findByName(username)
    }

    fun saveProfile(userDetails: UserDetails) : UserDetails? {
        return userDetailsRepository.save(userDetails)
    }

    fun getProfileById(id: Long) : UserDetails? {
        return userDetailsRepository.findUserDetailsById(id)
    }

    fun updateProfile(userDetails: UserDetails) : Int {
        val user = userDetailsRepository.findByName(userDetails.name)
        return userDetailsRepository.updateUserDetails(user!!.id, userDetails.address, userDetails.dateOfBirth,
                userDetails.name, userDetails.telephoneNumber, userDetails.age)
    }

    fun getAllUsers(): List<UserDetails> {
        return userDetailsRepository.findAll().toList()
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