package com.approvers.devlazaApi.errorResponses

import com.fasterxml.jackson.annotation.JsonProperty


data class ErrorContents(
		@JsonProperty("Message") val message: String,
		@JsonProperty("Detail") val detail: String,
		@JsonProperty("Code") val code: String
)