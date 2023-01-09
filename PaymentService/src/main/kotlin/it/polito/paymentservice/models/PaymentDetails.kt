package it.polito.paymentservice.models

import com.fasterxml.jackson.annotation.JsonProperty

class PaymentDetails(
    @JsonProperty("paymentInformation")
    var paymentInformation: PaymentInformation,
    @JsonProperty("totalCost")
    var totalCost: Float,
    @JsonProperty("userId")
    var userId: Long,
    @JsonProperty("numberOfTickets")
    var numberOfTickets: Int,
    @JsonProperty("ticketId")
    var ticketId: Long,
    @JsonProperty("orderId")
    var orderId: Long
)