package com.approvers.devlazaApi.errorResponses.errors

import java.lang.RuntimeException

open class DevlazaException(message: String): RuntimeException(message) {
}