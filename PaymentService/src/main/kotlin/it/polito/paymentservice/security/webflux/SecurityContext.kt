package it.polito.paymentservice.security.webflux

import it.polito.paymentservice.repositories.UserDetailsRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class SecurityContext @Autowired constructor(
    var authenticationManager: AuthenticationManager,
    var jwtUtil: JwtUtils,
    var userRepository: UserDetailsRepository
): ServerSecurityContextRepository {

    @Value("\${prefix}")
    lateinit var bearer : String

    override fun save(exchange: ServerWebExchange?, context: SecurityContext?): Mono<Void> {
        return Mono.empty()
    }

    override fun load(exchange: ServerWebExchange?): Mono<SecurityContext> {
        return Mono.justOrEmpty(exchange?.request?.headers?.getFirst(HttpHeaders.AUTHORIZATION))
            .filter {
                it.startsWith(bearer)
            }
            .map {
                it.substring(bearer.length)
            }
            .flatMap {
                    jwt -> mono {  returnAuth(jwt) }
            }
            .flatMap{
                    auth -> authenticationManager.authenticate(auth)
                .map {
                    SecurityContextImpl(it)
                }
            }
    }

    private suspend fun returnAuth(token: String): UsernamePasswordAuthenticationToken{
        val username = jwtUtil.getDetailsJwt(token).get("username")
        var user = userRepository.findByName(username!!).awaitSingleOrNull()
        var authorities: MutableList<SimpleGrantedAuthority> = mutableListOf()
        authorities.add(SimpleGrantedAuthority(jwtUtil.getDetailsJwt(token).get("roles")))
        return UsernamePasswordAuthenticationToken(
            user?.name,
            token,
            authorities
        )
    }
}
