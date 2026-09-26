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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.doxia.site.LinkItem;
import org.apache.maven.doxia.site.Menu;
import org.apache.maven.doxia.site.MenuItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuBreadcrumbsTest {

    private static MenuItem item(String name, String href, MenuItem... children) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setHref(href);
        if (children.length > 0) {
            item.setItems(Arrays.asList(children));
        }
        return item;
    }

    private static Menu menu(String name, MenuItem... items) {
        Menu menu = new Menu();
        menu.setName(name);
        menu.setItems(Arrays.asList(items));
        return menu;
    }

    private static void assertTrail(List<LinkItem> trail, String... expectedNames) {
        List<String> names = new ArrayList<>();
        for (LinkItem link : trail) {
            names.add(link.getName());
        }
        assertEquals(Arrays.asList(expectedNames), names);
    }

    @Test
    void oneLevelMatch() {
        List<Menu> menus = Collections.singletonList(
                menu("Overview", item("Introduction", "index.html"), item("Download", "download.html")));

        List<LinkItem> trail = MenuBreadcrumbs.derive(menus, "download.html");

        // only the enclosing menu is an ancestor; the matched item itself is not included
        assertTrail(trail, "Overview");
        assertEquals(null, trail.get(0).getHref()); // a Menu has no href of its own
    }

    @Test
    void nestedItemChainTwoLevelsDeep() {
        MenuItem userGuide = item("User Guide", "guides/user/index.html");
        MenuItem guides = item("Guides", null, userGuide);
        List<Menu> menus = Collections.singletonList(menu("Documentation", guides));

        List<LinkItem> trail = MenuBreadcrumbs.derive(menus, "guides/user/index.html");

        assertTrail(trail, "Documentation", "Guides");
        assertEquals(null, trail.get(1).getHref()); // "Guides" has no href of its own, only nested items
    }

    @Test
    void nestedItemChainThreeLevelsDeep() {
        MenuItem leaf = item("Configuration", "guides/user/advanced/configuration.html");
        MenuItem advanced = item("Advanced", "guides/user/advanced/index.html", leaf);
        MenuItem userGuide = item("User Guide", "guides/user/index.html", advanced);
        List<Menu> menus = Collections.singletonList(menu("Documentation", userGuide));

        List<LinkItem> trail = MenuBreadcrumbs.derive(menus, "guides/user/advanced/configuration.html");

        assertTrail(trail, "Documentation", "User Guide", "Advanced");
        assertEquals("guides/user/index.html", trail.get(1).getHref());
        assertEquals("guides/user/advanced/index.html", trail.get(2).getHref());
    }

    @Test
    void pageAbsentFromEveryMenu() {
        List<Menu> menus = Collections.singletonList(menu("Overview", item("Introduction", "index.html")));

        List<LinkItem> trail = MenuBreadcrumbs.derive(menus, "not-in-any-menu.html");

        assertTrue(trail.isEmpty());
    }

    @Test
    void noMenusAtAll() {
        assertTrue(MenuBreadcrumbs.derive(Collections.emptyList(), "index.html").isEmpty());
    }

    @Test
    void secondMenuContainsTheMatch() {
        List<Menu> menus = Arrays.asList(
                menu("Overview", item("Introduction", "index.html")),
                menu("Documentation", item("User Guide", "guides/user/index.html")));

        List<LinkItem> trail = MenuBreadcrumbs.derive(menus, "guides/user/index.html");

        assertTrail(trail, "Documentation");
    }

    @Test
    void hrefWithLeadingSlashMatches() {
        List<Menu> menus = Collections.singletonList(menu("Overview", item("Introduction", "/index.html")));

        assertTrail(MenuBreadcrumbs.derive(menus, "index.html"), "Overview");
    }

    @Test
    void hrefWithLeadingDotSlashMatches() {
        List<Menu> menus = Collections.singletonList(menu("Overview", item("Introduction", "./index.html")));

        assertTrail(MenuBreadcrumbs.derive(menus, "index.html"), "Overview");
    }

    @Test
    void externalHrefNeverMatches() {
        List<Menu> menus = Collections.singletonList(menu("Overview", item("Apache", "https://apache.org/index.html")));

        assertTrue(MenuBreadcrumbs.derive(menus, "index.html").isEmpty());
    }

    @Test
    void hrefWithFragmentNeverMatches() {
        List<Menu> menus = Collections.singletonList(menu("Overview", item("Introduction", "index.html#top")));

        assertTrue(MenuBreadcrumbs.derive(menus, "index.html").isEmpty());
    }

    @Test
    void itemWithoutHrefIsStillWalkedForItsChildren() {
        // a pure category item (no href of its own) whose child matches
        MenuItem child = item("Child", "category/child.html");
        MenuItem category = item("Category", null, child);
        List<Menu> menus = Collections.singletonList(menu("Overview", category));

        assertTrail(MenuBreadcrumbs.derive(menus, "category/child.html"), "Overview", "Category");
    }
}
