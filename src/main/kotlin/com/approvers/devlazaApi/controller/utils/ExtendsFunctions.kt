package com.approvers.devlazaApi.controller.utils

import com.approvers.devlazaApi.errors.BadRequest
import com.approvers.devlazaApi.errors.NotFound
import com.approvers.devlazaApi.model.ProjectMember
import com.approvers.devlazaApi.model.Tags
import com.approvers.devlazaApi.model.TagsToProjectsBridge
import com.approvers.devlazaApi.model.Token
import com.approvers.devlazaApi.repository.ProjectMemberRepository
import com.approvers.devlazaApi.repository.TagsRepository
import com.approvers.devlazaApi.repository.TagsToProjectsBridgeRepository
import com.approvers.devlazaApi.repository.TokenRepository
import java.util.*

fun String.toUUID():UUID{
    val id: UUID
    try{
        id = UUID.fromString(this)
    }catch (e: IllegalArgumentException){
        throw BadRequest("The format of id is invalid")
    }
    return id
}

fun String?.divideToTags(): List<String>{
    val tags: MutableList<String> = this.getTags() ?: return listOf()

    return tags.filterNot { it.isEmpty() }
}

fun String?.getTags(): MutableList<String>?{
    if (this !is String) return null

    val regex = Regex("\\+")
    val tags: MutableList<String>
    tags = if (regex.containsMatchIn(this)){
        this.split("+").toMutableList()
    }else{
        this.split(" ").toMutableList()
    }
    return tags
}

fun TokenRepository.getUserIdFromToken(token: String): UUID{
    val checkedToken: Token = this.checkToken(token)
    return checkedToken.userId
}

fun TokenRepository.checkToken(token: String): Token{
    val tokenList: List<Token> = this.findByToken(token)
    if (tokenList.isEmpty()) throw NotFound("This token is invalid")
    return tokenList[0]
}

fun TagsRepository.createNewTag(tag: String){
    if (this.findByName(tag).isNotEmpty()) return

    val newTag = Tags(name=tag)
    this.save(newTag)
}

fun TagsToProjectsBridgeRepository.addTagToProject(rawTags: String, projectId: UUID, tagsRepository: TagsRepository){
    val tags: List<String> = rawTags.divideToTags()

    for (tag in tags){
        tagsRepository.createNewTag(tag)
        val tmp = TagsToProjectsBridge(tagName=tag, projectId=projectId)
        this.save(tmp)
    }
}

fun ProjectMemberRepository.getProjectMember(userId: UUID, projectId: UUID): ProjectMember{
    val projectMemberSetWithUserId: Set<ProjectMember> = this.findByUserId(userId).toSet()
    val projectMemberSetWithProjectId: Set<ProjectMember> = this.findByProjectId(projectId).toSet()

    val getResultList: List<ProjectMember> = projectMemberSetWithProjectId.intersect(projectMemberSetWithUserId).toList()

    if (getResultList.isEmpty()) throw NotFound("No project members were found for the given information")

    return getResultList[0]
}

fun ProjectMemberRepository.checkProjectMemberExist(userId: UUID, projectId: UUID): Boolean{
    return try {
        this.getProjectMember(userId, projectId)
        true
    }catch (e: NotFound){
        false
    }
}
