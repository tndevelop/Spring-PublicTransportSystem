package it.polito.catalogueservice.ticketcatalogueservice.models

import com.fasterxml.jackson.annotation.JsonProperty

class Test(
    @JsonProperty("totalCost")
    var totalCost: Float
)