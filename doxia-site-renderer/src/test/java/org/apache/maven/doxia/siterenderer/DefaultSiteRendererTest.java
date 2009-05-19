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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.site.decoration.io.xpp3.DecorationXpp3Reader;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;


/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @author <a href="mailto:evenisse@codehaus.org>Emmanuel Venisse</a>
 * @version $Id$
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
     * @throws java.lang.Exception if something goes wrong.
     * @see org.codehaus.plexus.PlexusTestCase#setUp()
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        renderer = (Renderer) lookup( Renderer.ROLE );

        // copy the default-site.vm
        InputStream is =
            this.getResourceAsStream( "/org/apache/maven/doxia/siterenderer/resources/default-site.vm" );
        assertNotNull( is );
        OutputStream os = new FileOutputStream( new File( getBasedir(), "target/test-classes/default-site.vm" ) );
        try
        {
            IOUtil.copy( is, os );
        }
        finally
        {
            IOUtil.close( is );
            IOUtil.close( os );
        }

        // Safety
        FileUtils.deleteDirectory( getTestFile( OUTPUT ) );
    }

    /**
     * @throws java.lang.Exception if something goes wrong.
     * @see org.codehaus.plexus.PlexusTestCase#tearDown()
     */
    protected void tearDown()
        throws Exception
    {
        release( renderer );
        super.tearDown();
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void testRender()
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Render the site
        // ----------------------------------------------------------------------
        DecorationModel decoration = new DecorationXpp3Reader()
            .read( new FileReader( getTestFile( "src/test/resources/site/site.xml" ) ) );

        SiteRenderingContext ctxt = new SiteRenderingContext();
        ctxt.setTemplateName( "default-site.vm" );
        ctxt.setTemplateClassLoader( getClassLoader() );
        ctxt.setUsingDefaultTemplate( true );
        Map templateProp = new HashMap();
        templateProp.put( "outputEncoding", "UTF-8" );
        ctxt.setTemplateProperties( templateProp );
        ctxt.setDecoration( decoration );
        ctxt.addSiteDirectory( getTestFile( "src/test/resources/site" ) );

        renderer.render( renderer.locateDocumentFiles( ctxt ).values(), ctxt, getTestFile( OUTPUT ) );

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
        MiscVerifier verifier = new MiscVerifier();
        verifier.verify( "target/output/misc.html" );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyDocbookPageExists()
        throws Exception
    {
        File nestedItems = getTestFile( "target/output/docbook.html" );
        assertNotNull( nestedItems );
        assertTrue( nestedItems.exists() );
    }

    /**
     * @throws Exception if something goes wrong.
     */
    public void verifyApt()
        throws Exception
    {
        AptVerifier verifier = new AptVerifier();
        verifier.verify( "target/output/apt.html" );
    }
}
