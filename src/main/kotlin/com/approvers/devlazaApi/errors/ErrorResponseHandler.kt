package com.approvers.devlazaApi.errors

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ErrorResponseHandler : ResponseEntityExceptionHandler() {
    override fun handleExceptionInternal(ex: Exception, body: Any?, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        val returnBody: ErrorContents = if (body !is ErrorContents) {
            val code = when (status.reasonPhrase) {
                "Bad Request" -> "400"
                "Not Found" -> "404"
                "UnAuthorized" -> "401"
                "Forbidden" -> "403"
                else -> ""
            }
            ErrorContents(status.reasonPhrase, "", code)
        } else {
            body
        }
        return ResponseEntity(returnBody, headers, status)
    }

    @ExceptionHandler(NotFound::class)
    fun handle404(ex: NotFound, request: WebRequest): ResponseEntity<Any> {
        val headers = HttpHeaders()
        val body = ErrorContents("NotFound", ex.message!!, "404")
        val status = HttpStatus.NOT_FOUND

        return handleExceptionInternal(ex, body, headers, status, request)
    }

    @ExceptionHandler(BadRequest::class)
    fun handle400(ex: BadRequest, request: WebRequest): ResponseEntity<Any> {
        val headers = HttpHeaders()
        val body = ErrorContents("BadRequest", ex.message!!, "400")
        val status = HttpStatus.BAD_REQUEST

        return handleExceptionInternal(ex, body, headers, status, request)
    }

    @ExceptionHandler(UnAuthorized::class)
    fun handle401(ex: UnAuthorized, request: WebRequest): ResponseEntity<Any> {
        val headers = HttpHeaders()
        val body = ErrorContents("UnAuthorized", ex.message!!, "401")
        val status = HttpStatus.UNAUTHORIZED

        return handleExceptionInternal(ex, body, headers, status, request)
    }

    @ExceptionHandler(Forbidden::class)
    fun handle403(ex: Forbidden, request: WebRequest): ResponseEntity<Any> {
        val headers = HttpHeaders()
        val body = ErrorContents("Forbidden", ex.message!!, "403")
        val status = HttpStatus.FORBIDDEN

        return handleExceptionInternal(ex, body, headers, status, request)
    }

    @ExceptionHandler(InternalServerError::class)
    fun handle500(ex: InternalServerError, request: WebRequest): ResponseEntity<Any> {
        val headers = HttpHeaders()
        val body = ErrorContents("InternalServerError", ex.message!!, "500")
        val status = HttpStatus.INTERNAL_SERVER_ERROR

        return handleExceptionInternal(ex, body, headers, status, request)
    }
}