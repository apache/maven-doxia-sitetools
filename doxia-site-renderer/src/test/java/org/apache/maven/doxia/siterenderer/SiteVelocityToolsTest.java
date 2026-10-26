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

import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

import org.apache.maven.doxia.site.PublishDate;
import org.apache.maven.doxia.site.SiteModel;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.RenderTool;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.tools.generic.ValueParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteVelocityToolsTest {

    @TempDir
    Path tempDir;

    /**
     * Shaped like the Reflow skin's site-tools.xml: a SafeConfig tool reading the context, a tool without
     * configure(Map), and an override of a built-in tool's property.
     */
    private static final String SITE_TOOLS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<tools>\n"
            + "  <data key=\"answer\" value=\"42\"/>\n"
            + "  <toolbox scope=\"application\">\n"
            + "    <tool class=\"" + CustomTool.class.getName() + "\" greeting=\"Hello\"/>\n"
            + "    <tool class=\"" + PlainTool.class.getName() + "\"/>\n"
            + "    <tool class=\"" + SetterTool.class.getName() + "\" depth=\"5\" flag=\"yes\"/>\n"
            + "    <tool key=\"date\" format=\"MM/dd/yy\"/>\n"
            + "  </toolbox>\n"
            + "</tools>\n";

    @Test
    void beanUtilsAndDigesterAreNotOnTheClasspath() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.apache.commons.beanutils.PropertyUtils"));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.apache.commons.digester3.Digester"));
    }

    @Test
    void siteToolsXmlThroughRenderer() throws Exception {
        Path config = tempDir.resolve("META-INF/maven/site-tools.xml");
        Files.createDirectories(config.getParent());
        Files.write(config, SITE_TOOLS.getBytes(StandardCharsets.UTF_8));

        SiteModel siteModel = new SiteModel();
        PublishDate publishDate = new PublishDate();
        publishDate.setTimezone("UTC");
        siteModel.setPublishDate(publishDate);
        SiteRenderingContext siteRenderingContext = new SiteRenderingContext();
        siteRenderingContext.setSiteModel(siteModel);
        siteRenderingContext.setLocale(Locale.ENGLISH);

        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        Context context;
        try (URLClassLoader loader =
                new URLClassLoader(new URL[] {tempDir.toUri().toURL()}, original)) {
            thread.setContextClassLoader(loader);
            context = new DefaultSiteRenderer().createToolManagedVelocityContext(siteRenderingContext);
        } finally {
            thread.setContextClassLoader(original);
        }
        context.put("project", "demo");

        assertEquals(
                "Hello demo|plain|5 true|10/26/26|42|&lt;x&gt;|Maven 3 &amp; 4",
                evaluate(
                        context,
                        "$custom.hello()|$plain.name|$setter.depth $setter.flag"
                                + "|$date.format($date.toDate('yyyy-MM-dd', '2026-10-26'))|$answer"
                                + "|$render.eval('#if(true)$esc.html(\"<x>\")#end')|$esc.html('Maven 3 & 4')"));
    }

    @Test
    void renderEvalSeesTheContext() {
        Context context = new SiteVelocityTools()
                .toolboxProperty(Scope.REQUEST, "scope", Scope.REQUEST)
                .tool(Scope.REQUEST, RenderTool.class)
                .tool(Scope.APPLICATION, EscapeTool.class)
                .createContext();
        context.put("head", "#if(true)$esc.html('<x>') $foo#end");
        context.put("foo", "bar");

        assertEquals("&lt;x&gt; bar", evaluate(context, "$render.eval($head)"));
    }

    @Test
    void externalEntitiesAreNotResolved() throws Exception {
        Path secret = tempDir.resolve("secret.txt");
        Files.write(secret, "SECRET".getBytes(StandardCharsets.UTF_8));
        Path config = tempDir.resolve("site-tools.xml");
        String xml = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE tools [<!ENTITY x SYSTEM \"" + secret.toUri() + "\">]>\n"
                + "<tools><data key=\"leak\" type=\"string\">&x;</data></tools>\n";
        Files.write(config, xml.getBytes(StandardCharsets.UTF_8));

        Context context;
        try {
            context = new SiteVelocityTools().read(config.toUri().toURL()).createContext();
        } catch (IllegalStateException e) {
            return; // rejecting the document is fine too
        }
        assertFalse(String.valueOf(context.get("leak")).contains("SECRET"));
    }

    @Test
    void toolWithoutClassMustOverrideAnExistingTool() throws Exception {
        Path config = tempDir.resolve("site-tools.xml");
        Files.write(
                config,
                "<tools><toolbox scope=\"application\"><tool key=\"nope\" format=\"x\"/></toolbox></tools>"
                        .getBytes(StandardCharsets.UTF_8));
        URL url = config.toUri().toURL();

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new SiteVelocityTools().read(url));
        assertTrue(e.getMessage().contains("nope"), e.getMessage());
    }

    @Test
    void defaultKey() {
        assertEquals("esc", SiteVelocityTools.defaultKey(EscapeTool.class));
        assertEquals("custom", SiteVelocityTools.defaultKey(CustomTool.class));
        assertEquals("plain", SiteVelocityTools.defaultKey(PlainTool.class));
    }

    @Test
    void convert() {
        assertEquals(Boolean.TRUE, SiteVelocityTools.convert("yes", "auto"));
        assertEquals(Boolean.FALSE, SiteVelocityTools.convert("off", "auto"));
        assertEquals(Boolean.TRUE, SiteVelocityTools.convert("Yes", "auto"));
        assertEquals(Boolean.TRUE, SiteVelocityTools.convert("1", "boolean"));
        assertEquals(1e20, SiteVelocityTools.convert("100000000000000000000", "auto"));
        assertEquals(5, SiteVelocityTools.convert("5", "auto"));
        assertEquals(5_000_000_000L, SiteVelocityTools.convert("5000000000", "auto"));
        assertEquals(1.5, SiteVelocityTools.convert("1.5", "auto"));
        assertEquals("java.lang.Math.PI", SiteVelocityTools.convert("java.lang.Math.PI", "auto"));
        assertEquals("5", SiteVelocityTools.convert("5", "string"));
        assertEquals(Arrays.asList(1, 2), SiteVelocityTools.convert("1, 2", "list.number"));
        assertEquals(Arrays.asList("a", "b"), SiteVelocityTools.convert("a,b", "list"));
    }

    private static String evaluate(Context context, String template) {
        StringWriter writer = new StringWriter();
        new VelocityEngine().evaluate(context, writer, "test", template);
        return writer.toString();
    }

    @DefaultKey("custom")
    public static class CustomTool extends SafeConfig {
        private Context context;

        private String greeting;

        @Override
        protected void configure(ValueParser values) {
            context = (Context) values.get("velocityContext");
            greeting = values.getString("greeting");
        }

        public String hello() {
            return greeting + " " + context.get("project");
        }
    }

    public static class PlainTool {
        public String getName() {
            return "plain";
        }
    }

    public static class SetterTool {
        private int depth;

        private boolean flag;

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        public int getDepth() {
            return depth;
        }

        public boolean getFlag() {
            return flag;
        }
    }
}
