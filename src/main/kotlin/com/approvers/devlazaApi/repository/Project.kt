package com.approvers.devlazaApi.repository

import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.model.Sites
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectsRepository: JpaRepository<Projects, String>

@Repository
interface SitesRepository: JpaRepository<Sites, Long>
