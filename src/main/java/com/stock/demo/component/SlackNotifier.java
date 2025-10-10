package com.stock.demo.component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class SlackNotifier {
private final String webhookUrl;
private final RestClient client = RestClient.create();

public SlackNotifier(@Value("${app.notification.slack.webhook-url:}") String webhookUrl) {
 this.webhookUrl = webhookUrl;
}

public void send(String text) {
 if (webhookUrl == null || webhookUrl.isBlank()) return; // no-op if not configured
 try {
   client.post()
     .uri(webhookUrl)
     .contentType(MediaType.APPLICATION_JSON)
     .body(Map.of("text", text))
     .retrieve()
     .toBodilessEntity();
 } catch (Exception e) {
   System.err.println("Slack notify failed: " + e.getMessage());
 }
}
}
