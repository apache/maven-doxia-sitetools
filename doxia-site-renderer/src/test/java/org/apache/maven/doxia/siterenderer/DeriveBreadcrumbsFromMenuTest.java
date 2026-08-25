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

import javax.inject.Inject;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.maven.doxia.site.Body;
import org.apache.maven.doxia.site.LinkItem;
import org.apache.maven.doxia.site.Menu;
import org.apache.maven.doxia.site.MenuItem;
import org.apache.maven.doxia.site.SiteModel;
import org.apache.velocity.context.Context;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link SiteRenderingContext#isDeriveBreadcrumbsFromMenu()} flag, wired through
 * {@link DefaultSiteRenderer#createDocumentVelocityContext(DocumentRenderingContext, SiteRenderingContext)}.
 */
@PlexusTest
class DeriveBreadcrumbsFromMenuTest {

    @Inject
    private PlexusContainer container;

    private DefaultSiteRenderer siteRenderer;

    @BeforeEach
    void setUp() throws Exception {
        siteRenderer = (DefaultSiteRenderer) container.lookup(SiteRenderer.class);
    }

    private static SiteModel siteModelWithMenu() {
        MenuItem userGuide = new MenuItem();
        userGuide.setName("User Guide");
        userGuide.setHref("guides/user/index.html");

        Menu menu = new Menu();
        menu.setName("Documentation");
        menu.setItems(Collections.singletonList(userGuide));

        Body body = new Body();
        body.setMenus(Collections.singletonList(menu));

        SiteModel siteModel = new SiteModel();
        siteModel.setBody(body);
        return siteModel;
    }

    private static DocumentRenderingContext documentAt(String outputPathWithoutHtmlExtension) {
        return new DocumentRenderingContext(new File("."), outputPathWithoutHtmlExtension, "generator");
    }

    @Test
    void flagOffLeavesSiteModelUntouched() {
        SiteModel siteModel = siteModelWithMenu();
        SiteRenderingContext siteRenderingContext = new SiteRenderingContext();
        siteRenderingContext.setSiteModel(siteModel);
        siteRenderingContext.setDeriveBreadcrumbsFromMenu(false);

        Context context =
                siteRenderer.createDocumentVelocityContext(documentAt("guides/user/index"), siteRenderingContext);

        // the exact same instance is exposed: nothing was derived, nothing was cloned
        assertSame(siteModel, context.get("site"));
        assertSame(siteModel, context.get("decoration"));
        assertTrue(siteModel.getBody().getBreadcrumbs().isEmpty());
    }

    @Test
    void flagOnDerivesBreadcrumbsWhenNoneDeclared() {
        SiteModel siteModel = siteModelWithMenu();
        SiteRenderingContext siteRenderingContext = new SiteRenderingContext();
        siteRenderingContext.setSiteModel(siteModel);
        siteRenderingContext.setDeriveBreadcrumbsFromMenu(true);

        Context context =
                siteRenderer.createDocumentVelocityContext(documentAt("guides/user/index"), siteRenderingContext);

        SiteModel exposed = (SiteModel) context.get("site");
        assertSame(exposed, context.get("decoration"));

        List<LinkItem> breadcrumbs = exposed.getBody().getBreadcrumbs();
        assertEquals(1, breadcrumbs.size());
        assertEquals("Documentation", breadcrumbs.get(0).getName());

        // the original site model, shared across every document of this rendering, is untouched
        assertTrue(siteModel.getBody().getBreadcrumbs().isEmpty());
    }

    @Test
    void explicitBreadcrumbsWinOverDerivation() {
        SiteModel siteModel = siteModelWithMenu();
        LinkItem explicit = new LinkItem();
        explicit.setName("Explicit crumb");
        siteModel.getBody().setBreadcrumbs(Collections.singletonList(explicit));

        SiteRenderingContext siteRenderingContext = new SiteRenderingContext();
        siteRenderingContext.setSiteModel(siteModel);
        siteRenderingContext.setDeriveBreadcrumbsFromMenu(true);

        Context context =
                siteRenderer.createDocumentVelocityContext(documentAt("guides/user/index"), siteRenderingContext);

        // the declared breadcrumbs are used as-is, no derivation is attempted, no clone is made
        assertSame(siteModel, context.get("site"));
        List<LinkItem> breadcrumbs = ((SiteModel) context.get("site")).getBody().getBreadcrumbs();
        assertEquals(1, breadcrumbs.size());
        assertEquals("Explicit crumb", breadcrumbs.get(0).getName());
    }

    @Test
    void pageAbsentFromEveryMenuLeavesBreadcrumbsEmpty() {
        SiteModel siteModel = siteModelWithMenu();
        SiteRenderingContext siteRenderingContext = new SiteRenderingContext();
        siteRenderingContext.setSiteModel(siteModel);
        siteRenderingContext.setDeriveBreadcrumbsFromMenu(true);

        Context context =
                siteRenderer.createDocumentVelocityContext(documentAt("not-in-any-menu/index"), siteRenderingContext);

        // nothing was found: same instance is exposed, exactly as when the flag is off
        assertSame(siteModel, context.get("site"));
        assertTrue(siteModel.getBody().getBreadcrumbs().isEmpty());
    }
}
