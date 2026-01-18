package br.com.barbearia.apibarbearia.notification.email.sender;

public interface EmailSender {
    void sendHtml(String to, String subject, String html);
}
