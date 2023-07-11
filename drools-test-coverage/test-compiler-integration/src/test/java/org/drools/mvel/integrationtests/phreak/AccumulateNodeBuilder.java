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

import java.util.Map;

import org.drools.base.base.ClassObjectType;
import org.drools.base.rule.Accumulate;
import org.drools.base.rule.Declaration;
import org.drools.base.rule.Pattern;
import org.drools.base.rule.SingleAccumulate;
import org.drools.base.rule.accessor.ReadAccessor;
import org.drools.base.rule.constraint.AlphaNodeFieldConstraint;
import org.drools.core.common.BetaConstraints;
import org.drools.core.common.EmptyBetaConstraints;
import org.drools.core.common.SingleBetaConstraints;
import org.drools.core.reteoo.AccumulateNode;
import org.drools.core.reteoo.CoreComponentFactory;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.LeftTupleSource;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.reteoo.builder.NodeFactory;
import org.drools.model.Variable;
import org.drools.model.impl.DeclarationImpl;
import org.drools.modelcompiler.constraints.LambdaReadAccessor;
import org.drools.mvel.accessors.ClassFieldAccessorStore;
import org.drools.mvel.integrationtests.optaccumulate.ConstraintCollectors;
import org.drools.mvel.integrationtests.optaccumulate.UniAccumulator;

public class AccumulateNodeBuilder {

    BuildContext buildContext;
    int nodeType;
    Class leftType = Object.class;
    Class rightType = Object.class;
    String leftFieldName;
    String leftVariableName;
    String constraintFieldName;
    String constraintOperator;
    String constraintVariableName;

    public AccumulateNodeBuilder(int nodeType, BuildContext buildContext) {
        this.nodeType = nodeType;
        this.buildContext = buildContext;
    }

    public static AccumulateNodeBuilder create(int nodeType, BuildContext buildContext) {
        return new AccumulateNodeBuilder(nodeType, buildContext);
    }

    public AccumulateNodeBuilder setLeftType(Class type) {
        this.leftType = type;
        return this;
    }

    public AccumulateNodeBuilder setRightType(Class type) {
        this.rightType = type;
        return this;
    }

    public AccumulateNodeBuilder setBinding(String leftFieldName,
                                            String leftVariableName) {
        this.leftFieldName = leftFieldName;
        this.leftVariableName = leftVariableName;
        return this;
    }

    public AccumulateNodeBuilder setConstraint(String constraintFieldName,
                                               String constraintOperator,
                                               String constraintVariableName) {
        this.constraintFieldName = constraintFieldName;
        this.constraintOperator = constraintOperator;
        this.constraintVariableName = constraintVariableName;
        return this;
    }

    public AccumulateNode buildAccumulate() {
        LeftTupleSource leftInput = null;
        ObjectSource rightInput = null;
        NodeFactory nFactory = CoreComponentFactory.get().getNodeFactoryService();

        EntryPointNode epn = buildContext.getRuleBase().getRete().getEntryPointNodes().values().iterator().next();

        ObjectTypeNode otn = nFactory.buildObjectTypeNode(buildContext.getNextNodeId(),
                                                          epn,
                                                          new ClassObjectType(leftType),
                                                          buildContext);

        if (leftInput == null) {
            leftInput = nFactory.buildLeftInputAdapterNode(buildContext.getNextNodeId(), otn, buildContext, false);
        }

        if (rightInput == null) {
            rightInput = nFactory.buildObjectTypeNode(buildContext.getNextNodeId(),
                                                      epn,
                                                      new ClassObjectType(rightType),
                                                      buildContext);
        }

        ReteTesterHelper reteTesterHelper = new ReteTesterHelper();

        Pattern pattern = new Pattern(0, new ClassObjectType(leftType));

        LambdaReadAccessor lambdaReadAccessor = new LambdaReadAccessor(leftType, (Object a) -> {
            return ((A) a).getObject();
        });

        pattern.addDeclaration(new Declaration(leftVariableName, lambdaReadAccessor, pattern));

        Map<String, Declaration> innerDeclarations = pattern.getInnerDeclarations();
        //BetaNodeFieldConstraint betaConstraint = null;
        BetaConstraints betaConstraints;
        if (constraintFieldName != null) {
            ClassFieldAccessorStore store = (ClassFieldAccessorStore) reteTesterHelper.getStore();

            ReadAccessor extractor = store.getReader(leftType,
                                                     leftFieldName);

            Declaration declr = new Declaration(leftVariableName,
                                                extractor,
                                                pattern);
            betaConstraints = new SingleBetaConstraints(reteTesterHelper.getBoundVariableConstraint(rightType,
                                                                                                    constraintFieldName,
                                                                                                    declr,
                                                                                                    constraintOperator), buildContext.getRuleBase().getRuleBaseConfiguration());
        } else {
            betaConstraints = new EmptyBetaConstraints();
        }

        Variable varA = new DeclarationImpl(leftType, leftVariableName);

        var collector = ConstraintCollectors.sum((Integer a) -> a);
        UniAccumulator biAccumulator = new UniAccumulator(varA, collector);

        Accumulate accumulate = new SingleAccumulate(pattern,
                                                     pattern.getRequiredDeclarations(),
                                                     biAccumulator);

        return new AccumulateNode(buildContext.getNextNodeId(),
                                  leftInput,
                                  rightInput,
                                  new AlphaNodeFieldConstraint[0],
                                  betaConstraints,
                                  betaConstraints,
                                  accumulate,
                                  buildContext);
    }
}
