package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.model.*
import com.approvers.devlazaApi.repository.MailTokenRepository
import com.approvers.devlazaApi.repository.TokenRepository
import com.approvers.devlazaApi.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.mail.MailException
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid
import kotlin.math.log

@RestController
@RequestMapping("/users")
class UserController(private val userRepository: UserRepository, private val mailTokenRepository: MailTokenRepository, private val tokenRepository: TokenRepository){
    private val charPool:List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    @Autowired
    private lateinit var sender: MailSender
    @GetMapping("/")
    fun getAllUsers(): List<User> = userRepository.findAll()

    @GetMapping("/{id}")
    fun getUserByShowId(@PathVariable(value="id") id: String): ResponseEntity<List<User>>{
        val users: List<User> = userRepository.findByShowId(id)
        if (users.isEmpty()) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(users)
    }

    @GetMapping("/auth/{mailToken}")
    fun authMailToken(@PathVariable(value="mailToken") mailToken: String): ResponseEntity<String>{
        val tokenCache: List<MailToken> = mailTokenRepository.findByToken(mailToken)
        if (tokenCache.isEmpty()) return ResponseEntity.notFound().build()
        val token: MailToken = tokenCache[0]
        val userId: UUID = token.userId
        val user: User = userRepository.findById(userId)[0]
        user.mailAuthorized = 1
        mailTokenRepository.delete(token)
        userRepository.save(user)
        return ResponseEntity.ok("Authorized!!!")
    }

    @PostMapping("/new")
    fun addNewUser(
            @Valid @RequestBody userPoster: UserPoster
    ): User{
        val sameMailAddressChecker: List<User> = userRepository.findByMailAddress(userPoster.mailAddress)
        if (sameMailAddressChecker.isNotEmpty()) return User(
                name="The email address is already used",
                passWord="",
                showId="",
                mailAddress=""
        )

        val newUser = User(
                name=userPoster.name,
                passWord=userPoster.passWord,
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
        return newUser
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
        if (userList.isEmpty()) return ResponseEntity.notFound().build()

        val user: User = userList[0]
        val userId: UUID? = user.id

		if (user.mailAuthorized == 0) return ResponseEntity.badRequest().build()

        if (user.passWord != loginPoster.password) return ResponseEntity.badRequest().build()

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
        return ResponseEntity.badRequest().build()
    }
}