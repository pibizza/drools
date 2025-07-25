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
package org.drools.compiler.integrationtests.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.drools.testcoverage.common.model.Cheese;
import org.drools.testcoverage.common.model.Message;
import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieBaseUtil;
import org.drools.testcoverage.common.util.TestParametersUtil2;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;

import static org.assertj.core.api.Assertions.assertThat;

public class AndTest {

    public static Stream<KieBaseTestConfiguration> parameters() {
        return TestParametersUtil2.getKieBaseCloudConfigurations(true).stream();
    }

    @ParameterizedTest(name = "KieBase type={0}")
	@MethodSource("parameters")
    public void testExplicitAnd(KieBaseTestConfiguration kieBaseTestConfiguration) {
        final String drl = "package HelloWorld\n" +
                " \n" +
                "import " + Message.class.getCanonicalName() + ";\n" +
                "import " + Cheese.class.getCanonicalName() + " ;\n" +
                "\n" +
                "global java.util.List list;\n" +
                "\n" +
                "rule \"Hello World\"\n" +
                "    when\n" +
                "        Message() and Cheese()\n" +
                "    then\n" +
                "        list.add(\"hola\");\n" +
                "end";

        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("and-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            ksession.insert(new Message("hola"));

            ksession.fireAllRules();
            assertThat(list.size()).isEqualTo(0);

            ksession.insert(new Cheese("brie", 33));
            ksession.fireAllRules();
            assertThat(((List) ksession.getGlobal("list")).size()).isEqualTo(1);
        } finally {
            ksession.dispose();
        }
    }
}
