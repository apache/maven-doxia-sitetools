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
package org.apache.maven.doxia.tools;

import java.io.File;
import java.util.Locale;

import org.apache.maven.doxia.site.Body;
import org.apache.maven.doxia.site.Menu;
import org.apache.maven.doxia.site.MenuItem;
import org.apache.maven.doxia.site.SiteModel;
import org.apache.maven.doxia.tools.stubs.SiteToolMavenProjectStub;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
class DefaultSiteToolTest {

    private DefaultSiteTool tool = new DefaultSiteTool();

    /**
     * test getNormalizedPath().
     */
    @Test
    void getNormalizedPath() {
        assertNull(DefaultSiteTool.getNormalizedPath(null));
        assertEquals("", DefaultSiteTool.getNormalizedPath(""));
        assertEquals("", DefaultSiteTool.getNormalizedPath("."));
        assertEquals("", DefaultSiteTool.getNormalizedPath("./"));
        assertEquals("foo", DefaultSiteTool.getNormalizedPath("foo"));
        assertEquals("foo/bar", DefaultSiteTool.getNormalizedPath("foo/bar"));
        assertEquals("foo/bar", DefaultSiteTool.getNormalizedPath("foo\\bar"));
        assertEquals("foo/bar", DefaultSiteTool.getNormalizedPath("foo/./bar"));
        assertEquals("foo/bar", DefaultSiteTool.getNormalizedPath("foo//bar"));
        assertEquals("", DefaultSiteTool.getNormalizedPath("foo/../"));
        assertEquals("", DefaultSiteTool.getNormalizedPath("foo/.."));
        assertEquals("bar", DefaultSiteTool.getNormalizedPath("foo/../bar"));
        assertEquals("foo", DefaultSiteTool.getNormalizedPath("./foo"));
        assertEquals("../foo", DefaultSiteTool.getNormalizedPath("../foo"));
        assertEquals("../../foo", DefaultSiteTool.getNormalizedPath("../../foo"));
        assertEquals("index.html", DefaultSiteTool.getNormalizedPath("./foo/../index.html"));

        // note: space is preserved and double slash is removed!
        assertEquals(
                "file:/Documents and Settings/", DefaultSiteTool.getNormalizedPath("file://Documents and Settings/"));
    }

    @SuppressWarnings("deprecation")
    @Test
    void getRelativePath() {
        assertEquals(
                ".." + File.separator + "bar.html",
                tool.getRelativePath("http://example.com/foo/bar.html", "http://example.com/foo/baz.html"));
    }

    @SuppressWarnings("deprecation")
    @Test
    void getRelativePathSame() {
        assertTrue(tool.getRelativePath("http://example.com/foo/bar.html", "http://example.com/foo/bar.html")
                .isEmpty());
    }

    @SuppressWarnings("deprecation")
    @Test
    void getRelativePathDifferentSchemes() {
        assertEquals(
                "scp://example.com/foo/bar.html",
                tool.getRelativePath("scp://example.com/foo/bar.html", "http://example.com/foo/bar.html"));
        assertEquals("file:///tmp/bloop", tool.getRelativePath("file:///tmp/bloop", "scp://localhost:/tmp/blop"));
    }

    @SuppressWarnings("deprecation")
    @Test
    void getRelativePathDifferentDomains() {
        assertEquals(
                "https://example.org/bar.html",
                tool.getRelativePath("https://example.org/bar.html", "https://example.com/bar.html"));
        assertEquals(
                "dav:https://nexus2.mysite.net:123/nexus/content/sites/site/mysite-child/2.0.0/",
                tool.getRelativePath(
                        "dav:https://nexus2.mysite.net:123/nexus/content/sites/site/mysite-child/2.0.0/",
                        "dav:https://nexus1.mysite.net:123/nexus/content/sites/site/mysite-parent/1.0.0/"));
    }

    /**
     * A parent POM without a name is listed under its artifactId, as <code>MavenProject.getName()</code> does.
     */
    @Test
    void parentMenuItemFallsBackToArtifactId() {
        SiteToolMavenProjectStub parent = new SiteToolMavenProjectStub("org.example", "unnamed-parent", "1.0");
        parent.setDistgributionManagementSiteUrl("https://example.org/site");
        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub("org.example", "child", "1.0");
        project.setDistgributionManagementSiteUrl("https://example.org/site/child");

        SiteModel siteModel = new SiteModel();
        Menu menu = new Menu();
        menu.setRef("parent");
        menu.setName("Parent");
        Body body = new Body();
        body.addMenu(menu);
        siteModel.setBody(body);

        tool.populateParentMenu(siteModel, Locale.ENGLISH, project, parent, false);

        MenuItem item = siteModel.getMenuRef("parent").getItems().get(0);
        assertEquals("unnamed-parent", item.getName());
        assertEquals("../index.html", item.getHref());
    }
}
