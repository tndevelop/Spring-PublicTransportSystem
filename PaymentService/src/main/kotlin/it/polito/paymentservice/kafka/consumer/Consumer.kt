package it.polito.paymentservice.kafka.consumer

import it.polito.paymentservice.models.PaymentDetails
import it.polito.paymentservice.entities.Transaction
import it.polito.paymentservice.models.TransactionOutcome
import it.polito.paymentservice.repositories.TransactionRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant

@Component
class Consumer(
        @Value("\${kafka.topics.result}") val topic: String,
        @Autowired
        private val kafkaTemplate: KafkaTemplate<String, Any>
    ) {

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["\${kafka.topics.payment}"], groupId = "ppr")
    fun listenGroupFoo(consumerRecord: ConsumerRecord<Any, Any>, ack: Acknowledgment) {
        logger.info("Message received")
        logger.info("total cost: {}", (consumerRecord.value() as PaymentDetails).totalCost)

        // save transaction in database
        val paymentDetails = (consumerRecord.value() as PaymentDetails)
        val transaction = Transaction(paymentDetails.userId, paymentDetails.totalCost.toDouble(),
                paymentDetails.numberOfTickets,paymentDetails.ticketId.toInt(), Timestamp.from(Instant.now()))
        runBlocking { launch {
            transactionRepository.save(transaction)
            logger.info("Transaction saved")
        } }

//        fireback a message to ticketcatalogueservice
        try {
            val outcome = TransactionOutcome("Success", paymentDetails.orderId, paymentDetails.userId)

            logger.info("Receiving product request")
            logger.info("Sending message to Kafka {}", outcome)
            val message: Message<TransactionOutcome> = MessageBuilder
                .withPayload(outcome)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader("X-Custom-Header", "Custom header here")
                .build()
            kafkaTemplate.send(message)
            logger.info("Message sent with success")
       } catch (e: Exception) {
            logger.error("Exception: {}",e)
       }

//        ack.acknowledge()
    }
}