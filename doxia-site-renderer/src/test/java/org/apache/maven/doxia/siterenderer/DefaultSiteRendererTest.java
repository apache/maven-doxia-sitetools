package org.apache.maven.doxia.siterenderer;

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

import javax.inject.Inject;

import static org.codehaus.plexus.testing.PlexusExtension.getBasedir;
import static org.codehaus.plexus.testing.PlexusExtension.getTestFile;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.doxia.Doxia;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.site.decoration.io.xpp3.DecorationXpp3Reader;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
@PlexusTest
public class DefaultSiteRendererTest
{
    /**
     * All output produced by this test will go here.
     */
    private static final String OUTPUT = "target/output";

    /**
     * The renderer used to produce output.
     */
    private Renderer renderer;

    /**
     * The locale before executing tests.
     */
    private Locale oldLocale;

    @Inject
    private PlexusContainer container;

    private File skinJar = new File( getBasedir(), "target/test-classes/skin.jar" );

    private File minimalSkinJar = new File( getBasedir(), "target/test-classes/minimal-skin.jar" );

    /**
     * @throws java.lang.Exception if something goes wrong.
     */
    @BeforeEach
    protected void setUp()
        throws Exception
    {
        renderer = (Renderer) container.lookup( Renderer.class );

        InputStream skinIS = getClass().getResourceAsStream( "velocity-toolmanager.vm" );
        JarOutputStream jarOS = new JarOutputStream( new FileOutputStream( skinJar ) );
        try
        {
            jarOS.putNextEntry( new ZipEntry( "META-INF/maven/site.vm" ) );
            IOUtil.copy( skinIS, jarOS );
            jarOS.closeEntry();
        }
        finally
        {
            IOUtil.close( skinIS );
            IOUtil.close( jarOS );
        }

        skinIS = new ByteArrayInputStream( "<main id=\"contentBox\">$bodyContent</main>".getBytes( StandardCharsets.UTF_8 ) );
        jarOS = new JarOutputStream( new FileOutputStream( minimalSkinJar ) );
        try
        {
            jarOS.putNextEntry( new ZipEntry( "META-INF/maven/site.vm" ) );
            IOUtil.copy( skinIS, jarOS );
            jarOS.closeEntry();
        }
        finally
        {
            IOUtil.close( skinIS );
            IOUtil.close( jarOS );
        }
    }

