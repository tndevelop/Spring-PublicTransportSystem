package it.polito.paymentservice.models

import com.fasterxml.jackson.annotation.JsonProperty

class TransactionOutcome (
        @JsonProperty("status")
        var status: String,
        @JsonProperty("orderId")
        var orderId: Long,
        @JsonProperty("userId")
        var userId: Long
        )
