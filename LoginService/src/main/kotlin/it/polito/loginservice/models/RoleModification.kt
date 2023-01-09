package it.polito.loginservice.models

import it.polito.loginservice.enums.Authority
import it.polito.loginservice.enums.Role

class RoleModification (
    var username: String,
    var role: Role,
    var authority: Authority? = null
)