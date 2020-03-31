package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.model.MailToken
import com.approvers.devlazaApi.model.Token
import com.approvers.devlazaApi.model.User
import com.approvers.devlazaApi.repository.MailTokenRepository
import com.approvers.devlazaApi.repository.TokenRepository
import com.approvers.devlazaApi.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.Email

@RestController
@RequestMapping("/users")
class UserController(private val userRepository: UserRepository, private val mailTokenRepository: MailTokenRepository, private val tokenRepository: TokenRepository){
    private val charPool:List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    @GetMapping("/")
    fun getAllUsers(): List<User> = userRepository.findAll()

    @GetMapping("/{id}")
    fun getUserByShowId(@PathVariable(value="id") id: String): ResponseEntity<List<User>>{
        val users: List<User> = userRepository.findByShowId(id)
        if (users.isEmpty()) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(users)
    }

    @PatchMapping("/{mailToken}")
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
            @RequestParam(name="name", required=true) name: String,
            @RequestParam(name="passWord", required=true) rawPassWord: String,
            @RequestParam(name="showId") showId: String,
            @Valid @Email @RequestParam(name="mailAddress") mailAddress: String
    ): User{
        val newUser = User(
                name=name,
                passWord=rawPassWord,
                showId=showId,
                mailAddress=mailAddress
        )
        return userRepository.save(newUser)
    }

    @PostMapping("/login")
    fun login(
            @RequestParam(name="address", required=true) address: String,
            @RequestParam(name="password", required=true) password: String
    ): ResponseEntity<String>{
        val userList: List<User> = userRepository.findByMailAddress(address)
        if (userList.isEmpty()) return ResponseEntity.notFound().build()
        val user: User = userList[0]
        val userId: UUID? = user.id
        if (userId is UUID){
            var token: String
            while (true) {
                token = (1..32)
                        .map { kotlin.random.Random.nextInt(0, charPool.size) }
                        .map(charPool::get)
                        .joinToString("")
                if (tokenRepository.findByToken(token).isEmpty()) break
            }
            val generatedToken = Token(token=token, userId=userId)
            tokenRepository.save(generatedToken)
            return ResponseEntity.ok(token)
        }
        return ResponseEntity.badRequest().build()
    }
}