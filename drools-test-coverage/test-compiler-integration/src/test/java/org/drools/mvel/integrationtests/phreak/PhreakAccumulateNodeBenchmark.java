package org.drools.mvel.integrationtests.phreak;

import java.util.concurrent.TimeUnit;

import org.drools.base.reteoo.NodeTypeEnums;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.TupleSets;
import org.drools.core.phreak.PhreakAccumulateNode;
import org.drools.core.reteoo.AccumulateNode;
import org.drools.core.reteoo.BetaMemory;
import org.drools.core.reteoo.JoinNode;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.reteoo.LeftTupleNode;
import org.drools.core.reteoo.SegmentMemory;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.kiesession.rulebase.KnowledgeBaseFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import static org.drools.mvel.integrationtests.phreak.Pair.t;
import static org.drools.mvel.integrationtests.phreak.PhreakJoinNodeTest.createContext;

@State(Scope.Benchmark)
public class PhreakAccumulateNodeBenchmark {

    BuildContext buildContext;
    AccumulateNode        accumulateNode;
    JoinNode              sinkNode;
    InternalWorkingMemory wm;
    BetaMemory            bm;
    SegmentMemory         smem;

    BetaMemory            bm0;
    SegmentMemory         smem0;
    AccumulateNode.SingleAccumulateMemory sam;

    A a0;
    A a1;
    A a2;
    A a3;
    A a4;
    A a5;

    B b0;

    int expectedResult;
    private int expectedUpdateResult1;
    private int expectedUpdateResult2;

    public PhreakAccumulateNodeBenchmark() {

    }

    @Setup(Level.Trial)
    public void doForkSetup() {
        a0 = A.a(0);
        a1 = A.a(1);
        a2 = A.a(2);
        a3 = A.a(3);
        a4 = A.a(4);
        a5 = A.a(5);
        b0 = B.b(0);

        expectedResult = 10;

        expectedUpdateResult1 = 15;
        expectedUpdateResult2 = 17;

        setupAccumulateNode();

    }

    public void setupAccumulateNode() {
        buildContext = createContext();

        accumulateNode = AccumulateNodeBuilder.create(NodeTypeEnums.AccumulateNode, buildContext )
                .setLeftType( A.class )
                .setBinding( "object", "$object" )
                .buildAccumulate();

        sinkNode = (JoinNode) BetaNodeBuilder.create(NodeTypeEnums.JoinNode, buildContext ).build();

        accumulateNode.addTupleSink(sinkNode );

        wm = (InternalWorkingMemory) KnowledgeBaseFactory.newKnowledgeBase(buildContext.getRuleBase()).newKieSession();
        sam = (AccumulateNode.SingleAccumulateMemory) wm.getNodeMemory(accumulateNode);
        bm = sam.getBetaMemory();

        SegmentMemory.SegmentPrototype proto1 = new SegmentMemory.SegmentPrototype(accumulateNode, accumulateNode);
        proto1.setNodesInSegment(new LeftTupleNode[]{accumulateNode});
        proto1.setMemories(new SegmentMemory.MemoryPrototype[]{new SegmentMemory.BetaMemoryPrototype(0, null)});

        smem = proto1.newSegmentMemory(wm);
        bm.setSegmentMemory( smem );

        SegmentMemory.SegmentPrototype proto2 = new SegmentMemory.SegmentPrototype(sinkNode, sinkNode);
        proto2.setNodesInSegment(new LeftTupleNode[]{sinkNode});
        proto2.setMemories(new SegmentMemory.MemoryPrototype[]{new SegmentMemory.BetaMemoryPrototype(0, null)});

        smem0 = proto2.newSegmentMemory(wm);
        bm0 =(BetaMemory)  wm.getNodeMemory(sinkNode );
        bm0.setSegmentMemory( smem0 );
        smem.add( smem0 );
    }

    @Benchmark
    @Fork(value = 3)
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    public Object benchmarkInsert() {

        Scenario test = new Scenario(PhreakAccumulateNode.class, accumulateNode, sinkNode, this.sam, wm);

        wm.insert(expectedResult);

        TupleSets<LeftTuple> actualResultLeftTuples = test
                .left().insert(b0)// Left is usually InitialFactHandle
                .right().insert(a0, a1, a2, a3, a4)
                .result().insert(t(b0, expectedResult))
                .left(b0) // are left memories needed?
                .right(a0, a1, a2, a3, a4) // are right memories needed?
                .run().getActualResultLeftTuples();

        actualResultLeftTuples.resetAll();

        wm.reset();

        return actualResultLeftTuples;

    }

    @Benchmark
    @Fork(value = 3)
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    public Object benchmarkUpdate() {
        Scenario test = new Scenario(PhreakAccumulateNode.class, accumulateNode, sinkNode, this.sam, wm);
        test.setAssertEnabled(false);

        int expectedResult = expectedUpdateResult1;
        wm.insert(expectedResult);

        test.left().insert(b0)// Left is usually InitialFactHandle
                .right().insert(a0, a1, a2, a3, a4, a5)
                .preStaged(smem0).insert( )
                .delete( )
                .update( )
                .postStaged(smem0)
                .delete( )
                .update( )
                .run();

        Scenario testUpdate = new Scenario(PhreakAccumulateNode.class, accumulateNode, sinkNode, this.sam, wm);
        testUpdate.setAssertEnabled(false);

        InternalFactHandle fh = wm.getFactHandle(a0);
        wm.getObjectStore().updateHandle( fh, a2 );

        int updatedExpectedResult = expectedUpdateResult2; // a0 becomes 2
        wm.insert(updatedExpectedResult);

        testUpdate.right().update(a2)
                .preStaged(smem0).insert( )
                .delete( )
                .update( )
                .postStaged(smem0).update( t(b0, updatedExpectedResult) )
                .delete( )
                .update( )
                .run();

        wm.reset();

        return testUpdate;
    }
}
