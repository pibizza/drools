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

package org.drools.beliefs.bayes.example;

import org.drools.beliefs.bayes.BayesInstance;
import org.drools.beliefs.bayes.BayesVariable;
import org.drools.beliefs.bayes.BayesNetwork;
import org.drools.beliefs.bayes.JunctionTree;
import org.drools.beliefs.bayes.JunctionTreeBuilder;
import org.drools.beliefs.bayes.JunctionTreeClique;
import org.drools.beliefs.graph.Graph;
import org.drools.beliefs.graph.GraphNode;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.drools.beliefs.bayes.GraphTest.connectParentToChildren;
import static org.drools.beliefs.bayes.GraphTest.scaleDouble;

public class SprinkerTest {
    Graph<BayesVariable> graph = new BayesNetwork();

    GraphNode<BayesVariable> cloudyNode = graph.addNode();
    GraphNode<BayesVariable> sprinklerNode = graph.addNode();
    GraphNode<BayesVariable> rainNode = graph.addNode();
    GraphNode<BayesVariable> wetGrassNode = graph.addNode();

    BayesVariable cloudy = new BayesVariable<>("Cloudy", cloudyNode.getId(), new String[]{"true", "false"}, new double[][]{{0.5, 0.5}});
    BayesVariable sprinkler = new BayesVariable<>("Sprinkler", sprinklerNode.getId(), new String[]{"true", "false"}, new double[][]{{0.5, 0.5}, {0.9, 0.1}});
    BayesVariable rain =  new BayesVariable<>("Rain", rainNode.getId(), new String[] {"true", "false"}, new double[][] {{0.8, 0.2}, {0.2, 0.8}});
    BayesVariable wetGrass = new BayesVariable<>("WetGrass", wetGrassNode.getId(), new String[] {"true", "false"}, new double[][] {{1.0, 0.0}, {0.1, 0.9}, {0.1, 0.9}, {0.01, 0.99}});

    JunctionTree jTree;

    @Before
    public void setUp() {
        connectParentToChildren(cloudyNode, sprinklerNode, rainNode);
        connectParentToChildren(sprinklerNode, wetGrassNode);
        connectParentToChildren(rainNode, wetGrassNode);

        cloudyNode.setContent(cloudy);
        sprinklerNode.setContent(sprinkler);
        rainNode.setContent(rain);
        wetGrassNode.setContent(wetGrass);

        JunctionTreeBuilder jtBuilder = new JunctionTreeBuilder(graph);
        jTree = jtBuilder.build();
    }

    @Test
    public void testInitialize() {
        JunctionTreeClique jtNode = jTree.getRoot();

        // cloud, rain sprinkler
        assertThat(scaleDouble(3, jtNode.getPotentials())).containsExactly(0.2, 0.05, 0.2, 0.05, 0.09, 0.36, 0.01, 0.04);

        // wetGrass
        jtNode = jTree.getRoot().getChildren().get(0).getChild();
        assertThat(scaleDouble(3, jtNode.getPotentials())).containsExactly(1.0, 0.0, 0.1, 0.9, 0.1, 0.9, 0.01, 0.99);
    }

    @Test
    public void testNoEvidence() {
        JunctionTreeBuilder jtBuilder = new JunctionTreeBuilder(graph);
        JunctionTree jTree = jtBuilder.build();

        JunctionTreeClique jtNode = jTree.getRoot();
        BayesInstance bayesInstance = new BayesInstance(jTree);
        bayesInstance.globalUpdate();

        assertThat(scaleDouble(3, bayesInstance.marginalize("Cloudy").getDistribution())).containsExactly(0.5, 0.5);
        assertThat(scaleDouble(3,  bayesInstance.marginalize("Rain").getDistribution())).containsExactly(0.5, 0.5);
        assertThat(scaleDouble(3, bayesInstance.marginalize("Sprinkler").getDistribution())).containsExactly(0.7, 0.3);
        assertThat(scaleDouble(3,  bayesInstance.marginalize("WetGrass").getDistribution())).containsExactly(0.353, 0.647);
    }

    @Test
    public void testGrassWetEvidence() {
        JunctionTreeBuilder jtBuilder = new JunctionTreeBuilder(graph);
        JunctionTree jTree = jtBuilder.build();

        JunctionTreeClique jtNode = jTree.getRoot();
        BayesInstance bayesInstance = new BayesInstance(jTree);

        bayesInstance.setLikelyhood("WetGrass", new double[]{1.0, 0.0});

        bayesInstance.globalUpdate();

        assertThat(scaleDouble(3, bayesInstance.marginalize("Cloudy").getDistribution())).containsExactly(0.639, 0.361);
        assertThat(scaleDouble(3,  bayesInstance.marginalize("Rain").getDistribution())).containsExactly(0.881, 0.119);
        assertThat(scaleDouble(3, bayesInstance.marginalize("Sprinkler").getDistribution())).containsExactly(0.938, 0.062);
        assertThat(scaleDouble(3,  bayesInstance.marginalize("WetGrass").getDistribution())).containsExactly(1.0, 0.0);
    }

    @Test
    public void testSprinklerEvidence() {
        JunctionTreeBuilder jtBuilder = new JunctionTreeBuilder(graph);
        JunctionTree jTree = jtBuilder.build();

        JunctionTreeClique jtNode = jTree.getRoot();
        BayesInstance bayesInstance = new BayesInstance(jTree);

        bayesInstance.setLikelyhood("Sprinkler", new double[]{1.0, 0.0});
        bayesInstance.setLikelyhood("Cloudy", new double[]{1.0, 0.0});

        bayesInstance.globalUpdate();

        assertThat(scaleDouble(3, bayesInstance.marginalize("Cloudy").getDistribution())).containsExactly(1.0, 0.0);
        assertThat(scaleDouble(3,  bayesInstance.marginalize("Rain").getDistribution())).containsExactly(0.8, 0.2);
        assertThat(scaleDouble(3, bayesInstance.marginalize("Sprinkler").getDistribution())).containsExactly(1.0, 0.0);
        assertThat(scaleDouble(3,  bayesInstance.marginalize("WetGrass").getDistribution())).containsExactly(0.82, 0.18);
    }
}