    /**
     * @throws java.lang.Exception if something goes wrong.
     */
    @AfterEach
    protected void tearDown()
        throws Exception
    {
        container.release( renderer );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    @Test
    public void testRenderExceptionMessageWhenLineNumberIsNotAvailable()
        throws Exception
    {
        final File testBasedir = getTestFile( "src/test/resources/site/xdoc" );
        final String testDocumentName = "head.xml";
        final String exceptionMessage = "parse error occurred";

        Doxia doxiaInstance = container.lookup( Doxia.class );
        Doxia doxiaSpy = spy( doxiaInstance );
        Mockito.doThrow( new ParseException( exceptionMessage ) )
                .when( doxiaSpy )
                .parse( Mockito.<Reader>any(), Mockito.anyString(), Mockito.<Sink>any(), Mockito.anyString() );
        Renderer renderer = container.lookup( Renderer.class );
        ReflectionUtils.setVariableValueInObject( renderer, "doxia", doxiaSpy );

        RenderingContext renderingContext = new RenderingContext( testBasedir, "", testDocumentName, "xdoc", "",
                false );

        try
        {
            renderer.renderDocument( null, renderingContext, new SiteRenderingContext() );
            fail( "should fail with exception" );
        }
        catch ( RendererException e )
        {
            assertEquals(
                    String.format( "Error parsing '%s%s%s'",
                            testBasedir.getAbsolutePath(), File.separator, testDocumentName ),
                    e.getMessage() );
        }
    }

    /**
     * @throws Exception if something goes wrong.
     */
    @Test
    public void testRenderExceptionMessageWhenLineNumberIsAvailable()
        throws Exception
    {
        final File testBasedir = getTestFile( "src/test/resources/site/xdoc" );
        final String testDocumentName = "head.xml";
        final String exceptionMessage = "parse error occurred";

        Doxia doxiaInstance = container.lookup( Doxia.class );
        Doxia doxiaSpy = spy( doxiaInstance );
        Mockito.doThrow( new ParseException( exceptionMessage, 42, 36 ) )
                .when( doxiaSpy )
                .parse( Mockito.<Reader>any(), Mockito.anyString(), Mockito.<Sink>any(), Mockito.anyString() );
        Renderer renderer = container.lookup( Renderer.class );
        ReflectionUtils.setVariableValueInObject( renderer, "doxia", doxiaSpy );

        RenderingContext renderingContext = new RenderingContext( testBasedir, "", testDocumentName, "xdoc", "",
                false );

        try
        {
            renderer.renderDocument( null, renderingContext, new SiteRenderingContext() );
            fail( "should fail with exception" );
        }
        catch ( RendererException e )
        {
            assertEquals(
                    String.format( "Error parsing '%s%s%s', line 42",
                            testBasedir.getAbsolutePath(), File.separator, testDocumentName ),
                    e.getMessage() );
        }
    }

    /**
     * @throws Exception if something goes wrong.
     */
    @Test
    public void testRender()
        throws Exception
    {
        // Safety
        FileUtils.deleteDirectory( getTestFile( OUTPUT ) );

        // ----------------------------------------------------------------------
        // Render the site from src/test/resources/site to OUTPUT
        // ----------------------------------------------------------------------
        DecorationModel decoration = new DecorationXpp3Reader()
            .read( new FileInputStream( getTestFile( "src/test/resources/site/site.xml" ) ) );

        SiteRenderingContext ctxt = getSiteRenderingContext( decoration, "src/test/resources/site", false );
        ctxt.setRootDirectory( getTestFile( "" ) );
        renderer.render( renderer.locateDocumentFiles( ctxt, true ).values(), ctxt, getTestFile( OUTPUT ) );

        ctxt = getSiteRenderingContext( decoration, "src/test/resources/site-validate", true );
        ctxt.setRootDirectory( getTestFile( "" ) );
        renderer.render( renderer.locateDocumentFiles( ctxt, true ).values(), ctxt, getTestFile( OUTPUT ) );

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
    public void testExternalReport()
        throws Exception
    {
        DocumentRenderer docRenderer = mock( DocumentRenderer.class );
        when( docRenderer.isExternalReport() ).thenReturn( true );
        when( docRenderer.getOutputName() ).thenReturn( "external/index" );
        when( docRenderer.getRenderingContext() ).thenReturn( new RenderingContext( new File( "" ), "index.html",
                                                                                    "generator:external" ) );

        SiteRenderingContext context = new SiteRenderingContext();

        renderer.render( Collections.singletonList( docRenderer ), context, new File( "target/output" ) );

        verify( docRenderer ).renderDocument( isNull( Writer.class ), eq( renderer ), eq( context ) );
    }

    @Test
    public void testVelocityToolManager()
        throws Exception
    {
        StringWriter writer = new StringWriter();

        SiteRenderingContext siteRenderingContext = new SiteRenderingContext();
        siteRenderingContext.setDecoration( new DecorationModel() );

        Map<String, Object> attributes = new HashMap<String, Object>();

        /*
         * We need to add doxiaSiteRendererVersion manually because version property from pom.properties
         * is not available at test time in some cases.
         */
        attributes.put( "doxiaSiteRendererVersion", "1.7-bogus" );

        siteRenderingContext.setTemplateProperties( attributes );

        siteRenderingContext.setTemplateName( "org/apache/maven/doxia/siterenderer/velocity-toolmanager.vm" );
        RenderingContext context = new RenderingContext( new File( "" ), "document.html", "generator" );
        SiteRendererSink sink = new SiteRendererSink( context );
        renderer.mergeDocumentIntoSite( writer, sink, siteRenderingContext );

        String renderResult = writer.toString();
        String expectedResult =
            IOUtils.toString(
                getClass().getResourceAsStream( "velocity-toolmanager.expected.txt" ),
                StandardCharsets.UTF_8 );
        expectedResult = StringUtils.unifyLineSeparators( expectedResult );
        assertEquals( expectedResult, renderResult );
    }

    @Test
    public void testVelocityToolManagerForSkin()
        throws Exception
    {
        StringWriter writer = new StringWriter();

        File skinFile = skinJar;

        Map<String, Object> attributes = new HashMap<String, Object>();

        /*
         * We need to add doxiaSiteRendererVersion manually because version property from pom.properties
         * is not available at test time in some cases.
         */
        attributes.put( "doxiaSiteRendererVersion", "1.7-bogus" );

        Artifact skin = new DefaultArtifact( "org.group", "artifact",
            VersionRange.createFromVersion( "1.1" ), null, "jar", "", null );
        skin.setFile( skinFile );
        SiteRenderingContext siteRenderingContext =
            renderer.createContextForSkin( skin, attributes, new DecorationModel(), "defaultWindowTitle",
                                           Locale.ROOT );
        RenderingContext context = new RenderingContext( new File( "" ), "document.html", "generator" );
        SiteRendererSink sink = new SiteRendererSink( context );
        renderer.mergeDocumentIntoSite( writer, sink, siteRenderingContext );
        String renderResult = writer.toString();
        String expectedResult = StringUtils.unifyLineSeparators(
            IOUtils.toString(
                getClass().getResourceAsStream( "velocity-toolmanager.expected.txt" ),
                StandardCharsets.UTF_8 ) );
        assertEquals( expectedResult, renderResult );
    }

    @Test
    public void testMatchVersion()
        throws Exception
    {
        DefaultSiteRenderer r = (DefaultSiteRenderer) renderer;
        assertTrue( r.matchVersion( "1.7", "1.7" ) );
        assertFalse( r.matchVersion( "1.7", "1.8" ) );
    }

    private SiteRenderingContext getSiteRenderingContext( DecorationModel decoration, String siteDir, boolean validate )
        throws RendererException, IOException
    {
        File skinFile = minimalSkinJar;

        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put( "outputEncoding", "UTF-8" );

        Artifact skin = new DefaultArtifact( "org.group", "artifact",
        VersionRange.createFromVersion( "1.1" ), null, "jar", "", null );
        skin.setFile( skinFile );
        SiteRenderingContext siteRenderingContext =
            renderer.createContextForSkin( skin, attributes,decoration, "defaultWindowTitle",
                                                   Locale.ROOT );
        siteRenderingContext.addSiteDirectory( getTestFile( siteDir ) );
        siteRenderingContext.setValidate( validate );

        return siteRenderingContext;
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyCdcPage()
        throws Exception
    {
        File nestedItems = getTestFile( "target/output/cdc.html" );
        assertNotNull( nestedItems );
        assertTrue( nestedItems.exists() );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyNestedItemsPage()
        throws Exception
    {
        NestedItemsVerifier verifier = new NestedItemsVerifier();
        verifier.verify( "target/output/nestedItems.html" );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyMultipleBlock()
        throws Exception
    {
        MultipleBlockVerifier verifier = new MultipleBlockVerifier();
        verifier.verify( "target/output/multipleblock.html" );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyMacro()
        throws Exception
    {
        File macro = getTestFile( "target/output/macro.html" );
        assertNotNull( macro );
        assertTrue( macro.exists() );

        Reader reader = null;
        try
        {
            reader = ReaderFactory.newXmlReader( macro );
            String content = IOUtil.toString( reader );
            assertEquals( content.indexOf( "</macro>" ), -1 );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyEntitiesPage()
        throws Exception
    {
        EntitiesVerifier verifier = new EntitiesVerifier();
        verifier.verify( "target/output/entityTest.html" );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyJavascriptPage()
        throws Exception
    {
        JavascriptVerifier verifier = new JavascriptVerifier();
        verifier.verify( "target/output/javascript.html" );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyFaqPage()
        throws Exception
    {
        FaqVerifier verifier = new FaqVerifier();
        verifier.verify( "target/output/faq.html" );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyAttributes()
        throws Exception
    {
        AttributesVerifier verifier = new AttributesVerifier();
        verifier.verify( "target/output/attributes.html" );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyApt()
        throws Exception
    {
        AbstractVerifier verifier = new AptVerifier();
        verifier.verify( "target/output/apt.html" );

        verifier = new CommentsVerifier();
        verifier.verify( "target/output/apt.html" );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyExtensionInFilename()
        throws Exception
    {
        File output = getTestFile( "target/output/extension.apt.not.at.end.html" );
        assertNotNull( output );
        assertTrue( output.exists() );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyNewlines()
        throws Exception
    {
        /* apt */
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/apt.html" ), "ISO-8859-1" ) );
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/cdc.html" ), "ISO-8859-1" ) );
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/interpolation.html" ), "ISO-8859-1" ) );
        /* fml */
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/faq.html" ), "ISO-8859-1" ) );
        /* xdoc */
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/attributes.html" ), "ISO-8859-1" ) );
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/javascript.html" ), "ISO-8859-1" ) );
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/head.html" ), "ISO-8859-1" ) );
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/macro.html" ), "ISO-8859-1" ) );
    }

    private void checkNewlines( String content )
    {
        int cr = StringUtils.countMatches( content, "\r" );
        int lf = StringUtils.countMatches( content, "\n" );
        assertTrue( ( cr == 0 ) || ( cr == lf ),
                    "Should contain only Windows or Unix newlines: cr = " + cr + ", lf = " + lf);
    }

    /**
     * Validate the generated pages.
     *
     * @throws Exception if something goes wrong.
     * @since 1.1.1
     */
    public void validatePages()
        throws Exception
    {
        new Xhtml5ValidatorTest().validateGeneratedPages();
    }

    protected static class Xhtml5ValidatorTest
        extends AbstractXmlValidator
    {

        /**
         * Validate the generated documents.
         *
         * @throws Exception
         */
        public void validateGeneratedPages()
            throws Exception
        {
            setValidate( false );
            try
            {
                testValidateFiles();
            }
            finally
            {
                tearDown();
            }
        }

        private static String[] getIncludes()
        {
            return new String[] { "**/*.html" };
        }

        /** {@inheritDoc} */
        protected String addNamespaces( String content )
        {
            return content;
        }

        /** {@inheritDoc} */
        protected EntityResolver getEntityResolver()
        {
            /* HTML5 restricts use of entities to XML only */
            return null;
        }

        /** {@inheritDoc} */
        protected Map<String,String> getTestDocuments()
            throws IOException
        {
            Map<String,String> testDocs = new HashMap<String,String>();

            File dir = new File( getBasedir(), "target/output" );

            List<String> l =
                FileUtils.getFileNames( dir, getIncludes()[0], FileUtils.getDefaultExcludesAsString(), true );

            for ( String file : l )
            {
                file = StringUtils.replace( file, "\\", "/" );

                Reader reader = ReaderFactory.newXmlReader( new File( file ) );
                try
                {
                    testDocs.put( file, IOUtil.toString( reader ) );
                }
                finally
                {
                    IOUtil.close( reader );
                }
            }

            return testDocs;
        }

        /** {@inheritDoc} */
        @Override
        protected boolean isFailErrorMessage( String message )
        {
            return true;
        }
    }
}
