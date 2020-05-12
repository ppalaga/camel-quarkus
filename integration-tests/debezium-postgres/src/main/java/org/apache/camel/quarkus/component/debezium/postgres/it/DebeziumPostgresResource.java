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
package org.apache.camel.quarkus.component.debezium.postgres.it;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.jboss.logging.Logger;

@Path("/debezium-postgres")
@ApplicationScoped
public class DebeziumPostgresResource {

    private static final Logger LOG = Logger.getLogger(DebeziumPostgresResource.class);

    public static final String DB_NAME = "postgresDB";
    public static final String DB_USERNAME = "user";
    public static final String DB_PASSWORD = "changeit";
    public static final String PROPERTY_HOSTNAME = "quarkus.postgres.hostname";
    public static final String PROPERTY_PORT = "quarkus.postgres.port";

    private static final String DB_CONNECTION = "jdbc:postgresql://{{" + PROPERTY_HOSTNAME + "}}:{{" + PROPERTY_PORT + "}}/"
            + DB_NAME;

    @Inject
    CamelContext context;

    @Inject
    ConsumerTemplate consumerTemplate;

    AgroalDataSource dataSource;

    void onStart(@Observes org.apache.camel.quarkus.core.CamelMainEvents.BeforeConfigure ev) throws SQLException {
        String jdbcUrl = context.getPropertiesComponent().resolveProperty(DB_CONNECTION).get();
        AgroalDataSourceConfigurationSupplier configurationSupplier = new AgroalDataSourceConfigurationSupplier()
                .connectionPoolConfiguration(cp -> cp.connectionFactoryConfiguration(cf -> cf.jdbcUrl(jdbcUrl)
                        .principal(new NamePrincipal(DB_USERNAME))
                        .credential(new SimplePassword(DB_PASSWORD)))
                        .maxSize(10));

        dataSource = AgroalDataSource.from(configurationSupplier);
    }

    void beforeStop(@Observes org.apache.camel.quarkus.core.CamelMainEvents.BeforeStop ev) {
        if (dataSource != null) {
            dataSource.close();
        }

    }

    @Path("/executeUpdate")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public int insert(String insert) throws SQLException, InterruptedException {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            Statement statement = con.createStatement();
            return statement.executeUpdate(insert);
        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                con.close();
            }
        }
        //nothing happened
        return 0;
    }

    @Path("/getEvent")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getEvent() throws Exception {
        final Exchange message = consumerTemplate.receive("direct:event", 5000);
        return message.getIn().getBody(String.class);
    }

}
