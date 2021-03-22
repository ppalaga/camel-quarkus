/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.quarkus.componentdsl.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.jboss.logging.Logger;

class ComponentDSLProcessor {
    private static final Logger LOG = Logger.getLogger(ComponentDSLProcessor.class);

    private static final String FEATURE = "camel-componentdsl";

    @BuildStep
    FeatureBuildItem feature() {
        // see https://github.com/apache/camel-quarkus/issues/2354
        LOG.warnf(
                "camel-quarkus-componentdsl is deprecated and will be removed in the future; use camel-quarkus-core instead");
        return new FeatureBuildItem(FEATURE);
    }
}
