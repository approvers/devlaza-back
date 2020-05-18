package dev.approvers.devlazaApi.domain.service

import org.springframework.mail.MailMessage
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Service

@Service
class MailService(
    private val sender: MailSender
) {
    fun send(block: MailMessage.() -> Unit) {
        val message = SimpleMailMessage().apply(block)
        sender.send(message)
    }
}
