package it.polito.paymentservice.kafka.consumer

import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.paymentservice.models.PaymentDetails
import kotlin.text.Charsets.UTF_8

class PaymentDetailsDeserializer : Deserializer<PaymentDetails> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun deserialize(topic: String?, data: ByteArray?): PaymentDetails? {
        log.info("Deserializing...")
        var stringData: String = String(
            data ?: throw SerializationException("Error when deserializing byte[] to Product"), UTF_8
        )
        return objectMapper.readValue(
            stringData
            , PaymentDetails::class.java
        )
    }

    override fun close() {}

}