package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.errors.BadRequest
import com.approvers.devlazaApi.errors.NotFound
import com.approvers.devlazaApi.model.AuthPoster
import com.approvers.devlazaApi.model.User
import com.approvers.devlazaApi.repository.UserRepository
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.UnsupportedEncodingException
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
class AuthController(
        private val userRepository: UserRepository
) {
    private val secret: String = System.getenv("secret") ?: "secret"
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)
    private val verifier: JWTVerifier = JWT.require(algorithm).build()

    @PostMapping("/")
    fun getUserInfo(@Valid @RequestBody authPoster: AuthPoster): User {
        val token: String = authPoster.token

        val userId: UUID = decode(token)

        val tmp: List<User> = userRepository.findById(userId)

        if (tmp.isEmpty()) throw NotFound("No users were found for that token")

        return tmp[0]
    }


    private fun decode(token: String): UUID{
        val userId: UUID
        try{
            val decodedJWT: DecodedJWT = verifier.verify(token)

            userId = UUID.fromString(
                    decodedJWT.getClaim("USER_ID").asString()
            )
        }catch (e: Exception){
            when(e){
                is UnsupportedEncodingException, is JWTVerificationException
                -> throw BadRequest("token is invalid")
                else -> throw e
            }
        }

        return userId
    }
}