package it.polito.travelerservice.entities

import javax.persistence.*

@Entity
class Turnstile(
    @Column(name = "count")
    var count : Int = 0
)
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "turnstile_id")
    var id : Long = 0L


}