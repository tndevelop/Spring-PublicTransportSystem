package it.polito.travelerservice.controllers

import it.polito.travelerservice.entities.UserDetails
import it.polito.travelerservice.services.AppUserDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.*

@RestController
class ProfileController {

    @Autowired
    private var appUserDetailsService: AppUserDetailsService? = null

    @GetMapping("/my/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    fun getProfile() : ResponseEntity<Map<String, String>> {

        val username = (SecurityContextHolder.getContext().authentication.principal as User).username

        var returnMap: Map<String, String>
        var status: HttpStatus
        try {
            val user = appUserDetailsService?.getProfile(username)
            if (user != null) {
                returnMap = mapOf("name" to user.name, "address" to user.address, "date_of_birth"  to user.dateOfBirth.toString(),
                        "telephone_number" to user.telephoneNumber, "id" to user.id.toString(), "age" to user.age.toString())
                status = HttpStatus.ACCEPTED
            } else {
                returnMap = mapOf("message" to "User not found")
                status = HttpStatus.NO_CONTENT
            }
        } catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }

        return ResponseEntity(returnMap, status)
    }

    @PutMapping("/my/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    fun updateProfile(@RequestBody user: UserDetails) : ResponseEntity<Map<String, String>> {

        var returnMap: Map<String, String>
        var status: HttpStatus

        // Check the username of user = username in authentication token
        val username = (SecurityContextHolder.getContext().authentication.principal as User).username
        if (username != user.name) {
            returnMap = mapOf("message" to "You cannot change your username.")
            status = HttpStatus.BAD_REQUEST
            return ResponseEntity(returnMap, status)
        }

        try {
            val returnCode = appUserDetailsService?.updateProfile(user)
            if (returnCode == 1) {
                returnMap = mapOf("message" to "User details updated")
                status = HttpStatus.ACCEPTED
            } else {
                returnMap = mapOf("message" to "User not found")
                status = HttpStatus.NO_CONTENT
            }
        } catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }

        return ResponseEntity(returnMap, status)
    }

    @PostMapping("/my/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    fun saveProfile(@RequestBody user: UserDetails) : ResponseEntity<Map<String, String>> {

        var returnMap: Map<String, String>
        var status: HttpStatus

        try {
            appUserDetailsService?.saveProfile(user)
            returnMap = mapOf("message" to "User saved.")
            status = HttpStatus.OK
        } catch (e: Exception) {
            returnMap = mapOf("error_message" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }

        return ResponseEntity(returnMap, status)
    }
}