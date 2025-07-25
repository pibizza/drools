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
package org.kie.dmn.model.api.dmndi;

import java.util.List;

import org.kie.dmn.model.api.DMNModelInstrumentedBase;

public interface DiagramElement extends DMNModelInstrumentedBase {

    public DiagramElement.Extension getExtension();

    public void setExtension(DiagramElement.Extension value);

    public Style getStyle();

    public void setStyle(Style value);

    public Style getSharedStyle();

    public void setSharedStyle(Style value);

    public String getId();

    public void setId(String value);

    public static interface Extension {

        public List<Object> getAny();
    }

}
