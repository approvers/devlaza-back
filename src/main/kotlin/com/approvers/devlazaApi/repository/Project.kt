package com.approvers.devlazaApi.repository

import com.approvers.devlazaApi.model.ProjectMember
import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.model.Sites
import com.approvers.devlazaApi.model.Tags
import com.approvers.devlazaApi.model.TagsToProjectsBridge
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProjectsRepository : JpaRepository<Projects, String> {
    fun findById(id: UUID): List<Projects>
    fun findByNameLike(name: String): List<Projects>
    fun findByCreatedUserId(createdUserId: UUID): List<Projects>
    fun findByRecruiting(status: Int): List<Projects>
}

@Repository
interface SitesRepository : JpaRepository<Sites, Long> {
    fun findByProjectId(id: UUID): List<Sites>
}

@Repository
interface TagsRepository : JpaRepository<Tags, String> {
    fun findByName(name: String): List<Tags>
}

@Repository
interface TagsToProjectsBridgeRepository : JpaRepository<TagsToProjectsBridge, String> {
    fun findByProjectId(id: UUID): List<TagsToProjectsBridge>
}

@Repository
interface ProjectMemberRepository : JpaRepository<ProjectMember, String> {
    fun findByUserId(id: UUID): List<ProjectMember>
    fun findByProjectId(projectId: UUID): List<ProjectMember>
    fun findByProjectIdAndUserId(projectId: UUID, userId: UUID): List<ProjectMember>
}
