package it.polito.travelerservice.services

import it.polito.travelerservice.entities.Turnstile
import it.polito.travelerservice.entities.Transit
import it.polito.travelerservice.models.TurnstileToAuthenticate
import it.polito.travelerservice.models.TransitInfo
import it.polito.travelerservice.repositories.TurnstileRepository
import it.polito.travelerservice.repositories.TransitRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TurnstileService(val turnstileRepository: TurnstileRepository, val transitRepository : TransitRepository) {

    @Value("\${keyTickets}")
    lateinit var key: String

    fun authenticate(turnstileToAuthenticate: TurnstileToAuthenticate): Any {
        val id = turnstileRepository.save(Turnstile(turnstileToAuthenticate.count))

        return id
    }

    fun updateTurnstileCount(turnstileToAuthenticate: TurnstileToAuthenticate): Int {
        turnstileRepository.updateTurnstileCount(turnstileToAuthenticate.id, turnstileToAuthenticate.count)

        val t = turnstileRepository.findById(turnstileToAuthenticate.id)

        return t.get().count

    }

    fun addTransit(transitInfo : TransitInfo): Int{
        var turnstile = turnstileRepository.findById(transitInfo.turnstileId)
        turnstileRepository.updateTurnstileCount(turnstile.get().id, turnstile.get().count + 1)

        transitRepository.save(Transit(transitInfo.passengerId, transitInfo.turnstileId))

        return turnstile.get().count + 1
    }


}