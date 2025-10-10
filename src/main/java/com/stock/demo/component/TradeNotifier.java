package com.stock.demo.component;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TradeNotifier {
private final SlackNotifier slack;
private final EmailNotifier email;

public TradeNotifier(SlackNotifier slack, EmailNotifier email) {
 this.slack = slack;
 this.email = email;
}

public void tradePlaced(String action, String ticker, BigDecimal qty, String status, String orderId) {
 String qtyStr = qty == null ? "-" : qty.stripTrailingZeros().toPlainString();
 String msg = """
     📈 Trade executed
     • Action : %s
     • Ticker : %s
     • Qty    : %s
     • Status : %s
     • OrderId: %s
     """.formatted(action, ticker, qtyStr, status == null ? "-" : status, orderId == null ? "-" : orderId);

 slack.send(msg);
 email.send("Trade: %s %s (%s)".formatted(action, ticker, status == null ? "-" : status), msg);
}
}

