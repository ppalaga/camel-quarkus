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
package org.apache.camel.quarkus.component.bean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.camel.builder.RouteBuilder;

/**
 * A {@link RouteBuilder} with a @Produces method that makes it a CDI bean. It would normally get removed from the CDI
 * container.
 */
@ApplicationScoped
public class RouteBuilderWithProducer extends RouteBuilder {

    @Override
    public void configure() {
        from("direct:with-producer")
                .setBody(exchange -> "with-producer");
    }

    @Produces
    @ApplicationScoped
    Hello hello() {
        return new Hello();
    }

    public static class Hello {
        public String getHello() {
            return "hello";
        }
    }

}
