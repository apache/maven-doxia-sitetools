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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.doxia.Doxia;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.site.SiteModel;
import org.apache.maven.doxia.site.io.xpp3.SiteXpp3Reader;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext.SiteDirectory;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.doxia.xsd.AbstractXmlValidator;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.testing.PlexusTest;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.ReflectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xml.sax.EntityResolver;

import static org.codehaus.plexus.testing.PlexusExtension.getBasedir;
import static org.codehaus.plexus.testing.PlexusExtension.getTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
@PlexusTest
public class DefaultSiteRendererTest {
    /**
     * All output produced by this test will go here.
     */
    private static final String OUTPUT = "target/output";

    /**
     * The site renderer used to produce output.
     */
    private SiteRenderer siteRenderer;

    @Inject
    private PlexusContainer container;

    private File skinJar = new File(getBasedir(), "target/test-classes/skin.jar");

    private File minimalSkinJar = new File(getBasedir(), "target/test-classes/minimal-skin.jar");

    /**
     * @throws java.lang.Exception if something goes wrong.
     */
    @BeforeEach
    protected void setUp() throws Exception {
        siteRenderer = (SiteRenderer) container.lookup(SiteRenderer.class);

        InputStream skinIS = getClass().getResourceAsStream("velocity-toolmanager.vm");
        JarOutputStream jarOS = new JarOutputStream(new FileOutputStream(skinJar));
        try {
            jarOS.putNextEntry(new ZipEntry("META-INF/maven/site.vm"));
            IOUtil.copy(skinIS, jarOS);
            jarOS.closeEntry();
        } finally {
            IOUtil.close(skinIS);
            IOUtil.close(jarOS);
        }

        skinIS = new ByteArrayInputStream(
                "<main id=\"contentBox\">$bodyContent</main>".getBytes(StandardCharsets.UTF_8));
        jarOS = new JarOutputStream(new FileOutputStream(minimalSkinJar));
        try {
            jarOS.putNextEntry(new ZipEntry("META-INF/maven/site.vm"));
            IOUtil.copy(skinIS, jarOS);
            jarOS.closeEntry();
        } finally {
            IOUtil.close(skinIS);
            IOUtil.close(jarOS);
        }
    }

    /**
     * @throws java.lang.Exception if something goes wrong.
     */
    @AfterEach
    protected void tearDown() throws Exception {
        container.release(siteRenderer);
    }

    /**
     * @throws Exception if something goes wrong.
     */
    @Test
    public void testRenderExceptionMessageWhenLineNumberIsNotAvailable() throws Exception {
        final File testBasedir = getTestFile("src/test/resources/site/xdoc");
        final String testDocument = "head.xml";
        final String exceptionMessage = "parse error occurred";

        Doxia doxiaInstance = container.lookup(Doxia.class);
        Doxia doxiaSpy = spy(doxiaInstance);
        Mockito.doThrow(new ParseException(exceptionMessage))
                .when(doxiaSpy)
                .parse(Mockito.<Reader>any(), Mockito.anyString(), Mockito.<Sink>any(), Mockito.nullable(String.class));
        SiteRenderer siteRenderer = container.lookup(SiteRenderer.class);
        ReflectionUtils.setVariableValueInObject(siteRenderer, "doxia", doxiaSpy);

        DocumentRenderingContext docRenderingContext =
                new DocumentRenderingContext(testBasedir, "", testDocument, "xdoc", "", false);

        try {
            siteRenderer.renderDocument(null, docRenderingContext, new SiteRenderingContext());
            fail("should fail with exception");
        } catch (RendererException e) {
            assertEquals(
                    String.format(
                            "Error parsing '%s%s%s'", testBasedir.getAbsolutePath(), File.separator, testDocument),
                    e.getMessage());
        }
    }

    /**
     * @throws Exception if something goes wrong.
     */
    @Test
    public void testRenderExceptionMessageWhenLineNumberIsAvailable() throws Exception {
        final File testBasedir = getTestFile("src/test/resources/site/xdoc");
        final String testDocumentName = "head.xml";
        final String exceptionMessage = "parse error occurred";

        Doxia doxiaInstance = container.lookup(Doxia.class);
        Doxia doxiaSpy = spy(doxiaInstance);
        Mockito.doThrow(new ParseException(exceptionMessage, 42, 36))
                .when(doxiaSpy)
                .parse(Mockito.<Reader>any(), Mockito.anyString(), Mockito.<Sink>any(), Mockito.nullable(String.class));
        SiteRenderer siteRenderer = container.lookup(SiteRenderer.class);
        ReflectionUtils.setVariableValueInObject(siteRenderer, "doxia", doxiaSpy);

        DocumentRenderingContext docRenderingContext =
                new DocumentRenderingContext(testBasedir, "", testDocumentName, "xdoc", "", false);

        try {
            siteRenderer.renderDocument(null, docRenderingContext, new SiteRenderingContext());
            fail("should fail with exception");
        } catch (RendererException e) {
            assertEquals(
                    String.format(
                            "Error parsing '%s%s%s', line 42",
                            testBasedir.getAbsolutePath(), File.separator, testDocumentName),
                    e.getMessage());
        }
    }

