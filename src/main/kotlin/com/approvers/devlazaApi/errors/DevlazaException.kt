package com.approvers.devlazaApi.errors

import java.lang.RuntimeException

open class DevlazaException(message: String): RuntimeException(message)