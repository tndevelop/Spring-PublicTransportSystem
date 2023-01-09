package it.polito.travelerservice.entities

import com.fasterxml.jackson.annotation.JsonManagedReference
import org.hibernate.annotations.GenericGenerator
import org.springframework.security.core.GrantedAuthority
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList

@Entity
class UserDetails(
        @Column
        var name: String = "") {

    @Id
    //@GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0L

    @Column
    var address: String = ""

    @Column
    var dateOfBirth: LocalDate = LocalDate.now()

    @Column
    var telephoneNumber: String = ""

    @Column
    var age: Int = 0

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.REMOVE), orphanRemoval = true)
    @JsonManagedReference
    var ticketsPurchased = mutableListOf<TicketPurchased>()

    fun addTicket(t: TicketPurchased) {
        t.user = this
        ticketsPurchased.add(t)
    }
}