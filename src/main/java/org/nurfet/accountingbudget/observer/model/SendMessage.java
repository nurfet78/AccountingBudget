package org.nurfet.accountingbudget.observer.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.accountingbudget.model.AbstractEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class SendMessage extends AbstractEntity {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private String content;

    private LocalDateTime timestamp;

    private SendMessage(String content) {
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public static SendMessage create(String content) {
        return new SendMessage(content);
    }

    public String getFormattedDate() {
        return timestamp.format(DATE_FORMATTER);
    }
}
