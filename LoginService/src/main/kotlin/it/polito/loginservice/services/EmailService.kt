package it.polito.loginservice.services


import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service


@Service
class EmailService( private val emailSender: JavaMailSender) {

    @Async
    fun sendEmail(subject: String, text: String, targetEmail: String) {
        val message = SimpleMailMessage()
        message.setSubject(subject)
        message.setText(text)
        message.setTo(targetEmail)

        emailSender.send(message)
    }

}
