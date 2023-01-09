package it.polito.paymentservice.entities

import java.time.LocalDate

class UserDetails(
        var name: String = "") {
    var id: Long = 0L
    var address: String = ""
    var dateOfBirth: LocalDate = LocalDate.now()
    var telephoneNumber: String = ""
    var age: Int = 0
}