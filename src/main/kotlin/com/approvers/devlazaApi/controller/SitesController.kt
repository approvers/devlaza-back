package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.model.Sites
import com.approvers.devlazaApi.model.SitesPoster
import com.approvers.devlazaApi.repository.SitesRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

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
	fun searchFromProjectId(@PathVariable(value="project_id") rawId: String?): ResponseEntity<List<Sites>> {
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
