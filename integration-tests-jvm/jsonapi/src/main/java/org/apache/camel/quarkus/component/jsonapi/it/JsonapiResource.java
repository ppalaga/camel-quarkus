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
package org.apache.camel.quarkus.component.jsonapi.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.camel.CamelContext;
import org.jboss.logging.Logger;

@Path("/jsonapi")
@ApplicationScoped
public class JsonapiResource {

    private static final Logger LOG = Logger.getLogger(JsonapiResource.class);

    private static final String DATAFORMAT_JSONAPI = "jsonApi";
    @Inject
    CamelContext context;

    @Path("/load/dataformat/jsonapi")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response loadDataformatJsonApi() throws Exception {
        /* This is an autogenerated test */
        if (context.resolveDataFormat(DATAFORMAT_JSONAPI) != null) {
            return Response.ok().build();
        }
        LOG.warnf("Could not load [%s] from the Camel context", DATAFORMAT_JSONAPI);
        return Response.status(500, DATAFORMAT_JSONAPI + " could not be loaded from the Camel context").build();
    }
}
