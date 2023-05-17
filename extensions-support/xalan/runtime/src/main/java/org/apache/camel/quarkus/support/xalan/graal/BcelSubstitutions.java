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
package org.apache.camel.quarkus.support.xalan.graal;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.RecomputeFieldValue.Kind;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

final class BcelSubstitutions {
}

/**
 * {@code org.apache.bcel.util.ModularRuntimeImage} references JrtFileSystem that is unavailable on GraalVM.
 */
@TargetClass(className = "org.apache.bcel.util.ModularRuntimeImage")
final class ModularRuntimeImage {

    @Substitute
    public ModularRuntimeImage() {
    }

    @Substitute
    public ModularRuntimeImage(final String javaHome) throws IOException {
    }

    @Substitute
    public void close() throws IOException {
    }

    @Substitute
    public FileSystem getFileSystem() {
        throw new UnsupportedOperationException(
                "ModularRuntimeImage.getFileSystem() was substituted by camel-quarkus-support-xalan");
    }

    @Substitute
    public List<Path> list(final Path dirPath) throws IOException {
        return new ArrayList<>();
    }

    @Substitute
    public List<Path> list(final String dirName) throws IOException {
        return new ArrayList<>();
    }

    @Substitute
    public List<Path> modules() throws IOException {
        return new ArrayList<>();
    }

    @Substitute
    public List<Path> packages() throws IOException {
        return new ArrayList<>();
    }

}

@TargetClass(className = "org.apache.bcel.util.ClassPath", innerClass = "JrtModule")
final class JrtModule {

    @Substitute
    URL getResource(final String name) {
        /* The target method references the JRT filesystem that is not available on GrallVM */
        return null;
    }
}

@TargetClass(className = "org.apache.bcel.util.ClassPath")
final class ClassPath {

    /** The original initializer references the JRT filesystem that is not available on GrallVM */
    @Alias
    @RecomputeFieldValue(kind = Kind.FromAlias)
    public static org.apache.bcel.util.ClassPath SYSTEM_CLASS_PATH = new org.apache.bcel.util.ClassPath("");

    /** The target method references the JRT filesystem that is not available on GrallVM */
    @Substitute
    public static String getClassPath() {
        return "";
    }

}
