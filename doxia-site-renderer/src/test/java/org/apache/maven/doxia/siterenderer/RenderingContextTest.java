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

import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;

import static org.codehaus.plexus.testing.PlexusExtension.getBasedir;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 20 oct. 07
 */
@PlexusTest
public class RenderingContextTest {

    /**
     * Test getRelativePath() with various file paths.
     *
     * @throws java.lang.Exception if any.
     */
    @Test
    public void testFilePathWithDot() throws Exception {
        File baseDir = new File(getBasedir() + File.separatorChar + "test" + File.separatorChar + "resources");

        String document = "file.with.dot.in.name.xml";
        DocumentRenderingContext docRenderingContext =
                new DocumentRenderingContext(baseDir, "test", document, "", "xml", false);
        assertEquals("file.with.dot.in.name.html", docRenderingContext.getOutputPath());
        assertEquals(".", docRenderingContext.getRelativePath());

        document = "file.with.dot.in.name";
        docRenderingContext = new DocumentRenderingContext(baseDir, document, "generator"); // not Doxia source
        assertEquals("file.with.dot.in.name.html", docRenderingContext.getOutputPath());
        assertEquals(".", docRenderingContext.getRelativePath());

        document = "index.xml.vm";
        docRenderingContext = new DocumentRenderingContext(baseDir, "test", document, "", "xml", false);
        assertEquals("index.html", docRenderingContext.getOutputPath());
        assertEquals(".", docRenderingContext.getRelativePath());

        document = "download.apt.vm";
        docRenderingContext = new DocumentRenderingContext(baseDir, "test", document, "", "apt", false);
        assertEquals("download.html", docRenderingContext.getOutputPath());
        assertEquals(".", docRenderingContext.getRelativePath());

        document = "path/file.apt";
        docRenderingContext = new DocumentRenderingContext(baseDir, "test", document, "", "apt", false);
        assertEquals("path/file.html", docRenderingContext.getOutputPath());
        assertEquals("..", docRenderingContext.getRelativePath());

        document = "path/file";
        docRenderingContext = new DocumentRenderingContext(baseDir, document, "generator"); // not Doxia source
        assertEquals("path/file.html", docRenderingContext.getOutputPath());
        assertEquals("..", docRenderingContext.getRelativePath());
    }
}
