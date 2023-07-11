package org.drools.mvel.integrationtests.optaccumulate;

import java.util.function.ToIntFunction;

public final class ConstraintCollectors {

    private static final Runnable NOOP = () -> {
        // No operation.
    };

    public static <A> UniConstraintCollector<A, ?, Integer> sum(ToIntFunction<? super A> groupValueMapping) {
        return new DefaultUniConstraintCollector<>(
                MutableInt::new,
                (resultContainer, a) -> {
                    int value = groupValueMapping.applyAsInt(a);
                    return innerSum(resultContainer, value);
                },
                MutableInt::intValue);
    }

    private static Runnable innerSum(MutableInt resultContainer, int value) {
        resultContainer.add(value);
        return () -> resultContainer.subtract(value);
    }
}
