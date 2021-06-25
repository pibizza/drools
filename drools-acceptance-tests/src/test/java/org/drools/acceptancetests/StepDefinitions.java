package org.drools.acceptancetests;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.acceptancetests.model.Person;
import org.drools.core.io.impl.ClassPathResource;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Results;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

public class StepDefinitions {

	private List<String> rules = new ArrayList<>();
	private Map<String, Object> facts = new HashMap<>();
	private KieSession kieSession;
	
	@Given("the rule")
	public void given_the_rule(String rule) {
		rules.add(rule);
	}
	
	@Given("a standard session")
	public void given_a_standard_session() {
        KieHelper kieHelper = new KieHelper();
        kieSession = kieHelper.addContent(rules.get(0), ResourceType.DRL).build().newKieSession();
		for (Object fact: facts.values()) {
			kieSession.insert(fact);
		}
	}
	
	@Given("the person {string} with age {int}")
	public void and_the_person_with_age(String name, int age) {
		Person person = new Person();
		person.setName(name);
		person.setAge(age);
		facts.put(name, person);
	}
	
	@Then("the age of {string} is {int}")
	public void the_age_of_someone_is_somuch(String name, int age) {
		assertEquals(age, ((Person)facts.get(name)).getAge());
	}
	
	@When("I fire all the rules") 
	public void I_fire_all_the_rules() {
		kieSession.fireAllRules();
	}
	
}
