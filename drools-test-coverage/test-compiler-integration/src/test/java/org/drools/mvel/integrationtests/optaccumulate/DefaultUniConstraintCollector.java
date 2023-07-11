package org.drools.mvel.integrationtests.optaccumulate;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultUniConstraintCollector<A, ResultContainer_, Result_>
        implements UniConstraintCollector<A, ResultContainer_, Result_> {

    private final Supplier<ResultContainer_> supplier;
    private final BiFunction<ResultContainer_, A, Runnable> accumulator;
    private final Function<ResultContainer_, Result_> finisher;

    public DefaultUniConstraintCollector(Supplier<ResultContainer_> supplier,
                                         BiFunction<ResultContainer_, A, Runnable> accumulator,
                                         Function<ResultContainer_, Result_> finisher) {
        this.supplier = supplier;
        this.accumulator = accumulator;
        this.finisher = finisher;
    }

    @Override
    public Supplier<ResultContainer_> supplier() {
        return supplier;
    }

    @Override
    public BiFunction<ResultContainer_, A, Runnable> accumulator() {
        return accumulator;
    }

    @Override
    public Function<ResultContainer_, Result_> finisher() {
        return finisher;
    }

}
