/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
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

package org.drools.compiler.lang.api.impl;

import org.drools.compiler.lang.api.AnnotationDescrBuilder;
import org.drools.compiler.lang.api.CEDescrBuilder;
import org.drools.compiler.lang.api.PackageDescrBuilder;
import org.drools.compiler.lang.api.QueryDescrBuilder;
import org.drools.compiler.lang.descr.AndDescr;
import org.drools.compiler.lang.descr.QueryDescr;

/**
 * A descr builder for queries
 */
public class QueryDescrBuilderImpl extends BaseDescrBuilderImpl<PackageDescrBuilder, QueryDescr>
        implements
        QueryDescrBuilder {

    protected QueryDescrBuilderImpl(PackageDescrBuilder parent) {
        super(parent, new QueryDescr());
    }

    @Override
    public AnnotationDescrBuilder<QueryDescrBuilder> newAnnotation(String name) {
        AnnotationDescrBuilder<QueryDescrBuilder> annotation = new AnnotationDescrBuilderImpl<>(this, name);
        descr.addAnnotation(annotation.getDescr());
        return annotation;
    }

    @Override
    public QueryDescrBuilder name(String name) {
        descr.setName(name);
        return this;
    }

    @Override
    public CEDescrBuilder<QueryDescrBuilder, AndDescr> lhs() {
        CEDescrBuilder<QueryDescrBuilder, AndDescr> ce = new CEDescrBuilderImpl<>(this, new AndDescr());
        descr.setLhs(ce.getDescr());
        return ce;
    }

    @Override
    public QueryDescrBuilder parameter(String type,
            String variable) {
        descr.addParameter(type,
                variable);
        return this;
    }

}
