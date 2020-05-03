package com.approvers.devlazaApi.security

import org.springframework.security.access.prepost.PreAuthorize

/**
 * User以上の権限を持っていることが必要である
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('USER')")
annotation class RequireUser

/**
 * Admin以上の権限を持っていることが必要である
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('ADMIN')")
annotation class RequireAdmin
