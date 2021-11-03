/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.compiler.lang.api.impl;

import org.drools.compiler.lang.api.AnnotationDescrBuilder;
import org.drools.compiler.lang.api.PackageDescrBuilder;
import org.drools.compiler.lang.api.PatternDescrBuilder;
import org.drools.compiler.lang.api.WindowDeclarationDescrBuilder;
import org.drools.compiler.lang.descr.WindowDeclarationDescr;

public class WindowDeclarationDescrBuilderImpl extends BaseDescrBuilderImpl<PackageDescrBuilder, WindowDeclarationDescr>
        implements
        WindowDeclarationDescrBuilder {

    protected WindowDeclarationDescrBuilderImpl(PackageDescrBuilder parent) {
        super(parent,
                new WindowDeclarationDescr());
    }

    @Override
    public WindowDeclarationDescrBuilder name(String name) {
        descr.setName(name);
        return this;
    }

    @Override
    public AnnotationDescrBuilder<WindowDeclarationDescrBuilder> newAnnotation(String name) {
        AnnotationDescrBuilder<WindowDeclarationDescrBuilder> annotation = new AnnotationDescrBuilderImpl<>(this,
                name);
        descr.addAnnotation(annotation.getDescr());
        return annotation;
    }

    @Override
    public PatternDescrBuilder<WindowDeclarationDescrBuilder> pattern(String type) {
        PatternDescrBuilder<WindowDeclarationDescrBuilder> pattern = new PatternDescrBuilderImpl<>(this,
                type);
        descr.setPattern(pattern.getDescr());
        return pattern;
    }

    @Override
    public PatternDescrBuilder<WindowDeclarationDescrBuilder> pattern() {
        PatternDescrBuilder<WindowDeclarationDescrBuilder> pattern = new PatternDescrBuilderImpl<>(this);
        descr.setPattern(pattern.getDescr());
        return pattern;
    }

}
