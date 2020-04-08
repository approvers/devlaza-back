package com.approvers.devlazaApi.controller.search.project

import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.model.TagsToProjectsBridge
import com.approvers.devlazaApi.repository.ProjectsRepository
import com.approvers.devlazaApi.repository.TagsToProjectsBridgeRepository
import java.util.UUID

class SearchWithTags : ProjectSearcherWithOtherRepository<List<String>, TagsToProjectsBridgeRepository> {
    override fun search(projectsSet: Set<Projects>, param: List<String>, repository: ProjectsRepository, secondaryRepository: TagsToProjectsBridgeRepository): Set<Projects> {
        if (param.isEmpty()) return projectsSet

        val result: MutableSet<Projects> = mutableSetOf()
        for (project in projectsSet) {
            val projectsTags: MutableList<TagsToProjectsBridge> = secondaryRepository.getTagsBridgeWithProjectID(project.id!!)
            if (checkTagsMatchProject(param, projectsTags)) result.add(project)
        }

        return projectsSet.intersect(result)
    }

    private fun TagsToProjectsBridgeRepository.getTagsBridgeWithProjectID(projectId: UUID): MutableList<TagsToProjectsBridge> {
        val result: MutableList<TagsToProjectsBridge> = mutableListOf()

        val allTags: List<TagsToProjectsBridge> = this.findAll()
        for (tag in allTags) {
            if (tag.projectId == projectId) result.add(tag)
        }

        return result
    }

    private fun checkTagsMatchProject(tags: List<String>, projectTags: MutableList<TagsToProjectsBridge>): Boolean {
        var tagsCount: Int = tags.size

        for (tagsToProjectBridge in projectTags) {
            val tagName: String = tagsToProjectBridge.tagName
            for (tag in tags) {
                if (tag == tagName) {
                    tagsCount--
                    break
                }
            }
        }

        return tagsCount == 0
    }
}
