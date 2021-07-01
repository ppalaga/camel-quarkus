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
package org.apache.camel.quarkus.component.messaging.qpid.it.qpid;

import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.apache.camel.util.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.TestcontainersConfiguration;

public class AMQBrokerTestResource implements QuarkusTestResourceLifecycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AMQBrokerTestResource.class);
    private static final String AMQ_IMAGE = "registry.redhat.io/amq7/amq-broker:latest";
    private static final String AMQ_USER = "admin";
    private static final String AMQ_PASSWORD = "admin123.";
    private static final int AMQ_PORT = 61616;

    private GenericContainer<?> container;

    @Override
    public Map<String, String> start() {
        LOGGER.info(TestcontainersConfiguration.getInstance().toString());

        try {
            container = new GenericContainer<>(AMQ_IMAGE)
                    .withExposedPorts(AMQ_PORT)
                    .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
                    .withEnv("BROKER_CONFIG_MAX_DISK_USAGE", "100")
                    .withEnv("AMQ_USER", AMQ_USER)
                    .withEnv("AMQ_PASSWORD", AMQ_PASSWORD)
                    .withEnv("AMQ_REQUIRE_LOGIN", "true")
                    .waitingFor(Wait.forListeningPort());

            container.start();

            String brokerUrlTcp = String.format("amqp://127.0.0.1:%d", container.getMappedPort(AMQ_PORT));

            return CollectionHelper.mapOf(
                    "quarkus.qpid-jms.url", brokerUrlTcp,
                    "quarkus.qpid-jms.username", AMQ_USER,
                    "quarkus.qpid-jms.password", AMQ_PASSWORD);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            if (container != null) {
                container.stop();
            }
        } catch (Exception e) {
            // ignored
        }
    }
}
