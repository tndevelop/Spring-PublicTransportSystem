package it.polito.loginservice.repositories

import it.polito.loginservice.entities.User
import it.polito.loginservice.enums.Authority
import it.polito.loginservice.enums.Role
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserRepository : CrudRepository<User, Long>{

    fun findByUsername(username: String) : User?

    fun findByEmail(email: String) : User?

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.active = TRUE WHERE u.id = ?1")
    fun activateUser(id: Long): Int

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.role = ?2, u.authority = ?3 WHERE u.id = ?1")
    fun modifyRole(id: Long, role: Role, authority: Authority?): Int

}