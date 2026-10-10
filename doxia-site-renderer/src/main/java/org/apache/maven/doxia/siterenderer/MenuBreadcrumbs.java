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
import java.util.Collections;
import java.util.List;

import org.apache.maven.doxia.site.LinkItem;
import org.apache.maven.doxia.site.Menu;
import org.apache.maven.doxia.site.MenuItem;
import org.apache.maven.doxia.site.SiteUtils;

/**
 * Derives a breadcrumb trail for a document from the site descriptor's menu hierarchy, for use on
 * documents which do not declare an explicit <code>&lt;breadcrumbs&gt;</code> element.
 *
 * <p>This only walks the {@link Menu#getItems()} it is given: any <code>ref</code>,
 * <code>inherit</code> or <code>inheritAsRef</code> attribute on a {@link Menu} is expected to
 * already be resolved into concrete {@link MenuItem}s by the caller (as done by the Maven Site
 * Plugin's site tool before the site model reaches the renderer). A menu which is still an
 * unresolved reference simply contributes no items to search.</p>
 *
 * @since 2.1.1
 */
class MenuBreadcrumbs {

    private MenuBreadcrumbs() {}

    /**
     * Locate {@code outputPath} among the {@code href} of the given menus' items (searched
     * recursively through nested {@code <item>} elements), and build a breadcrumb trail from the
     * {@code name} of the enclosing menu followed by the {@code name} of each enclosing item, from
     * the outermost to the innermost. The located item itself is not included in the trail, matching
     * how an explicit <code>&lt;breadcrumbs&gt;</code> element is conventionally written (listing only
     * the ancestors of the current page).
     *
     * @param menus the site's menus, not null
     * @param outputPath the current document's output path, relative to the site root (see
     *      {@link DocumentRenderingContext#getOutputPath()}), not null
     * @return the derived breadcrumb trail, outermost first; empty if {@code outputPath} is not found
     *      in any menu item
     */
    static List<LinkItem> derive(List<Menu> menus, String outputPath) {
        for (Menu menu : menus) {
            List<LinkItem> trail = new ArrayList<>();
            if (menu.getName() != null) {
                LinkItem menuLink = new LinkItem();
                menuLink.setName(menu.getName());
                trail.add(menuLink);
            }
            if (find(menu.getItems(), outputPath, trail)) {
                return trail;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Recursively search {@code items} (and their nested items) for one whose href matches
     * {@code outputPath}. While descending, every item visited on the current path is appended to
     * {@code trail}; if the search backtracks without finding a match, those entries are removed
     * again so that {@code trail} only ever holds ancestors of a found item.
     */
    private static boolean find(List<MenuItem> items, String outputPath, List<LinkItem> trail) {
        if (items == null) {
            return false;
        }
        for (MenuItem item : items) {
            if (matches(item.getHref(), outputPath)) {
                return true;
            }

            int sizeBeforeDescending = trail.size();
            LinkItem itemLink = new LinkItem();
            itemLink.setName(item.getName());
            itemLink.setHref(item.getHref());
            trail.add(itemLink);

            if (find(item.getItems(), outputPath, trail)) {
                return true;
            }

            // this item's branch did not contain outputPath: backtrack
            while (trail.size() > sizeBeforeDescending) {
                trail.remove(trail.size() - 1);
            }
        }
        return false;
    }

    /**
     * Compare a menu item's href to the current document's output path. Both are expected to be
     * relative to the site root: a leading <code>/</code> or <code>./</code> is stripped from the
     * href before comparison. External links (as recognized by {@link SiteUtils#isLink(String)}) and
     * hrefs containing a fragment (<code>#</code>) or query never match. This is a conservative,
     * literal comparison: hrefs written with <code>../</code> segments, or otherwise not relative to
     * the site root, will not be matched.
     */
    private static boolean matches(String href, String outputPath) {
        if (href == null || href.isEmpty() || SiteUtils.isLink(href)) {
            return false;
        }
        if (href.indexOf('#') >= 0 || href.indexOf('?') >= 0) {
            return false;
        }
        String normalized = href;
        if (normalized.startsWith("./")) {
            normalized = normalized.substring(2);
        } else if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized.equals(outputPath);
    }
}
