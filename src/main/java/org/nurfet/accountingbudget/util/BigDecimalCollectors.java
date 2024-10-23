package org.nurfet.accountingbudget.util;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class BigDecimalCollectors {

    private BigDecimalCollectors() { }

    static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();

    public static <T> Collector<T, ?, BigDecimal>
    summingBigDecimal(ToBigDecimalFunction<? super T> mapper) {
        return new CollectorImpl<>(
                () -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
                (a, t) -> {
                    BigDecimal val = mapper.applyAsBigDecimal(t);
                    if (val != null) {
                        sumWithCompensation(a, val);
                        a[2] = a[2].add(val);
                    }
                },
                (a, b) -> {
                    sumWithCompensation(a, b[0]);
                    a[2] = a[2].add(b[2]);
                    return sumWithCompensation(a, b[1].negate());
                },
                BigDecimalCollectors::computeFinalSum,
                CH_NOID);
    }

    static BigDecimal[] sumWithCompensation(BigDecimal[] intermediateSum, BigDecimal value) {
        if (intermediateSum[0] == null) intermediateSum[0] = BigDecimal.ZERO;
        if (intermediateSum[1] == null) intermediateSum[1] = BigDecimal.ZERO;
        if (intermediateSum[2] == null) intermediateSum[2] = BigDecimal.ZERO;

        BigDecimal tmp = value.subtract(intermediateSum[1]);
        BigDecimal sum = intermediateSum[0];
        BigDecimal velvel = sum.add(tmp);
        intermediateSum[1] = velvel.subtract(sum).subtract(tmp);
        intermediateSum[0] = velvel;
        return intermediateSum;
    }

    static BigDecimal computeFinalSum(BigDecimal[] summands) {
        BigDecimal tmp = summands[0].subtract(summands[1]);
        BigDecimal simpleSum = summands[summands.length - 1];
        if (tmp.compareTo(BigDecimal.ZERO) == 0 && simpleSum.compareTo(BigDecimal.ZERO) != 0)
            return simpleSum;
        else
            return tmp;
    }

    record CollectorImpl<T, A, R>(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner,
                                  Function<A, R> finisher,
                                  Set<Characteristics> characteristics) implements Collector<T, A, R> {
    }
}
