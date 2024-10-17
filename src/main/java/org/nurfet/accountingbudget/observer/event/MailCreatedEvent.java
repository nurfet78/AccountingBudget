package org.nurfet.accountingbudget.observer.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nurfet.accountingbudget.observer.model.SendMessage;

@Getter
@AllArgsConstructor
public class MailCreatedEvent {

    private SendMessage sendMessage;
}
