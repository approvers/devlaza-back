package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.model.*
import com.approvers.devlazaApi.repository.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/projects")
class ProjectsController(
        private val projectsRepository: ProjectsRepository,
        private val sitesController: SitesController,
        private val tagsToProjectsBridgeRepository: TagsToProjectsBridgeRepository,
        private val tagsRepository: TagsRepository,
        private val tokenRepository: TokenRepository,
        private val userRepository: UserRepository
){
    @GetMapping("/")
    fun getAllProjects(): List<Projects> = projectsRepository.findAll()

    @PostMapping("/")
    fun createNewProject(@Valid @RequestBody rawData: ProjectPoster): Projects{
        val token: Token = tokenCheck(rawData.token) ?: return Projects(name="invalid token", introduction="")
        val projects = Projects(
                name = rawData.name,
                introduction = rawData.introduction,
                createdUserId = token.userId
        )

        projectsRepository.save(projects)

        saveSites(rawData.sites, projects.id!!)

        saveTags(rawData.tags, projects.id!!)

        return projects
    }

    // TODO: tag検索と時間での絞り込みの実装、Userテーブルとの連携
    @GetMapping("/condition")
    fun searchWithConditions(
            @RequestParam(name="keyword", defaultValue="#{null}") keyword: String?,
            @RequestParam(name="count", defaultValue="100") rawCount: Int?,
            @RequestParam(name="user", defaultValue="#{null}") user:String?,
            @RequestParam(name="tags", defaultValue="#{null}") rawTags: String?,
            @RequestParam(name="sort", defaultValue="asc") sortOrder: String,
            @RequestParam(name="recruiting", defaultValue="1") rawRecruiting: String?
    ): ResponseEntity<List<Projects>>{
        val recruiting: Int = if(rawRecruiting != null && rawRecruiting.toIntOrNull() != null) {
            rawRecruiting.toInt()
        }else{
            1
        }
        val tags: List<String> = divideTags(rawTags)

        val searchProject = SearchProject(
                projectsRepository.findAll().toSet(),
                projectsRepository,
                tagsToProjectsBridgeRepository
        )
        searchProject.withKeyWord(keyword)
        searchProject.withUser(user)
        searchProject.withRecruiting(recruiting)
        searchProject.withTags(tags)
        searchProject.decideSort(sortOrder)

        val projectsList: List<Projects> = searchProject.getResult()

        if (projectsList.isEmpty()) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(projectsList)
    }

    @GetMapping("/{id}")
    fun getProjectById(@PathVariable(value="id") rawId: String?): ResponseEntity<Projects>{
        val projectId: UUID = convertStringToUUID(rawId) ?: return ResponseEntity.badRequest().build()

        val project: Projects =  getProject(projectId)?: return ResponseEntity.badRequest().build()

        return ResponseEntity.ok(project)
    }

    @DeleteMapping("/{id}")
    fun deleteProject(@PathVariable(value="id") rawId: String?, @RequestParam(name="token", required=true) token: String): ResponseEntity<String>{
        val projectId: UUID = convertStringToUUID(rawId) ?: return ResponseEntity.badRequest().build()

        val project: Projects = getProject(projectId) ?: return ResponseEntity.badRequest().build()

        val userIdFromToken: UUID = getUserFromToken(token) ?: return ResponseEntity.badRequest().build()

        if (project.createdUserId == userIdFromToken){
            projectsRepository.delete(project)
            return ResponseEntity.ok("Deleted")
        }
        return ResponseEntity.badRequest().build()
    }

    private fun getProject(projectId: UUID): Projects?{
        val projectsList: List<Projects> = projectsRepository.findById(projectId)
        if (projectsList.isEmpty()) return null
        return projectsList[0]
    }

    private fun getUserFromToken(token: String): UUID?{
        val tokenList: List<Token> = tokenRepository.findByToken(token)

        if (tokenList.isEmpty()) return null

        return tokenList[0].userId
    }

    private fun convertStringToUUID(rawId: String?): UUID?{
        val projectId: UUID
        try{
            projectId = UUID.fromString(rawId)
        }catch (e: IllegalArgumentException){
            return null
        }
        return projectId
    }

    private fun saveTags(rawTags: String, projectId: UUID){
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

    private fun createNewTag(tag: String){
        if (tagsRepository.findByName(tag).isNotEmpty()) return

        val newTag = Tags(name=tag)
        tagsRepository.save(newTag)
    }

    private fun tokenCheck(token: String): Token?{
        val tokenList: List<Token> = tokenRepository.findByToken(token)
        if (tokenList.isEmpty()) return null
        return tokenList[0]
    }

    private fun divideSites(rawSites: String?): MutableList<List<String>>?{
        if (rawSites !is String) return null
        val dividedRawSites: List<String> = rawSites.split("+")
        val sites: MutableList<List<String>> = mutableListOf()

        for (rawSite in dividedRawSites){
            val colonIndex: Int = rawSite.indexOf("-:-")
            if (colonIndex == -1) continue
            val site: List<String> = rawSite.split("-:-")
            sites.add(site)
        }
        return sites
    }

    private fun saveSites(rawSites: String?, projectId: UUID){
        val sites: MutableList<List<String>> = divideSites(rawSites) ?: return

        for (site in sites) {
            sitesController.createNewSite(
                    SitesPoster(
                            explanation = site[0],
                            url = site[1],
                            projectId = projectId
                    )
            )
        }
    }


    private fun divideTags(rawTags: String?): List<String>{
        val tags = getTags(rawTags) ?: return listOf()
        while (tags.indexOf("") != -1){
            tags.removeAt(tags.indexOf(""))
        }
        return tags.toList()
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
}

@RestController
@RequestMapping("/sites")
class SitesController(private val sitesRepository: SitesRepository){
    @GetMapping("/")
    fun getAllSites(): List<Sites> = sitesRepository.findAll()

    @PostMapping("/add")
    fun createNewSite(@Valid @RequestBody rawData: SitesPoster): Sites{
        val site = Sites(
                explanation = rawData.explanation,
                url = rawData.url,
                projectId = rawData.projectId
        )
        return sitesRepository.save(site)
    }

    @GetMapping("/{project_id}")
    fun searchFromProjectId(@PathVariable(value="project_id") rawId: String?): ResponseEntity<List<Sites>>{
        val projectId: UUID
        try {
            projectId = UUID.fromString(rawId)
        }catch (e: IllegalArgumentException){
            return ResponseEntity.badRequest().build()
        }
        val sitesList = sitesRepository.findByProjectId(projectId)
        if (sitesList.isNotEmpty()) return ResponseEntity.ok(sitesList.toList())
        return ResponseEntity.notFound().build()
    }
}

class SearchProject(private var projects: Set<Projects>, private val projectsRepository: ProjectsRepository, private val tagsToProjectsBridgeRepository: TagsToProjectsBridgeRepository){
    fun withKeyWord(keyword: String?){
        if(keyword !is String) return
        val searchResults: Set<Projects> = projectsRepository.findByNameLike("%$keyword%").toSet()
        projects = projects.intersect(searchResults)
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
