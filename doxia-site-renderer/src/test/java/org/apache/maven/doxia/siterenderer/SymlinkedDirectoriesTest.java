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

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Checks that site directories behave as with the plexus-utils scanner they replaced: symbolic links to
 * directories are followed, and copied resources keep their modification time.
 */
class SymlinkedDirectoriesTest {
    @TempDir
    Path tmp;

    @Test
    void documentUnderSymlinkedDirectoryIsFound() throws IOException {
        Path site = Files.createDirectories(tmp.resolve("site"));
        Files.createFile(site.resolve("index.md"));
        Path shared = Files.createDirectories(tmp.resolve("shared/deep"));
        Files.createFile(shared.resolve("guide.md"));
        link(site.resolve("docs"), tmp.resolve("shared"));

        assertEquals(
                Arrays.asList("docs/deep/guide.md", "index.md"),
                DefaultSiteRenderer.getFileNames(site.toFile(), "**/*", null, true));
    }

    @Test
    void symlinkedResourceDirectoryIsCopied() throws IOException {
        Path resources = Files.createDirectories(tmp.resolve("resources"));
        Path images = Files.createDirectories(tmp.resolve("images"));
        Files.write(images.resolve("logo.png"), new byte[] {1, 2, 3});
        link(resources.resolve("img"), images);
        Path output = tmp.resolve("output");

        new DefaultSiteRenderer().copyDirectory(resources.toFile(), output.toFile());

        assertTrue(Files.isRegularFile(output.resolve("img/logo.png")));
    }

    @Test
    void copiedResourceKeepsLastModifiedTime() throws IOException {
        Path resources = Files.createDirectories(tmp.resolve("resources"));
        Path style = resources.resolve("css/site.css");
        Files.createDirectories(style.getParent());
        Files.write(style, new byte[] {1});
        FileTime old = FileTime.fromMillis(1_000_000_000_000L);
        Files.setLastModifiedTime(style, old);
        Path output = tmp.resolve("output");

        new DefaultSiteRenderer().copyDirectory(resources.toFile(), output.toFile());

        assertEquals(old, Files.getLastModifiedTime(output.resolve("css/site.css")));
    }

    @Test
    void symlinkCycleListsEachFileOnce() throws IOException {
        Path site = Files.createDirectories(tmp.resolve("site/sub"));
        Files.createFile(site.resolve("page.md"));
        link(site.resolve("loop"), tmp.resolve("site"));

        List<String> names =
                DefaultSiteRenderer.getFileNames(tmp.resolve("site").toFile(), "**/*", null, true);

        assertEquals(Arrays.asList("sub/page.md"), names);
    }

    private static void link(Path link, Path target) throws IOException {
        try {
            Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException | FileSystemException e) {
            assumeTrue(false, "symbolic links are not supported here: " + e);
        }
    }
}
