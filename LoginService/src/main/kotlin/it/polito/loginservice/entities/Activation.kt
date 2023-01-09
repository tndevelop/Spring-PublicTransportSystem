package it.polito.loginservice.entities

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*


@Entity
class Activation (
        @OneToOne
        var user: User
        ) {
    @Id

    @Column(length=16)
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    var id: UUID? = null

    @Column(columnDefinition = "INT CHECK(attempt_counter >=0)" )
    var attemptCounter: Int = 5

    //Generates a random 6 digit activation code
    var activationCode: Int = Random().nextInt(899999) + 100000

    // Activation deadline currently set manually to 7 days from creation
    var activationDeadline: Date = Date(System.currentTimeMillis() + 7*24*60*60*1000)
}
