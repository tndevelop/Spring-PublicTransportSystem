package it.polito.travelerservice.services

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.travelerservice.entities.TicketPurchased
import it.polito.travelerservice.models.TicketToValidate
import it.polito.travelerservice.models.TicketsToBuy
import it.polito.travelerservice.repositories.TicketPurchasedRepository
import it.polito.travelerservice.repositories.UserDetailsRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import java.rmi.AlreadyBoundException
import java.sql.Timestamp
import java.util.*

@Service
class TicketPurchasedService(val ticketPurchasedRepository: TicketPurchasedRepository, val userDetailsRepository: UserDetailsRepository) {

    @Value("\${keyTickets}")
    lateinit var key: String

    fun purchaseTickets(buyTickets: TicketsToBuy): MutableIterable<TicketPurchased> {
        var ticketsPurchased: MutableList<TicketPurchased> = emptyList<TicketPurchased>().toMutableList()

        val username = (SecurityContextHolder.getContext().authentication.principal as User).username
        val user = userDetailsRepository.findByName(username)

        for(i in 1..buyTickets.quantity) {

            var now: Long = System.currentTimeMillis()
            val exp = System.currentTimeMillis() + buyTickets.validity * 60 * 1000
            val ticket = TicketPurchased(
                Timestamp(now),
                Timestamp(exp),
                buyTickets.zones,
                "",
                buyTickets.type,
                user)
            val returnedTicket: TicketPurchased = ticketPurchasedRepository.save(ticket)

            val claims = Jwts.claims().setSubject(returnedTicket.sub.toString())
            claims["roles"] = arrayListOf("ROLE_CUSTOMER")
            claims["zid"] = buyTickets.zones
            claims["type"] = buyTickets.type

            val token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(exp))
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()

            ticketPurchasedRepository.updateTicketToken(token, returnedTicket.sub)
            ticketsPurchased.add(ticketPurchasedRepository.findTicketPurchasedBySub(returnedTicket.sub)!!)
        }

        return ticketsPurchased
    }

    fun getTicketsPurchasedByUserId(userId: Long) : List<TicketPurchased> {
        return ticketPurchasedRepository.findByUserId(userId)
    }

    fun getTicketPurchasedByTicketId(ticketId: Long) : Optional<TicketPurchased> {
        return ticketPurchasedRepository.findById(ticketId)
    }

    fun validateTicket(jws : String): Boolean?{
        val ticketId: Long
        try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(key.toByteArray()))
                .build()
                .parseClaimsJws(jws)

            ticketId = (claims.body["sub"] as String).toLong()

        }catch(e : Exception){
            throw JwtException(e.message)
        }
        if (ticketPurchasedRepository.findTicketPurchasedBySub(ticketId)?.used == true){
            throw AlreadyBoundException()
        }

        ticketPurchasedRepository.consumeTicket(ticketId)
        val tick = ticketPurchasedRepository.findTicketPurchasedBySub(ticketId)//tick?.used
        return true
    }
}