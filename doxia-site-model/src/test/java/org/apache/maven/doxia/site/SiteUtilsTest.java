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

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteUtilsTest {
    @Test
    void isLink() {
        assertFalse(SiteUtils.isLink(null));
        assertFalse(SiteUtils.isLink(""));
        assertFalse(SiteUtils.isLink(" "));
        assertTrue(SiteUtils.isLink("http://maven.apache.org/"));
        assertTrue(SiteUtils.isLink("https://maven.apache.org/"));
        assertTrue(SiteUtils.isLink("ftp://maven.apache.org/pub/"));
        assertTrue(SiteUtils.isLink("file:///home"));
        assertTrue(SiteUtils.isLink("mailto:toto@maven.org"));
        assertTrue(SiteUtils.isLink("any-protocol://"));
    }

    @Test
    void getCustomChild() {
        Xpp3Dom dom = new Xpp3Dom("root");
        Xpp3Dom level1 = new Xpp3Dom("level1");
        dom.addChild(level1);
        Xpp3Dom level2 = new Xpp3Dom("level2");
        level2.setValue("value");
        level1.addChild(level2);

        assertEquals(level1, SiteUtils.getCustomChild(dom, "level1"));
        assertEquals(level2, SiteUtils.getCustomChild(dom, "level1.level2"));
        assertNull(SiteUtils.getCustomChild(dom, "no.level2"));
        assertNull(SiteUtils.getCustomChild(dom, "level1.no"));

        assertEquals("value", SiteUtils.getCustomValue(dom, "level1.level2"));
        assertNull(SiteUtils.getCustomValue(dom, "no.level2"));
        assertNull(SiteUtils.getCustomValue(dom, "level1.no"));

        assertEquals("value", SiteUtils.getCustomValue(dom, "level1.level2", "default"));
        assertEquals("default", SiteUtils.getCustomValue(dom, "no.level2", "default"));
        assertEquals("default", SiteUtils.getCustomValue(dom, "level1.no", "default"));
    }
}
