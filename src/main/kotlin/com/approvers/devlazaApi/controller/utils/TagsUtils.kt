package com.approvers.devlazaApi.controller.utils

import com.approvers.devlazaApi.model.TagsToProjectsBridge
import com.approvers.devlazaApi.model.Tags
import com.approvers.devlazaApi.repository.TagsRepository
import com.approvers.devlazaApi.repository.TagsToProjectsBridgeRepository
import java.util.UUID

class TagsUtils(
		private val tagsRepository: TagsRepository,
		private val tagsToProjectsBridgeRepository: TagsToProjectsBridgeRepository
){
	fun divideTags(rawTags: String?): List<String>{
		val tags = getTags(rawTags) ?: return listOf()
		while (tags.indexOf("") != -1){
			tags.removeAt(tags.indexOf(""))
		}
		return tags.toList()
	}

	fun saveTags(rawTags: String, projectId: UUID){
		val tags: List<String> = divideTags(rawTags)

		for (tag in tags){
			createNewTag(tag)
			val tmp = TagsToProjectsBridge(
					tagName=tag,
					projectId=projectId
			)
			tagsToProjectsBridgeRepository.save(tmp)
		}
	}

	private fun getTags(rawTags: String?): MutableList<String>?{
		if (rawTags !is String) return null

		val regex = Regex("\\+")
		val tags: MutableList<String>
		tags = if (regex.containsMatchIn(rawTags)){
			rawTags.split("+").toMutableList()
		}else{
			rawTags.split(" ").toMutableList()
		}
		return tags
	}

	private fun createNewTag(tag: String){
		if (tagsRepository.findByName(tag).isNotEmpty()) return

		val newTag = Tags(name=tag)
		tagsRepository.save(newTag)
	}

}