    /**
     * @throws Exception if something goes wrong.
     */
    @Test
    public void testRender() throws Exception {
        // Safety
        org.apache.commons.io.FileUtils.deleteDirectory(getTestFile(OUTPUT));

        // ----------------------------------------------------------------------
        // Render the site from src/test/resources/site to OUTPUT
        // ----------------------------------------------------------------------
        SiteModel siteModel =
                new SiteXpp3Reader().read(new FileInputStream(getTestFile("src/test/resources/site/site.xml")));

        SiteRenderingContext ctxt = getSiteRenderingContext(siteModel, "src/test/resources/site", false);
        ctxt.setRootDirectory(getTestFile(""));
        siteRenderer.render(siteRenderer.locateDocumentFiles(ctxt, true).values(), ctxt, getTestFile(OUTPUT));

        ctxt = getSiteRenderingContext(siteModel, "src/test/resources/site-validate", true);
        ctxt.setRootDirectory(getTestFile(""));
        siteRenderer.render(siteRenderer.locateDocumentFiles(ctxt, true).values(), ctxt, getTestFile(OUTPUT));

        // ----------------------------------------------------------------------
        // Verify specific pages
        // ----------------------------------------------------------------------
        verifyCdcPage();
        verifyNestedItemsPage();
        verifyMultipleBlock();
        verifyMacro();
        verifyEntitiesPage();
        verifyJavascriptPage();
        verifyFaqPage();
        verifyAttributes();
        verifyApt();
        verifyExtensionInFilename();
        verifyNewlines();

        // ----------------------------------------------------------------------
        // Validate the rendering pages
        // ----------------------------------------------------------------------
        validatePages();
    }

    @Test
    public void testExternalReport() throws Exception {
        DocumentRenderer docRenderer = mock(DocumentRenderer.class);
        when(docRenderer.isExternalReport()).thenReturn(true);
        when(docRenderer.getOutputName()).thenReturn("external/index");
        when(docRenderer.getRenderingContext())
                .thenReturn(new DocumentRenderingContext(new File(""), "index.html", "generator:external"));

        SiteRenderingContext context = new SiteRenderingContext();

        siteRenderer.render(Collections.singletonList(docRenderer), context, new File("target/output"));

        verify(docRenderer).renderDocument(isNull(), eq(siteRenderer), eq(context));
    }

    @Test
    public void testVelocityToolManager() throws Exception {
        StringWriter writer = new StringWriter();

        SiteRenderingContext siteRenderingContext = new SiteRenderingContext();
        siteRenderingContext.setSiteModel(new SiteModel());

        Map<String, Object> attributes = new HashMap<>();

        /*
         * We need to add doxiaSiteRendererVersion manually because version property from pom.properties
         * is not available at test time in some cases.
         */
        attributes.put("doxiaSiteRendererVersion", "1.7-bogus");

        siteRenderingContext.setTemplateProperties(attributes);

        siteRenderingContext.setTemplateName("org/apache/maven/doxia/siterenderer/velocity-toolmanager.vm");
        DocumentRenderingContext docRenderingContext =
                new DocumentRenderingContext(new File(""), "document.html", "generator");
        SiteRendererSink sink = new SiteRendererSink(docRenderingContext);
        siteRenderer.mergeDocumentIntoSite(writer, sink, siteRenderingContext);

        String renderResult = writer.toString();
        String expectedResult = IOUtils.toString(
                getClass().getResourceAsStream("velocity-toolmanager.expected.txt"), StandardCharsets.UTF_8);
        expectedResult = StringUtils.unifyLineSeparators(expectedResult);
        assertEquals(expectedResult, renderResult);
    }

