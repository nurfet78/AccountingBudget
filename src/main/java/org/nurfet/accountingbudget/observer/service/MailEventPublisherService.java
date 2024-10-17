package org.nurfet.accountingbudget.observer.service;

import lombok.RequiredArgsConstructor;
import org.nurfet.accountingbudget.observer.event.MailCreatedEvent;
import org.nurfet.accountingbudget.observer.model.SendMessage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailEventPublisherService {

    private final ApplicationEventPublisher eventPublisher;

    public void publishMailCreatedEvent(SendMessage sendMessage) {
        eventPublisher.publishEvent(new MailCreatedEvent(sendMessage));
    }
}
