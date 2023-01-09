package it.polito.catalogueservice.ticketcatalogueservice.security.webflux

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
class SecurityConfig @Autowired constructor(
    val authenticationManager: AuthenticationManager,
    val securityContext: SecurityContext
){

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun springSecurityFilterChain(httpSecurity: ServerHttpSecurity): SecurityWebFilterChain {
        httpSecurity
            .authorizeExchange()
            .pathMatchers(HttpMethod.GET, "/test").permitAll()
            .pathMatchers(HttpMethod.POST, "/admin/tickets").hasRole("ADMIN")
            .pathMatchers(HttpMethod.PUT, "/admin/tickets").hasRole("ADMIN")
            .pathMatchers(HttpMethod.GET, "/admin/orders").hasRole("ADMIN")
            .pathMatchers(HttpMethod.GET, "/admin/orders/**").hasRole("ADMIN")
            .pathMatchers(HttpMethod.POST, "/shop").hasRole("CUSTOMER")
            .pathMatchers(HttpMethod.POST, "/my/profile").hasRole("CUSTOMER")
            .pathMatchers(HttpMethod.GET, "/tickets").permitAll()
            .pathMatchers(HttpMethod.GET, "/orders").hasRole("CUSTOMER")
            .pathMatchers(HttpMethod.GET, "/orders/**").hasRole("CUSTOMER")
            .and()
            .httpBasic().disable()
            .formLogin().disable()
            .csrf().disable()
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContext)

        return httpSecurity.build()
    }

}