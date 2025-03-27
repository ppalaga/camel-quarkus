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

package org.apache.camel.quarkus.component.cics.it;

import java.time.Duration;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.apache.camel.quarkus.test.support.certificate.CertificatesUtil;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.utility.TestcontainersConfiguration;

public class CicsTestResource implements QuarkusTestResourceLifecycleManager {
    private static final Logger LOG = LoggerFactory.getLogger(CicsTestResource.class);
    private static final String CICS_IMAGE = ConfigProvider.getConfig().getValue("cics.container.image", String.class);

    private GenericContainer container;
    private int sslPort, tcpPort;

    @Override
    public Map<String, String> start() {
        LOG.info(TestcontainersConfiguration.getInstance().toString());

        try {
            container = startContainer("ctg.ini", 2006);

            return Map.of("ctg.tcp.port", (tcpPort = container.getMappedPort(2006)) + "",
                    "ctg.ssl.port", (sslPort = container.getMappedPort(8573)) + "",
                    "ctg.host", container.getHost());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private GenericContainer startContainer(String iniFile, int tcpPort) {
        var container = new GenericContainer<>(CICS_IMAGE)
                .withEnv("LICENSE", "accept")
                .withExposedPorts(8573, tcpPort)
                .withLogConsumer(new Slf4jLogConsumer(LOG))
                .withCopyToContainer(MountableFile.forHostPath(CertificatesUtil.keystoreFile("localhost", "p12")),
                        "/home/ctg/config/server.keystore")
                .withCopyFileToContainer(MountableFile.forClasspathResource(iniFile), "/var/cicscli/ctg.ini")
                .waitingFor(Wait.forLogMessage(".*CTG6512I CICS Transaction Gateway initialization complete.*", 1))
                .withStartupTimeout(Duration.ofSeconds(60L));

        container.start();
        return container;
    }

    @Override
    public void stop() {
        try {
            if (container != null) {
                container.stop();
            }
        } catch (Exception ex) {
            LOG.error("An issue occurred while stopping the CicsTestResource", ex);
        }
    }

    @Override
    public void inject(Object testInstance) {
        CicsTest test = (CicsTest) testInstance;
        test.sslPort = sslPort;
        test.tcpPort = tcpPort;
        test.host = container.getHost();
    }
}
