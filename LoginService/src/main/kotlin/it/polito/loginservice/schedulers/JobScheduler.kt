package it.polito.loginservice.schedulers

import it.polito.loginservice.entities.Activation
import it.polito.loginservice.repositories.ActivationRepository
import it.polito.loginservice.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.util.*

@Configuration
@EnableScheduling
class JobScheduler : InterruptedException() {

    @Autowired
    lateinit var activationRepo: ActivationRepository
    @Autowired
    lateinit var userRepo: UserRepository

    @Scheduled(fixedDelay = 3000, initialDelay= 2000)
    fun checkExpiredActivationCode() //: InterruptedException
    {

        println("Scheduler--> it's" + Date())
        var t = Date(System.currentTimeMillis())
        var activationList : MutableIterable<Activation> = activationRepo.findAll()

        for(a in activationList){
            if(a.activationDeadline<t){
                userRepo.delete(a.user)
                activationRepo.delete(a)
            }
        }

  }


}