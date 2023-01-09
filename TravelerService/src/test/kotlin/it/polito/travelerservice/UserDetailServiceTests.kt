package it.polito.travelerservice

import it.polito.travelerservice.entities.UserDetails
import it.polito.travelerservice.repositories.UserDetailsRepository
import it.polito.travelerservice.services.AppUserDetailsService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserDetailServiceTests {

    @Autowired
    lateinit var userDetailsRepository : UserDetailsRepository

    @Autowired
    lateinit var appUserDetailsService: AppUserDetailsService

    @BeforeEach
    fun init() {
        userDetailsRepository.deleteAll()
    }

    @Test
    fun getProfile(){
        val userDetails = UserDetails("Name")
        userDetailsRepository.save(userDetails)
        val user = appUserDetailsService.getProfile("Name")
        assert(user!!.name == "Name")
    }

    @Test
    fun updateProfile(){
        val userDetails = UserDetails("Name")
        var user = userDetailsRepository.save(userDetails)
        user.apply { address = "New address" }
        appUserDetailsService.updateProfile(user)
        val userUpdated = appUserDetailsService.getProfile("Name")
        assert(userUpdated!!.address == "New address")
    }
}