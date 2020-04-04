package com.approvers.devlazaApi.errorResponses.errors

import java.lang.RuntimeException

open class DevlazaErrorBase(message: String): RuntimeException(message) {
}