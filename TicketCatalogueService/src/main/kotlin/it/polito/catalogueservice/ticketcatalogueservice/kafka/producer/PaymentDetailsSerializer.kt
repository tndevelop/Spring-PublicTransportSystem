package it.polito.catalogueservice.ticketcatalogueservice.kafka.producer

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.io.SerializationException
import it.polito.catalogueservice.ticketcatalogueservice.models.PaymentDetails
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.LoggerFactory

class PaymentDetailsSerializer: Serializer<PaymentDetails> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun serialize(topic: String?, data: PaymentDetails?): ByteArray? {
        log.info("Serializing...")
        return objectMapper.writeValueAsBytes(
            data ?: throw SerializationException("Error when serializing Product to ByteArray[]")
        )
    }

    override fun close() {}
}