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
package io.quarkus.camel.core.runtime.support;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.ShutdownableService;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultModelJAXBContextFactory;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.ModelJAXBContextFactory;
import org.apache.camel.spi.Registry;
import org.apache.camel.support.ResourceHelper;
import org.apache.camel.util.ObjectHelper;
import org.graalvm.nativeimage.ImageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.Arc;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.camel.core.runtime.CamelConfig.BuildTime;
import io.quarkus.camel.core.runtime.CamelConfig.Runtime;
import io.quarkus.camel.core.runtime.CamelRuntime;
import io.quarkus.camel.core.runtime.InitializedEvent;
import io.quarkus.camel.core.runtime.InitializingEvent;
import io.quarkus.camel.core.runtime.StartedEvent;
import io.quarkus.camel.core.runtime.StartingEvent;
import io.quarkus.camel.core.runtime.StoppedEvent;
import io.quarkus.camel.core.runtime.StoppingEvent;

public class FastCamelRuntime implements CamelRuntime {

    private static final Logger log = LoggerFactory.getLogger(FastCamelRuntime.class);

    protected CamelContext context;
    protected BeanContainer beanContainer;
    protected Registry registry;
    protected Properties properties;
    protected List<RoutesBuilder> builders;
    protected BuildTime buildTimeConfig;
    protected Runtime runtimeConfig;
    protected ModelJAXBContextFactory jaxbContextFactory;

    @Override
    public void init(BuildTime buildTimeConfig) {
        this.buildTimeConfig = buildTimeConfig;

        if (buildTimeConfig.disableJaxb) {
            jaxbContextFactory = () -> {
                throw new UnsupportedOperationException();
            };
        } else {
            jaxbContextFactory = new DefaultModelJAXBContextFactory();
            // The creation of the JAXB context is very time consuming, so always prepare it
            // when running in native mode, but lazy create it in java mode so that we don't
            // waste time if using java routes
            if (ImageInfo.inImageBuildtimeCode()) {
                try {
                    jaxbContextFactory.newJAXBContext();
                } catch (JAXBException e) {
                    throw RuntimeCamelException.wrapRuntimeCamelException(e);
                }
            }
        }

        if (!buildTimeConfig.deferInitPhase) {
            doInit();
        }
    }

    @Override
    public void start(Runtime runtimeConfig) throws Exception {
        this.runtimeConfig = runtimeConfig;
        if (buildTimeConfig.deferInitPhase) {
            doInit();
        }
        doStart();
    }

    @Override
    public void stop() throws Exception {
        doStop();
    }

    public void doInit() {
        try {
            this.context = createContext();

            // Configure the camel context using properties in the form:
            //
            //     camel.context.${name} = ${value}
            //
            RuntimeSupport.bindProperties(properties, context, PFX_CAMEL_CONTEXT);

            context.setLoadTypeConverters(false);
            context.setModelJAXBContextFactory(jaxbContextFactory);

            PropertiesComponent pc = createPropertiesComponent(properties);
            RuntimeSupport.bindProperties(pc.getInitialProperties(), pc, PFX_CAMEL_PROPERTIES);
            context.addComponent("properties", pc);

            this.context.getTypeConverterRegistry().setInjector(this.context.getInjector());
            fireEvent(InitializingEvent.class, new InitializingEvent());
            this.context.init();
            fireEvent(InitializedEvent.class, new InitializedEvent());

            loadRoutes(context);
        } catch (Exception e) {
            throw RuntimeCamelException.wrapRuntimeCamelException(e);
        }
    }

    public void doStart() throws Exception {
        fireEvent(StartingEvent.class, new StartingEvent());
        context.start();
        fireEvent(StartedEvent.class, new StartedEvent());

        if (runtimeConfig.dumpRoutes) {
            dumpRoutes();
        }
    }

    protected void doStop() throws Exception {
        fireEvent(StoppingEvent.class, new StoppingEvent());
        context.stop();
        fireEvent(StoppedEvent.class, new StoppedEvent());
        if (context instanceof ShutdownableService) {
            ((ShutdownableService) context).shutdown();
        }
    }

    protected void loadRoutes(CamelContext context) throws Exception {
        for (RoutesBuilder b : builders) {
            context.addRoutes(b);
        }

        List<String> routesUris = buildTimeConfig.routesUris.stream()
                .filter(ObjectHelper::isNotEmpty)
                .collect(Collectors.toList());
        if (ObjectHelper.isNotEmpty(routesUris)) {
            log.debug("Loading xml routes from {}", routesUris);
            ModelCamelContext mcc = context.adapt(ModelCamelContext.class);
            for (String routesUri : routesUris) {
                // TODO: if pointing to a directory, we should load all xmls in it
                //   (maybe with glob support in it to be complete)
                try (InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(mcc, routesUri.trim())) {
                    mcc.addRouteDefinitions(is);
                }
            }
        } else {
            log.debug("No xml routes configured");
        }

        context.adapt(FastCamelContext.class).reifyRoutes();
    }

    protected CamelContext createContext() {
        FastCamelContext context = new FastCamelContext();
        context.setRegistry(registry);
        return context;
    }

    protected <T> void fireEvent(Class<T> clazz, T event) {
        Arc.container().beanManager().getEvent().select(clazz).fire(event);
    }

    public void setBeanContainer(BeanContainer beanContainer) {
        this.beanContainer = beanContainer;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void addProperties(Properties properties) {
        this.properties.putAll(properties);
    }

    @Override
    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public void setBuilders(List<RoutesBuilder> builders) {
        this.builders = builders;
    }

    public CamelContext getContext() {
        return context;
    }

    @Override
    public Registry getRegistry() {
        return registry;
    }

    @Override
    public BuildTime getBuildTimeConfig() {
        return buildTimeConfig;
    }

    @Override
    public Runtime getRuntimeConfig() {
        return runtimeConfig;
    }

    protected PropertiesComponent createPropertiesComponent(Properties initialPoperties) {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setInitialProperties(initialPoperties);

        RuntimeSupport.bindProperties(properties, pc, PFX_CAMEL_PROPERTIES);

        return pc;
    }

    protected void dumpRoutes() {
        List<Route> routes = getContext().getRoutes();
        if (routes.isEmpty()) {
            log.info("No route definitions");
        } else {
            log.info("Route definitions:");
            for (Route route : routes) {
                RouteDefinition def = (RouteDefinition) route.getRouteContext().getRoute();
                log.info(def.toString());
            }
        }
    }

}
