/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.dmn.model.impl;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.List;
import org.kie.dmn.model.api.FunctionItem;
import org.kie.dmn.model.api.InformationItem;

public abstract class AbstractTFunctionItem extends AbstractTDMNElement implements FunctionItem {

    protected List<InformationItem> parameters;
    protected QName outputTypeRef;

    @Override
    public List<InformationItem> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        return this.parameters;
    }

    @Override
    public QName getOutputTypeRef() {
        return outputTypeRef;
    }

    @Override
    public void setOutputTypeRef(QName value) {
        this.outputTypeRef = value;
    }

}
