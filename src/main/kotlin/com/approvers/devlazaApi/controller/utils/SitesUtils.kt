package com.approvers.devlazaApi.controller.utils

import com.approvers.devlazaApi.controller.SitesController
import com.approvers.devlazaApi.model.SitesPoster
import java.util.*

class SitesUtils(
		private val sitesController: SitesController
) {

	private fun divideSites(rawSites: String?): MutableList<List<String>>?{
		if (rawSites !is String) return null
		val dividedRawSites: List<String> = rawSites.split("+")
		val sites: MutableList<List<String>> = mutableListOf()

		for (rawSite in dividedRawSites){
			val site: List<String> = rawSite.split(",")
			sites.add(site)
		}
		return sites
	}

	fun saveSites(rawSites: String?, projectId: UUID){
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
}