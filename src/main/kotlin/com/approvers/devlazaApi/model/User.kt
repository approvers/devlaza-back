package com.approvers.devlazaApi.model

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class User(
		@Id @GeneratedValue(generator="uuid2") @GenericGenerator(name="uuid2", strategy="uuid2") @Column(columnDefinition="BINARY(16)") var id: UUID? = null,
		@Column(name="name", nullable=false) var name: String,
		@Column(name="birthDay", nullable=false) var birthDay: LocalDateTime,
		@Column(name="bio", nullable=false) var bio:String = "",
		@Column(name="favoriteLang", nullable=false) var favoriteLang: String,
		@Column(name="passWord", nullable=false) var passWord: String
)

@Entity
data class Token(
		@Column(name="token") var token: String,
		@Column(name="user_id") var userId:UUID,
		@Id @GeneratedValue(generator="uuid2") @GenericGenerator(name="uuid2", strategy="uuid2") @Column(columnDefinition="BINARY(16)") var id: UUID
)

@Entity
data class DevelopExp(
		@Id @GeneratedValue(generator="uuid2") @GenericGenerator(name="uuid2", strategy="uuid2") @Column(columnDefinition="BINARY(16)") var id: UUID,
		@Column(name="userId") var userId: UUID,
		@Column(name="caption") var caption: String
)

@Entity
data class Follow(
		@Id @GeneratedValue(generator="uuid2") @GenericGenerator(name="uuid2", strategy="uuid2") @Column(columnDefinition="BINARY(16)") var id: UUID,
		@Column(name="user_id") var userId: Int,
		@Column(name="following_id") var followingId: Int
)
