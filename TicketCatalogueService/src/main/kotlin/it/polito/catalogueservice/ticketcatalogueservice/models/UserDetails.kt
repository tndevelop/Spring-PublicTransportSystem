package it.polito.catalogueservice.ticketcatalogueservice.models

import java.time.LocalDate

class UserDetails(
        var name: String = "") {
    var id: Long = 0L
    var address: String = ""
    var dateOfBirth: LocalDate = LocalDate.now()
    var telephoneNumber: String = ""
    var age: Int = 0
}