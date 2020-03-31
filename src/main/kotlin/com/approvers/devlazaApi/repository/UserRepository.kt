package com.approvers.devlazaApi.repository

import com.approvers.devlazaApi.model.*
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository: JpaRepository<User, String>{
    fun findByName(name: String): List<User>
    fun findByNameLike(name: String): List<User>
    fun findById(id: UUID): List<User>
    fun findByShowId(id: String): List<User>
    fun findByMailAddress(address: String): List<User>
}

interface TokenRepository: JpaRepository<Token, UUID>{
    fun findByToken(token: String): List<Token>
}

interface DevelopExpRepository: JpaRepository<DevelopExp, UUID>{
    fun findByUserId(userId: UUID): List<DevelopExp>
}

interface FollowRepository : JpaRepository<Follow, UUID> {
    fun findByUserId(userId: UUID): List<Follow>
    fun findByFollowingUserId(userId: UUID): List<Follow>
}

interface MailTokenRepository: JpaRepository<MailToken, UUID> {
    fun findByToken(token: String): List<MailToken>
}