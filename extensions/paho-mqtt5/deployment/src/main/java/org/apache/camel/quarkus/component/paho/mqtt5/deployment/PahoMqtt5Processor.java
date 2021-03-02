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
package org.apache.camel.quarkus.component.paho.mqtt5.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;
import org.eclipse.paho.mqttv5.client.internal.SSLNetworkModuleFactory;
import org.eclipse.paho.mqttv5.client.internal.TCPNetworkModuleFactory;
import org.eclipse.paho.mqttv5.client.logging.JSR47Logger;
import org.eclipse.paho.mqttv5.client.spi.NetworkModuleFactory;
import org.eclipse.paho.mqttv5.client.websocket.WebSocketNetworkModuleFactory;
import org.eclipse.paho.mqttv5.client.websocket.WebSocketSecureNetworkModuleFactory;

class PahoMqtt5Processor {

    private static final String FEATURE = "camel-paho-mqtt5";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    ReflectiveClassBuildItem registerReflectiveClasses() {
        return new ReflectiveClassBuildItem(false, false, JSR47Logger.class);
    }

    @BuildStep
    ServiceProviderBuildItem registerServiceProviders() {
        return new ServiceProviderBuildItem(
                NetworkModuleFactory.class.getName(),
                TCPNetworkModuleFactory.class.getName(),
                SSLNetworkModuleFactory.class.getName(),
                WebSocketNetworkModuleFactory.class.getName(),
                WebSocketSecureNetworkModuleFactory.class.getName());
    }

    @BuildStep()
    NativeImageResourceBundleBuildItem hapiMessages() {
        return new NativeImageResourceBundleBuildItem("org.eclipse.paho.mqttv5.client.internal.nls.logcat");
    }

}
