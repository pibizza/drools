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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.drools.core.impl.RuleBaseFactory;
import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieBaseUtil;
import org.drools.testcoverage.common.util.TestParametersUtil2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.time.SessionPseudoClock;

import static org.assertj.core.api.Assertions.assertThat;

public class WindowTest {

    public static Stream<KieBaseTestConfiguration> parameters() {
        return TestParametersUtil2.getKieBaseStreamConfigurations(true).stream();
    }

    private KieSession ksession;

    private SessionPseudoClock clock;

    private String drl =
            "package org.drools.mvel.integrationtests;\n" +
            "\n" +
            "import org.drools.mvel.integrationtests.WindowTest.TestEvent\n" +
            "\n" +
            "global java.util.List result\n" +
            "\n" +
            "declare TestEvent\n" +
            "    @role( event )\n" +
            "end\n" +
            "\n" +
            "declare window DeclaredTimeWindow\n" +
            "    TestEvent ( name == \"timeDec\" ) over window:time( 50ms ) from entry-point EventStream\n" +
            "end\n" +
            "\n" +
            "declare window DeclaredLengthWindow\n" +
            "    TestEvent ( name == \"lengthDec\" ) over window:length( 5 ) from entry-point EventStream\n" +
            "end\n" +
            "\n" +
            "query \"TestTimeWindow\"\n" +
            "    Number( $eventCount : longValue ) from\n" +
            "        accumulate (\n" +
            "            $event : TestEvent ( name == \"time\" ) over window:time( 300ms ) from entry-point EventStream,\n" +
            "            count($event)\n" +
            "        )\n" +
            "end\n" +
            "\n" +
            "query \"TestLengthWindow\"\n" +
            "    Number( $eventCount : longValue ) from\n" +
            "        accumulate (\n" +
            "            $event : TestEvent ( name == \"length\" ) over window:length( 10 ) from entry-point EventStream,\n" +
            "            count($event)\n" +
            "        )\n" +
            "end\n" +
            "\n" +
            "query \"TestDeclaredTimeWindow\"\n" +
            "    Number( $eventCount : longValue ) from\n" +
            "        accumulate ( \n" +
            "            $event : TestEvent () from window DeclaredTimeWindow,\n" +
            "            count( $event )\n" +
            "        )\n" +
            "end\n" +
            "\n" +
            "query \"TestDeclaredLengthWindow\"\n" +
            "    Number( $eventCount : longValue ) from\n" +
            "        accumulate ( \n" +
            "            $event : TestEvent () from window DeclaredLengthWindow,\n" +
            "            count( $event )\n" +
            "        )\n" +
            "end\n" +
            "\n" +
            "rule \"TestDeclaredTimeWindowRule\"\n" +
            "    when\n" +
            "        Number( $eventCount : longValue, longValue > 0 ) from \n" +
            "            accumulate ( \n" +
            "                $event : TestEvent () from window DeclaredTimeWindow,\n" +
            "                count( $event )\n" +
            "            )\n" +
            "    then\n" +
            "        result.add($eventCount);\n" +
            "end\n" +
            "\n" +
            "rule \"TestDeclaredLengthWindowRule\"\n" +
            "    when\n" +
            "        Number( $eventCount : longValue, longValue > 0 ) from \n" +
            "            accumulate ( \n" +
            "                $event : TestEvent () from window DeclaredLengthWindow,\n" +
            "                count( $event )\n" +
            "            )\n" +
            "    then\n" +
            "        result.add($eventCount);\n" +
            "end\n";

    public void initialization(KieBaseTestConfiguration kieBaseTestConfiguration) {
        KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("test", kieBaseTestConfiguration, drl);

        KieSessionConfiguration ksconfig = RuleBaseFactory
                .newKnowledgeSessionConfiguration();
        ksconfig.setOption(ClockTypeOption.PSEUDO);

        ksession = kbase.newKieSession(ksconfig, null);

        clock = ksession.getSessionClock();
    }

    @AfterEach
    public void clean() {
        ksession.dispose();
    }

    @ParameterizedTest(name = "KieBase type={0}")
    @MethodSource("parameters")
    public void testTimeWindow(KieBaseTestConfiguration KieBaseTestConfiguration) throws InterruptedException {
        initialization(KieBaseTestConfiguration);
        EntryPoint entryPoint = ksession.getEntryPoint("EventStream");
        final long results[] = new long[] { 1, 2, 3, 3, 3 };
        TestEvent event;

        for (int i = 0; i < 5; i++) {
            event = new TestEvent(null, "time", null);
            entryPoint.insert(event);

            assertThat(ksession.getQueryResults("TestTimeWindow")
                    .iterator().next().get("$eventCount")).isEqualTo(results[i]);
            clock.advanceTime(100, TimeUnit.MILLISECONDS);
        }
    }

