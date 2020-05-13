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
package org.apache.camel.quarkus.component.debezium.postgres.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class DebeziumPostgresProcessor {

    private static final String FEATURE = "camel-debezium-postgres";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    //    @BuildStep
    //    ReflectiveClassBuildItem registerForReflection(CombinedIndexBuildItem combinedIndex) {
    //        IndexView index = combinedIndex.getIndex();
    //
    //        String[] dtos = index.getKnownClasses().stream()
    //                .map(ci -> ci.name().toString())
    //                .filter(n -> n.startsWith("org.apache.kafka.connect.json") || n.startsWith("io.debezium.connector")
    //                        || n.startsWith("org.apache.kafka"))
    //                .sorted()
    //                .peek(System.out::println)
    //                .toArray(String[]::new);
    //
    //        return new ReflectiveClassBuildItem(false, true, dtos);
    //    }

    //    @BuildStep
    //    IndexDependencyBuildItem registerDependencyForIndex() {
    //        return new IndexDependencyBuildItem("org.apache.kafka", "connect-json");
    //    }

    //    @BuildStep
    //    void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
    //        indexDependency.produce(new IndexDependencyBuildItem("org.apache.kafka", "connect-json"));
    //        indexDependency.produce(new IndexDependencyBuildItem("org.apache.kafka", "connect-api"));
    //    }

    //    @BuildStep
    //    void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
    //        indexDependency.produce(new IndexDependencyBuildItem("org.apache.kafka", "connect-json"));
    //        indexDependency.produce(new IndexDependencyBuildItem("org.apache.kafka", "connect-runtime"));
    //        indexDependency.produce(new IndexDependencyBuildItem("io.debezium", "debezium-connector-postgres"));
    //    }
}
