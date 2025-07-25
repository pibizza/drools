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

import java.util.Collection;

import org.drools.core.impl.InternalRuleBase;
import org.drools.core.impl.RuleBaseFactory;
import org.drools.kiesession.rulebase.KnowledgeBaseFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.time.SessionPseudoClock;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicEvalTest {

    // KieBuilder doesn't have addPackage(). So we don't re-write this test at the moment.
    // If needed, we may test it with KieContainer.updateToVersion()

    KieBase kbase;
    KieSession session;
    SessionPseudoClock clock;
    Collection<? extends Object> effects;
    KnowledgeBuilder kbuilder;
    KieBaseConfiguration baseConfig;
    KieSessionConfiguration sessionConfig;

    @BeforeEach
    public void setUp() throws Exception {

        baseConfig = RuleBaseFactory.newKnowledgeBaseConfiguration();
        // use stream mode to enable proper event processing (see Drools Fusion 5.5.0 Doc "Event Processing Modes")
        baseConfig.setOption( EventProcessingOption.STREAM );
        kbase = KnowledgeBaseFactory.newKnowledgeBase(baseConfig);

        // config
        sessionConfig = RuleBaseFactory.newKnowledgeSessionConfiguration();
        // use a pseudo clock, which starts at 0 and can be advanced manually
        sessionConfig.setOption( ClockTypeOption.PSEUDO );

        // create and return session
        session = kbase.newKieSession(sessionConfig, null);
        clock = session.getSessionClock();

    }

    public void loadPackages( Resource res, ResourceType type ) {
        kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( res, type );

        KnowledgeBuilderErrors errors = kbuilder.getErrors();

        if (errors.size() > 0) {
            for (KnowledgeBuilderError error : errors) {
                System.err.println( error );
            }
            throw new IllegalArgumentException( "Could not parse knowledge." );
        }

    }

    @AfterEach
    public void tearDown() {
        if (session != null) {
            session.dispose();
        }
        kbase = null;
        effects = null;
        clock = null;
        kbuilder = null;
        baseConfig = null;
        sessionConfig = null;
    }

    @Test
    public void testDynamicAdd() {
        String test =
                "\nrule id3" +
                "\nwhen" +
                "\neval(0 < 1)" + // this eval works
                "\nthen" +
                "\ninsertLogical( \"done\" );" +
                "\nend";

        loadPackages( ResourceFactory.newByteArrayResource( test.getBytes() ), ResourceType.DRL );
        ((InternalRuleBase)session.getKieBase()).addPackages(kbuilder.getKnowledgePackages());
        session.addEventListener( new DebugRuleRuntimeEventListener( ) );

        int fired = session.fireAllRules(); // 1
        System.out.println(fired);
        effects = session.getObjects();
        assertThat(effects.contains("done")).as("fired").isTrue();

        // so the above works, let's try it again
        String test2 =
                "\nrule id4" +
                "\nwhen" +
                "\neval(0 == 0 )" + // this eval doesn't
                "\nthen" +
                "\ninsertLogical( \"done2\" );" +
                "\nend";

        loadPackages(ResourceFactory.newByteArrayResource(test2.getBytes()), ResourceType.DRL);
        ((InternalRuleBase)session.getKieBase()).addPackages(kbuilder.getKnowledgePackages());


        fired = session.fireAllRules(); // 0
        System.out.println(fired);
        effects = session.getObjects();
        assertThat(effects.contains("done2")).as("fired").isTrue(); // fails
    }

    @Test
    public void testDynamicAdd2() {
        String test =
                "rule id3\n" +
                "when\n" +
                "eval(0 == 0)\n" +
                "String( this == \"go\" )\n" + // this eval works
                "then\n" +
                "insertLogical( \"done\" );\n" +
                "end\n" +
                "rule id5\n" +
                "when\n" +
                "eval(0 == 0)\n" +
                "Integer( this == 7 )\n" + // this eval works
                "then\n" +
                "insertLogical( \"done3\" );\n" +
                "end\n";


        loadPackages( ResourceFactory.newByteArrayResource( test.getBytes() ), ResourceType.DRL );
        ((InternalRuleBase)session.getKieBase()).addPackages(kbuilder.getKnowledgePackages());
        session.addEventListener( new DebugRuleRuntimeEventListener( ) );

        session.insert( "go" );
        session.insert( 5 );
        session.insert( 7 );

        int fired = session.fireAllRules(); // 1
        System.out.println(fired);
        effects = session.getObjects();
        assertThat(effects.contains("done")).as("fired").isTrue();

        // so the above works, let's try it again
        String test2 =
                "\nrule id4" +
                "\nwhen" +
                "\neval(0 == 0 )" + // this eval doesn't
                "\nInteger( this == 5 )" +
                "\nthen" +
                "\ninsertLogical( \"done2\" );" +
                "\nend";

        loadPackages(ResourceFactory.newByteArrayResource(test2.getBytes()), ResourceType.DRL);
        ((InternalRuleBase)session.getKieBase()).addPackages(kbuilder.getKnowledgePackages());


        fired = session.fireAllRules(); // 0
        System.out.println(fired);
        effects = session.getObjects();
        assertThat(effects.contains("done2")).as("fired").isTrue(); // fails

        for ( Object o : session.getObjects() ) {
            System.out.println( o );
        }
    }
}
