/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.api.event.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AgendaEventListener to track fired rules. When rule is fired for the first
 * time it's added to fired rules and when the rule fires afterwards the counter
 * is incremented to make it possible to track how many times the rule was fired
 */
public abstract class TrackingAgendaEventListener extends DefaultAgendaEventListener {

    private Map<String, Integer> firedRules = new HashMap<>();
    private List<String> firedRulesHistory = new ArrayList<>();

    

    public void firedRule(String rule) {
        if (isRuleFired(rule)) {
        	firedRules.put(rule,
        			firedRules.get(rule) + 1);
        } else {
        	firedRules.put(rule,
                           1);
        }
        firedRulesHistory.add(rule);
    }


    /**
     * Return true if the rule was fired at least once
     * @param rule - name of the rule
     * @return true if the rule was fired
     */
    public boolean isRuleFired(final String rule) {
        return firedRules.containsKey(rule);
    }

    
    /**
     * Returns number saying how many times the rule was fired
     * @param rule - name ot the rule
     * @return number how many times rule was fired, 0 if rule wasn't fired
     */
    public int ruleFiredCount(final String rule) {
        if (isRuleFired(rule)) {
            return firedRules.get(rule);
        } else {
            return 0;
        }
    }

    
    /**
     * @return how many rules were fired
     */
    public int rulesCount() {
        return firedRules.size();
    }

    public int eventCount() {
        return firedRulesHistory.size();
    }
    
    /**
     * Clears all the information
     */
    public void clear() {
    	firedRules.clear();
        firedRulesHistory.clear();
    }

    public Collection<String> getFiredRules() {
        return firedRules.keySet();
    }

    public List<String> getRulesFiredOrder() {
        return firedRulesHistory;
    }
    

	public static class AfterMatchFiredEventListener extends TrackingAgendaEventListener {
	    @Override
	    public void afterMatchFired(final AfterMatchFiredEvent event) {
	    	firedRule(event.getMatch().getRule().getName());
	    }
	}
	
	public static class BeforeMatchFiredEventListener extends TrackingAgendaEventListener {
	    @Override
	    public void beforeMatchFired(final BeforeMatchFiredEvent event) {
	    	firedRule(event.getMatch().getRule().getName());
	    }
	}

	public static class MatchCreatedEventListener extends TrackingAgendaEventListener {
	    @Override
	    public void matchCreated(final MatchCreatedEvent event) {
	    	firedRule(event.getMatch().getRule().getName());
	    }
	}

	public static class MatchCanceledEventListener extends TrackingAgendaEventListener {
	    @Override
	    public void matchCancelled(MatchCancelledEvent event) {
	    	firedRule(event.getMatch().getRule().getName());
	    }
	}

}
