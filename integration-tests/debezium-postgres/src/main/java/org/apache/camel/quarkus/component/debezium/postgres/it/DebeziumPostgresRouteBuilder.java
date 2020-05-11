package org.apache.camel.quarkus.component.debezium.postgres.it;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.builder.RouteBuilder;

public class DebeziumPostgresRouteBuilder extends RouteBuilder {

    static final AtomicInteger EVENT_COUNTER = new AtomicInteger(0);

    @Override
    public void configure() {

        from("debezium-postgres:localhost?"
                + "databaseHostname={{database.hostname}}"
                + "&databasePort={{database.port}}"
                + "&databaseUser=postgres"
                + "&databasePassword=postgres"
                + "&databaseDbname=postgres"
                + "&databaseServerName=qa"
                //                + "&offsetStorageFileName=test.backup")//                + "&offsetStorage=")
                + "&offsetStorage=org.apache.kafka.connect.storage.MemoryOffsetBackingStore")
                        .log("event created")
                        .to("seda:event");
    }
}