    @ParameterizedTest(name = "KieBase type={0}")
    @MethodSource("parameters")
    public void testLengthWindow(KieBaseTestConfiguration kieBaseTestConfiguration) {
        initialization(kieBaseTestConfiguration);
        EntryPoint entryPoint = ksession.getEntryPoint("EventStream");
        TestEvent event;

        for (int i = 1; i <= 20; i++) {
            event = new TestEvent(null, "length", null);
            entryPoint.insert(event);

            assertThat(((Long) ksession.getQueryResults("TestLengthWindow")
                    .iterator().next().get("$eventCount")).intValue()).isEqualTo((Math.min(i, 10)));
        }
    }

    @ParameterizedTest(name = "KieBase type={0}")
    @MethodSource("parameters")
    public void testDeclaredTimeWindowInQuery(KieBaseTestConfiguration kieBaseTestConfiguration) throws InterruptedException {
        initialization(kieBaseTestConfiguration);
        final long results[] = new long[] { 1, 2, 3, 4, 5, 5, 5, 5, 5, 5 };
        EntryPoint entryPoint = ksession.getEntryPoint("EventStream");
        TestEvent event;

        for (int i = 0; i < 10; i++) {
            event = new TestEvent(null, "timeDec", null);
            entryPoint.insert(event);

            assertThat(ksession.getQueryResults("TestDeclaredTimeWindow")
                    .iterator().next().get("$eventCount")).isEqualTo(results[i]);
            clock.advanceTime(10, TimeUnit.MILLISECONDS);
        }
    }

    @ParameterizedTest(name = "KieBase type={0}")
    @MethodSource("parameters")
    public void testDeclaredLengthWindowInQuery(KieBaseTestConfiguration kieBaseTestConfiguration) {
        initialization(kieBaseTestConfiguration);
        EntryPoint entryPoint = ksession.getEntryPoint("EventStream");
        TestEvent event;

        for (int i = 1; i <= 10; i++) {
            event = new TestEvent(null, "lengthDec", null);
            entryPoint.insert(event);
            assertThat(((Long) ksession
                    .getQueryResults("TestDeclaredLengthWindow")
                    .iterator().next().get("$eventCount")).intValue()).isEqualTo((Math.min(i, 5)));
        }
    }

    @ParameterizedTest(name = "KieBase type={0}")
    @MethodSource("parameters")
    public void testDeclaredTimeWindowInRule(KieBaseTestConfiguration kieBaseTestConfiguration) throws InterruptedException {
        initialization(kieBaseTestConfiguration);
        final long results[] = new long[] { 1, 2, 3, 4, 5, 5, 5, 5, 5, 5 };
        EntryPoint entryPoint = ksession.getEntryPoint("EventStream");
        List<Long> result = new ArrayList<Long>();
        ksession.setGlobal("result", result);
        TestEvent event;

        for (int i = 0; i < 10; i++) {
            event = new TestEvent(null, "timeDec", null);
            entryPoint.insert(event);
            ksession.fireAllRules();
            assertThat(result.get(result.size() - 1).longValue()).isEqualTo(results[i]);
            clock.advanceTime(10, TimeUnit.MILLISECONDS);
        }
    }

    @ParameterizedTest(name = "KieBase type={0}")
    @MethodSource("parameters")
    public void testDeclaredLengthWindowInRule(KieBaseTestConfiguration kieBaseTestConfiguration) {
        initialization(kieBaseTestConfiguration);
        EntryPoint entryPoint = ksession.getEntryPoint("EventStream");
        List<Long> result = new ArrayList<Long>();
        ksession.setGlobal("result", result);
        TestEvent event;

        for (int i = 1; i <= 10; i++) {
            event = new TestEvent(null, "lengthDec", null);
            entryPoint.insert(event);
            ksession.fireAllRules();
            assertThat(result.get(result.size() - 1)
                    .longValue()).isEqualTo((Math.min(i, 5)));
        }
    }

    public class TestEvent implements Serializable {

        private static final long serialVersionUID = -6985691286327371275L;

        private final Integer id;
        private final String name;
        private Serializable value;

        public TestEvent(Integer id, String name, Serializable value) {
            this.id = id;
            this.name = name;
            this.value = value;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Serializable getValue() {
            return value;
        }

        public void setValue(Serializable value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("TestEvent[id=%s, name=%s, value=%s]", id,
                                 name, value);
        }
    }
}
