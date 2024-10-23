package org.nurfet.accountingbudget.util;

import java.math.BigDecimal;
import java.util.Objects;

@FunctionalInterface
public interface BigDecimalConsumer {

    void accept(BigDecimal value);

    default BigDecimalConsumer andThen(BigDecimalConsumer after) {
        Objects.requireNonNull(after);
        return (BigDecimal t) -> { accept(t); after.accept(t); };
    }
}
