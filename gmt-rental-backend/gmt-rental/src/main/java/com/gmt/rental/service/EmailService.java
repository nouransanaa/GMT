package com.gmt.rental.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${gmt.mail.from:${spring.mail.username:no-reply@gmt-rental.com}}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.fromEmail = (fromEmail == null || fromEmail.isBlank()) ? "no-reply@gmt-rental.com" : fromEmail.trim();
    }

    public void sendVerificationOtpEmail(String recipientEmail, String otpCode, int expirationMinutes) {
        String subject = "Votre code de vérification GMT Rental : " + otpCode;
        String htmlContent = buildOtpHtmlEmail(otpCode, expirationMinutes);
        sendHtmlEmail(recipientEmail, subject, htmlContent);
    }

    public void sendPasswordResetOtpEmail(String recipientEmail, String otpCode, int expirationMinutes) {
        String subject = "Réinitialisation de votre mot de passe GMT Rental";
        String htmlContent = buildPasswordResetHtmlEmail(otpCode, expirationMinutes);
        sendHtmlEmail(recipientEmail, subject, htmlContent);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail.trim());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de vérification envoyé avec succès à {}", toEmail);
        } catch (MessagingException e) {
            log.error("Erreur de composition du message MIME pour {} : {}", toEmail, e.getMessage());
            throw new RuntimeException("Erreur lors de la préparation de l'e-mail de vérification.", e);
        } catch (Exception e) {
            log.error("Échec de l'envoi de l'e-mail à {} : {}", toEmail, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'e-mail de vérification. Vérifiez la configuration SMTP.", e);
        }
    }

    private String buildOtpHtmlEmail(String otpCode, int expirationMinutes) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
              <meta charset="UTF-8">
              <style>
                body { margin: 0; padding: 0; background-color: #0c1424; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #edf2fa; }
                .container { max-width: 560px; margin: 40px auto; background-color: #111a2d; border: 1px solid #25334d; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 40px rgba(0,0,0,0.5); }
                .header { background: radial-gradient(ellipse at 50% 0%, #24334e, #111a2d); padding: 32px 24px; text-align: center; border-bottom: 1px solid #25334d; }
                .brand { font-size: 13px; font-weight: 700; letter-spacing: 0.18em; color: #d8ad63; text-transform: uppercase; margin: 0 0 8px; }
                .title { font-size: 24px; font-weight: 700; color: #ffffff; margin: 0; }
                .body { padding: 32px 28px; text-align: center; }
                .text { color: #9aa8bf; font-size: 15px; line-height: 1.6; margin: 0 0 24px; }
                .otp-box { display: inline-block; background-color: #0c1424; border: 2px dashed #d8ad63; border-radius: 12px; padding: 18px 36px; margin: 8px 0 24px; letter-spacing: 0.35em; font-size: 32px; font-weight: 800; color: #d8ad63; font-family: 'Courier New', Courier, monospace; }
                .warning { font-size: 13px; color: #e57373; margin: 0 0 20px; }
                .footer { padding: 24px; text-align: center; font-size: 12px; color: #5f718d; border-top: 1px solid #1c273c; background-color: #0b1120; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <p class="brand">GMT RENTAL</p>
                  <h1 class="title">Vérification de votre compte</h1>
                </div>
                <div class="body">
                  <p class="text">Bonjour,<br>Un compte a été créé pour vous sur la plateforme GMT Rental. Pour confirmer votre adresse e-mail et activer votre compte, veuillez utiliser le code de vérification ci-dessous :</p>
                  <div class="otp-box">{{OTP_CODE}}</div>
                  <p class="warning">Ce code est valable pendant <strong>{{EXPIRATION_MINUTES}} minutes</strong>. Ne le partagez avec personne.</p>
                  <p class="text" style="font-size: 13px;">Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet e-mail en toute sécurité.</p>
                </div>
                <div class="footer">
                  © GMT Rental. Tous droits réservés.
                </div>
              </div>
            </body>
            </html>
            """
            .replace("{{OTP_CODE}}", otpCode)
            .replace("{{EXPIRATION_MINUTES}}", String.valueOf(expirationMinutes));
    }

    private String buildPasswordResetHtmlEmail(String otpCode, int expirationMinutes) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
              <meta charset="UTF-8">
              <style>
                body { margin: 0; padding: 0; background-color: #0c1424; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #edf2fa; }
                .container { max-width: 560px; margin: 40px auto; background-color: #111a2d; border: 1px solid #25334d; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 40px rgba(0,0,0,0.5); }
                .header { background: radial-gradient(ellipse at 50% 0%, #24334e, #111a2d); padding: 32px 24px; text-align: center; border-bottom: 1px solid #25334d; }
                .brand { font-size: 13px; font-weight: 700; letter-spacing: 0.18em; color: #d8ad63; text-transform: uppercase; margin: 0 0 8px; }
                .title { font-size: 24px; font-weight: 700; color: #ffffff; margin: 0; }
                .body { padding: 32px 28px; text-align: center; }
                .text { color: #9aa8bf; font-size: 15px; line-height: 1.6; margin: 0 0 24px; }
                .otp-box { display: inline-block; background-color: #0c1424; border: 2px dashed #d8ad63; border-radius: 12px; padding: 18px 36px; margin: 8px 0 24px; letter-spacing: 0.35em; font-size: 32px; font-weight: 800; color: #d8ad63; font-family: 'Courier New', Courier, monospace; }
                .warning { font-size: 13px; color: #e57373; margin: 0 0 20px; }
                .footer { padding: 24px; text-align: center; font-size: 12px; color: #5f718d; border-top: 1px solid #1c273c; background-color: #0b1120; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <p class="brand">GMT RENTAL</p>
                  <h1 class="title">Réinitialisation du mot de passe</h1>
                </div>
                <div class="body">
                  <p class="text">Bonjour,<br>Une demande de réinitialisation de mot de passe a été demandée pour votre compte GMT Rental. Saisissez le code suivant pour poursuivre :</p>
                  <div class="otp-box">{{OTP_CODE}}</div>
                  <p class="warning">Ce code est valable pendant <strong>{{EXPIRATION_MINUTES}} minutes</strong>. Ne le partagez avec personne.</p>
                  <p class="text" style="font-size: 13px;">Si vous n'êtes pas à l'origine de cette demande, ignorez cet e-mail ou contactez l'administrateur.</p>
                </div>
                <div class="footer">
                  © GMT Rental. Tous droits réservés.
                </div>
              </div>
            </body>
            </html>
            """
            .replace("{{OTP_CODE}}", otpCode)
            .replace("{{EXPIRATION_MINUTES}}", String.valueOf(expirationMinutes));
    }
}

