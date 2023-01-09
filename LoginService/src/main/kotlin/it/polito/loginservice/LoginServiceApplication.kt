package it.polito.loginservice


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LoginServiceApplication{

}

fun main(args: Array<String>) {
	runApplication<LoginServiceApplication>(*args)
}
