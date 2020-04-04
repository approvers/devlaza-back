package com.approvers.devlazaApi.errors

import com.approvers.devlazaApi.errors.ErrorContents
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.Exception

@RestControllerAdvice
class ErrorResponseHandler: ResponseEntityExceptionHandler() {
    override fun handleExceptionInternal(ex: Exception, body: Any?, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        val returnBody: ErrorContents = if (body !is ErrorContents){
            val code = when(status.reasonPhrase){
                "Bad Request" -> "400"
                "Not Found" -> "404"
                else -> ""
            }
            ErrorContents(status.reasonPhrase, "", code)
        }else{
            body
        }
        return ResponseEntity(returnBody, headers, status)
    }
}