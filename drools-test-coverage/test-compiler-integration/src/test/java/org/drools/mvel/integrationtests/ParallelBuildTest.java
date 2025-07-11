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
package org.drools.mvel.integrationtests;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieUtil;
import org.drools.testcoverage.common.util.TestParametersUtil2;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.Message;

import static org.assertj.core.api.Assertions.assertThat;

public class ParallelBuildTest {

    public static Stream<KieBaseTestConfiguration> parameters() {
        return TestParametersUtil2.getKieBaseCloudConfigurations(true).stream();
    }

    private final List<Class<?>> classes = Arrays.asList(
            java.util.List.class,
            java.awt.Color.class,
            java.util.concurrent.Callable.class,
            java.util.concurrent.atomic.AtomicBoolean.class,
            java.util.concurrent.locks.Lock.class,
            java.util.zip.ZipFile.class,
            java.awt.color.ColorSpace.class,
            java.awt.font.TextMeasurer.class,
            java.awt.geom.Area.class,
            java.awt.im.InputContext.class,
            java.net.Inet4Address.class,
            java.io.File.class
    );

    @ParameterizedTest(name = "KieBase type={0}")
    @MethodSource("parameters")
    public void testParallelBuild(KieBaseTestConfiguration kieBaseTestConfiguration) {
        StringBuilder sb = new StringBuilder();
        int rc = 0;
        for (Class<?> c : classes) {
            sb.append("rule \"rule_" + rc++ + "\"\n");
            sb.append("  when\n");
            sb.append("    a : " + c.getName() + "()\n");
            sb.append("  then\n");
            sb.append("    System.out.print(\".\");\n");
            sb.append("end\n");
            sb.append("\n");
        }

        KieBuilder kieBuilder = KieUtil.getKieBuilderFromDrls(kieBaseTestConfiguration, false, sb.toString());
        List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
        assertThat(errors.isEmpty()).as(errors.toString()).isTrue();
    }

}
