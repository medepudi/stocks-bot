package com.stock.demo.component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotifier {
private final JavaMailSender mailSender;   // may be null
private final String to;
private final String from;

public EmailNotifier(
   @Autowired(required = false) JavaMailSender mailSender,
   @Value("${app.notification.email.to:}") String to,
   @Value("${app.notification.email.from:}") String from) {
 this.mailSender = mailSender;
 this.to = to;
 this.from = from;
}

public void send(String subject, String body) {
 // If mail isn’t configured, silently no-op (but log to console)
 if (mailSender == null || to.isBlank() || from.isBlank()) {
   System.out.println("EmailNotifier disabled (missing mailSender/to/from)");
   return;
 }
 try {
   SimpleMailMessage msg = new SimpleMailMessage();
   msg.setFrom(from);
   msg.setTo(to);
   msg.setSubject(subject);
   msg.setText(body);
   mailSender.send(msg);
 } catch (Exception e) {
   System.err.println("Email notify failed: " + e.getMessage());
 }
}
}
