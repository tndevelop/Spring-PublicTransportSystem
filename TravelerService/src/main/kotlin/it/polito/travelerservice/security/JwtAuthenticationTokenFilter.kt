package it.polito.travelerservice.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationTokenFilter: OncePerRequestFilter() {

    @Value("\${prefix}")
    lateinit var prefix : String

    @Value("\${header}")
    lateinit var headerparam : String

    @Autowired
    lateinit var userDetailsService: UserDetailsService

    @Autowired
    lateinit var jwtUtils: JwtUtils

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
            req: HttpServletRequest,
            res: HttpServletResponse,
            chain: FilterChain
    ) {
        val header = req.getHeader(headerparam)
        if (header == null || !header.startsWith(prefix)) {
            println("couldn't find bearer string, will ignore the header")
        }
        else {
            getAuthentication(header, req)
        }
        chain.doFilter(req, res)
    }

    private fun getAuthentication(token: String, req: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        try {
            if (!jwtUtils.validateJwt(token)) {
                return null
            }
            val userMap = jwtUtils.getDetailsJwt(token)

            val authorities = ArrayList<SimpleGrantedAuthority>()
            authorities.add(SimpleGrantedAuthority(userMap.get("roles")))

            val userDetails = userDetailsService.loadUserByUsername(userMap.get("username"))

            val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(userDetails, "null", authorities)
            usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(req)
            SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
            return usernamePasswordAuthenticationToken
        } catch (e: Exception) {
            return null
        }
    }

}