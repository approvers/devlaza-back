package com.approvers.devlazaApi.model

import org.hibernate.annotations.GenericGenerator
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Index

@Entity
@Table(indexes = [
    Index(columnList="name"),
    Index(columnList="recruiting")
])
data class Projects(
        @Column(name="name", nullable = false) var name:String,
        @Column(name="introduction") var introduction: String,
        @Id @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2") @Column(columnDefinition = "BINARY(16)") var id: UUID? = null,
        @Column(name="created_at", nullable = false) var created_at: LocalDateTime = LocalDateTime.now(),
        @Column(name="created_user_id", nullable = false) var createdUserId: UUID? = null,
        @Column(name="recruiting", nullable = false) var recruiting: Int = 1
): Serializable

data class ProjectPoster(
        var name: String,
        var token: String,
        var introduction: String,
        var sites: String,
        var tags: String
)

@Entity
@Table(indexes = [
    Index(columnList="url"),
    Index(columnList="project_id")
])
data class Sites(
        @Column(name="explanation", nullable = false) var explanation: String,
        @Column(name="url", nullable = false) var url: String,
        @Id @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2") @Column(columnDefinition = "BINARY(16)") var id: UUID? = null,
        @Column(name="projectId", nullable = false) var projectId: UUID
): Serializable

data class SitesPoster(
        var url: String,
        var explanation: String,
        var projectId: UUID
)

@Entity
@Table(indexes = [Index(name="tags_index", columnList="NAME", unique=true)])
data class Tags(
        @Id @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2") @Column(columnDefinition = "BINARY(16)") var id: UUID? = null,
        @Column(name="name", nullable = false) var name: String
): Serializable

@Entity
@Table(indexes = [Index(name="tags_to_project_index", columnList="PROJECT_ID, TAG_NAME", unique=true)])
data class TagsToProjectsBridge(
        @Column(name="project_id", nullable = false) var projectId: UUID,
		@Column(name="tag_name", nullable = false) var tagName: String,
        @Id @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2") @Column(columnDefinition = "BINARY(16)") var id: UUID? = null
): Serializable

@Entity
@Table(indexes = [Index(name="favorite_index", columnList="USER_ID, PROJECT_ID", unique=true)])
data class Favorite(
		@Id @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2") @Column(columnDefinition = "BINARY(16)") var id: UUID? = null,
        @Column(name="user_id", nullable = false) var user_id: String,
        @Column(name="project_id", nullable = false) var project_id: String
)

@Entity
@Table(indexes = [Index(name="project_member_index", columnList="PROJECT_ID, USER_ID", unique=true)])
data class ProjectMember(
		@Id @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2") @Column(columnDefinition = "BINARY(16)") var id: UUID? = null,
		@Column(name="project_id", nullable=false) var projectId: UUID,
		@Column(name="user_id", nullable=false) var userId: UUID
)
