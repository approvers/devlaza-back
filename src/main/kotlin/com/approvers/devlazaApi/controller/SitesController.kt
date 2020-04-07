package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.errors.BadRequest
import com.approvers.devlazaApi.errors.NotFound
import com.approvers.devlazaApi.model.Sites
import com.approvers.devlazaApi.model.SitesPoster
import com.approvers.devlazaApi.repository.SitesRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/sites")
class SitesController(private val sitesRepository: SitesRepository){
    @PostMapping("/add")
    fun createNewSite(@Valid @RequestBody rawData: SitesPoster): Sites{
        val newSite = Sites(
                explanation = rawData.explanation,
                url = rawData.url,
                projectId = rawData.projectId
        )
        for (site in sitesRepository.findByProjectId(rawData.projectId)){
            if (rawData.url == site.url) throw BadRequest("The site's url is already registered")
        }

        return sitesRepository.save(newSite)
    }

    @GetMapping("/{project_id}")
    fun searchFromProjectId(@PathVariable(value="project_id") rawId: String?): ResponseEntity<List<Sites>> {
        val projectId: UUID
        try {
            projectId = UUID.fromString(rawId)
        }catch (e: IllegalArgumentException){
            throw BadRequest("The format of the given ID is abnormal")
        }
        val sitesList = sitesRepository.findByProjectId(projectId)
        if (sitesList.isNotEmpty()) return ResponseEntity.ok(sitesList.toList())
        throw NotFound("No project corresponding to ID was found")
    }

    fun saveSites(rawSites: String?, projectId: UUID){
        val sites: List<DividedSites> = rawSites.divideToSites()

        for (site in sites) {
            createNewSite(
                    SitesPoster(
                            explanation = site.explanation,
                            url = site.url,
                            projectId = projectId
                    )
            )
        }
    }

    private fun String?.divideToSites(): List<DividedSites>{
        if (this !is String) return listOf()
        val rawSites: List<String> = this.split("+")
        val sites: MutableList<DividedSites> = mutableListOf()

        for (site in rawSites){
            val tmp: List<String> = site.split(",")
            val sitesContent = DividedSites(explanation = tmp[0], url =  tmp[1])

            if (sitesContent in sites) continue

            try{
                sites.add(sitesContent)
            }catch (e: IndexOutOfBoundsException){
                println("warning: url or explanation is null")
            }
        }
        return sites.toList()
    }

    private data class DividedSites(
            val explanation: String,
            val url: String
    )
}
