package fr.esgi.color_run.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private final String fromEmail = "polo76989@gmail.com";
    private final String password = "xzpnbdatezcmkyty";

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
    }

    public void sendVerificationEmail(String toEmail, String code) {
        sendEmail(toEmail, "Code de vérification ColorRun", code);
    }

    public void sendEmail(String toEmail, String subject, String code) {
        String htmlContent =
                "<!DOCTYPE html>" +
                        "<html lang='fr'>" +
                        "<head>" +
                        "  <meta charset='UTF-8'>" +
                        "  <title>ColorRun - Vérification</title>" +
                        "</head>" +
                        "<body style='font-family:Arial,sans-serif;background-color:#f9fafb;margin:0;padding:20px;color:#111'>" +
                        "  <div style='max-width:600px;margin:auto;background-color:#ffffff;padding:30px;border-radius:8px;box-shadow:0 0 10px rgba(0,0,0,0.05)'>" +
                        "    <h2 style='text-align:center;color:#f59e0b;'>Bienvenue sur ColorRun !</h2>" +
                        "    <p>Bonjour,</p>" +
                        "    <p>Merci pour votre inscription. Pour activer votre compte, veuillez utiliser le code de vérification ci-dessous</p>" +
                        "    <div style='margin:30px 0;text-align:center'>" +
                        "      <span style='display:inline-block;background-color:#f59e0b;color:white;font-weight:bold;padding:10px 20px;border-radius:5px;font-size:18px;'>" + code + "</span>" +
                        "    </div>" +
                        "    <p style='text-align:center;'>Ce code est valide pour une courte durée.</p>" +
                        "    <p style='margin-top:40px;'>Sportivement,<br>L'équipe ColorRun 🏃</p>" +
                        "  </div>" +
                        "</body>" +
                        "</html>";

        try {
            Message message = new MimeMessage(createSession());
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
