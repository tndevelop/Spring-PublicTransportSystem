package it.polito.travelerservice.entities

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Transit(
    @Column
    var passengerId : Long,

    @Column
    var turnstileId : Long,

    @Column(name = "transit_time")
    var transitTime: Timestamp = Timestamp.valueOf(LocalDateTime.now()),

    )
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "transit_id")
    var transitId: Long = 0L
}