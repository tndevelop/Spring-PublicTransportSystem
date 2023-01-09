package it.polito.paymentservice.security.webflux

import it.polito.paymentservice.repositories.UserDetailsRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationManager @Autowired constructor(var jwtUtil: JwtUtils,
                                                   var userRepository: UserDetailsRepository)
    : ReactiveAuthenticationManager {

    @Autowired
    lateinit var userDetailsService: UserDetailsService

    override fun authenticate(authentication: Authentication?): Mono<Authentication> {
        val token = authentication?.credentials.toString()
        return Mono.justOrEmpty(authentication)
            .flatMap { jwt -> mono {  validate(token) } }
            .onErrorMap { error -> IllegalArgumentException(error) }
    }

    private suspend fun validate(token:String): Authentication {
        val userName = jwtUtil.getDetailsJwt(token).get("username")
        val user = userDetailsService.loadUserByUsername(userName) // userRepository.findByName(userName!!).awaitSingleOrNull()
        if (jwtUtil.validateJwt(token)) {
            val authorities : MutableList<SimpleGrantedAuthority> = mutableListOf()
            authorities.add(SimpleGrantedAuthority(jwtUtil.getDetailsJwt(token).get("roles")))
            val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(user, token, authorities)
            SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
            return usernamePasswordAuthenticationToken
        }
        throw IllegalArgumentException("Token is not valid!")
    }

}
