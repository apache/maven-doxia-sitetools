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

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
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
import org.apache.maven.doxia.Doxia;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.site.decoration.io.xpp3.DecorationXpp3Reader;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.doxia.xsd.AbstractXmlValidator;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.ReflectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.mockito.Mockito;
import org.xml.sax.EntityResolver;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class DefaultSiteRendererTest
    extends PlexusTestCase
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

    private File skinJar = new File( getBasedir(), "target/test-classes/skin.jar" );

    /**
     * @throws java.lang.Exception if something goes wrong.
     * @see org.codehaus.plexus.PlexusTestCase#setUp()
     */
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        renderer = (Renderer) lookup( Renderer.ROLE );

        // copy the default-site.vm and default-site-macros.vm
        copyVm( "default-site.vm", "\n\n\n\r\n\r\n\r\n" );
        copyVm( "default-site-macros.vm", "" );

        InputStream skinIS = this.getResourceAsStream( "velocity-toolmanager.vm" );
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

        oldLocale = Locale.getDefault();
        Locale.setDefault( Locale.ENGLISH );
    }

    private void copyVm( String filename, String append )
        throws IOException
    {
        InputStream is = this.getResourceAsStream( "/org/apache/maven/doxia/siterenderer/resources/" + filename );
        assertNotNull( is );
        OutputStream os = new FileOutputStream( new File( getBasedir(), "target/test-classes/" + filename ) );
        try
        {
            IOUtil.copy( is, os );
            os.write( append.getBytes( "ISO-8859-1" ) );
        }
        finally
        {
            IOUtil.close( is );
            IOUtil.close( os );
        }
    }

    /**
     * @throws java.lang.Exception if something goes wrong.
     * @see org.codehaus.plexus.PlexusTestCase#tearDown()
     */
    @Override
    protected void tearDown()
        throws Exception
    {
        release( renderer );
        super.tearDown();

        Locale.setDefault( oldLocale );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void testRenderExceptionMessageWhenLineNumberIsNotAvailable()
        throws Exception
    {
        final File testBasedir = getTestFile( "src/test/resources/site/xdoc" );
        final String testDocumentName = "head.xml";
        final String exceptionMessage = "parse error occurred";

        Doxia doxiaInstance = (Doxia) lookup( Doxia.class );
        Doxia doxiaSpy = spy( doxiaInstance );
        Mockito.doThrow( new ParseException( exceptionMessage ) )
                .when( doxiaSpy )
                .parse( Mockito.<Reader>any(), Mockito.anyString(), Mockito.<Sink>any() );
        Renderer renderer = (Renderer) lookup( Renderer.class );
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
                    String.format( "Error parsing '%s%s%s': %s",
                            testBasedir.getAbsolutePath(), File.separator, testDocumentName, exceptionMessage ),
                    e.getMessage() );
        }
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void testRenderExceptionMessageWhenLineNumberIsAvailable()
        throws Exception
    {
        final File testBasedir = getTestFile( "src/test/resources/site/xdoc" );
        final String testDocumentName = "head.xml";
        final String exceptionMessage = "parse error occurred";

        Doxia doxiaInstance = (Doxia) lookup( Doxia.class );
        Doxia doxiaSpy = spy( doxiaInstance );
        Mockito.doThrow( new ParseException( exceptionMessage, 42, 36 ) )
                .when( doxiaSpy )
                .parse( Mockito.<Reader>any(), Mockito.anyString(), Mockito.<Sink>any() );
        Renderer renderer = (Renderer) lookup( Renderer.class );
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
                    String.format( "Error parsing '%s%s%s': line [42] %s",
                            testBasedir.getAbsolutePath(), File.separator, testDocumentName, exceptionMessage ),
                    e.getMessage() );
        }
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void testRender()
        throws Exception
    {
        // Safety
        FileUtils.deleteDirectory( getTestFile( OUTPUT ) );

        // ----------------------------------------------------------------------
        // Render the site from src/test/resources/site to OUTPUT
        // ----------------------------------------------------------------------
        DecorationModel decoration = new DecorationXpp3Reader()
            .read( new FileReader( getTestFile( "src/test/resources/site/site.xml" ) ) );

        SiteRenderingContext ctxt = getSiteRenderingContext( decoration, "src/test/resources/site", false );
        ctxt.setRootDirectory( getTestFile( "" ) );
        renderer.render( renderer.locateDocumentFiles( ctxt, true ).values(), ctxt, getTestFile( OUTPUT ) );

        ctxt = getSiteRenderingContext( decoration, "src/test/resources/site-validate", true );
        ctxt.setRootDirectory( getTestFile( "" ) );
        renderer.render( renderer.locateDocumentFiles( ctxt, true ).values(), ctxt, getTestFile( OUTPUT ) );

        // ----------------------------------------------------------------------
        // Verify specific pages
        // ----------------------------------------------------------------------
        verifyHeadPage();
        verifyCdcPage();
        verifyNestedItemsPage();
        verifyMultipleBlock();
        verifyMacro();
        verifyEntitiesPage();
        verifyJavascriptPage();
        verifyFaqPage();
        verifyAttributes();
        verifyMisc();
        verifyDocbookPageExists();
        verifyApt();
        verifyExtensionInFilename();
        verifyNewlines();

        // ----------------------------------------------------------------------
        // Validate the rendering pages
        // ----------------------------------------------------------------------
        validatePages();
    }

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

    public void testVelocityToolManagerForTemplate()
        throws Exception
    {
        StringWriter writer = new StringWriter();

        File templateFile =
            new File( getBasedir(), "target/test-classes/org/apache/maven/doxia/siterenderer/velocity-toolmanager.vm" );
        Map<String, Object> attributes = new HashMap<String, Object>();

        /*
         * We need to add doxiaSiteRendererVersion manually because version property from pom.properties
         * is not available at test time in some cases.
         */
        attributes.put( "doxiaSiteRendererVersion", "1.7-bogus" );

        SiteRenderingContext siteRenderingContext =
            renderer.createContextForTemplate( templateFile, attributes, new DecorationModel(),
                                               "defaultWindowTitle", Locale.ENGLISH );
        RenderingContext context = new RenderingContext( new File( "" ), "document.html", "generator" );
        SiteRendererSink sink = new SiteRendererSink( context );
        renderer.mergeDocumentIntoSite( writer, sink, siteRenderingContext );

        String renderResult = writer.toString();
        InputStream in = getClass().getResourceAsStream( "velocity-toolmanager.expected.txt" );
        String expectedResult = StringUtils.unifyLineSeparators( IOUtils.toString( in, StandardCharsets.UTF_8 ) );
        assertEquals( expectedResult, renderResult );
    }

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

        Artifact skin = new DefaultArtifact( "org.group", "artifact", "1.1", null, "jar", "", null );
        skin.setFile( skinFile );
        SiteRenderingContext siteRenderingContext =
            renderer.createContextForSkin( skin, attributes, new DecorationModel(), "defaultWindowTitle",
                                           Locale.ENGLISH );
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

    public void testMatchVersion()
        throws Exception
    {
        DefaultSiteRenderer r = (DefaultSiteRenderer) renderer;
        assertTrue( r.matchVersion( "1.7", "1.7" ) );
        assertFalse( r.matchVersion( "1.7", "1.8" ) );
    }

    private SiteRenderingContext getSiteRenderingContext( DecorationModel decoration, String siteDir, boolean validate )
    {
        SiteRenderingContext ctxt = new SiteRenderingContext();
        ctxt.setTemplateName( "default-site.vm" );
        ctxt.setTemplateClassLoader( getClassLoader() );
        ctxt.setUsingDefaultTemplate( true );
        final Map<String, String> templateProp = new HashMap<String, String>();
        templateProp.put( "outputEncoding", "UTF-8" );
        ctxt.setTemplateProperties( templateProp );
        ctxt.setDecoration( decoration );
        ctxt.addSiteDirectory( getTestFile( siteDir ) );
        ctxt.setValidate( validate );

        return ctxt;
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyHeadPage()
        throws Exception
    {
        new HeadVerifier().verify( "target/output/head.html" );
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
    public void verifyMisc()
        throws Exception
    {
        AbstractVerifier verifier = new MiscVerifier();
        verifier.verify( "target/output/misc.html" );

        verifier = new CommentsVerifier();
        verifier.verify( "target/output/misc.html" );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyDocbookPageExists()
        throws Exception
    {
        File output = getTestFile( "target/output/docbook.html" );
        assertNotNull( output );
        assertTrue( output.exists() );
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
        /* confluence */
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/confluence/anchor.html" ), "ISO-8859-1" ) );
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/confluence/code.html" ), "ISO-8859-1" ) );
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/confluence/table.html" ), "ISO-8859-1" ) );
        /* docbook */
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/docbook.html" ), "ISO-8859-1" ) );
        checkNewlines( FileUtils.fileRead( getTestFile( "target/output/sdocbook_full.html" ), "ISO-8859-1" ) );
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
        assertTrue( "Should contain only Windows or Unix newlines: cr = " + cr + ", lf = " + lf, ( cr == 0 )
            || ( cr == lf ) );
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
            setUp();
            testValidateFiles();
            tearDown();
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
