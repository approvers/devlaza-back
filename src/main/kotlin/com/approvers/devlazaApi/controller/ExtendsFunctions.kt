package com.approvers.devlazaApi.controller

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

    while (tags.indexOf("") != -1){
        tags.removeAt(tags.indexOf(""))
    }

    return tags.toList()
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
    val checkedToken: Token = this.tokenCheck(token)
    return checkedToken.userId
}

fun TokenRepository.tokenCheck(token: String): Token{
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

fun ProjectMemberRepository.getProjectMember(userId: UUID, projectId: UUID): MutableList<ProjectMember>{
    val projectMemberList: List<ProjectMember> =  this.findAll()
    val result: MutableList<ProjectMember> = mutableListOf()

    for (projectMember in projectMemberList){
        if (projectId == projectMember.projectId && userId == projectMember.userId) result.add(projectMember)
    }
    return result
}
