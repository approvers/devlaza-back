package com.approvers.devlazaApi.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import java.util.UUID

private const val EXPIRE_TIME = 5 * 60 * 1000
private val SECRET = System.getenv("SECRET") ?: "secret"

private const val ISSUER = "devlaza"
private const val SUBJECT = "token"

private val ALGORITHM = Algorithm.HMAC256(SECRET)
private val verifier = JWT.require(ALGORITHM)
        .withIssuer(ISSUER)
        .withSubject(SUBJECT)
        .build()

private object ClaimKeys {
    const val USERNAME = "username"
    const val USER_ID = "user_id"
}

fun LoginUser.sign(): String? {
    val expireDate = Date(System.currentTimeMillis() + EXPIRE_TIME)
    return try {
        val algorithm = Algorithm.HMAC256(this.password)
        JWT.create()
                .withSubject(this.mailAddress)
                .withIssuer(ISSUER)
                .withSubject(SUBJECT)
                .withAudience(this.mailAddress)
                .withClaim(ClaimKeys.USERNAME, this.username)
                .withClaim(ClaimKeys.USER_ID, "${this.id}")
                .withIssuedAt(Date())
                .withExpiresAt(expireDate)
                .sign(algorithm)
    } catch (ex: Exception) {
        null
    }
}

fun String.verifyToken(mailAddress: String): Boolean {
    return try {
        val decoded = verifier.verify(this)
        mailAddress in decoded.audience
    } catch (ex: Exception) {
        false
    }
}

val String.userId: UUID?
    get() = try {
        val jwt = JWT.decode(this)
        val id = jwt.getClaim(ClaimKeys.USER_ID).asString()
        UUID.fromString(id)
    } catch (ex: Exception) {
        null
    }

val String.username: String?
    get() = try {
        val jwt = JWT.decode(this)
        jwt.getClaim(ClaimKeys.USERNAME).asString()
    } catch (ex: Exception) {
        null
    }

val String.mailAddress: String?
    get() = try {
        val jwt = JWT.decode(this)
        jwt.audience.single()
    } catch (ex: Exception) {
        null
    }