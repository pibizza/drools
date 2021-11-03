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

import org.drools.compiler.lang.api.AbstractClassTypeDeclarationBuilder;
import org.drools.compiler.lang.api.AnnotationDescrBuilder;
import org.drools.compiler.lang.api.FieldDescrBuilder;
import org.drools.compiler.lang.api.PackageDescrBuilder;
import org.drools.compiler.lang.api.TypeDeclarationDescrBuilder;
import org.drools.compiler.lang.descr.TypeDeclarationDescr;

public class TypeDeclarationDescrBuilderImpl extends BaseDescrBuilderImpl<PackageDescrBuilder, TypeDeclarationDescr>
        implements
        TypeDeclarationDescrBuilder {

    protected TypeDeclarationDescrBuilderImpl(PackageDescrBuilder parent) {
        super(parent, new TypeDeclarationDescr());
    }

    @Override
    public TypeDeclarationDescrBuilder name(String type) {
        descr.setTypeName(type);
        return this;
    }

    @Override
    public TypeDeclarationDescrBuilder superType(String type) {
        descr.addSuperType(type);
        return this;
    }

    @Override
    public TypeDeclarationDescrBuilder setTrait(boolean trait) {
        descr.setTrait(trait);
        return this;
    }

    @Override
    public AnnotationDescrBuilder<TypeDeclarationDescrBuilder> newAnnotation(String name) {
        AnnotationDescrBuilder<TypeDeclarationDescrBuilder> annotation = new AnnotationDescrBuilderImpl<>(this, name);
        descr.addAnnotation(annotation.getDescr());
        return annotation;
    }

    @Override
    public FieldDescrBuilder<AbstractClassTypeDeclarationBuilder<TypeDeclarationDescr>> newField(String name) {
        FieldDescrBuilder<AbstractClassTypeDeclarationBuilder<TypeDeclarationDescr>> field = new FieldDescrBuilderImpl(this, name);
        descr.addField(field.getDescr());
        return field;
    }

}
