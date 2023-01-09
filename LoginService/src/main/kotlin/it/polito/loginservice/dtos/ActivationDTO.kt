package it.polito.loginservice.dtos

import it.polito.loginservice.entities.Activation
import it.polito.loginservice.entities.User
import java.util.*

data class ActivationDTO (val id: UUID?, val attemptCounter: Int, val user: User, val activationCode: Int, val activationDeadline: Date)

fun Activation.activationToDTO(): ActivationDTO{
    return ActivationDTO(id, attemptCounter, user, activationCode, activationDeadline)
}