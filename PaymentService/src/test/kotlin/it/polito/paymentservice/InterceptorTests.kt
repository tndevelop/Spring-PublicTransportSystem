package it.polito.paymentservice

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InterceptorTests {



/*
    @Test
    fun tooManyRequests(){

        val baseUrl = "http://localhost:$port"

        val validations= listOf<Validation>(
            Validation(), Validation(), Validation(),
            Validation(), Validation(), Validation(),
            Validation(), Validation(), Validation(),
            Validation(), Validation()
        )

        var tooManyStatusObserved = false
        var threadList = ArrayList<Thread>()

        for(i in validations){
            threadList.add(
                Thread {
                    val request = i
                    val response = restTemplate.postForEntity<Unit>(
                        "$baseUrl/user/validate",
                        request
                    )
                    if (response.statusCode == HttpStatus.TOO_MANY_REQUESTS) {
                        tooManyStatusObserved = true
                    }
                })
        }

        for (thread in threadList) {
            thread.start()
        }

        for (thread in threadList) {
            thread.join()
        }
        assert(tooManyStatusObserved)


    }*/


}