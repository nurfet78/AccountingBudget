package org.nurfet.accountingbudget.observer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.accountingbudget.observer.event.MailCreatedEvent;
import org.nurfet.accountingbudget.observer.model.SendMessage;
import org.nurfet.accountingbudget.observer.MailObserver;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements MailObserver {

    private final JavaMailSender emailSender;

    private static final String FROM_EMAIL = "nurfet3@gmail.com";
    private static final String TO_EMAIL = "nurfet3@gmail.com";

    @Override
    @Async
    @EventListener
    public void handlePostEvent(MailCreatedEvent event) {
        SendMessage sendMessage = event.getSendMessage();

        log.info("Получено сообщение: {} в {}", sendMessage.getContent(), sendMessage.getFormattedDate());
        sendEmail(sendMessage.getContent());
    }

    private void sendEmail(String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(TO_EMAIL);
        message.setSubject("Оповещение о бюджете");
        message.setText(text);
        emailSender.send(message);
        log.info("Электронное письмо отправлено {} с темой:: {}", TO_EMAIL, "Оповещение о бюджете");
    }
}
