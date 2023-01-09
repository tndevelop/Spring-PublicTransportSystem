package it.polito.loginservice.services

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.loginservice.dtos.UserDTO
import it.polito.loginservice.dtos.userToDTO
import it.polito.loginservice.entities.Activation
import it.polito.loginservice.entities.User
import it.polito.loginservice.enums.Authority
import it.polito.loginservice.enums.Role
import it.polito.loginservice.models.SendUser
import it.polito.loginservice.repositories.ActivationRepository
import it.polito.loginservice.repositories.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*


@Service
class UserService(
        val emailService: EmailService,
        val userRepository: UserRepository,
        val activationRepository: ActivationRepository){

    @Value("\${key}")
    lateinit var key: String

    //It returns a provisional random ID
    fun register(username: String, password: String, email: String) : UUID? {
        /*
        checks that no other user with the same name exists,
        generates a random verification token and sends it as part of
        a verification URL via email to the supplied address and persists a new User
        object in the repository
        * */
        val pwEncoder= BCryptPasswordEncoder()

        //CHECK 1: IS THE USER IN THE DB?
        if(userRepository.findByUsername(username)!=null){
            throw IllegalStateException("Username already exists, please log in instead.")
        }

        //CHECK 2: Email already used by another user?
        if(userRepository.findByEmail(email)!=null){
            throw IllegalStateException("Email already in use by an existing user.")
        }

        //CHECK 3: Password satisfies rules?
        var passwordValid = true
        if (password == "" || password.length < 8 || password.contains(" ")) { passwordValid = false  } // Check long enough
        // Check contains 1 of each upper/lower/number/symbol
        if (!password.contains("[A-Z]".toRegex()) || !password.contains("[a-z]".toRegex())) {
            passwordValid = false
        }
        if ( !password.contains("[0-9]".toRegex()) || !password.contains(("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex())) ) {
            passwordValid = false
        }
        if (!passwordValid){
            throw IllegalStateException("Password does not meet requirements")
        }

        //save the User entity
        val userEntity = User(username, email, pwEncoder.encode(password))
        try {
            userRepository.save(userEntity)
        } catch (e: DataIntegrityViolationException) {
            throw DataIntegrityViolationException(e.localizedMessage)
        }

        //save the corresponding Activation entity
        val activation = Activation(userEntity).apply { attemptCounter = 5}
        var id : UUID?
        var code : Int
        activationRepository.save(activation).apply { id= this.id ; code= this.activationCode}

        /*emailService.sendEmail("Verify your email",
            "Hi, ${userEntity.username}!\n\n " +
                "Please verify your email with the following code:\n" +
                "${activation.activationCode}",
                userEntity.email)*/

        // Tell the other services to create a new user
        val claims = Jwts.claims().setSubject(userEntity.username)
        claims["roles"] = arrayListOf("ROLE_CUSTOMER")
        val token = Jwts.builder()
                .setSubject(userEntity.username)
                .setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + 7*24*60*60*1000)) //expTime in days
                .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
                .compact()
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer $token")

        val request = "{\"name\" : \"${userEntity.username}\", \"id\" : \"${userEntity.id}\"}"

        RestTemplate().exchange(
                "http://localhost:8081/my/profile",
                HttpMethod.POST,
                HttpEntity<String>(request, headers),
                String::class.java
        )
       RestTemplate().exchange(
                "http://localhost:8082/my/profile",
                HttpMethod.POST,
                HttpEntity<String>(request, headers),
                String::class.java
        )
        RestTemplate().exchange(
                "http://localhost:8080/my/profile",
                HttpMethod.POST,
                HttpEntity<String>(request, headers),
                String::class.java
        )

        return id

    }


    fun validate(activationId: UUID, activationCode: Int) : UserDTO {
        //searches the User repository for the activation
        //code and updates the corresponding User object, setting it as verified

        val activationCheck= activationRepository.findByIdOrNull(activationId)

        if(activationCheck==null){
            throw IllegalStateException("This Activation Code doesn't exist")
        }
        if(activationCheck.activationCode!=activationCode){
            //Remove activation and user if counter would reach 0
            if (activationRepository.findByIdOrNull(activationId)!!.attemptCounter == 1) {
                userRepository.deleteById(activationCheck.user.id)
                //activationRepository.deleteById(activationId) //already deleted by cascade
            } else {
                // Activation code invalid, decrement attempt counter
                activationRepository.decrementCounter(activationId)
            }
            throw IllegalStateException("This Activation Code is not valid")
        }

        //Check if user exists in repository, shouldn't be possible to not
        val userCheck= userRepository.findByIdOrNull(activationCheck.user.id)
        if(userCheck==null){
            throw IllegalStateException("This user doesn't exist")
        }

        //Activate user
        userRepository.activateUser(activationCheck.user.id)
        activationRepository.deleteById(activationId)

        return userRepository.findByIdOrNull(activationCheck.user.id)!!.userToDTO()
    }

    fun findUserByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    fun createAdmin(username: String, password: String, email: String) : Long? {
        val pwEncoder= BCryptPasswordEncoder()

        //CHECK 1: IS THE USER IN THE DB?
        if(userRepository.findByUsername(username)!=null){
            throw IllegalStateException("Username already exists, please log in instead.")
        }

        //CHECK 2: Email already used by another user?
        if(userRepository.findByEmail(email)!=null){
            throw IllegalStateException("Email already in use by an existing user.")
        }

        //CHECK 3: Password satisfies rules?
        var passwordValid = true
        if (password == "" || password.length < 8 || password.contains(" ")) { passwordValid = false  } // Check long enough
        // Check contains 1 of each upper/lower/number/symbol
        if (!password.contains("[A-Z]".toRegex()) || !password.contains("[a-z]".toRegex())) {
            passwordValid = false
        }
        if ( !password.contains("[0-9]".toRegex()) || !password.contains(("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex())) ) {
            passwordValid = false
        }
        if (!passwordValid){
            throw IllegalStateException("Password does not meet requirements")
        }

        //save the User entity
        val userEntity = User(username, email, pwEncoder.encode(password)).also { it.role = Role.ROLE_ADMIN}
        try {
            var id : Long?
            userRepository.save(userEntity).apply { id = this.id}
            return id
        } catch (e: DataIntegrityViolationException) {
            throw DataIntegrityViolationException(e.localizedMessage)
        }

    }

    fun modifyRole(username: String, role: Role, authority: Authority?) : Boolean {

        val uid = userRepository.findByUsername(username)?.id
        if (uid == null) {return false}

        val res = userRepository.modifyRole(uid, role, authority)
        return res == 1

    }
}