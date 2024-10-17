package org.nurfet.accountingbudget.observer;

import org.nurfet.accountingbudget.observer.event.MailCreatedEvent;

public interface MailObserver {

    void handlePostEvent(MailCreatedEvent event);
}
