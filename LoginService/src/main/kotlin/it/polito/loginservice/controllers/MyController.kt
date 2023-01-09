package it.polito.loginservice.controllers

import it.polito.loginservice.models.Validation
import it.polito.loginservice.entities.User
import it.polito.loginservice.models.LoginRequest
import it.polito.loginservice.models.RoleModification
import it.polito.loginservice.utils.JWTUtils
import it.polito.loginservice.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.lang.Integer.parseInt
import java.util.*


@RestController
class MyController {

    @Autowired
    lateinit var userService: UserService

    @Autowired
    var jwtUtils: JWTUtils? = null

    @PostMapping("/user/register")
    fun register(@RequestBody user: User) : ResponseEntity<Map<String, String>> {

        var returnMap: Map<String, String>
        var status: HttpStatus
        try {
            val provisional_id = userService?.register(user.username, user.password, user.email)
            returnMap = mapOf("provisional_id" to provisional_id.toString(), "email" to user.email)
            status = HttpStatus.ACCEPTED
        } catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)
    }

    @PostMapping("/user/validate")
    fun validate(@RequestBody validation : Validation) : ResponseEntity<Map<String, String>> {

        var returnMap: Map<String, String>
        var status: HttpStatus

        try {
            val user = userService?.validate(UUID.fromString(validation.provisional_id), parseInt(validation.activation_code))
            returnMap = mapOf("userID" to user!!.id.toString(), "username" to user.username, "email" to user.email)
            status = HttpStatus.ACCEPTED
        } catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.NOT_FOUND
        }
        return ResponseEntity(returnMap, status)
    }

    @PostMapping("/user/login")
    fun login(@RequestBody loginReq: LoginRequest): ResponseEntity<Map<String, String>> {

        var returnMap: Map<String, String>
        var status: HttpStatus
        try {
//            check if username exists
            val userDetails: User = userService?.findUserByUsername(loginReq.username)
                ?: throw Exception("Invalid credentials! Try again.")

//            check the password if it matches the encrypted one assigned to the username
            val correctPass = BCryptPasswordEncoder().matches(loginReq.password, userDetails.password)
            if (!correctPass)
                throw Exception("Login Failed. Try again.")

            var jwt: String = jwtUtils!!.generateJwtToken(userDetails)

//            jwt string already contains sub, iat, exp, roles
            returnMap = mapOf("jwt" to jwt)
            status = HttpStatus.OK
        }
        catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.UNAUTHORIZED
        }
        return ResponseEntity(returnMap, status)
    }

    // End point to create administrator accounts, available only to users with the ADMIN role, only works if user has
    // enrolling capability
    @PostMapping("/user/createAdmin")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ENROLLER')")
    fun createAdmin(@RequestBody user: User): ResponseEntity<Map<String, String>> {

        var returnMap: Map<String, String>
        var status: HttpStatus

        val thisUsername = (SecurityContextHolder.getContext().authentication.principal as org.springframework.security.core.userdetails.User).username
        val thisUser = userService.findUserByUsername(thisUsername)

        try {
            val id = userService.createAdmin(user.username, user.password, user.email)
            returnMap = mapOf("id" to id.toString(), "email" to user.email)
            status = HttpStatus.ACCEPTED
        } catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)
    }

    // End point for enrollers to assign roles to users (use this to also enable enrolling capability for other administrators)
    @PostMapping("/user/modify")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ENROLLER')")
    fun modify(@RequestBody modification: RoleModification): ResponseEntity<Map<String, String>> {
        var returnMap: Map<String, String>
        var status: HttpStatus

        val thisUsername = (SecurityContextHolder.getContext().authentication.principal as org.springframework.security.core.userdetails.User).username
        val user = userService.findUserByUsername(thisUsername)

        try {
            val res = userService.modifyRole(modification.username, modification.role, modification.authority)
            if (res == true) {
                returnMap = mapOf("user_updated" to res.toString())
                status = HttpStatus.ACCEPTED
            } else {
                returnMap = mapOf("error_message" to "Error updating role for user ${modification.username}")
                status = HttpStatus.INTERNAL_SERVER_ERROR
            }
        } catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)
    }
}
