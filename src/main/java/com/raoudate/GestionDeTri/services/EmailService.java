package com.raoudate.GestionDeTri.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendAccountStatusEmail(String to, String username, String status) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("status", status);

            String html = templateEngine.process("account_status", context);

            helper.setTo(to);
            helper.setSubject("Modification de votre compte - Poste du Togo");
            helper.setText(html, true);
            helper.setFrom("noreply@laposte.tg");

            mailSender.send(mimeMessage);
            log.info("Email de changement de statut envoyé à: {}", to);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email de statut: {}", e.getMessage());
        }
    }

    @Async
    public void sendAccountLockedEmail(String to, String username, String reason) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("reason", reason);

            String html = templateEngine.process("account_locked", context);

            helper.setTo(to);
            helper.setSubject("Votre compte a été bloqué - Poste du Togo");
            helper.setText(html, true);
            helper.setFrom("noreply@laposte.tg");

            mailSender.send(mimeMessage);
            log.info("Email de blocage de compte envoyé à: {}", to);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email de blocage: {}", e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("resetLink", "http://localhost:4200/reset-password?token=" + resetToken);

            String html = templateEngine.process("password_reset", context);

            helper.setTo(to);
            helper.setSubject("Réinitialisation de votre mot de passe - Poste du Togo");
            helper.setText(html, true);
            helper.setFrom("noreply@laposte.tg");

            mailSender.send(mimeMessage);
            log.info("Email de réinitialisation de mot de passe envoyé à: {}", to);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation: {}", e.getMessage());
        }
    }
}
