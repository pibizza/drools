package org.drools.mvel.integrationtests.optaccumulate;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;


public interface UniConstraintCollector<A, ResultContainer_, Result_> {

    /**
     * A lambda that creates the result container, one for each group key combination.
     *
     * @return never null
     */
    Supplier<ResultContainer_> supplier();

    /**
     * A lambda that extracts data from the matched fact,
     * accumulates it in the result container
     * and returns an undo operation for that accumulation.
     *
     * @return never null, the undo operation. This lambda is called when the fact no longer matches.
     */
    BiFunction<ResultContainer_, A, Runnable> accumulator();

    /**
     * A lambda that converts the result container into the result.
     *
     * @return null when the result would be invalid, such as maximum value from an empty container.
     */
    Function<ResultContainer_, Result_> finisher();

}
