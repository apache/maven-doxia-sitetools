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

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlApplet;
import com.gargoylesoftware.htmlunit.html.HtmlDefinitionDescription;
import com.gargoylesoftware.htmlunit.html.HtmlDefinitionList;
import com.gargoylesoftware.htmlunit.html.HtmlDefinitionTerm;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHeader2;
import com.gargoylesoftware.htmlunit.html.HtmlHeader4;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlOrderedList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlParameter;
import com.gargoylesoftware.htmlunit.html.HtmlPreformattedText;
import com.gargoylesoftware.htmlunit.html.HtmlScript;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableHeaderCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

import com.gargoylesoftware.htmlunit.html.UnknownHtmlElement;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.site.decoration.io.xpp3.DecorationXpp3Reader;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @author <a href="mailto:evenisse@codehaus.org>Emmanuel Venisse</a>
 * @version $Id$
 */
public class DefaultSiteRendererTest
    extends PlexusTestCase
{
    private static final String OUTPUT = "target/output";

    private Renderer renderer;

    /**
     * @throws java.lang.Exception
     * @see org.codehaus.plexus.PlexusTestCase#setUp()
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        renderer = (Renderer) lookup( Renderer.ROLE );

        // Safety
        FileUtils.deleteDirectory( getTestFile( OUTPUT ) );
    }

    /**
     * @throws java.lang.Exception
     * @see org.codehaus.plexus.PlexusTestCase#tearDown()
     */
    protected void tearDown()
        throws Exception
    {
        release( renderer );
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testRender()
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Render the site
        // ----------------------------------------------------------------------
        DecorationModel decoration = new DecorationXpp3Reader()
            .read( new FileReader( getTestFile( "src/test/site/site.xml" ) ) );

        SiteRenderingContext ctxt = new SiteRenderingContext();
        ctxt.setTemplateName( "default-site.vm" );
        ctxt.setTemplateClassLoader( getClassLoader() );
        ctxt.setUsingDefaultTemplate( true );
        Map templateProp = new HashMap();
        templateProp.put( "outputEncoding", "UTF-8" );
        ctxt.setTemplateProperties( templateProp );
        ctxt.setDecoration( decoration );
        ctxt.addSiteDirectory( getTestFile( "src/test/site" ) );

        renderer.render( renderer.locateDocumentFiles( ctxt ).values(), ctxt, getTestFile( OUTPUT ) );

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
        verifyMisc();
        verifyDocbookPageExists();
        verifyApt();
    }

    /**
     * @throws Exception
     */
    public void verifyCdcPage()
        throws Exception
    {
        File nestedItems = getTestFile( "target/output/cdc.html" );
        assertNotNull( nestedItems );
        assertTrue( nestedItems.exists() );
    }

    /**
     * @throws Exception
     */
    public void verifyNestedItemsPage()
        throws Exception
    {
        File nestedItems = getTestFile( "target/output/nestedItems.html" );
        assertNotNull( nestedItems );
        assertTrue( nestedItems.exists() );

        // HtmlUnit
        WebClient webClient = new WebClient();
        HtmlPage page = (HtmlPage) webClient.getPage( nestedItems.toURI().toURL() );
        assertNotNull( page );

        HtmlElement element = page.getHtmlElementById( "contentBox" );
        assertNotNull( element );
        HtmlDivision division = (HtmlDivision) element;
        assertNotNull( division );

        Iterator elementIterator = division.getAllHtmlChildElements();

        // ----------------------------------------------------------------------
        // Verify link
        // ----------------------------------------------------------------------

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        HtmlHeader2 h2 = (HtmlHeader2) elementIterator.next();
        assertNotNull( h2 );
        assertEquals( h2.asText().trim(), "List Section" );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( a.getAttributeValue( "name" ), "List_Section" );

        // ----------------------------------------------------------------------
        // Unordered lists
        // ----------------------------------------------------------------------
        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        HtmlHeader4 h4 = (HtmlHeader4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( h4.asText().trim(), "Unordered lists" );

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Below is an unordered list, followed by six paragraphs." );

        HtmlUnorderedList ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        HtmlListItem li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 1." );

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.getFirstDomChild().asText().trim(), "Item 11." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.getFirstDomChild().asText().trim(), "Item 12." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 13." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 14." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 2." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 3." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 4." );

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 41." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 42." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 43." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 44." );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 1 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 2 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 3 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 4 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 5 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 6 below list." );

        // ----------------------------------------------------------------------
        // Ordered lists
        // ----------------------------------------------------------------------
        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        h4 = (HtmlHeader4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( h4.asText().trim(), "Ordered lists" );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Below is an ordered list, followed by six paragraphs." );

        HtmlOrderedList ol = (HtmlOrderedList) elementIterator.next();
        assertNotNull( ol );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 1." );

        ol = (HtmlOrderedList) elementIterator.next();
        assertNotNull( ol );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 11." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 12." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 13." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 14." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 2." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 3." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 4." );

        ol = (HtmlOrderedList) elementIterator.next();
        assertNotNull( ol );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 41." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 42." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 43." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "Item 44." );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 1 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 2 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 3 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 4 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 5 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 6 below list." );

        // ----------------------------------------------------------------------
        // Definition lists
        // ----------------------------------------------------------------------
        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        h4 = (HtmlHeader4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( h4.asText().trim(), "Definition lists" );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Below is a definition list, followed by six paragraphs." );

        HtmlDefinitionList dl = (HtmlDefinitionList) elementIterator.next();
        assertNotNull( dl );

        HtmlDefinitionTerm dt = (HtmlDefinitionTerm) elementIterator.next();
        assertNotNull( dt );
        assertEquals( dt.getFirstDomChild().asText().trim(), "Term 1." );
        HtmlDefinitionDescription dd = (HtmlDefinitionDescription) elementIterator.next();
        assertNotNull( dd );
        assertEquals( dd.getFirstDomChild().asText().trim(), "Description 1." );

        dt = (HtmlDefinitionTerm) elementIterator.next();
        assertNotNull( dt );
        assertEquals( dt.getFirstDomChild().asText().trim(), "Term 2." );
        dd = (HtmlDefinitionDescription) elementIterator.next();
        assertNotNull( dd );
        assertEquals( dd.getFirstDomChild().asText().trim(), "Description 2." );

        dl = (HtmlDefinitionList) elementIterator.next();
        assertNotNull( dl );
        dt = (HtmlDefinitionTerm) elementIterator.next();
        assertNotNull( dt );
        assertEquals( dt.getFirstDomChild().asText().trim(), "Term 21." );
        dd = (HtmlDefinitionDescription) elementIterator.next();
        assertNotNull( dd );
        assertEquals( dd.getFirstDomChild().asText().trim(), "Description 21." );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 1 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 2 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 3 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 4 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 5 below list." );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Paragraph 6 below list." );

        assertFalse( elementIterator.hasNext() );
    }

    /**
     * @throws Exception
     */
    public void verifyMultipleBlock()
        throws Exception
    {
        File multipleblock = getTestFile( "target/output/multipleblock.html" );
        assertNotNull( multipleblock );
        assertTrue( multipleblock.exists() );

        // HtmlUnit
        WebClient webClient = new WebClient();
        HtmlPage page = (HtmlPage) webClient.getPage( multipleblock.toURI().toURL() );
        assertNotNull( page );

        HtmlElement element = page.getHtmlElementById( "contentBox" );
        assertNotNull( element );
        HtmlDivision division = (HtmlDivision) element;
        assertNotNull( division );

        Iterator elementIterator = division.getAllHtmlChildElements();

        // ----------------------------------------------------------------------
        // Verify link
        // ----------------------------------------------------------------------

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        HtmlHeader2 h2 = (HtmlHeader2) elementIterator.next();
        assertNotNull( h2 );
        assertEquals( h2.asText().trim(), "section name" );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( a.getAttributeValue( "name" ), "section_name" );

        // ----------------------------------------------------------------------
        // Paragraph
        // ----------------------------------------------------------------------

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "text" );

        // ----------------------------------------------------------------------
        // Unordered list
        // ----------------------------------------------------------------------

        HtmlUnorderedList ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        HtmlListItem li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "list1" );

        // ----------------------------------------------------------------------
        // Paragraph
        // ----------------------------------------------------------------------

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "text2" );

        // ----------------------------------------------------------------------
        // Unordered list
        // ----------------------------------------------------------------------

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "list1" );

        // ----------------------------------------------------------------------
        // Paragraph
        // ----------------------------------------------------------------------

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "text3" );

        // ----------------------------------------------------------------------
        // Unordered list
        // ----------------------------------------------------------------------

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.getFirstDomChild().asText().trim(), "list1" );

        assertFalse( elementIterator.hasNext() );
    }

    /**
     * @throws Exception
     */
    public void verifyMacro()
        throws Exception
    {
        File macro = getTestFile( "target/output/macro.html" );
        assertNotNull( macro );
        assertTrue( macro.exists() );

        String content = IOUtil.toString( new FileReader( macro ) );
        assertEquals( content.indexOf( "</macro>" ), -1 );
    }

    /**
     * @throws Exception
     */
    public void verifyEntitiesPage()
        throws Exception
    {
        File entityTest = getTestFile( "target/output/entityTest.html" );
        assertNotNull( entityTest );
        assertTrue( entityTest.exists() );

        // HtmlUnit
        WebClient webClient = new WebClient();
        HtmlPage page = (HtmlPage) webClient.getPage( entityTest.toURI().toURL() );
        assertNotNull( page );

        HtmlElement element = page.getHtmlElementById( "contentBox" );
        assertNotNull( element );
        HtmlDivision division = (HtmlDivision) element;
        assertNotNull( division );

        Iterator elementIterator = division.getAllHtmlChildElements();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        HtmlHeader2 h2 = (HtmlHeader2) elementIterator.next();
        assertNotNull( h2 );
        assertEquals( h2.asText().trim(), "section name" );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( a.getAttributeValue( "name" ), "section_name" );

        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        HtmlHeader4 h4 = (HtmlHeader4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( h4.asText().trim(), "Entities" );

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "'&' '<' '>' '\"' ''' ' ' ' '" );

        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        h4 = (HtmlHeader4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( h4.asText().trim(), "Comment" );

        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        h4 = (HtmlHeader4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( h4.asText().trim(), "CDATA" );

        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "source" );

        HtmlPreformattedText pre = (HtmlPreformattedText) elementIterator.next();
        assertNotNull( pre );
        assertEquals( pre.asText().trim(), "<project xmlns:ant=\"jelly:ant\">" );

        assertFalse( elementIterator.hasNext() );
    }

    /**
     * @throws Exception
     */
    public void verifyJavascriptPage()
        throws Exception
    {
        File jsTest = getTestFile( "target/output/javascript.html" );
        assertNotNull( jsTest );
        assertTrue( jsTest.exists() );

        // HtmlUnit
        WebClient webClient = new WebClient();

        final List collectedAlerts = new ArrayList();
        webClient.setAlertHandler( new CollectingAlertHandler( collectedAlerts ) );

        HtmlPage page = (HtmlPage) webClient.getPage( jsTest.toURI().toURL() );
        assertNotNull( page );

        HtmlElement element = page.getHtmlElementById( "contentBox" );
        assertNotNull( element );
        HtmlDivision division = (HtmlDivision) element;
        assertNotNull( division );

        Iterator elementIterator = division.getAllHtmlChildElements();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        HtmlHeader2 h2 = (HtmlHeader2) elementIterator.next();
        assertNotNull( h2 );
        assertEquals( h2.asText().trim(), "Test" );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( a.getAttributeValue( "name" ), "Test" );

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "You should see a JavaScript alert..." );

        HtmlScript script = (HtmlScript) elementIterator.next();
        assertNotNull( script  );
        assertEquals( script.getAttributeValue( "type" ), "text/javascript" );
        assertEquals( script.asText().trim(), "" );
        final List expectedAlerts = Collections.singletonList( "Hello!" );
        assertEquals( expectedAlerts, collectedAlerts );
    }

    /**
     * @throws Exception
     */
    public void verifyFaqPage()
        throws Exception
    {
        File faqTest = getTestFile( "target/output/faq.html" );
        assertNotNull( faqTest );
        assertTrue( faqTest.exists() );

        // HtmlUnit
        WebClient webClient = new WebClient();
        HtmlPage page = (HtmlPage) webClient.getPage( faqTest.toURI().toURL() );
        assertNotNull( page );

        HtmlElement element = page.getHtmlElementById( "contentBox" );
        assertNotNull( element );
        HtmlDivision division = (HtmlDivision) element;
        assertNotNull( division );

        Iterator elementIterator = division.getAllHtmlChildElements();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertEquals( div.getAttributeValue( "class" ), "section" );

        HtmlHeader2 h2 = (HtmlHeader2) elementIterator.next();
        assertEquals( h2.asText().trim(), "Oft Asked Questions" );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "name" ), "Oft_Asked_Questions" );

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        element = (HtmlElement) elementIterator.next();
        assertEquals( element.getTagName(), "b" );
        assertEquals( element.asText().trim(), "Contributing" );

        HtmlOrderedList ol = (HtmlOrderedList) elementIterator.next();
        assertEquals( ol.getFirstDomChild().asText().trim(), "One stupid question & a silly answer?" );

        HtmlListItem li = (HtmlListItem) elementIterator.next();
        assertEquals( li.getFirstDomChild().asText().trim(), "One stupid question & a silly answer?" );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "href" ), "#stupid-question" );

        element = (HtmlElement) elementIterator.next();
        assertEquals( element.getTagName(), "b" );
        assertEquals( element.asText().trim(), "stupid" );

        p = (HtmlParagraph) elementIterator.next();
        element = (HtmlElement) elementIterator.next();
        assertEquals( element.getTagName(), "b" );
        assertEquals( element.asText().trim(), "Using Maven" );

        ol = (HtmlOrderedList) elementIterator.next();
        assertEquals( ol.getFirstDomChild().asText().trim(), "How do I disable a report on my site?" );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstDomChild().asText().trim(), "How do I disable a report on my site?" );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "href" ), "#disable-reports" );

        div = (HtmlDivision) elementIterator.next();
        assertEquals( div.getAttributeValue( "class" ), "section" );

        h2 = (HtmlHeader2) elementIterator.next();
        assertEquals( h2.asText().trim(), "Contributing" );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "name" ), "Contributing" );

        HtmlDefinitionList dl = (HtmlDefinitionList) elementIterator.next();

        HtmlDefinitionTerm dt = (HtmlDefinitionTerm) elementIterator.next();
        assertEquals( dt.getFirstDomChild().asText().trim(), "One stupid question & a silly answer?" );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "name" ), "stupid-question" );

        element = (HtmlElement) elementIterator.next();
        assertEquals( element.getTagName(), "b" );
        assertEquals( element.asText().trim(), "stupid" );

        HtmlDefinitionDescription dd = (HtmlDefinitionDescription) elementIterator.next();

        p = (HtmlParagraph) elementIterator.next();

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "href" ), "#Using_Maven" );
        assertEquals( a.asText().trim(), "local link" );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "href" ), "./cdc.html" );
        assertEquals( a.asText().trim(), "source document" );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "href" ), "http://maven.apache.org/?l=a&m=b" );
        assertEquals( a.asText().trim(), "external link" );

        element = (HtmlElement) elementIterator.next();
        assertEquals( element.getTagName(), "i" );
        assertEquals( element.asText().trim(), "italic" );

        HtmlTable table = (HtmlTable) elementIterator.next();
        assertEquals( table.getAttributeValue( "border" ), "0" );

        element = (HtmlElement) elementIterator.next();
        // this is a htmlunit bug
        assertEquals( element.getTagName(), "tbody" );

        HtmlTableRow tr = (HtmlTableRow) elementIterator.next();
        HtmlTableDataCell td = (HtmlTableDataCell) elementIterator.next();
        assertEquals( td.getAttributeValue( "align" ), "right" );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "href" ), "#top" );
        assertEquals( a.asText().trim(), "[top]" );


        div = (HtmlDivision) elementIterator.next();
        assertEquals( div.getAttributeValue( "class" ), "section" );

        h2 = (HtmlHeader2) elementIterator.next();
        assertEquals( h2.asText().trim(), "Using Maven" );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "name" ), "Using_Maven" );

        dl = (HtmlDefinitionList) elementIterator.next();

        dt = (HtmlDefinitionTerm) elementIterator.next();
        assertEquals( dt.getFirstDomChild().asText().trim(), "How do I disable a report on my site?" );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "name" ), "disable-reports" );

        dd = (HtmlDefinitionDescription) elementIterator.next();

        p = (HtmlParagraph) elementIterator.next();

        element = (HtmlElement) elementIterator.next();
        assertEquals( element.getTagName(), "tt" );
        assertEquals( element.asText().trim(), "<source></source>" );

        div = (HtmlDivision) elementIterator.next();
        assertEquals( div.getAttributeValue( "class" ), "source" );

        HtmlPreformattedText pre = (HtmlPreformattedText) elementIterator.next();
        assertEquals( pre.asText().trim(), "<source>1.5</source>" );

        table = (HtmlTable) elementIterator.next();
        assertEquals( table.getAttributeValue( "border" ), "0" );

        element = (HtmlElement) elementIterator.next();
        // this is a htmlunit bug
        assertEquals( element.getTagName(), "tbody" );

        tr = (HtmlTableRow) elementIterator.next();
        td = (HtmlTableDataCell) elementIterator.next();
        assertEquals( td.getAttributeValue( "align" ), "right" );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "href" ), "#top" );
        assertEquals( a.asText().trim(), "[top]" );

        assertFalse( elementIterator.hasNext() );
    }

    /**
     * @throws Exception
     */
    public void verifyAttributes()
        throws Exception
    {
        File attributes = getTestFile( "target/output/attributes.html" );
        assertNotNull( attributes );
        assertTrue( attributes.exists() );

        // HtmlUnit
        WebClient webClient = new WebClient();
        HtmlPage page = (HtmlPage) webClient.getPage( attributes.toURI().toURL() );
        assertNotNull( page );

        HtmlElement element = page.getHtmlElementById( "contentBox" );
        assertNotNull( element );
        HtmlDivision division = (HtmlDivision) element;
        assertNotNull( division );

        Iterator elementIterator = division.getAllHtmlChildElements();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertEquals( "section", div.getAttributeValue( "class" ) );

        HtmlHeader2 h2 = (HtmlHeader2) elementIterator.next();
        assertNotNull( h2 );
        assertEquals( "section", h2.asText().trim() );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( "section", a.getAttributeValue( "name" ) );

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );

        assertEquals( "ID",  p.getAttributeValue( "id" ) );
        assertEquals( "CLASS", p.getAttributeValue( "class" ) );
        assertEquals( "TITLE", p.getAttributeValue( "title" ) );
        assertEquals( "STYLE", p.getAttributeValue( "style" ) );
        assertEquals( "LANG", p.getAttributeValue( "lang" ) );

        HtmlImage img = (HtmlImage) elementIterator.next();
        assertNotNull( img );

        assertEquals( "project.png", img.getAttributeValue( "src" ) );
        assertEquals( "150", img.getAttributeValue( "width" ) );
        assertEquals( "93", img.getAttributeValue( "height" ) );
        assertEquals( "border: 1px solid silver", img.getAttributeValue( "style" ) );
        assertEquals( "Project", img.getAttributeValue( "alt" ) );

        // test object identity to distinguish the case ATTRIBUTE_VALUE_EMPTY
        assertTrue( img.getAttributeValue( "dummy" ) == HtmlElement.ATTRIBUTE_NOT_DEFINED );

        HtmlTable table = (HtmlTable) elementIterator.next();
        assertEquals( "1", table.getAttributeValue( "border" ) );
        assertEquals( "none", table.getAttributeValue( "class" ) );

        element = (HtmlElement) elementIterator.next();
        // this is a htmlunit bug
        assertEquals( "tbody", element.getTagName() );

        HtmlTableRow tr = (HtmlTableRow) elementIterator.next();
        HtmlTableHeaderCell th = (HtmlTableHeaderCell) elementIterator.next();

        th = (HtmlTableHeaderCell) elementIterator.next();
        assertEquals( "center", th.getAttributeValue( "align" ) );
        assertEquals( "2", th.getAttributeValue( "colspan" ) );
        assertEquals( "50%", th.getAttributeValue( "width" ) );

        tr = (HtmlTableRow) elementIterator.next();

        th = (HtmlTableHeaderCell) elementIterator.next();
        assertEquals( "left", th.getAttributeValue( "align" ) );
        assertEquals( "2", th.getAttributeValue( "rowspan" ) );
        assertEquals( "middle", th.getAttributeValue( "valign" ) );

        HtmlTableDataCell td = (HtmlTableDataCell) elementIterator.next();
        td = (HtmlTableDataCell) elementIterator.next();
        tr = (HtmlTableRow) elementIterator.next();
        td = (HtmlTableDataCell) elementIterator.next();
        td = (HtmlTableDataCell) elementIterator.next();

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );

        UnknownHtmlElement unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "u", unk.getTagName());
        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "s", unk.getTagName());
        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "sub", unk.getTagName());
        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "sup", unk.getTagName());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );

        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "b", unk.getTagName());
        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "i", unk.getTagName());
        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "i", unk.getTagName());
        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "b", unk.getTagName());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "color: red; margin-left: 20px", p.getAttributeValue( "style" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Anchor", a.getAttributeValue( "name" ) );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#Anchor", a.getAttributeValue( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#Anchor", a.getAttributeValue( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "http://maven.apache.org/", a.getAttributeValue( "href" ) );
        assertEquals( "externalLink", a.getAttributeValue( "class" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "./cdc.html", a.getAttributeValue( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "cdc.html", a.getAttributeValue( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "cdc.pdf", a.getAttributeValue( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "./cdc.txt", a.getAttributeValue( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "/index.html", a.getAttributeValue( "href" ) );
    }

    /**
     * @throws Exception
     */
    public void verifyMisc()
        throws Exception
    {
        File misc = getTestFile( "target/output/misc.html" );
        assertNotNull( misc );
        assertTrue( misc.exists() );

        // HtmlUnit
        WebClient webClient = new WebClient();
        HtmlPage page = (HtmlPage) webClient.getPage( misc.toURI().toURL() );
        assertNotNull( page );

        HtmlElement element = page.getHtmlElementById( "contentBox" );
        assertNotNull( element );
        HtmlDivision division = (HtmlDivision) element;
        assertNotNull( division );

        Iterator elementIterator = division.getAllHtmlChildElements();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        HtmlApplet applet = (HtmlApplet) elementIterator.next();
        assertEquals( "org.micro.applet.Main", applet.getAttributeValue( "code" ) );
        assertEquals( "micro-applet.jar", applet.getAttributeValue( "archive" ) );

        HtmlParameter param = (HtmlParameter) elementIterator.next();
        assertEquals( "midlet", param.getAttributeValue( "name" ) );
        assertEquals( "org.micro.applet.SimpleDemoMIDlet", param.getAttributeValue( "value" ) );

    }

    /**
     * @throws Exception
     */
    public void verifyDocbookPageExists()
        throws Exception
    {
        File nestedItems = getTestFile( "target/output/docbook.html" );
        assertNotNull( nestedItems );
        assertTrue( nestedItems.exists() );
    }

    /**
     * @throws Exception
     */
    public void verifyApt()
        throws Exception
    {
        File attributes = getTestFile( "target/output/apt.html" );
        assertNotNull( attributes );
        assertTrue( attributes.exists() );

        // HtmlUnit
        WebClient webClient = new WebClient();
        HtmlPage page = (HtmlPage) webClient.getPage( attributes.toURI().toURL() );
        assertNotNull( page );

        HtmlElement element = page.getHtmlElementById( "contentBox" );
        assertNotNull( element );
        HtmlDivision division = (HtmlDivision) element;
        assertNotNull( division );

        Iterator elementIterator = division.getAllHtmlChildElements();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertEquals( "section", div.getAttributeValue( "class" ) );

        HtmlHeader2 h2 = (HtmlHeader2) elementIterator.next();
        assertNotNull( h2 );
        assertEquals( "Links", h2.asText().trim() );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Links", a.getAttributeValue( "name" ) );

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Anchor", a.getAttributeValue( "name" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "cdc.html", a.getAttributeValue( "name" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#Anchor", a.getAttributeValue( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#Anchor", a.getAttributeValue( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "http://maven.apache.org/", a.getAttributeValue( "href" ) );
        assertEquals( "externalLink", a.getAttributeValue( "class" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "http://maven.apache.org/", a.getAttributeValue( "href" ) );
        assertEquals( "externalLink", a.getAttributeValue( "class" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "./cdc.html", a.getAttributeValue( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#cdc.html", a.getAttributeValue( "href" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "/index.html", a.getAttributeValue( "href" ) );
    }
}
