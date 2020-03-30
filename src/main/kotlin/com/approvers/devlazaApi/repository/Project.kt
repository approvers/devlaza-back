package com.approvers.devlazaApi.repository

import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.model.Sites
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProjectsRepository: JpaRepository<Projects, String>{
	fun findById(id:UUID): List<Projects>
	fun findByNameLike(name: String): List<Projects>
	fun findByCreatedUserId(id: String): List<Projects>
	fun findByRecruiting(status: Int): List<Projects>
}

@Repository
interface SitesRepository: JpaRepository<Sites, Long>{
	fun findByProjectId(id: UUID): List<Sites>
}
