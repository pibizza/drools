/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.drools.beliefs.bayes;

import org.drools.beliefs.graph.Graph;
import org.drools.beliefs.graph.GraphNode;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.drools.beliefs.bayes.GraphTest.addNode;
import static org.drools.beliefs.bayes.GraphTest.bitSet;
import static org.drools.beliefs.bayes.GraphTest.scaleDouble;

public class BayesAbsorbtionTest {

    @Test
    public void testDivide1() {
        double[] newD = {10, 8, 4};
        double[] oldD = {2, 4, 1};
        double[] r = BayesAbsorption.dividePotentials(newD, oldD);

        assertThat(scaleDouble(3, r)).containsExactly(5, 2, 4);
    }

    @Test
    public void testDivide2() {
        double[] newD = {0.5, 1.0, 1.5, 2.0};
        double[] oldD = {0.1, 0.2, 0.3, 0.4};
        double[] r = BayesAbsorption.dividePotentials(newD, oldD);

        assertThat(scaleDouble(3, r)).containsExactly(5.0, 5.0, 5.0, 5.0);
    }

    @Test
    public void testAbsorption1() {
        // Absorbs into node1 into sep. A and B are in node1. A and B are in the sep.
        // this is a straight forward projection
        BayesVariable<String> a = new BayesVariable<>("A", 0, new String[] {"A1", "A2"}, null);
        BayesVariable<String> b = new BayesVariable<>("B", 1, new String[] {"B1", "B2"}, null);

        Graph<BayesVariable> graph = new BayesNetwork();
        GraphNode<BayesVariable> x0 = addNode(graph);
        GraphNode<BayesVariable> x1 = addNode(graph);

        x0.setContent(a);
        x1.setContent(b);


        JunctionTreeClique node1 = new JunctionTreeClique(0, graph, bitSet("0011"));
        JunctionTreeClique node2 = new JunctionTreeClique(1, graph, bitSet("0011"));
        SeparatorState sep = new JunctionTreeSeparator(0, node1, node2, bitSet("0011"), graph).createState();

        BayesVariable[] vars = {a, b};

        BayesVariable[] sepVars = {a, b};
        int[] sepVarPos = PotentialMultiplier.createSubsetVarPos(vars, sepVars);

        int sepVarNumberOfStates = PotentialMultiplier.createNumberOfStates(sepVars);
        int[] sepVarMultipliers = PotentialMultiplier.createIndexMultipliers(sepVars, sepVarNumberOfStates);

        updatePotentials(node1.getPotentials(), 0.44, 0.4);

        double[] oldSepPotentials = new double[sep.getPotentials().length];
        Arrays.fill(oldSepPotentials, 0.2);

        updatePotentials(sep.getPotentials(), 0.5, 0.5);

        BayesAbsorption p = new BayesAbsorption(sepVarPos, oldSepPotentials, sep.getPotentials(), sepVarMultipliers, vars, node1.getPotentials());
        p.absorb();

        assertThat(scaleDouble(3, node1.getPotentials())).containsExactly(0.035, 0.135, 0.3, 0.529);
    }

	private void updatePotentials(double[] potentials, double startingValue, double increment) {
		for (int i = 0; i < potentials.length; i++) {
			potentials[i] = startingValue;
			startingValue += + increment;
        }
	}

    @Test
    public void testAbsorption2() {
        // Absorbs into node1 into sep. A, B and C are in node1. A and B are in the sep.
        // this tests a non separator var, after the vars
        BayesVariable<String> a = new BayesVariable<>("A", 0, new String[] {"A1", "A2"}, null);
        BayesVariable<String> b = new BayesVariable<>("B", 1, new String[] {"B1", "B2"}, null);
        BayesVariable<String> c = new BayesVariable<>("C", 2, new String[] {"C1", "C2"}, null);

        Graph<BayesVariable> graph = new BayesNetwork();
        GraphNode x0 = addNode(graph);
        GraphNode x1 = addNode(graph);
        GraphNode x2 = addNode(graph);

        x0.setContent(a);
        x1.setContent(b);
        x2.setContent(c);


        JunctionTreeClique node1 = new JunctionTreeClique(0, graph, bitSet("0111"));
        JunctionTreeClique node2 = new JunctionTreeClique(1, graph, bitSet("0011"));
        SeparatorState sep = new JunctionTreeSeparator(0, node1, node2, bitSet("0011"), graph).createState();

        BayesVariable[] vars = {a, b, c};

        BayesVariable[] sepVars = {a, b};
        int[] sepVarPos = PotentialMultiplier.createSubsetVarPos(vars, sepVars);

        int sepVarNumberOfStates = PotentialMultiplier.createNumberOfStates(sepVars);
        int[] sepVarMultipliers = PotentialMultiplier.createIndexMultipliers(sepVars, sepVarNumberOfStates);

        double v = 0.44;
        updatePotentials(node1.getPotentials(), 0.44, 0.4);

        double[] oldSepPotentials = new double[sep.getPotentials().length];
        Arrays.fill(oldSepPotentials, 0.2);

        v = 0.5;
        updatePotentials(sep.getPotentials(), 0.5, 0.5);

        BayesAbsorption p = new BayesAbsorption(sepVarPos, oldSepPotentials, sep.getPotentials(), sepVarMultipliers, vars, node1.getPotentials());
        p.absorb();

        assertThat(scaleDouble(3, node1.getPotentials())).containsExactly(0.01, 0.019, 0.055, 0.073, 0.137, 0.163, 0.254, 0.289);
    }

    @Test
    public void testAbsorption3() {
        // Projects from node1 into sep. A, B and C are in node1. A and C are in the sep.
        // this tests a non separator var, in the middle of the vars
        BayesVariable a = new BayesVariable<>("A", 0, new String[] {"A1", "A2"},  null);
        BayesVariable b = new BayesVariable<>("B", 1, new String[] {"B1", "B2"},  null);
        BayesVariable c = new BayesVariable<>("C", 2, new String[] {"C1", "C2"},  null);


        Graph<BayesVariable> graph = new BayesNetwork();
        GraphNode x0 = addNode(graph);
        GraphNode x1 = addNode(graph);
        GraphNode x2 = addNode(graph);


        x0.setContent(a);
        x1.setContent(b);
        x2.setContent(c);
        JunctionTreeClique node1 = new JunctionTreeClique(0, graph, bitSet("0111"));
        JunctionTreeClique node2 = new JunctionTreeClique(1, graph, bitSet("0101"));
        SeparatorState sep = new JunctionTreeSeparator(0, node1, node2, bitSet("0101"), graph).createState();

        BayesVariable[] vars = {a, b, c};

        BayesVariable[] sepVars = {a, c};
        int[] sepVarPos = PotentialMultiplier.createSubsetVarPos(vars, sepVars);

        int sepVarNumberOfStates = PotentialMultiplier.createNumberOfStates(sepVars);
        int[] sepVarMultipliers = PotentialMultiplier.createIndexMultipliers(sepVars, sepVarNumberOfStates);

        updatePotentials(node1.getPotentials(), 0.44, 0.4);

        double[] oldSepPotentials = new double[sep.getPotentials().length];
        Arrays.fill(oldSepPotentials, 0.2);

        updatePotentials(sep.getPotentials(), 0.5, 0.5);

        BayesAbsorption p = new BayesAbsorption(sepVarPos, oldSepPotentials, sep.getPotentials(), sepVarMultipliers, vars, node1.getPotentials());
        p.absorb();

        assertThat(scaleDouble(3, node1.getPotentials())).containsExactly(0.01, 0.038, 0.028, 0.075, 0.139, 0.222, 0.194, 0.295);
    }
}