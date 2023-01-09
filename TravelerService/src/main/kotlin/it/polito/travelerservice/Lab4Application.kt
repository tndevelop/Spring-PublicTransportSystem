package it.polito.travelerservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication()
class Lab4Application

fun main(args: Array<String>) {
    runApplication<Lab4Application>(*args)
}
