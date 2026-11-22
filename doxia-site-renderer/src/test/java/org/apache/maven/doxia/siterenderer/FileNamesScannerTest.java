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
package org.apache.maven.doxia.siterenderer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks that the directory scanner in {@link DefaultSiteRenderer} reads include and exclude
 * patterns the way the plexus-utils scanner it replaces did.
 */
class FileNamesScannerTest {
    @TempDir
    Path basedir;

    @BeforeEach
    void createFiles() throws IOException {
        for (String name : Arrays.asList(
                "index.md", "navigation.xml", "changes.xml", "sub/page.md", "sub/deep/other.md", ".svn/entries")) {
            Path file = basedir.resolve(name);
            Files.createDirectories(file.getParent());
            Files.createFile(file);
        }
    }

    @Test
    void excludesAreTrimmed() throws IOException {
        assertEquals(
                Arrays.asList(".svn/entries", "index.md", "sub/deep/other.md", "sub/page.md"),
                scan("navigation.xml, changes.xml", false));
    }

    @Test
    void trailingSeparatorExcludesDirectory() throws IOException {
        assertEquals(Arrays.asList(".svn/entries", "changes.xml", "index.md", "navigation.xml"), scan("sub/", false));
        assertEquals(
                Arrays.asList(".svn/entries", "changes.xml", "index.md", "navigation.xml", "sub/page.md"),
                scan("sub\\deep\\", false));
    }

    @Test
    void wildcards() throws IOException {
        assertEquals(Arrays.asList(".svn/entries", "changes.xml", "navigation.xml"), scan("**/*.md", false));
        assertEquals(
                Arrays.asList(".svn/entries", "changes.xml", "index.md", "navigation.xml", "sub/deep/other.md"),
                scan("sub/*.md", false));
    }

    @Test
    void defaultExcludes() throws IOException {
        assertEquals(
                Arrays.asList("changes.xml", "index.md", "navigation.xml", "sub/deep/other.md", "sub/page.md"),
                scan(null, true));
    }

    private List<String> scan(String excludes, boolean defaultExcludes) throws IOException {
        File dir = basedir.toFile();
        return DefaultSiteRenderer.getFileNames(dir, "**/*", excludes, defaultExcludes);
    }
}
