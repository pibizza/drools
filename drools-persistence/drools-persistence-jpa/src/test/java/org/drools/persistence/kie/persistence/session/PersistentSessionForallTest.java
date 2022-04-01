/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.persistence.kie.persistence.session;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.drools.mvel.compiler.Person;
import org.drools.persistence.util.DroolsPersistenceUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.event.rule.TrackingAgendaEventListener;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

import static org.drools.persistence.util.DroolsPersistenceUtil.DROOLS_PERSISTENCE_UNIT_NAME;
import static org.drools.persistence.util.DroolsPersistenceUtil.OPTIMISTIC_LOCKING;
import static org.drools.persistence.util.DroolsPersistenceUtil.PESSIMISTIC_LOCKING;
import static org.drools.persistence.util.DroolsPersistenceUtil.createEnvironment;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class PersistentSessionForallTest {

    private KieSession kieSession;

    private Map<String, Object> context;
    private Environment env;
    private boolean locking;

    @Parameters(name="{0}")
    public static Collection<Object[]> persistence() {
        Object[][] locking = new Object[][] {
                { OPTIMISTIC_LOCKING },
                { PESSIMISTIC_LOCKING }
        };
        return Arrays.asList(locking);
    };

    public PersistentSessionForallTest(String locking) {
        this.locking = PESSIMISTIC_LOCKING.equals(locking);
    }

    @Before
    public void setUp() throws Exception {
        setupPersistence();
        createKieSession();
    }

    private void setupPersistence() {
        context = DroolsPersistenceUtil.setupWithPoolingDataSource(DROOLS_PERSISTENCE_UNIT_NAME);
        env = createEnvironment(context);
        if( locking ) {
            env.set(EnvironmentName.USE_PESSIMISTIC_LOCKING, true);
        }
    }

    private void createKieSession() {
        String drl =
                "import " + Person.class.getCanonicalName() + "\n" +
                "import " + Pet.class.getCanonicalName() + "\n" +
                "\n" +
                "import java.util.ArrayList;\n" +
                "\n" +
                "// all people known as \"cat lady\" have only cats as pets\n" +
                "rule \"Forall1\" when\n" +
                "  forall ( $pet : Pet ( owner.name  == 'cat lady' )\n" +
                "           Pet ( this == $pet, type == Pet.PetType.cat )\n" +
                "    )\n" +
                "then\n" +
                "end\n" +
                "\n" +
                "// all people known as \"dog lady\" have only dogs as pets\n" +
                "rule \"Forall2\" when\n" +
                "  forall ( $pet : Pet ( owner.name == 'dog lady')\n" +
                "           Pet ( this == $pet, type == Pet.PetType.dog )\n" +
                "  )\n" +
                "then\n" +
                "end\n";

        KieBase kbase = new KieHelper().addContent( drl, ResourceType.DRL ).build();
        kieSession = KieServices.Factory.get().getStoreServices().newKieSession( kbase, null, env );
    }

    @After
    public void tearDown() throws Exception {
        cleanUpKieSession();
        DroolsPersistenceUtil.cleanUp(context);
    }

    private void cleanUpKieSession() {
        if (kieSession != null) {
            kieSession.destroy();
        }
    }

    /**
     * Tests marshalling of persistent KieSession with forall.
     */
    @Test
    public void testNotMatchedCombination() {
        TrackingAgendaEventListener listener = new TrackingAgendaEventListener.AfterMatchFiredEventListener();
        kieSession.addEventListener(listener);

        Person owner = new Person("dog lady");
        Pet dog = new Pet(Pet.PetType.dog, owner);

        kieSession.insert(dog);
        kieSession.fireAllRules();

        assertTrue(listener.isRuleFired("Forall2"));
    }

    public static class Pet implements Serializable {

        private static final long serialVersionUID = -3519777750853629395L;

        public enum PetType {
            dog, cat
        }

        private PetType type;
        private int age;
        private Person owner;

        public Pet(PetType type) {
            this.type = type;
            age = 0;
        }

        public Pet(PetType type, Person owner) {
            this(type);
            this.owner = owner;
        }

        public PetType getType() {
            return type;
        }

        public void setType(PetType type) {
            this.type = type;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Person getOwner() {
            return owner;
        }

        public void setOwner(Person owner) {
            this.owner = owner;
        }
    }

}
