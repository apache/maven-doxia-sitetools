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
import java.util.Collections;

import org.codehaus.plexus.testing.PlexusExtension;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentRenderingContextTest {

    @Test
    void stripSuffixFromPath() {
        assertEquals(
                new File("test/base/some/suffix"),
                DocumentRenderingContext.stripSuffixFromPath(new File("test/base/some/suffix"), null));
        assertEquals(
                new File("test/base/some/suffix"),
                DocumentRenderingContext.stripSuffixFromPath(new File("test/base/some/suffix"), ""));
        assertEquals(
                new File("test/base/some"),
                DocumentRenderingContext.stripSuffixFromPath(new File("test/base/some/suffix"), "suffix"));
        assertEquals(
                new File("test/base"),
                DocumentRenderingContext.stripSuffixFromPath(new File("test/base/some/suffix"), "some/suffix"));
        assertThrows(
                IllegalArgumentException.class,
                () -> DocumentRenderingContext.stripSuffixFromPath(new File("test/base/some/suffix"), "other/suffix"));
        assertThrows(
                IllegalArgumentException.class,
                () -> DocumentRenderingContext.stripSuffixFromPath(new File("some/suffix"), "test/base/some/suffix"));
    }

    @Test
    void getDoxiaSource() {
        // non Doxia markup source
        DocumentRenderingContext renderingContext =
                new DocumentRenderingContext(new File("test/base"), "test/base/some/file.html", "generator");
        assertNull(renderingContext.getDoxiaSourcePath());
        File rootDirectory = PlexusExtension.getTestFile("");
        File siteDirectory = PlexusExtension.getTestFile("target/test-classes/site");
        File sourceDirectory = PlexusExtension.getTestFile("src/test/resources/site");
        renderingContext = new DocumentRenderingContext(
                new File(siteDirectory, "markdown"),
                "mermaid.md",
                "markdown",
                "md",
                rootDirectory,
                siteDirectory,
                Collections.singleton(sourceDirectory));
        assertEquals("src/test/resources/site/markdown/mermaid.md", renderingContext.getDoxiaSourcePath());
    }

    /**
     * Test getRelativePath() with various file paths.
     *
     * @throws java.lang.Exception if any.
     */
    @Test
    void filePathWithDot() throws Exception {
        File baseDir = new File("." + File.separatorChar + "test" + File.separatorChar + "resources");

        String document = "file.with.dot.in.name.xml";
        String baseDirRelativePath = "test" + File.separatorChar + "resources";
        DocumentRenderingContext docRenderingContext =
                new DocumentRenderingContext(baseDir, baseDirRelativePath, document, "", "xml", false);
        assertEquals("file.with.dot.in.name.html", docRenderingContext.getOutputPath());
        assertEquals(".", docRenderingContext.getRelativePath());

        document = "file.with.dot.in.name";
        docRenderingContext = new DocumentRenderingContext(baseDir, document, "generator"); // not Doxia source
        assertEquals("file.with.dot.in.name.html", docRenderingContext.getOutputPath());
        assertEquals(".", docRenderingContext.getRelativePath());

        document = "index.xml.vm";
        docRenderingContext = new DocumentRenderingContext(baseDir, baseDirRelativePath, document, "", "xml", false);
        assertEquals("index.html", docRenderingContext.getOutputPath());
        assertEquals(".", docRenderingContext.getRelativePath());

        document = "download.apt.vm";
        docRenderingContext = new DocumentRenderingContext(baseDir, baseDirRelativePath, document, "", "apt", false);
        assertEquals("download.html", docRenderingContext.getOutputPath());
        assertEquals(".", docRenderingContext.getRelativePath());

        document = "path/file.apt";
        docRenderingContext = new DocumentRenderingContext(baseDir, baseDirRelativePath, document, "", "apt", false);
        assertEquals("path/file.html", docRenderingContext.getOutputPath());
        assertEquals("..", docRenderingContext.getRelativePath());

        document = "path/file";
        docRenderingContext = new DocumentRenderingContext(baseDir, document, "generator"); // not Doxia source
        assertEquals("path/file.html", docRenderingContext.getOutputPath());
        assertEquals("..", docRenderingContext.getRelativePath());
    }
}
