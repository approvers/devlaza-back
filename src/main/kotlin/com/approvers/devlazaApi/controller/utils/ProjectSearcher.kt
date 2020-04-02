package com.approvers.devlazaApi.controller.utils

import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.model.TagsToProjectsBridge
import com.approvers.devlazaApi.repository.ProjectsRepository
import com.approvers.devlazaApi.repository.TagsToProjectsBridgeRepository
import java.util.*

class ProjectSearcher(private var projects: Set<Projects>, private val projectsRepository: ProjectsRepository, private val tagsToProjectsBridgeRepository: TagsToProjectsBridgeRepository){
	fun withKeyWord(keyword: String?){
		if(keyword !is String) return

		val keywords: List<String> = keyword.split(" ")

		for (key in keywords) {
			val searchResults: Set<Projects> = projectsRepository.findByNameLike("%$key%").toSet()
			projects = projects.intersect(searchResults)
		}
	}

	fun withUser(username: String?){
		if(username !is String) return
		val userResult: Set<Projects> = projectsRepository.findByCreatedUserId("$username").toSet()
		projects = projects.intersect(userResult)
	}

	fun withTags(tags: List<String>){
		if (tags.isEmpty()) return

		val result: MutableSet<Projects> = mutableSetOf()
		for (project in projects){
			val projectTags:MutableList<TagsToProjectsBridge> = getTagsBridgeWithProjectID(project.id!!)

			if(checkTagsMatchProject(tags, projectTags)) result.add(project)
		}
		projects = projects.intersect(result)
	}

	fun withRecruiting(recruiting: Int){
		if (recruiting == 1 || recruiting == 0){
			val recruitingResult: Set<Projects> = projectsRepository.findByRecruiting(recruiting).toSet()
			projects = projects.intersect(recruitingResult)
		}
	}

	fun decideSort(sortOrder: String?){
		val projectsList: MutableList<Projects> = projects.toMutableList()
		when(sortOrder){
			"asc" -> projectsList.sortBy{it.created_at}
			"desc" -> {
				projectsList.sortBy{it.created_at}
				projectsList.reverse()
			}
			else -> projectsList.sortBy{it.created_at}
		}
		projects = projectsList.toSet()
	}

	fun getResult(): List<Projects>{
		return projects.toMutableList()
	}

	private fun getTagsBridgeWithProjectID(projectId: UUID): MutableList<TagsToProjectsBridge>{
		val result: MutableList<TagsToProjectsBridge> = mutableListOf()

		val allTags: List<TagsToProjectsBridge> = tagsToProjectsBridgeRepository.findAll()
		for (tag in allTags){
			if (tag.projectId == projectId) result.add(tag)
		}

		return result
	}

	private fun checkTagsMatchProject(tags: List<String>, projectTags: MutableList<TagsToProjectsBridge>): Boolean{
		var tagsCount: Int = tags.size

		for (tagsToProjectBridge in projectTags){
			val tagName: String = tagsToProjectBridge.tagName
			for (tag in tags){
				if (tag == tagName){
					tagsCount--
					break
				}
			}
		}

		return tagsCount == 0
	}
}