package com.approvers.devlazaApi.security

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('USER')")
annotation class RequireUser

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('ADMIN')")
annotation class RequireAdmin
