package it.polito.paymentservice.controllers

import it.polito.paymentservice.entities.Transaction
import it.polito.paymentservice.services.TransactionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
class AdminController(val transactionService: TransactionService) {

    //Accessible only to users with the admin role
    @GetMapping("/admin/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun getTransactions(@RequestParam(required = false) dateRange : String?) : ResponseEntity<Map<String, Any>> {
        var returnMap : Map<String, Any>
        var status = HttpStatus.OK
        try{
            val transactions: Flow<Transaction>

            if (dateRange != null) {
                val dates = dateRange.split("-")
                val dateFrom = LocalDate.parse(dates[0], DateTimeFormatter.ofPattern("d/MM/yyyy"))
                val dateTo = LocalDate.parse(dates[1], DateTimeFormatter.ofPattern("d/MM/yyyy"))
                println("$dateFrom to $dateTo" )

                transactions = transactionService.getAllInRange(dateFrom, dateTo)
            } else {
                transactions = transactionService.getAll()
            }

            returnMap = mapOf("transactions" to transactions.toList())

        } catch (e: Exception) {
            returnMap = mapOf("error" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)
    }

    //Accessible only to users with the admin role
    @GetMapping("/admin/transactions/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun getTransactionsByUser(@RequestParam(required = false) dateRange : String?, @PathVariable userId: Int) : ResponseEntity<Map<String, Any>> {
        var returnMap : Map<String, Any>
        var status = HttpStatus.OK
        try{
            val transactions: Flow<Transaction>

            if (dateRange != null) {
                val dates = dateRange.split("-")
                val dateFrom = LocalDate.parse(dates[0], DateTimeFormatter.ofPattern("d/MM/yyyy"))
                val dateTo = LocalDate.parse(dates[1], DateTimeFormatter.ofPattern("d/MM/yyyy"))
                println("$dateFrom to $dateTo" )

                transactions = transactionService.findAllByUserIdInRange(userId.toLong(), dateFrom, dateTo)
            } else {
                transactions = transactionService.findAllById(userId.toLong())
            }

            returnMap = mapOf("transactions" to transactions.toList())

        } catch (e: Exception) {
            returnMap = mapOf("error" to e.message!!)
            status = HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(returnMap, status)
    }
}