    @Test
    public void testVelocityToolManagerForSkin() throws Exception {
        StringWriter writer = new StringWriter();

        File skinFile = skinJar;

        Map<String, Object> attributes = new HashMap<>();

        /*
         * We need to add doxiaSiteRendererVersion manually because version property from pom.properties
         * is not available at test time in some cases.
         */
        attributes.put("doxiaSiteRendererVersion", "1.7-bogus");

        Artifact skin = new DefaultArtifact(
                "org.group", "artifact", VersionRange.createFromVersion("1.1"), null, "jar", "", null);
        skin.setFile(skinFile);
        SiteRenderingContext siteRenderingContext =
                siteRenderer.createContextForSkin(skin, attributes, new SiteModel(), "defaultitle", Locale.ROOT);
        DocumentRenderingContext context = new DocumentRenderingContext(new File(""), "document.html", "generator");
        SiteRendererSink sink = new SiteRendererSink(context);
        siteRenderer.mergeDocumentIntoSite(writer, sink, siteRenderingContext);
        String renderResult = writer.toString();
        String expectedResult = StringUtils.unifyLineSeparators(IOUtils.toString(
                getClass().getResourceAsStream("velocity-toolmanager.expected.txt"), StandardCharsets.UTF_8));
        assertEquals(expectedResult, renderResult);
    }

    @Test
    public void testMatchVersion() throws Exception {
        DefaultSiteRenderer r = (DefaultSiteRenderer) siteRenderer;
        assertTrue(r.matchVersion("1.7", "1.7"));
        assertFalse(r.matchVersion("1.7", "1.8"));
    }

    @Test
    public void testLocateDocumentFiles() throws IOException, RendererException {
        SiteRenderingContext context = new SiteRenderingContext();
        File sourceDirectory = getTestFile("src/test/resources/site-validate");
        context.setRootDirectory(sourceDirectory);
        context.addSiteDirectory(new SiteDirectory(sourceDirectory, true));
        Set<String> outputFiles = siteRenderer.locateDocumentFiles(context).keySet();
        Set<String> expectedOutputFiles = new HashSet<>();
        expectedOutputFiles.add("entityTest.html");
        assertEquals(expectedOutputFiles, outputFiles);
    }

    @Test
    public void testLocateDocumentFilesWithNameClashes() throws IOException, RendererException {
        SiteRenderingContext context = new SiteRenderingContext();
        File sourceDirectory = getTestFile("src/test/resources/site-validate");
        context.setRootDirectory(sourceDirectory);
        context.addSiteDirectory(new SiteDirectory(sourceDirectory, true));
        context.addSiteDirectory(new SiteDirectory(sourceDirectory, true));
        assertThrows(RendererException.class, () -> siteRenderer.locateDocumentFiles(context));
    }

    @Test
    public void testLocateDocumentFilesWithNameClashesInSkippingDuplicatesDirectory()
            throws IOException, RendererException {
        SiteRenderingContext context = new SiteRenderingContext();
        File sourceDirectory = getTestFile("src/test/resources/site-validate");
        context.setRootDirectory(sourceDirectory);
        context.addSiteDirectory(new SiteDirectory(sourceDirectory, true));
        context.addSiteDirectory(new SiteDirectory(sourceDirectory, true, true));
        Set<String> outputFiles = siteRenderer.locateDocumentFiles(context).keySet();
        Set<String> expectedOutputFiles = new HashSet<>();
        expectedOutputFiles.add("entityTest.html");
        assertEquals(expectedOutputFiles, outputFiles);
    }

