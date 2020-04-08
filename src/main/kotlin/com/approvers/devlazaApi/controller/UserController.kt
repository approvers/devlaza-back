package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.errors.BadRequest
import com.approvers.devlazaApi.errors.NotFound
import com.approvers.devlazaApi.model.User
import com.approvers.devlazaApi.model.MailToken
import com.approvers.devlazaApi.model.UserPoster
import com.approvers.devlazaApi.model.LoginPoster
import com.approvers.devlazaApi.model.Token
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import com.approvers.devlazaApi.repository.MailTokenRepository
import com.approvers.devlazaApi.repository.TokenRepository
import com.approvers.devlazaApi.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.mail.MailException
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/users")
class UserController(
        private val userRepository: UserRepository,
        private val mailTokenRepository: MailTokenRepository,
        private val tokenRepository: TokenRepository,
        @Autowired private val sender: MailSender
){
    private val charPool:List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    @GetMapping("/")
    fun getAllUsers():List<User> = userRepository.findAll()

    @GetMapping("/{id}")
    fun getUserByShowId(@PathVariable(value="id") id: String): ResponseEntity<User>{
        val user = userRepository.findByShowId(id).singleOrNull() ?: throw NotFound("No users matching id found")
        return ResponseEntity.ok(user)
    }

    @GetMapping("/auth/{mailToken}")
    fun authMailToken(@PathVariable(value="mailToken") mailToken: String): ResponseEntity<Unit>{
        val tokenCache: List<MailToken> = mailTokenRepository.findByToken(mailToken)
        if (tokenCache.isEmpty()) NotFound("This mail token not found")
        val token: MailToken = tokenCache[0]
        val userId: UUID = token.userId
        val user: User = userRepository.findById(userId)[0]
        user.mailAuthorized = 1
        mailTokenRepository.delete(token)
        userRepository.save(user)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/")
    fun addNewUser(
            @Valid @RequestBody userPoster: UserPoster
    ): ResponseEntity<User>{
        val sameMailAddressChecker: List<User> = userRepository.findByMailAddress(userPoster.mailAddress)
        if (sameMailAddressChecker.isNotEmpty()) throw BadRequest("The email address is already in use.")

        val newUser = User(
                name=userPoster.name,
                passWord=userPoster.password,
                showId=userPoster.showId,
                mailAddress=userPoster.mailAddress
        )

        var token: String
        while (true){
            token = createToken()
            if (mailTokenRepository.findByToken(token).isEmpty()) break
        }

        userRepository.save(newUser)

        val mailToken = MailToken(
                token=token,
                userId= newUser.id!!
        )

        mailTokenRepository.save(mailToken)
        val message = SimpleMailMessage()
        message.setFrom("ufiapprovers@gmail.com")
        message.setTo(userPoster.mailAddress)
        message.setText("このURLから認証を完了してください\nhttp://localhost:8080/users/auth/$token")
        try{
            sender.send(message)
        }catch (e: MailException){
            println(e)
        }
        return ResponseEntity.ok(newUser)
    }

    fun createToken() = (1..32)
                    .map { kotlin.random.Random.nextInt(0, charPool.size) }
                    .map(charPool::get)
                    .joinToString("")

    @PostMapping("/login")
    fun login(
            @Valid @RequestBody loginPoster: LoginPoster
    ): ResponseEntity<String>{
        val userList: List<User> = userRepository.findByMailAddress(loginPoster.address)
        if (userList.isEmpty()) throw NotFound("Could not find user from email address")

        val user: User = userList[0]
        val userId: UUID? = user.id

		if (user.mailAuthorized == 0) throw BadRequest("The email address is not authenticated")

        if (user.passWord != loginPoster.password) throw BadRequest("Password invalid")

        if (userId is UUID){
            var token: String
            while (true) {
                token = createToken()
                if (tokenRepository.findByToken(token).isEmpty()) break
            }
            val generatedToken = Token(token=token, userId=userId)
            tokenRepository.save(generatedToken)
            return ResponseEntity.ok(token)
        }
        throw NotFound("Could not find user id")
    }
}
