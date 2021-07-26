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
package org.apache.camel.quarkus.component.sql;

import java.util.List;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "camel.sql", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class CamelSqlConfig {

    /**
     * A comma separated list of absolute classpath resource paths pointing at SQL scripts referenced by SQL endpoints.
     * They must not start with slash (e.g. {@code sql/my-script.sql}) and they may optionally start with
     * the {@code classpath:} prefix (e.g. {@code classpath:sql/my-script.sql}).
     *
     * @deprecated use {@code quarkus.native.resources.includes} (always without the {@code classpath:} prefix) instead
     *
     */
    @ConfigItem
    public Optional<List<String>> scriptFiles;
}
