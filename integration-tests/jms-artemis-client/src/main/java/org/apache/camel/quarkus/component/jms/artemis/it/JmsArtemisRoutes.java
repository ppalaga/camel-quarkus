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
package org.apache.camel.quarkus.component.jms.artemis.it;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.TransactionManager;

import org.apache.camel.builder.RouteBuilder;

/**
 * NOTE: This only exists as a workaround for this logic not existing in
 * community 2.13.1 camel-quarkus-integration-test-messaging-jms.
 *
 * It should be removed when a better soluton is found.
 */
@ApplicationScoped
public class JmsArtemisRoutes extends RouteBuilder {
    @Inject
    TransactionManager transactionManager;

    @Override
    public void configure() throws Exception {
        from("jms:queue:xa")
                .log("Received message ${body}")
                .to("mock:xaResult");

        from("direct:xa")
                .transacted()
                .process(x -> {
                    transactionManager.getTransaction().enlistResource(new DummyXAResource());
                })
                .to("jms:queue:xa?disableReplyTo=true")
                .choice()
                .when(body().startsWith("fail"))
                .log("Forced to rollback")
                .process(x -> {
                    transactionManager.setRollbackOnly();
                })
                .otherwise()
                .log("Message added: ${body}")
                .endChoice();
    }
}
