package it.polito.loginservice.services

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service(value = "userDService")
class AppUserDetailsService : UserDetailsService{

    override fun loadUserByUsername(username: String): org.springframework.security.core.userdetails.UserDetails {

        val authorities = ArrayList<GrantedAuthority>()
        return User(
                username,
                "null",
                authorities
        )

    }

}