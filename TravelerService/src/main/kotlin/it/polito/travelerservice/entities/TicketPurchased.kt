package it.polito.travelerservice.entities

import com.fasterxml.jackson.annotation.JsonBackReference
import org.hibernate.annotations.GenericGenerator
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.*

@Entity
class TicketPurchased(
    @Column(name = "issued_at")
    var iat: Timestamp = Timestamp.valueOf(LocalDateTime.now()),

    @Column(name = "expiry")
    var exp: Timestamp = Timestamp.valueOf(LocalDateTime.now()),

    @Column(name = "zone_id")
    var zid: String = "",

    @Column(name = "jws")
//    [Note that this JWT will be used for providing
//physical access to the train area and will be signed by a key that has nothing to do
//with the key used by the LoginService]
    var jws: String = "",

    var type: String = "",

    @ManyToOne
    @JoinColumn
    @JsonBackReference
    var user: UserDetails? = null,

    @Column(name= "used")
    var used: Boolean = false
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "ticket_id")
    var sub: Long = 0L



}