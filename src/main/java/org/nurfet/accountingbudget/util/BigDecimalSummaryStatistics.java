package org.nurfet.accountingbudget.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import java.util.stream.Collector;

public class BigDecimalSummaryStatistics implements BigDecimalConsumer {

    private long count;

    private BigDecimal sum;

    private BigDecimal sumCompensation;

    private BigDecimal simpleSum;

    private BigDecimal min;

    private BigDecimal max;

    public BigDecimalSummaryStatistics() {
        count = 0;
        sum = BigDecimal.ZERO;
        sumCompensation = BigDecimal.ZERO;
        simpleSum = BigDecimal.ZERO;
        min = null;
        max = null;
    }

    public BigDecimalSummaryStatistics(long count, BigDecimal min, BigDecimal max, BigDecimal sum) throws IllegalArgumentException {
        if (count < 0L) {
            throw new IllegalArgumentException("Negative count value");
        } else if (count > 0L) {
            if (min.compareTo(max) > 0) {
                throw new IllegalArgumentException("Minimum greater than maximum");
            }

            if (max == null || sum == null) {
                throw new IllegalArgumentException("Minimum, maximum, or sum is null");
            }

            this.count = count;
            this.sum = sum;
            this.simpleSum = sum;
            this.sumCompensation = BigDecimal.ZERO;
            this.min = min;
            this.max = max;
        }
    }

    @Override
    public void accept(BigDecimal value) {
        ++count;
        simpleSum = simpleSum.add(value);
        sumWithCompensation(value);
        min = (min == null) ? value : min.min(value);
        max = (max == null) ? value : max.max(value);
    }

    public void combine(BigDecimalSummaryStatistics other) {
        count += other.count;
        simpleSum = simpleSum.add(other.simpleSum);
        sumWithCompensation(other.sum);
        sumWithCompensation(other.sumCompensation.negate());
        min = (min == null) ? other.min : (other.min == null ? min : min.min(other.min));
        max = (max == null) ? other.max : (other.max == null ? max : max.max(other.max));
    }

    private void sumWithCompensation(BigDecimal value) {
        BigDecimal tmp = value.subtract(sumCompensation);
        BigDecimal velvel = sum.add(tmp);
        sumCompensation = velvel.subtract(sum).subtract(tmp);
        sum = velvel;
    }

    public final long getCount() {
        return count;
    }

    public final BigDecimal getSum() {
        BigDecimal tmp = sum.subtract(sumCompensation);
        return (tmp.compareTo(BigDecimal.ZERO) == 0 && !simpleSum.equals(BigDecimal.ZERO)) ? simpleSum : tmp;
    }

    public final BigDecimal getMin() {
        return min;
    }

    public final BigDecimal getMax() {
        return max;
    }

    public final BigDecimal getAverage() {
        return getCount() > 0 ? getSum().divide(BigDecimal.valueOf(getCount()), 10, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return String.format(
                "%s{count=%d, sum=%s, min=%s, average=%s, max=%s}",
                this.getClass().getSimpleName(),
                getCount(),
                getSum(),
                getMin(),
                getAverage(),
                getMax());
    }

    public static <T> Collector<T, ?, BigDecimalSummaryStatistics> summarizingBigDecimal(Function<? super T, BigDecimal> mapper) {
        return Collector.of(
                BigDecimalSummaryStatistics::new,
                (r, t) -> r.accept(mapper.apply(t)),
                (l, r) -> {
                    l.combine(r);
                    return l;
                },
                Collector.Characteristics.UNORDERED, Collector.Characteristics.IDENTITY_FINISH
        );
    }
}
