package org.nurfet.accountingbudget.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction extends AbstractEntity {

    @Column(nullable = false)
    private LocalDate date;

    @NotNull(message = "Сумма должна быть указана")
    @DecimalMin(value = "0.01", inclusive = true, message = "Сумма должна быть больше нуля")
    @Column(nullable = false)
    private BigDecimal amount;

    @NotNull(message = "Тип транзакции должен быть выбран")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;


    @NotNull(message = "Категория должна быть выбрана")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Size(max = 255, message = "Описание не должно превышать 255 символов")
    private String description;

    @AssertTrue(message = "Дата должна быть выбрана")
    public boolean isDateSelected() {
        return date != null;
    }

    @AssertTrue(message = "Дата транзакции не может быть позже текущей даты.")
    public boolean isDateValid() {
        return date == null || !date.isAfter(LocalDate.now());
    }
}
