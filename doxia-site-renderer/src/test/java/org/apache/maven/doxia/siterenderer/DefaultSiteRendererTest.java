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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
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
import org.codehaus.plexus.util.ReflectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xml.sax.EntityResolver;

import static org.codehaus.plexus.testing.PlexusExtension.getBasedir;
import static org.codehaus.plexus.testing.PlexusExtension.getTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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

    private static File minimalSkinJar;

    @BeforeAll
    static void beforeAll() throws IOException {
        // only create once, as otherwise Windows might have issues overwriting/deleting it while MS Defender is
        // scanning it
        // as multiple test are using it
        minimalSkinJar = new File(getBasedir(), "target/test-classes/minimal-skin.jar");
        createJarFromDirectory(getTestFile("src/test/resources/skin-minimal").toPath(), minimalSkinJar.toPath());
    }

    /**
     * @throws java.lang.Exception if something goes wrong.
     */
    @BeforeEach
    protected void setUp() throws Exception {
        siteRenderer = (SiteRenderer) container.lookup(SiteRenderer.class);
    }

    private static void createJarFromDirectory(Path srcDirectory, Path jarfile) throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try (FileSystem zipfs = FileSystems.newFileSystem(URI.create("jar:" + jarfile.toUri()), env)) {
            deepCopy(srcDirectory, zipfs);
        }
    }

    private static void deepCopy(Path src, FileSystem zipfs) throws IOException {
        try (Stream<Path> stream = Files.walk(src)) {
            stream.forEach(source -> {
                try {
                    // skip root
                    if (!src.equals(source)) {
                        Path relativeSrcPath = src.relativize(source);
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(zipfs.getPath(relativeSrcPath.toString()));
                        } else {
                            Files.copy(
                                    source,
                                    zipfs.getPath(relativeSrcPath.toString()),
                                    StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
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
    void renderExceptionMessageWhenLineNumberIsNotAvailable() throws Exception {
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

        DocumentRenderingContext docRenderingContext = new DocumentRenderingContext(
                testBasedir, "src/test/resources/site/xdoc", testDocument, "xdoc", "", false);

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
    void renderExceptionMessageWhenLineNumberIsAvailable() throws Exception {
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

        DocumentRenderingContext docRenderingContext = new DocumentRenderingContext(
                testBasedir, "src/test/resources/site/xdoc", testDocumentName, "xdoc", "", false);

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
    void render() throws Exception {
        File outputDirectory = getTestFile(OUTPUT);
        // Safety
        org.apache.commons.io.FileUtils.deleteDirectory(outputDirectory);

        // ----------------------------------------------------------------------
        // Render the site from src/test/resources/site to OUTPUT
        // ----------------------------------------------------------------------
        SiteModel siteModel =
                new SiteXpp3Reader().read(new FileInputStream(getTestFile("src/test/resources/site/site.xml")));

        SiteRenderingContext ctxt =
                getSiteRenderingContext(siteModel, minimalSkinJar, "src/test/resources/site", false);
        ctxt.setRootDirectory(getTestFile(""));
        siteRenderer.render(siteRenderer.locateDocumentFiles(ctxt, true).values(), ctxt, outputDirectory);

        ctxt = getSiteRenderingContext(siteModel, minimalSkinJar, "src/test/resources/site-validate", true);
        ctxt.setRootDirectory(getTestFile(""));
        siteRenderer.render(siteRenderer.locateDocumentFiles(ctxt, true).values(), ctxt, outputDirectory);

        siteRenderer.copyResources(ctxt, outputDirectory);
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
        verifyMermaidPage(null);
        // ----------------------------------------------------------------------
        // Validate the rendering pages
        // ----------------------------------------------------------------------
        validatePages();
    }

    void renderDocument(
            SiteRenderingContext context, String baseDir, String document, String extension, String parserId)
            throws Exception {
        File outputDirectory = getTestFile(OUTPUT);
        // Safety
        org.apache.commons.io.FileUtils.deleteDirectory(outputDirectory);

        DocumentRenderingContext docRenderingContext =
                new DocumentRenderingContext(getTestFile(baseDir), baseDir, document, parserId, extension, false);
        DocumentRenderer docRenderer = new DoxiaDocumentRenderer(docRenderingContext);

        siteRenderer.render(Collections.singletonList(docRenderer), context, outputDirectory);
        siteRenderer.copyResources(context, outputDirectory);
    }

    @Test
    void mermaidWithExternalJs() throws Exception {
        SiteModel siteModel = new SiteXpp3Reader()
                .read(new FileInputStream(getTestFile("src/test/resources/site-mermaid-externaljs/site.xml")));
        SiteRenderingContext context =
                getSiteRenderingContext(siteModel, minimalSkinJar, "src/test/resources/site", false);

        renderDocument(context, "src/test/resources/site/markdown", "mermaid.md", "md", "markdown");

        verifyMermaidPage(
                "<script src=\"https://cdn.jsdelivr.net/npm/mermaid@11.12.3/dist/mermaid.min.js\" integrity=\"sha512-/yppbXbmq0NnMz+OhfzrvQDSePwPAixE8Ywi8U7KayJPv5ZFVYo7bzBgkR5V6OQ/8YHEh/gPMarWD+6ZIuNj5A==\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\" fetchpriority=\"high\"></script>");
    }

    @Test
    void externalReport() throws Exception {
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
    void velocityToolManager() throws Exception {
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
                new DocumentRenderingContext(new File("."), "document.html", "generator");
        SiteRendererSink sink = new SiteRendererSink(docRenderingContext);
        siteRenderer.mergeDocumentIntoSite(writer, sink, siteRenderingContext);

        String renderResult = writer.toString();
        String expectedResult = IOUtils.toString(
                getClass().getResourceAsStream("velocity-toolmanager.expected.txt"), StandardCharsets.UTF_8);
        expectedResult = StringUtils.unifyLineSeparators(expectedResult);
        assertEquals(expectedResult, renderResult);
    }

    @Test
    @SuppressWarnings("deprecation")
    void velocityToolManagerForSkin() throws Exception {
        StringWriter writer = new StringWriter();

        File skinFile = new File(getBasedir(), "target/test-classes/skin.jar");
        try (InputStream skinIS = getClass().getResourceAsStream("velocity-toolmanager.vm");
                JarOutputStream jarOS = new JarOutputStream(new FileOutputStream(skinFile))) {
            jarOS.putNextEntry(new ZipEntry("META-INF/maven/site.vm"));
            IOUtil.copy(skinIS, jarOS);
            jarOS.closeEntry();
        }

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
        assertSame(skin, siteRenderingContext.getSkin());
        assertEquals(skinFile, siteRenderingContext.getSkinFile());
        assertEquals(skin.getId(), siteRenderingContext.getSkinId());
        DocumentRenderingContext context = new DocumentRenderingContext(new File(""), "document.html", "generator");
        SiteRendererSink sink = new SiteRendererSink(context);
        siteRenderer.mergeDocumentIntoSite(writer, sink, siteRenderingContext);
        String renderResult = writer.toString();
        String expectedResult = StringUtils.unifyLineSeparators(IOUtils.toString(
                getClass().getResourceAsStream("velocity-toolmanager.expected.txt"), StandardCharsets.UTF_8));
        assertEquals(expectedResult, renderResult);
    }

    @Test
    void matchVersion() throws Exception {
        DefaultSiteRenderer r = (DefaultSiteRenderer) siteRenderer;
        assertTrue(r.matchVersion("1.7", "1.7"));
        assertFalse(r.matchVersion("1.7", "1.8"));
    }

    /**
     * Skin prerequisites as skins declare them, against released and development sitetools versions. A single
     * version is a minimum.
     */
    @Test
    void matchVersionForSkinPrerequisites() throws Exception {
        DefaultSiteRenderer r = (DefaultSiteRenderer) siteRenderer;
        String[][] matching = {
            {"2.0.0", "2.0.0-M1"},
            {"2.1.0", "2.0.0"},
            {"2.1.1-SNAPSHOT", "2.1.0"},
            {"2.0.0-M16", "2.0.0-M1"},
            {"2.1.0", "[2.0.0,)"},
            {"2.1.0", "[2.0.0,3.0.0)"},
            {"2.0.0", "[2.0.0-M1,)"},
            {"1.11.1", "[1.8,2.0)"},
            {"2.1.0", "(,1.0],[2.0,)"},
        };
        for (String[] c : matching) {
            assertTrue(r.matchVersion(c[0], c[1]), c[0] + " should match " + c[1]);
        }
        String[][] notMatching = {
            {"2.0.0-M1", "2.0.0"},
            {"2.1.0-SNAPSHOT", "2.1.0"},
            {"1.11.1", "2.0.0-M1"},
            {"2.1.0", "[1.8,2.0)"},
            {"3.0.0", "[2.0.0,3.0.0)"},
            {"2.0.0-M16", "[2.0.0,)"},
            {"1.5", "(,1.0],[2.0,)"},
        };
        for (String[] c : notMatching) {
            assertFalse(r.matchVersion(c[0], c[1]), c[0] + " should not match " + c[1]);
        }
        assertThrows(RendererException.class, () -> r.matchVersion("2.1.0", "[2.0"));
    }

    @Test
    void locateDocumentFiles() throws Exception {
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
    void velocityExtensionBeforeOrAfterTheMarkupOne() throws Exception {
        File siteDir = getTestFile("target/velocity-extension");
        org.apache.commons.io.FileUtils.deleteDirectory(siteDir);
        File markdown = new File(siteDir, "markdown");
        markdown.mkdirs();
        Files.write(
                new File(markdown, "after.md.vm").toPath(),
                "# After\n\n#set( $word = 'rendered' )Velocity $word.\n".getBytes(StandardCharsets.UTF_8));
        Files.write(
                new File(markdown, "before.vm.md").toPath(),
                "# Before\n\n#set( $word = 'rendered' )Velocity $word.\n".getBytes(StandardCharsets.UTF_8));
        Files.write(new File(markdown, "plain.md").toPath(), "# Plain\n".getBytes(StandardCharsets.UTF_8));

        SiteRenderingContext ctxt =
                getSiteRenderingContext(new SiteModel(), minimalSkinJar, "target/velocity-extension", false);
        ctxt.setRootDirectory(getTestFile(""));
        ctxt.setProcessedContentOutput(new File(siteDir, "processed"));
        Map<String, DocumentRenderer> documents = siteRenderer.locateDocumentFiles(ctxt);
        assertEquals(new HashSet<>(Arrays.asList("after.html", "before.html", "plain.html")), documents.keySet());

        File output = new File(siteDir, "output");
        siteRenderer.render(documents.values(), ctxt, output);
        for (String page : new String[] {"after", "before"}) {
            String html =
                    new String(Files.readAllBytes(new File(output, page + ".html").toPath()), StandardCharsets.UTF_8);
            assertTrue(html.contains("Velocity rendered."), page);
            assertTrue(new File(siteDir, "processed/" + page + ".md").isFile(), page);
        }
    }

    @Test
    void withoutVelocityExtension() {
        assertEquals("index.md", DefaultSiteRenderer.withoutVelocityExtension("index.md.vm", "md"));
        assertEquals("index.md", DefaultSiteRenderer.withoutVelocityExtension("index.vm.md", "md"));
        assertEquals("dir/index.MD", DefaultSiteRenderer.withoutVelocityExtension("dir/index.VM.MD", "md"));
        assertEquals("index.md", DefaultSiteRenderer.withoutVelocityExtension("index.md", "md"));
        assertFalse(DefaultSiteRenderer.isVelocityDocument("index.md", "md"));
    }

    @Test
    void sharedSourceDirectoryPicksTheParserByExtension() throws Exception {
        File siteDir = getTestFile("target/shared-source");
        org.apache.commons.io.FileUtils.deleteDirectory(siteDir);
        write(new File(siteDir, "source/index.md"), "# Markdown\n");
        write(new File(siteDir, "source/guide/usage.apt"), "Usage\n\n* APT\n\n  Text.\n");
        write(new File(siteDir, "source/faq.fml"), "<faqs title=\"FAQ\"/>\n");
        write(new File(siteDir, "markdown/other.md"), "# Other\n");

        SiteRenderingContext context = new SiteRenderingContext();
        context.setRootDirectory(siteDir);
        context.addSiteDirectory(new SiteDirectory(siteDir, true));
        assertEquals(
                Collections.singleton("other.html"),
                siteRenderer.locateDocumentFiles(context).keySet());

        context.setSharedSourceDirectory(true);
        Map<String, DocumentRenderer> documents = siteRenderer.locateDocumentFiles(context);
        assertEquals(
                new HashSet<>(Arrays.asList("index.html", "guide/usage.html", "faq.html", "other.html")),
                documents.keySet());
        assertEquals(
                "apt", documents.get("guide/usage.html").getRenderingContext().getParserId());
        assertEquals(
                "markdown", documents.get("index.html").getRenderingContext().getParserId());

        // the same page in a format directory and the shared one is a duplicate
        write(new File(siteDir, "markdown/index.md"), "# Again\n");
        assertThrows(RendererException.class, () -> siteRenderer.locateDocumentFiles(context));
    }

    private static void write(File file, String content) throws IOException {
        file.getParentFile().mkdirs();
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void locateDocumentFilesWithNameClashes() throws Exception {
        SiteRenderingContext context = new SiteRenderingContext();
        File sourceDirectory = getTestFile("src/test/resources/site-validate");
        context.setRootDirectory(sourceDirectory);
        context.addSiteDirectory(new SiteDirectory(sourceDirectory, true));
        context.addSiteDirectory(new SiteDirectory(sourceDirectory, true));
        assertThrows(RendererException.class, () -> siteRenderer.locateDocumentFiles(context));
    }

    @Test
    void locateDocumentFilesWithNameClashesInSkippingDuplicatesDirectory() throws Exception {
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

    @Test
    void copyResourcesConditionally() throws Exception {
        File skinJar = new File(getBasedir(), "target/test-classes/skin-with-conditional-resources.jar");
        createJarFromDirectory(
                getTestFile("src/test/resources/skin-with-conditional-resources")
                        .toPath(),
                skinJar.toPath());

        SiteModel siteModel =
                new SiteXpp3Reader().read(new FileInputStream(getTestFile("src/test/resources/site/site.xml")));
        SiteRenderingContext context = getSiteRenderingContext(siteModel, skinJar, "src/test/resources/site", false);
        File sourceDirectory = getTestFile("src/test/resources/site-validate");
        context.setRootDirectory(sourceDirectory);
        Path outputDirectory = Files.createTempDirectory("site-output-");
        siteRenderer.copyResources(context, outputDirectory.toFile());
        assertTrue(Files.exists(outputDirectory.resolve("js/include.js")));
        assertTrue(Files.exists(outputDirectory.resolve("js/include2.js")));
        assertTrue(Files.exists(outputDirectory.resolve("js/include3.js")));
        assertFalse(Files.exists(outputDirectory.resolve("js/exclude.js")));
    }

    private SiteRenderingContext getSiteRenderingContext(
            SiteModel siteModel, File skinFile, String siteDir, boolean validate)
            throws RendererException, IOException {

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("outputEncoding", "UTF-8");

        SiteRenderingContext siteRenderingContext = siteRenderer.createContextForSkin(
                skinFile, "org.group:artifact:jar:1.1", attributes, siteModel, "defaultTitle", Locale.ROOT);
        assertEquals(skinFile, siteRenderingContext.getSkinFile());
        assertEquals("org.group:artifact:jar:1.1", siteRenderingContext.getSkinId());
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
            reader = new XmlStreamReader(macro);
            String content = IOUtil.toString(reader);
            assertEquals(-1, content.indexOf("</macro>"));
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

    private void verifyMermaidPage(String externalJsScript) throws Exception {
        MermaidVerifier verifier = new MermaidVerifier(externalJsScript);
        verifier.verify("target/output/mermaid.html");
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
                validateFiles();
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

                Reader reader = new XmlStreamReader(new File(file));
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

    private DocumentRenderer pageRenderer(File basedir, String inputName) {
        DocumentRenderer docRenderer = Mockito.mock(DocumentRenderer.class);
        Mockito.when(docRenderer.getRenderingContext())
                .thenReturn(new DocumentRenderingContext(basedir, inputName, "markdown"));
        Mockito.when(docRenderer.getOutputName()).thenReturn("page.html");
        return docRenderer;
    }

    @Test
    void newerSkinRendersUnchangedPageAgain() throws Exception {
        File basedir = getTestFile("target/rerender/skin");
        org.apache.commons.io.FileUtils.deleteDirectory(basedir);
        File input = new File(basedir, "src/page.md");
        File output = new File(basedir, "site/page.html");
        File skin = new File(basedir, "skin.jar");
        for (File f : new File[] {input, output, skin}) {
            f.getParentFile().mkdirs();
            Files.write(f.toPath(), "x".getBytes(StandardCharsets.UTF_8));
        }
        long now = System.currentTimeMillis();
        assertTrue(input.setLastModified(now - 30_000));
        assertTrue(output.setLastModified(now - 20_000));

        SiteRenderingContext context = new SiteRenderingContext();
        context.setSiteModel(new SiteModel());
        context.setSkin(skin, "org.example:skin:1");

        // skin older than the page: unchanged, not rendered
        assertTrue(skin.setLastModified(now - 25_000));
        DocumentRenderer unchanged = pageRenderer(basedir, "src/page.md");
        siteRenderer.render(Collections.singletonList(unchanged), context, new File(basedir, "site"));
        Mockito.verify(unchanged, Mockito.never()).renderDocument(Mockito.any(), Mockito.any(), Mockito.any());

        // skin newer than the page, for example a rebuilt or switched skin: rendered again
        assertTrue(skin.setLastModified(now - 10_000));
        DocumentRenderer changed = pageRenderer(basedir, "src/page.md");
        siteRenderer.render(Collections.singletonList(changed), context, new File(basedir, "site"));
        Mockito.verify(changed).renderDocument(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void failedRenderLeavesNoPartialPage() throws Exception {
        File basedir = getTestFile("target/rerender/failure");
        org.apache.commons.io.FileUtils.deleteDirectory(basedir);
        File input = new File(basedir, "src/page.md");
        input.getParentFile().mkdirs();
        Files.write(input.toPath(), "x".getBytes(StandardCharsets.UTF_8));

        DocumentRenderer failing = pageRenderer(basedir, "src/page.md");
        Mockito.doAnswer(invocation -> {
                    java.io.Writer writer = invocation.getArgument(0);
                    writer.write("<html><body>half");
                    writer.flush();
                    throw new RendererException("template error");
                })
                .when(failing)
                .renderDocument(Mockito.any(), Mockito.any(), Mockito.any());

        SiteRenderingContext context = new SiteRenderingContext();
        context.setSiteModel(new SiteModel());
        File outputDirectory = new File(basedir, "site");
        assertThrows(
                RendererException.class,
                () -> siteRenderer.render(Collections.singletonList(failing), context, outputDirectory));

        // a partly written page would be newer than its source and skipped as unchanged by the next run
        assertFalse(new File(outputDirectory, "page.html").exists());
    }
}
