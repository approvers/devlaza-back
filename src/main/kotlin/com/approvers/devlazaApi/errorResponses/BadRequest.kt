package com.approvers.devlazaApi.errorResponses

import java.lang.RuntimeException

class BadRequest(message: String) : RuntimeException(message) {
}