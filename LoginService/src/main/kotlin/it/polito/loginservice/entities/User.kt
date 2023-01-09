package it.polito.loginservice.entities

import it.polito.loginservice.enums.Authority
import it.polito.loginservice.enums.Role
import javax.persistence.*

@Entity
@Table(name = "users")
class User (
        @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(255) CHECK(username > '')")
        var username: String,
        @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(255) CHECK(email > '')")
        var email: String,
        @Column(nullable = false, columnDefinition = "VARCHAR(255) CHECK(password > '')")
        var password: String
        ){
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0L

    var active: Boolean = false

    @Column
    var role: Role = Role.ROLE_CUSTOMER

    @Column
    var authority: Authority? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.REMOVE])//
    var activation: Activation? = null

}