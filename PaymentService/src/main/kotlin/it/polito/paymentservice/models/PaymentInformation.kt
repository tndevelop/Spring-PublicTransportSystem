package it.polito.paymentservice.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigInteger

class PaymentInformation(
    @JsonProperty("cardNumber")
    var cardNumber: String,

    @JsonProperty("expirationDate")
    var expirationDate: String,

    @JsonProperty("ccv")
    var ccv: String,

    @JsonProperty("cardHolder")
    var cardHolder: String
) {
    override fun toString(): String {
        return super.toString()
    }
}