    private SiteRenderingContext getSiteRenderingContext(SiteModel siteModel, String siteDir, boolean validate)
            throws RendererException, IOException {
        File skinFile = minimalSkinJar;

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("outputEncoding", "UTF-8");

        Artifact skin = new DefaultArtifact(
                "org.group", "artifact", VersionRange.createFromVersion("1.1"), null, "jar", "", null);
        skin.setFile(skinFile);
        SiteRenderingContext siteRenderingContext =
                siteRenderer.createContextForSkin(skin, attributes, siteModel, "defaultTitle", Locale.ROOT);
        siteRenderingContext.addSiteDirectory(new SiteDirectory(getTestFile(siteDir), true));
        siteRenderingContext.setValidate(validate);

        return siteRenderingContext;
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyCdcPage() throws Exception {
        File nestedItems = getTestFile("target/output/cdc.html");
        assertNotNull(nestedItems);
        assertTrue(nestedItems.exists());
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyNestedItemsPage() throws Exception {
        NestedItemsVerifier verifier = new NestedItemsVerifier();
        verifier.verify("target/output/nestedItems.html");
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyMultipleBlock() throws Exception {
        MultipleBlockVerifier verifier = new MultipleBlockVerifier();
        verifier.verify("target/output/multipleblock.html");
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyMacro() throws Exception {
        File macro = getTestFile("target/output/macro.html");
        assertNotNull(macro);
        assertTrue(macro.exists());

        Reader reader = null;
        try {
            reader = ReaderFactory.newXmlReader(macro);
            String content = IOUtil.toString(reader);
            assertEquals(content.indexOf("</macro>"), -1);
        } finally {
            IOUtil.close(reader);
        }
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyEntitiesPage() throws Exception {
        EntitiesVerifier verifier = new EntitiesVerifier();
        verifier.verify("target/output/entityTest.html");
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyJavascriptPage() throws Exception {
        JavascriptVerifier verifier = new JavascriptVerifier();
        verifier.verify("target/output/javascript.html");
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyFaqPage() throws Exception {
        FaqVerifier verifier = new FaqVerifier();
        verifier.verify("target/output/faq.html");
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyAttributes() throws Exception {
        AttributesVerifier verifier = new AttributesVerifier();
        verifier.verify("target/output/attributes.html");
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyApt() throws Exception {
        AbstractVerifier verifier = new AptVerifier();
        verifier.verify("target/output/apt.html");

        verifier = new CommentsVerifier();
        verifier.verify("target/output/apt.html");
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyExtensionInFilename() throws Exception {
        File output = getTestFile("target/output/extension.apt.not.at.end.html");
        assertNotNull(output);
        assertTrue(output.exists());
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyNewlines() throws Exception {
        /* apt */
        checkNewlines(FileUtils.fileRead(getTestFile("target/output/apt.html"), "ISO-8859-1"));
        checkNewlines(FileUtils.fileRead(getTestFile("target/output/cdc.html"), "ISO-8859-1"));
        checkNewlines(FileUtils.fileRead(getTestFile("target/output/interpolation.html"), "ISO-8859-1"));
        /* fml */
        checkNewlines(FileUtils.fileRead(getTestFile("target/output/faq.html"), "ISO-8859-1"));
        /* xdoc */
        checkNewlines(FileUtils.fileRead(getTestFile("target/output/attributes.html"), "ISO-8859-1"));
        checkNewlines(FileUtils.fileRead(getTestFile("target/output/javascript.html"), "ISO-8859-1"));
        checkNewlines(FileUtils.fileRead(getTestFile("target/output/head.html"), "ISO-8859-1"));
        checkNewlines(FileUtils.fileRead(getTestFile("target/output/macro.html"), "ISO-8859-1"));
    }

    private void checkNewlines(String content) {
        int cr = StringUtils.countMatches(content, "\r");
        int lf = StringUtils.countMatches(content, "\n");
        assertTrue(
                (cr == 0) || (cr == lf), "Should contain only Windows or Unix newlines: cr = " + cr + ", lf = " + lf);
    }

    /**
     * Validate the generated pages.
     *
     * @throws Exception if something goes wrong.
     * @since 1.1.1
     */
    public void validatePages() throws Exception {
        new Xhtml5ValidatorTest().validateGeneratedPages();
    }

    protected static class Xhtml5ValidatorTest extends AbstractXmlValidator {

        /**
         * Validate the generated documents.
         *
         * @throws Exception
         */
        public void validateGeneratedPages() throws Exception {
            setValidate(false);
            try {
                testValidateFiles();
            } finally {
                tearDown();
            }
        }

        private static String[] getIncludes() {
            return new String[] {"**/*.html"};
        }

        /** {@inheritDoc} */
        protected String addNamespaces(String content) {
            return content;
        }

        /** {@inheritDoc} */
        protected EntityResolver getEntityResolver() {
            /* HTML5 restricts use of entities to XML only */
            return null;
        }

        /** {@inheritDoc} */
        protected Map<String, String> getTestDocuments() throws IOException {
            Map<String, String> testDocs = new HashMap<>();

            File dir = new File(getBasedir(), "target/output");

            List<String> l =
                    FileUtils.getFileNames(dir, getIncludes()[0], FileUtils.getDefaultExcludesAsString(), true);

            for (String file : l) {
                file = file == null || file.isEmpty() ? file : file.replace("\\", "/");

                Reader reader = ReaderFactory.newXmlReader(new File(file));
                try {
                    testDocs.put(file, IOUtil.toString(reader));
                } finally {
                    IOUtil.close(reader);
                }
            }

            return testDocs;
        }

        /** {@inheritDoc} */
        @Override
        protected boolean isFailErrorMessage(String message) {
            return true;
        }
    }
}
