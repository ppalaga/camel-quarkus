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

import groovy.io.FileType
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.nio.file.Files
import java.nio.file.Path

final Path basePath = project.basedir.toPath()

final JsonSlurper jsonSlurper = new JsonSlurper()
def productSourceJson = jsonSlurper.parse(basePath.resolve('src/main/resources/camel-quarkus-product-source.json'))
def extensions = productSourceJson.extensions
println "extensions = " + extensions

def productJson = [:]

productJson.extensions = []

extensions.each { k, v ->
    productJson.extensions << [
        "group-id": "org.apache.camel.quarkus",
        "artifact-id": k,
        "metadata" : [
            "redhat-support" : [ "supported" ]
        ]
    ]
}
final Path productJsonPath = basePath.resolve('target/camel-quarkus-product.json')
Files.createDirectories(productJsonPath.getParent())
Files.write(productJsonPath, JsonOutput.prettyPrint(JsonOutput.toJson(productJson)).getBytes('UTF-8'))
