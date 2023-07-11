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

package org.drools.mvel.integrationtests.phreak;

import org.drools.base.reteoo.NodeTypeEnums;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.phreak.PhreakAccumulateNode;
import org.drools.core.reteoo.AccumulateNode;
import org.drools.core.reteoo.BetaMemory;
import org.drools.core.reteoo.JoinNode;
import org.drools.core.reteoo.LeftTupleNode;
import org.drools.core.reteoo.SegmentMemory;
import org.drools.core.reteoo.SegmentMemory.BetaMemoryPrototype;
import org.drools.core.reteoo.SegmentMemory.MemoryPrototype;
import org.drools.core.reteoo.SegmentMemory.SegmentPrototype;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.kiesession.rulebase.KnowledgeBaseFactory;
import org.junit.Test;

import static org.drools.mvel.integrationtests.phreak.Pair.t;
import static org.drools.mvel.integrationtests.phreak.PhreakJoinNodeTest.createContext;

public class PhreakAccumulateNodeTest {
    BuildContext          buildContext;
    AccumulateNode        accumulateNode;
    JoinNode              sinkNode;
    InternalWorkingMemory wm;
    BetaMemory            bm;
    SegmentMemory         smem;
    
    BetaMemory            bm0;
    SegmentMemory         smem0;
    AccumulateNode.SingleAccumulateMemory sam;

    A a0 = A.a(0);
    A a1 = A.a(1);
    A a2 = A.a(2);
    A a3 = A.a(3);
    A a4 = A.a(4);

    B b0 = B.b(0);

    public void setupAccumulateNode() {
        buildContext = createContext();

        accumulateNode = AccumulateNodeBuilder.create(NodeTypeEnums.AccumulateNode, buildContext )
                .setLeftType( A.class )
                .setBinding( "object", "$object" )
//                .setRightType( B.class )
//                .setConstraint( "object", "!=", "$object" )
                .buildAccumulate();

        sinkNode = (JoinNode) BetaNodeBuilder.create( NodeTypeEnums.JoinNode, buildContext ).build();

        accumulateNode.addTupleSink(sinkNode );

        wm = (InternalWorkingMemory) KnowledgeBaseFactory.newKnowledgeBase(buildContext.getRuleBase()).newKieSession();
        sam = (AccumulateNode.SingleAccumulateMemory) wm.getNodeMemory(accumulateNode);
        bm = sam.getBetaMemory();

        SegmentPrototype proto1 = new SegmentPrototype(accumulateNode, accumulateNode);
        proto1.setNodesInSegment(new LeftTupleNode[]{accumulateNode});
        proto1.setMemories(new MemoryPrototype[]{new BetaMemoryPrototype(0, null)});

        smem = proto1.newSegmentMemory(wm);
        bm.setSegmentMemory( smem );

        SegmentPrototype proto2 = new SegmentPrototype(sinkNode, sinkNode);
        proto2.setNodesInSegment(new LeftTupleNode[]{sinkNode});
        proto2.setMemories(new MemoryPrototype[]{new BetaMemoryPrototype(0, null)});

        smem0 = proto2.newSegmentMemory(wm);
        bm0 =(BetaMemory)  wm.getNodeMemory( sinkNode );
        bm0.setSegmentMemory( smem0 );
        smem.add( smem0 );
    }

    @Test
    public void testAccumulate() {
        setupAccumulateNode();

        Scenario test = new Scenario(PhreakAccumulateNode.class, accumulateNode, sinkNode, this.sam, wm);

        wm.insert(10);

        test
            .left().insert(b0)// Left is usually InitialFactHandle
            .right().insert(a0, a1, a2, a3, a4)
            .result().insert(t(b0, 10))
            .left(b0) // are left memories needed?
            .right(a0, a1, a2, a3, a4) // are right memories needed?
            .run().getActualResultLeftTuples().resetAll();
    }

}
