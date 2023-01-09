package it.polito.loginservice.dtos

import it.polito.loginservice.entities.User

data class UserDTO(val id: Long, val username: String, val email: String, val password: String, val active: Boolean)

fun User.userToDTO(): UserDTO{
    return UserDTO(id, username, email, password, active)
}