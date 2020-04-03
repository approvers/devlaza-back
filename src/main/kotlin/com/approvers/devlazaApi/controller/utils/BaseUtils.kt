package com.approvers.devlazaApi.controller.utils

import java.util.*

class BaseUtils {
	fun convertStringToUUID(rawId: String?): UUID?{
		val id: UUID
		try{
			id = UUID.fromString(rawId)
		}catch (e: IllegalArgumentException){
			return null
		}
		return id
	}
}