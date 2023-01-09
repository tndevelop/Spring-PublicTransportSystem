package it.polito.travelerservice


import it.polito.travelerservice.dtos.TicketPurchasedDTO
import it.polito.travelerservice.dtos.UserDetailsDTO
import it.polito.travelerservice.dtos.ticketPurchasedToDTO
import it.polito.travelerservice.dtos.userDetailsToDTO
import it.polito.travelerservice.entities.TicketPurchased
import it.polito.travelerservice.entities.UserDetails
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime


@SpringBootTest
class DTOTests {

    @Test
    fun testUserDetailsDTO() {
        val userDetails = UserDetailsDTO(1, "Name","1 Long Street", LocalDate.of(1980, 10, 20), "39 123 433 5678")
        assert(userDetails.name == "Name")
    }

    @Test
    fun testUserDetailsToDTO() {
        val userDetails = UserDetails("Name").apply { address = "1 Long Street"; dateOfBirth = LocalDate.of(1980, 10, 20);
            telephoneNumber = "39 123 433 5678"}
        val userDetailsDTO: UserDetailsDTO = userDetails.userDetailsToDTO()
        assert(userDetailsDTO.name == "Name")
    }

    @Test
    fun testTicketPurchasedDTO() {
        val userDetails = UserDetails("Name").apply { address = "1 Long Street"; dateOfBirth = LocalDate.of(1980, 10, 20);
            telephoneNumber = "39 123 433 5678"}
        val ticketPurchased = TicketPurchasedDTO(0, Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(LocalDateTime.now().plusDays(7)), "A", "", ""/*, userDetails*/)
        assert(ticketPurchased.zid == "A")
    }

    @Test
    fun testTicketPurchaseToDTO() {
        val ticketPurchased = TicketPurchased().apply { sub = 0; iat = Timestamp.valueOf(LocalDateTime.now());
            exp = Timestamp.valueOf(LocalDateTime.now().plusDays(7)); zid = "A"; jws = ""}
        val ticketPurchasedDTO = ticketPurchased.ticketPurchasedToDTO()
        assert(ticketPurchasedDTO.zid == "A")
    }
}