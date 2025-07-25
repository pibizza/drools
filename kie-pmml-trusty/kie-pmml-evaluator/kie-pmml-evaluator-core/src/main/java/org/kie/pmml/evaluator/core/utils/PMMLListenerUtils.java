/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.pmml.evaluator.core.utils;

import java.util.function.Supplier;

import org.kie.pmml.api.models.PMMLStep;
import org.kie.pmml.api.runtime.PMMLRuntimeContext;

/**
 * Common utility methods related to <code>PMMLListener</code>
 */
public class PMMLListenerUtils {

    /**
     * Send the <code>PMMLStep</code> to all registered <code>PMMLListener</code>s
     *
     * @param stepSupplier
     * @param context
     */
    public static void stepExecuted(final Supplier<PMMLStep> stepSupplier, final PMMLRuntimeContext context) {
        if (!context.getEfestoListeners().isEmpty()) {
            final PMMLStep step = stepSupplier.get();
            context.getEfestoListeners().forEach(listener -> listener.stepExecuted(step));
        }
    }

    private PMMLListenerUtils() {
    }
}
