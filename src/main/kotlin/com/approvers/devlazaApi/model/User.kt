package com.approvers.devlazaApi.model

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Index
import javax.validation.constraints.Email

@Entity
@Table(indexes = [Index(name="member_index", columnList="NAME, ID", unique=true)])
data class User(
		@Id @GeneratedValue(generator="uuid2") @GenericGenerator(name="uuid2", strategy="uuid2") @Column(columnDefinition="BINARY(16)") var id: UUID? = null,
		@Column(name="name", nullable=false) var name: String,
		@Column(name="birthDay", nullable=false) var birthDay: LocalDateTime? = LocalDateTime.of(1900, 1, 1, 0, 0, 0),
		@Column(name="bio", nullable=false) var bio:String = "",
		@Column(name="favoriteLang", nullable=false) var favoriteLang: String = "",
		@Column(name="passWord", nullable=false) var passWord: String,
		@Column(name="showId", nullable=false) var showId: String,
		@Column(name="mailAuthorized", nullable=false) var mailAuthorized: Int = 0,
		@Column(name="mailAddress", nullable=false) var mailAddress: String
)

@Entity
data class Token(
		@Column(name="token") var token: String,
		@Column(name="userId") var userId:UUID,
		@Id @GeneratedValue(generator="uuid2") @GenericGenerator(name="uuid2", strategy="uuid2") @Column(columnDefinition="BINARY(16)") var id: UUID? = null
)

@Entity
@Table(indexes = [Index(name="develop_exp_index", columnList="USERID", unique=true)])
data class DevelopExp(
		@Id @GeneratedValue(generator="uuid2") @GenericGenerator(name="uuid2", strategy="uuid2") @Column(columnDefinition="BINARY(16)") var id: UUID? = null,
		@Column(name="user_id") var userId: UUID,
		@Column(name="caption") var caption: String
)

@Entity
@Table(indexes = [Index(columnList="USERID, following_user_id", unique=true)])
data class Follow(
		@Id @GeneratedValue(generator="uuid2") @GenericGenerator(name="uuid2", strategy="uuid2") @Column(columnDefinition="BINARY(16)") var id: UUID? = null,
		@Column(name="userId") var userId: UUID,
		@Column(name="following_user_id") var followingUserId: UUID
)

@Entity
@Table(indexes = [Index(columnList="token", unique=true)])
data class MailToken(
		@Id @GeneratedValue(generator="uuid2") @GenericGenerator(name="uuid2", strategy="uuid2") @Column(columnDefinition="BINARY(16)") var id: UUID? = null,
		@Column(name="user_id", nullable=false) var userId: UUID,
		@Column(name="token", nullable=false) var token: String
)

data class UserPoster(
		var name: String,
		var password: String,
		var showId: String,
		@Email var mailAddress: String
)

data class LoginPoster(
		var address: String,
		var password: String
)

data class AuthPoster(
		var token: String
)
