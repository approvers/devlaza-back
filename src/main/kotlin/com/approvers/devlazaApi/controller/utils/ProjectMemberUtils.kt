package com.approvers.devlazaApi.controller.utils

import com.approvers.devlazaApi.model.ProjectMember
import com.approvers.devlazaApi.repository.ProjectMemberRepository
import java.util.*

class ProjectMemberUtils(
		private val projectMemberRepository: ProjectMemberRepository
) {
	fun checkProjectMemberExists(userId: UUID, projectId: UUID): Boolean{
		return getProjectMember(userId, projectId).isNotEmpty()
	}

	fun getProjectMember(userId: UUID, projectId: UUID): MutableList<ProjectMember>{
		val projectMemberList: List<ProjectMember> = projectMemberRepository.findAll()
		val result: MutableList<ProjectMember> = mutableListOf()
		for (projectMember in projectMemberList){
			if (projectId == projectMember.projectId && userId == projectMember.userId) result.add(projectMember)
		}

		return result
	}
}