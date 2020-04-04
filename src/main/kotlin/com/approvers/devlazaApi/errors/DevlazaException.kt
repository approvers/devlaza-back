package com.approvers.devlazaApi.errors

import java.lang.RuntimeException

abstract class DevlazaException(message: String): RuntimeException(message)