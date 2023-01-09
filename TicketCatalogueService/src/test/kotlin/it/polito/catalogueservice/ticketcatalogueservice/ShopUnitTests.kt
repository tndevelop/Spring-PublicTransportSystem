package it.polito.catalogueservice.ticketcatalogueservice

import com.google.gson.GsonBuilder
import it.polito.catalogueservice.ticketcatalogueservice.entities.TicketCatalogue
import it.polito.catalogueservice.ticketcatalogueservice.models.PaymentInformation
import it.polito.catalogueservice.ticketcatalogueservice.models.TicketToAdd
import it.polito.catalogueservice.ticketcatalogueservice.repositories.TicketCatalogueRepository
import it.polito.catalogueservice.ticketcatalogueservice.services.ShopService
import it.polito.catalogueservice.ticketcatalogueservice.services.TicketCatalogueService
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class ShopUnitTests {

     var shopService = ShopService()


    @Test
    fun getProfileInfo() {
        // Relies on a user with username "Name" existing in the traveler service
        val username = "Name"
        val response: String? = shopService.getProfile(username)
        val responseObject = JSONObject(response)
        val name = responseObject.get("name").toString()

        assert(name == "Name")
    }


}