package org.drools.mvel.integrationtests.optaccumulate;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public interface BiConstraintCollector<A, B, ResultContainer_, Result_> {

    /**
     * A lambda that creates the result container, one for each group key combination.
     *
     * @return never null
     */
    Supplier<ResultContainer_> supplier();

    /**
     * A lambda that extracts data from the matched facts,
     * accumulates it in the result container
     * and returns an undo operation for that accumulation.
     *
     * @return never null, the undo operation. This lambda is called when the facts no longer matches.
     */
    TriFunction<ResultContainer_, A, B, Runnable> accumulator();

    /**
     * A lambda that converts the result container into the result.
     *
     * @return never null
     */
    Function<ResultContainer_, Result_> finisher();

}
