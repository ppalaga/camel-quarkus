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

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.Assert;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(DebeziumPostgresTestResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DebeziumPostgresTest {

    private static String COMPANY_1 = "Best Company";
    private static String COMPANY_2 = "Even Better Company";
    private static String CITY_1 = "Prague";
    private static String CITY_2 = "Paris";

    Connection connection;

    @Test
    @Order(1)
    public void insert() throws SQLException {
        int res = executeUpdate("INSERT INTO COMPANY (name, city) VALUES ('" + COMPANY_1 + "', '" + CITY_1 + "')");
        Assert.assertEquals(1, res);

        //validate event in queue
        RestAssured.get("/debezium-postgres/getEvent")
                .then()
                .statusCode(200)
                .body(containsString(COMPANY_1));
    }

    //    @Test
    @Order(2)
    public void testUpdate() throws SQLException {
        int res = executeUpdate("INSERT INTO COMPANY (name, city) VALUES ('" + COMPANY_2 + "', '" + CITY_2 + "')");
        Assert.assertEquals(1, res);

        //validate event in queue
        RestAssured.get("/debezium-postgres/getEvent")
                .then()
                .statusCode(200)
                .body(containsString(COMPANY_2));

        res = executeUpdate("UPDATE COMPANY SET name = '" + COMPANY_2 + "_changed' WHERE city = '" + CITY_2 + "'");
        Assert.assertEquals(1, res);

        //validate event with delete is in queue
        RestAssured.get("/debezium-postgres/getEvent")
                .then()
                .statusCode(204)
                .body(is(emptyOrNullString()));
        //validate event with create is in queue
        RestAssured.get("/debezium-postgres/getEvent")
                .then()
                .statusCode(200)
                .body(containsString(COMPANY_2 + "_changed"));
    }

    //    @Test
    @Order(3)
    public void testDelete() throws SQLException {
        int res = executeUpdate("DELETE FROM COMPANY");
        Assert.assertEquals(2, res);

        //validate event with delete is in queue
        RestAssured.get("/debezium-postgres/getEvent")
                .then()
                .statusCode(204)
                .body(is(emptyOrNullString()));
        RestAssured.get("/debezium-postgres/getEvent")
                .then()
                .statusCode(204)
                .body(is(emptyOrNullString()));
    }

    private int executeUpdate(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        }
    }

}
