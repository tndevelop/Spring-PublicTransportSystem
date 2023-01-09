package it.polito.catalogueservice.ticketcatalogueservice.kafka.consumer
import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.catalogueservice.ticketcatalogueservice.models.Test
import it.polito.catalogueservice.ticketcatalogueservice.models.TransactionOutcome
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.LoggerFactory
import kotlin.text.Charsets.UTF_8

class TransactionOutcomeDeserializer : Deserializer<TransactionOutcome> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun deserialize(topic: String?, data: ByteArray?): TransactionOutcome? {
        log.info("Deserializing...")
        return objectMapper.readValue(
            String(
                data ?: throw SerializationException("Error when deserializing byte[] to Product"), UTF_8
            ), TransactionOutcome::class.java
        )
    }

    override fun close() {}

}