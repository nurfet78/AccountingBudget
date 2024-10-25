package org.nurfet.accountingbudget.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class LimitChangeResult {

    private BigDecimal currentPeriodAmount;

    private BigDecimal nextPeriodAmount;

    private String message;
}
