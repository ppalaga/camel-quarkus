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

import com.redhat.camel.component.cics.CICSConstants;
import com.redhat.camel.component.cics.support.CICSDataExchangeType;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.smallrye.certs.Format;
import io.smallrye.certs.junit5.Certificate;
import org.apache.camel.quarkus.test.support.certificate.TestCertificates;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@TestCertificates(certificates = {
        @Certificate(name = "localhost", formats = { Format.PKCS12 }, password = "changeit"),
        @Certificate(name = "wrong", formats = { Format.PKCS12 }, password = "changeit") })
@QuarkusTestResource(CicsTestResource.class)
@QuarkusTest
class CicsTest {

    int sslPort, tcpPort;
    String host;

    @Test
    public void commareaTcpNoFactory() {
        test("cics", CICSDataExchangeType.COMMAREA, "?host=%s&port=%d&protocol=%s".formatted(host, tcpPort, "tcp"));
    }

    @Test
    public void commareaTcpFactory() {
        test("cicsFactory", CICSDataExchangeType.COMMAREA, null);
    }

    @Test
    public void commareaTcpPooledFactory() {
        test("cicsPooledFactory", CICSDataExchangeType.COMMAREA, null);
    }

    @Test
    public void commareaSslNoFactory() {
        test("cics", CICSDataExchangeType.COMMAREA, "?host=%s&port=%d&protocol=%s&sslKeyring=%s&sslPassword=changeit"
                .formatted(host, sslPort, "ssl", "target/certs/localhost-truststore.p12"));
    }

    @Test
    public void commareaSslFactory() {
        test("cicsSslFactory", CICSDataExchangeType.COMMAREA, null);
    }

    @Test
    public void commareaSslPooledFactory() {
        test("cicsSslPooledFactory", CICSDataExchangeType.COMMAREA, null);
    }

    @Test
    public void commareaCorrectUsernameTcpFactory() {
        test("cicsFactory", CICSDataExchangeType.COMMAREA, "?userId=cicstg&password=cicstg");
    }

    @Test
    public void commareaCorrectUsernameTcpFactory2() {
        test("cicsFactory", CICSDataExchangeType.COMMAREA, "?userId=wrong&password=wrong");
    }

    @Test
    public void sslWrongCertificate() {
        test("cicsWrongSslFactory", CICSDataExchangeType.COMMAREA, null, false);
    }

    /**
     * Channel should fail with a program deployed in the server (eciReady).
     * Test verifies, that channel type does not cause aby trouble for the native execution.
     */
    @Test
    public void channelTcpFactory() {
        //for proper channel testing , container has to be sent
        //    private static final Map<String, Object> containers = Map.of("FirstChar", "My First Container",
        //            "SecondByteArray", "My Second Container".getBytes());
        test("cicsFactory", CICSDataExchangeType.CHANNEL, null, false);
    }

    private void test(String component, CICSDataExchangeType dataExchangeType, String urlSuffix) {
        test(component, dataExchangeType, urlSuffix, true);
    }

    private void test(String component, CICSDataExchangeType dataExchangeType, String urlSuffix, boolean success) {
        ValidatableResponse vr = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .queryParam("urlSuffix", urlSuffix)
                .post("/cics/eciReady/" + component + "/" + dataExchangeType.name())
                .then()
                .statusCode(200);

        if (success) {
            vr.body("body", Matchers.notNullValue())
                    .body("$", Matchers.hasKey(CICSConstants.CICS_RETURN_CODE_HEADER))
                    .body("$", Matchers.hasKey(CICSConstants.CICS_RETURN_CODE_STRING_HEADER))
                    .body("$", Matchers.hasKey(CICSConstants.CICS_LUW_TOKEN_HEADER))
                    .body("$", Matchers.hasKey(CICSConstants.CICS_EXTEND_MODE_HEADER))
                    //validate output of the echo app for the commarea
                    .body("body", Matchers.matchesRegex("\\d+/\\d+/\\d+.*"));
        } else {
            vr.body("body", Matchers.emptyOrNullString());
        }
    }
}
