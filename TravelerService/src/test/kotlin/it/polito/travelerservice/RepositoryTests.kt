package it.polito.travelerservice;


import it.polito.travelerservice.entities.TicketPurchased
import it.polito.travelerservice.entities.UserDetails
import it.polito.travelerservice.repositories.TicketPurchasedRepository
import it.polito.travelerservice.repositories.UserDetailsRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate

@SpringBootTest
class RepositoryTests {

    @Autowired
    lateinit var userDetailsRepository : UserDetailsRepository

    @Autowired
    lateinit var ticketPurchasedRepository: TicketPurchasedRepository

    @BeforeEach
    fun destroyAll() {
        userDetailsRepository.deleteAll()
    }

    @Test
    fun insertUserDetails() {
        val userDetails = UserDetails("Name")
        userDetailsRepository.save(userDetails)
        assert(userDetailsRepository.count() == 1L)
    }

    @Test
    fun findUserDetails() {
        val userDetails = UserDetails("Name")
        userDetailsRepository.save(userDetails)
        val user = userDetailsRepository.findByName("Name")
        assert(user?.name == "Name")
    }


    @Test
    fun updateUserDetails(){
        val userDetails = UserDetails("Name")
        val user = userDetailsRepository.save(userDetails)
        assert(userDetailsRepository.count() == 1L)
        userDetailsRepository.updateUserDetails(user.id, "Address", LocalDate.now(), "New name", "telephone")
        val updatedUser = userDetailsRepository.findById(user.id)
        assert(updatedUser.get().name == "New name")
        assert(updatedUser.get().address == "Address")
        assert(updatedUser.get().telephoneNumber == "telephone")
    }

    @Test
    fun getTicketList() {
        val userDetails = UserDetails("Name")//.apply { ticketsPurchased = someTickets }
        val user = userDetailsRepository.save(userDetails)
        assert(userDetailsRepository.count() == 1L)

        var ticket = TicketPurchased(iat= Timestamp.from(Instant.now()), exp= Timestamp.from(Instant.parse("2022-12-12T10:37:30.00Z")), zid="A","","3h", user)
        ticketPurchasedRepository.save(ticket)

        val tickets = userDetailsRepository.findById(user.id).get().ticketsPurchased
        assert(tickets.size == 1)
    }
}
