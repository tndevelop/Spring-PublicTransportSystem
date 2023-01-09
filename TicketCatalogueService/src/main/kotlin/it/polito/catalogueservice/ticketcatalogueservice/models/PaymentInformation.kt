package it.polito.catalogueservice.ticketcatalogueservice.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class PaymentInformation(
    @JsonProperty("cardNumber")
    var cardNumber: String,

    @JsonProperty("expirationDate")
    var expirationDate: String,

    @JsonProperty("ccv")
    var ccv: String,

    @JsonProperty("cardHolder")
    var cardHolder: String
)   {

    override fun toString(): String {
        return super.toString()
    }

    fun validInfo(): String {
//        check card number
        if(!isValidCardNumber())
            return "Invalid Card Number."

//        check expiry
        if(isExpired(this.expirationDate))
            return "Credit Card expired."

//        check ccv
        if(!isValidCVVNumber(this.ccv))
            return "Invalid CCV."

        return "valid"
    }

    @JsonIgnore
    fun isValidCardNumber(): Boolean {
        var validNumber = false
        val visapattern = Regex("^4[0-9]{12}(?:[0-9]{3})?\$^5[1-5][0-9]{14}\$")
        val maestropattern = Regex("^(5018|5020|5038|6304|6759|6761|6763)[0-9]{8,15}\$")
        val masterpattern = Regex("^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14})\$")
        if(visapattern.containsMatchIn(this.cardNumber)) validNumber = true
        if(!validNumber && maestropattern.containsMatchIn(this.cardNumber)) validNumber = true
        if(!validNumber && masterpattern.containsMatchIn(this.cardNumber)) validNumber = true
        return validNumber
    }

    fun isExpired(str: String): Boolean {
        val simpleDateFormat = SimpleDateFormat("MM/yy")
        simpleDateFormat.isLenient = false
        val expiry: Date = simpleDateFormat.parse(this.expirationDate)
        return expiry.before(Date())
    }

    fun isValidCVVNumber(str: String?): Boolean {
        // Regex to check valid CVV number.
        val regex = "^[0-9]{3,4}$"

        // Compile the ReGex
        val p: Pattern = Pattern.compile(regex)

        // If the string is empty
        // return false
        if (str == null) {
            return false
        }

        // Find match between given string
        // and regular expression
        // using Pattern.matcher()
        val m: Matcher = p.matcher(str)

        // Return if the string
        // matched the ReGex
        return m.matches()
    }
}