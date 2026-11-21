/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.doxia.site;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks that {@link PathTool} keeps the behavior of the plexus-utils implementation it replaces.
 * The expected values are taken from that implementation's API documentation.
 */
class PathToolTest {
    @Test
    void relativePathFromBaseDirectory() {
        assertEquals("..", PathTool.getRelativePath("/usr/local/", "/usr/local/java/bin"));
        assertEquals("../..", PathTool.getRelativePath("/usr/local/", "/usr/local/java/bin/java.sh"));
        assertEquals("", PathTool.getRelativePath("/usr/local/java/bin/java.sh", "/usr/local/"));
    }

    @Test
    void relativePathWithinFilename() {
        assertEquals("../../../..", PathTool.getRelativePath("/usr/local/java/bin/java.sh"));
        assertEquals(".", PathTool.getRelativePath("java.sh"));
    }

    @Test
    void directoryComponent() {
        assertEquals("/usr/local/java", PathTool.getDirectoryComponent("/usr/local/java/bin"));
        assertEquals(".", PathTool.getDirectoryComponent("java.sh"));
        assertEquals("", PathTool.getDirectoryComponent(null));
    }

    @Test
    void relativeWebPath() {
        assertEquals(
                "plexus-utils/index.html",
                PathTool.getRelativeWebPath(
                        "http://plexus.codehaus.org/", "http://plexus.codehaus.org/plexus-utils/index.html"));
        assertEquals(
                "../../",
                PathTool.getRelativeWebPath(
                        "http://plexus.codehaus.org/plexus-utils/index.html", "http://plexus.codehaus.org/"));
        assertEquals("", PathTool.getRelativeWebPath(null, "http://plexus.codehaus.org/"));
    }

    @Test
    void relativeFilePath() {
        assertEquals(
                "java" + File.separator + "bin", PathTool.getRelativeFilePath("/usr/local", "/usr/local/java/bin"));
        assertEquals(
                ".." + File.separator + ".." + File.separator,
                PathTool.getRelativeFilePath("/usr/local/java/bin", "/usr/local/"));
        assertEquals("", PathTool.getRelativeFilePath(null, "/usr/local"));
    }

    @Test
    void calculateLink() {
        assertEquals("../../index.html", PathTool.calculateLink("/index.html", "../.."));
        assertEquals(
                "http://plexus.codehaus.org/plexus-utils/index.html",
                PathTool.calculateLink("http://plexus.codehaus.org/plexus-utils/index.html", "../.."));
        assertEquals(
                "/usr/local/java/bin/../index.html", PathTool.calculateLink("../index.html", "/usr/local/java/bin"));
        assertEquals("index.html", PathTool.calculateLink("/index.html", "."));
        assertEquals("sub/index.html", PathTool.calculateLink("index.html", "sub"));
    }
}
