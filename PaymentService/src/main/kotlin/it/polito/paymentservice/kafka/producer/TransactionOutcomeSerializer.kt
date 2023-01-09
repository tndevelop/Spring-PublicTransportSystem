package it.polito.paymentservice.kafka.producer

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.paymentservice.models.TransactionOutcome
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.LoggerFactory

class TransactionOutcomeSerializer: Serializer<TransactionOutcome> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun serialize(topic: String?, data: TransactionOutcome?): ByteArray? {
        log.info("Serializing...")
        return objectMapper.writeValueAsBytes(
            data ?: throw SerializationException("Error when serializing Product to ByteArray[]")
        )
    }

    override fun close() {}
}