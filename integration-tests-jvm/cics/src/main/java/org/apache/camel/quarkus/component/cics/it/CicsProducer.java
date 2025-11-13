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

import java.nio.file.Paths;

import com.redhat.camel.component.cics.CICSComponent;
import com.redhat.camel.component.cics.CICSConfiguration;
import com.redhat.camel.component.cics.pool.CICSPooledGatewayFactory;
import com.redhat.camel.component.cics.pool.CICSSingleGatewayFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CicsProducer {

    @ConfigProperty(name = "ctg.tcp.port")
    int tcpPort;

    @ConfigProperty(name = "ctg.ssl.port")
    int sslPort;

    @ConfigProperty(name = "ctg.host")
    String ctgHost;

    @Named("cics")
    public CICSComponent Component() {
        return new CICSComponent();
    }

    @Named("cicsFactory")
    public CICSComponent factoryComponent() {
        CICSConfiguration configuration = new CICSConfiguration();

        CICSSingleGatewayFactory factory = new CICSSingleGatewayFactory();
        factory.setPort(tcpPort);
        factory.setHost(ctgHost);
        factory.setProtocol("tcp");
        configuration.setGatewayFactory(factory);

        CICSComponent component = new CICSComponent();
        component.setConfiguration(configuration);
        return component;
    }

    @Named("cicsPooledFactory")
    public CICSComponent pooledFactoryComponent() throws Exception {
        CICSConfiguration configuration = new CICSConfiguration();

        CICSSingleGatewayFactory factory = new CICSSingleGatewayFactory();
        factory.setHost(ctgHost);
        factory.setPort(tcpPort);
        factory.setProtocol("tcp");

        CICSPooledGatewayFactory gatewayPool = new CICSPooledGatewayFactory(factory);
        gatewayPool.setMaxTotal(50);
        gatewayPool.setMinIdle(25);
        gatewayPool.setInitialPoolSize(30);
        gatewayPool.setJmxEnabled(true);
        gatewayPool.start();

        configuration.setGatewayFactory(gatewayPool);

        CICSComponent component = new CICSComponent();
        component.setConfiguration(configuration);
        return component;
    }

    @Named("cicsSslFactory")
    public CICSComponent sslFactoryComponent() {
        CICSConfiguration configuration = new CICSConfiguration();

        CICSSingleGatewayFactory factory = new CICSSingleGatewayFactory();
        factory.setPort(sslPort);
        factory.setHost(ctgHost);
        factory.setProtocol("ssl");
        factory.setSslPassword("changeit");
        factory.setSslKeyring(Paths.get("target/certs/localhost-truststore.p12").toAbsolutePath().toString());

        configuration.setGatewayFactory(factory);

        CICSComponent component = new CICSComponent();
        component.setConfiguration(configuration);
        return component;
    }

    @Named("cicsSslPooledFactory")
    public CICSComponent sslPooledFactoryComponent() throws Exception {
        CICSConfiguration configuration = new CICSConfiguration();

        CICSSingleGatewayFactory factory = new CICSSingleGatewayFactory();
        factory.setHost(ctgHost);
        factory.setPort(sslPort);
        factory.setProtocol("ssl");
        factory.setSslPassword("changeit");
        factory.setSslKeyring(Paths.get("target/certs/localhost-truststore.p12").toAbsolutePath().toString());

        CICSPooledGatewayFactory gatewayPool = new CICSPooledGatewayFactory(factory);
        gatewayPool.setMaxTotal(50);
        gatewayPool.setMinIdle(25);
        gatewayPool.setInitialPoolSize(30);
        gatewayPool.setJmxEnabled(true);
        gatewayPool.setSslKeyring(Paths.get("target/certs/localhost-keystore.p12").toAbsolutePath().toString());
        gatewayPool.setSslPassword("changeit");
        gatewayPool.start();
        configuration.setGatewayFactory(gatewayPool);

        CICSComponent component = new CICSComponent();
        component.setConfiguration(configuration);
        return component;
    }

    @Named("cicsWrongSslFactory")
    public CICSComponent sslWrongFactoryComponent() {
        CICSConfiguration configuration = new CICSConfiguration();

        CICSSingleGatewayFactory factory = new CICSSingleGatewayFactory();
        factory.setPort(sslPort);
        factory.setHost(ctgHost);
        factory.setProtocol("ssl");
        factory.setSslPassword("changeit");
        factory.setSslKeyring(Paths.get("target/certs/wrong-truststore.p12").toAbsolutePath().toString());

        configuration.setGatewayFactory(factory);

        CICSComponent component = new CICSComponent();
        component.setConfiguration(configuration);
        return component;
    }
}
