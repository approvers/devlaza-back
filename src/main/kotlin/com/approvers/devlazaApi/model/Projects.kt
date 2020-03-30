package com.approvers.devlazaApi.model

import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Projects(
        @Column(name="name", nullable = false) var name:String,
        @Column(name="introduction") var introduction: String,
        @Id @GeneratedValue var id: Long? = null,
        @Column(name="created_at", nullable = false) var created_at: String = LocalDateTime.now().toString(),
        @Column(name="created_user_id", nullable = false) var createdUserId: String,
        @Column(name="recruiting", nullable = false) var recruiting: Int = 1
): Serializable

data class ProjectPoster(
        var name: String,
        var user_id: String,
        var introduction: String,
        var sites: String,
        var tags: String
)

@Entity
data class Sites(
        @Column(name="explanation", nullable = false) var explanation: String,
        @Column(name="url", nullable = false) var url: String,
        @Id @GeneratedValue @Column(name="id", nullable = false) var id: Long? = null,
        @Column(name="projectId", nullable = false) var projectId: Long
): Serializable

data class SitesPoster(
        var url: String,
        var explanation: String,
        var projectId: Long
)

@Entity
data class Tags(
        @Id @Column(name="id", nullable = false) var id: Long,
        @Column(name="name", nullable = false) var name: String
): Serializable

@Entity
data class TagsToProjects(
        @Column(name="project_id", nullable = false) var project_id: String,
        @Column(name="tag_id", nullable = false) var tag_id: String,
        @Id @Column(name="id", nullable = false) var id: Long
): Serializable

@Entity
data class favorite(
        @Column(name="user_id", nullable = false) var user_id: String,
        @Column(name="project_id", nullable = false) var project_id: String,
        @Id @Column(name="id", nullable = false) var id: Long
)

