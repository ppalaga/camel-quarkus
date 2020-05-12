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
package org.apache.camel.quarkus.component.sql.it;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.sql.SqlComponent;
import org.springframework.util.LinkedCaseInsensitiveMap;

@Path("/sql")
@ApplicationScoped
public class SqlResource {

    public static final String DB_NAME = "postgresDB";
    public static final String DB_USERNAME = "user";
    public static final String DB_PASSWORD = "changeit";
    public static final String PROPERTY_HOSTNAME = "quarkus.postgres.hostname";
    public static final String PROPERTY_PORT = "quarkus.postgres.port";

    private static final String DB_CONNECTION = "jdbc:postgresql://{{" + PROPERTY_HOSTNAME + "}}:{{" + PROPERTY_PORT + "}}/"
            + DB_NAME;

    //    @Inject
    //    @DataSource("camel-sql")
    AgroalDataSource dataSource;

    @Inject
    CamelContext context;

    @Inject
    ProducerTemplate producerTemplate;

    void onStart(@Observes org.apache.camel.quarkus.core.CamelMainEvents.BeforeConfigure ev) throws SQLException {
        SqlComponent sqlComponent = context.getComponent("sql", SqlComponent.class);
        String jdbcUrl = context.getPropertiesComponent().resolveProperty(DB_CONNECTION).get();
        AgroalDataSourceConfigurationSupplier configurationSupplier = new AgroalDataSourceConfigurationSupplier()
                .connectionPoolConfiguration(cp -> cp.connectionFactoryConfiguration(cf -> cf.jdbcUrl(jdbcUrl)
                        .principal(new NamePrincipal(DB_USERNAME))
                        .credential(new SimplePassword(DB_PASSWORD)))
                        .maxSize(10));

        dataSource = AgroalDataSource.from(configurationSupplier);
        sqlComponent.setDataSource(dataSource);
        //        postConstruct();
    }

    void beforeStop(@Observes org.apache.camel.quarkus.core.CamelMainEvents.BeforeStop ev) {
        if (dataSource != null) {
            dataSource.close();
        }

    }

    private void postConstruct() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS camel");
                statement.execute("CREATE TABLE camel (ID INT PRIMARY KEY     NOT NULL, species TEXT NOT NULL)");
            }
        }
    }

    @Path("/get/{species}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getCamel(@PathParam("species") String species) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("species", species);

        return producerTemplate.requestBodyAndHeaders("sql:classpath:sql/get-camels.sql",
                null, params,
                String.class);
    }

    @Path("/post")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response createCamel(String species) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("species", species);

        producerTemplate.requestBodyAndHeaders(
                "sql:INSERT INTO camel (id, species) VALUES ('1', :#species)", null,
                params);

        return Response
                .created(new URI("https://camel.apache.org/"))
                .build();
    }

    @SuppressWarnings("unchecked")
    @Path("/storedproc")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String callStoredProcedure(@QueryParam("numA") int numA, @QueryParam("numB") int numB) {
        Map<String, Object> args = new HashMap<>();
        args.put("num1", numA);
        args.put("num2", numB);

        Map<String, List<LinkedCaseInsensitiveMap>> results = producerTemplate
                .requestBodyAndHeaders("sql-stored:ADD_NUMS(INTEGER ${headers.num1},INTEGER ${headers.num2})", null, args,
                        Map.class);

        return results.get("#result-set-1").get(0).get("PUBLIC.ADD_NUMS(?1, ?2)").toString();
    }
}
