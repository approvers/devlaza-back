package com.approvers.devlazaApi.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class TokenGenerator {
    private val secret: String = System.getenv("secret") ?: "secret"

    fun generate(userName: String, userId: UUID): String{
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
}