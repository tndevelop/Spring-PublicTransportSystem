package it.polito.travelerservice.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfig: WebSecurityConfigurerAdapter() {

    @Bean
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Bean
    fun authenticationTokenFilterBean(): JwtAuthenticationTokenFilter {
        return JwtAuthenticationTokenFilter()
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests().antMatchers("/my/profile").hasRole("CUSTOMER")
                .antMatchers("/my/tickets*").hasRole("CUSTOMER")
                .antMatchers("/admin/traveler*").hasRole("ADMIN")
                .antMatchers("/admin/transits*").hasRole("ADMIN")
                .antMatchers("/emb/*").hasRole("TURNSTILE")
                .antMatchers("/emb/tickets/*").hasRole("TURNSTILE")
                .antMatchers("/product").permitAll()
            .anyRequest().authenticated()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf().disable()

        http.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter::class.java)
    }

    @Bean
    fun encoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }
}