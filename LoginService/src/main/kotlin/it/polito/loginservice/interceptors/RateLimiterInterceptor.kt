package it.polito.loginservice.interceptors

import io.github.bucket4j.*
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.String
import java.time.Duration
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.Any
import kotlin.Boolean
import kotlin.Long


class RateLimiterInterceptor : HandlerInterceptor{
    val bucket = Bucket4j.builder()
        .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofSeconds(1))))
        .build()

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        val probe: ConsumptionProbe = bucket.tryConsumeAndReturnRemaining(1)
        return if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()))
            println("Token consumed, RemainingToken= " + String.valueOf(probe.getRemainingTokens()))
            true
        } else {
            println("Waiting for token, RemainingToken= " + String.valueOf(probe.getRemainingTokens()))
            val waitForRefill: Long = probe.getNanosToWaitForRefill() / 1000000000
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", waitForRefill.toString())
            response.sendError(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "You have exhausted your API Request Quota"
            )
            false
        }

    }
}