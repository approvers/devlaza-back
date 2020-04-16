package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.errors.BadRequest
import com.approvers.devlazaApi.errors.NotFound
import com.approvers.devlazaApi.errors.UnAuthorized
import com.approvers.devlazaApi.model.LoginPoster
import com.approvers.devlazaApi.model.MailToken
import com.approvers.devlazaApi.model.ProjectMember
import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.model.User
import com.approvers.devlazaApi.model.UserPoster
import com.approvers.devlazaApi.repository.MailTokenRepository
import com.approvers.devlazaApi.repository.ProjectMemberRepository
import com.approvers.devlazaApi.repository.ProjectsRepository
import com.approvers.devlazaApi.repository.UserRepository
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.mail.MailException
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.UnsupportedEncodingException
import java.util.Date
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/users")
class UserController(
    private val userRepository: UserRepository,
    private val mailTokenRepository: MailTokenRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val projectsRepository: ProjectsRepository,
    @Autowired private val sender: MailSender,
    @Value("\${boot_env}") private val bootEnv: String
) {
    private val secret: String = System.getenv("secret") ?: "secret"
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)
    private val verifier: JWTVerifier = JWT.require(algorithm).build()

    @GetMapping("")
    fun getAllUsers(): ResponseEntity<List<User>> {
        val users: List<User> = userRepository.findAll()
        if (users.isEmpty()) return ResponseEntity.noContent().build()
        return ResponseEntity.ok(users)
    }

    @GetMapping("/{id}")
    fun getUserByShowId(@PathVariable(value = "id") id: String): ResponseEntity<User> {
        val user = userRepository.findByShowId(id).singleOrNull() ?: throw NotFound("No users matching id found")
        return ResponseEntity.ok(user)
    }

    @GetMapping("/auth/{mailToken}")
    fun authMailToken(@PathVariable(value = "mailToken") mailToken: String): ResponseEntity<Unit> {
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

    @PostMapping("")
    fun addNewUser(
        @Valid @RequestBody userPoster: UserPoster
    ): ResponseEntity<User> {
        val sameMailAddressChecker: List<User> = userRepository.findByMailAddress(userPoster.mailAddress)
        if (sameMailAddressChecker.isNotEmpty()) throw BadRequest("The email address is already in use.")

        val newUser = User(
            name = userPoster.name,
            passWord = userPoster.password,
            showId = userPoster.showId,
            mailAddress = userPoster.mailAddress
        )

        var token: String

        userRepository.save(newUser)

        while (true) {
            token = createMailToken()
            if (mailTokenRepository.findByToken(token).isEmpty()) break
        }


        val mailToken = MailToken(
            token = token,
            userId = newUser.id!!
        )

        mailTokenRepository.save(mailToken)
        val message = SimpleMailMessage()
        message.setFrom("ufiapprovers@gmail.com")
        message.setTo(userPoster.mailAddress)
        message.setText("このURLから認証を完了してください\nhttp://localhost:8080/users/auth/$token")
        try {
            sender.send(message)
        } catch (e: MailException) {
            println(e)
        }
        return ResponseEntity.ok(newUser)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginPoster: LoginPoster
    ): ResponseEntity<String> {
        val userList: List<User> = userRepository.findByMailAddress(loginPoster.address)
        if (userList.isEmpty()) throw NotFound("Could not find user from email address")

        val user: User = userList[0]

        if (user.mailAuthorized == 0) throw BadRequest("The email address is not authenticated")

        if (user.passWord != loginPoster.password) throw UnAuthorized("Password invalid")

        val token: String = createToken(user.name, user.id!!)
        return ResponseEntity.ok(token)
    }

    @DeleteMapping("")
    fun deleteUser(
        @RequestBody tokenPoster: TokenPoster
    ): ResponseEntity<Unit> {
        val token: String = tokenPoster.token

        val userID: UUID = decode(token)
        val user: User = userRepository.findById(userID).singleOrNull()
            ?: throw NotFound("user not found with given token")

        deleteRelatedData(userID)

        userRepository.delete(user)
        return ResponseEntity.ok().build()
    }

    private fun deleteRelatedData(userID: UUID) {

        val projectsMemberInThisUser: List<ProjectMember> = projectMemberRepository.findByUserId(userID)

        for (projectMember in projectsMemberInThisUser) {
            projectMemberRepository.delete(projectMember)
        }

        val deleteUserHaveProjects: List<Projects> = projectsRepository.findByCreatedUserId(userID)

        for (project in deleteUserHaveProjects) {
            projectsRepository.delete(project)
        }
    }

    private fun createMailToken() = (1..32)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")


    private fun createToken(userName: String, userId: UUID): String {
        val issuedAt = Date()
        val algorithm: Algorithm = Algorithm.HMAC256(secret)
        val id: UUID = UUID.randomUUID()
        return JWT.create()
            .withIssuer("Approvers")
            .withIssuedAt(issuedAt)
            .withJWTId(id.toString())
            .withClaim("USER_ID", userId.toString())
            .withClaim("USER_name", userName)
            .sign(algorithm)
    }

    private fun decode(token: String): UUID {
        val userId: UUID
        try {
            val decodedJWT: DecodedJWT = verifier.verify(token)

            userId = UUID.fromString(
                decodedJWT.getClaim("USER_ID").asString()
            )
        } catch (e: Exception) {
            when (e) {
                is UnsupportedEncodingException, is JWTVerificationException, is IllegalArgumentException
                -> throw BadRequest("token is invalid")
                else -> throw e
            }
        }

        return userId
    }

    data class TokenPoster(val token: String)
}
