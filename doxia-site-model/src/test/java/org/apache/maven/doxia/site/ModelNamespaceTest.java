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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelNamespaceTest {

    private static String site(String version) {
        return "<site xmlns=\"http://maven.apache.org/SITE/" + version + "\"\n"
                + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><body/></site>";
    }

    @Test
    void declaredVersion() {
        assertEquals("2.0.0", ModelNamespace.declaredVersion(site("2.0.0"), "SITE"));
        assertEquals(
                "2.1.0", ModelNamespace.declaredVersion("<skin xmlns='http://maven.apache.org/SKIN/2.1.0'/>", "SKIN"));
        assertNull(ModelNamespace.declaredVersion(site("2.0.0"), "SKIN"));
        assertNull(ModelNamespace.declaredVersion("<site><body/></site>", "SITE"));
    }

    @Test
    void onlyANewerModelIsReported() {
        assertFalse(ModelNamespace.isNewerThan(site("2.0.0"), "SITE", "2.1.0"));
        assertFalse(ModelNamespace.isNewerThan(site("2.1.0"), "SITE", "2.1.0"));
        assertTrue(ModelNamespace.isNewerThan(site("2.1.1"), "SITE", "2.1.0"));
        assertTrue(ModelNamespace.isNewerThan(site("2.2.0"), "SITE", "2.1.0"));
        assertTrue(ModelNamespace.isNewerThan(site("2.10"), "SITE", "2.1.0"));
        // no namespace, or the 1.x decoration model: nothing to report
        assertFalse(ModelNamespace.isNewerThan("<site><body/></site>", "SITE", "2.1.0"));
        assertFalse(ModelNamespace.isNewerThan(
                "<project xmlns=\"http://maven.apache.org/DECORATION/1.8.0\"/>", "SITE", "2.1.0"));
    }

    @Test
    void supportedVersionsMatchTheModelsBuiltByModello() throws Exception {
        // doxia-site-model/pom.xml generates site.mdo and skin.mdo readers at this version
        String pom = new String(Files.readAllBytes(new File("pom.xml").toPath()), StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile(
                        "<model>src/main/mdo/site\\.mdo</model>\\s*</models>\\s*(?:<!--.*?-->\\s*)?"
                                + "<version>([^<]+)</version>",
                        Pattern.DOTALL)
                .matcher(pom);
        assertTrue(matcher.find(), "site.mdo version in pom.xml");
        assertEquals(matcher.group(1), ModelNamespace.SITE_MODEL_VERSION);
    }

    @Test
    void supportedSkinVersionMatchesTheSkinModel() throws Exception {
        // doxia-skin-model/pom.xml, next to this module, generates the skin.mdo reader at this version
        String pom = new String(
                Files.readAllBytes(new File("../doxia-skin-model/pom.xml").toPath()), StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile(
                        "<model>src/main/mdo/skin\\.mdo</model>\\s*</models>\\s*(?:<!--.*?-->\\s*)?<version>([^<]+)</version>",
                        Pattern.DOTALL)
                .matcher(pom);
        assertTrue(matcher.find(), "skin.mdo version in doxia-skin-model/pom.xml");
        assertEquals(matcher.group(1), ModelNamespace.SKIN_MODEL_VERSION);
    }
}
