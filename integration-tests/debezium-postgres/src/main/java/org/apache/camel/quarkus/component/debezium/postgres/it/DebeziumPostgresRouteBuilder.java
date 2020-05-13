package org.apache.camel.quarkus.component.debezium.postgres.it;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.builder.RouteBuilder;

public class DebeziumPostgresRouteBuilder extends RouteBuilder {

    static final AtomicInteger EVENT_COUNTER = new AtomicInteger(0);

    @Override
    public void configure() {

        from("debezium-postgres:localhost?"
                + "databaseHostname={{" + DebeziumPostgresResource.PROPERTY_HOSTNAME + "}}"
                + "&databasePort={{" + DebeziumPostgresResource.PROPERTY_PORT + "}}"
                + "&databaseUser=" + DebeziumPostgresResource.DB_USERNAME
                + "&databasePassword=" + DebeziumPostgresResource.DB_PASSWORD
                + "&databaseDbname=" + DebeziumPostgresResource.DB_NAME
                + "&databaseServerName=qa"
                //                + "&offsetStorageFileName=test.backup")
                //                + "&offsetStorage=")
                + "&offsetStorage=org.apache.kafka.connect.storage.MemoryOffsetBackingStore")
                        .to("direct:event");
    }
}
