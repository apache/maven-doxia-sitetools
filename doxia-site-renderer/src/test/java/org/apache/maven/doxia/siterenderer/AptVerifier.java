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

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHeader2;
import com.gargoylesoftware.htmlunit.html.HtmlHeader3;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.UnknownHtmlElement;

import java.util.Iterator;


/**
 * Verifies apt transformations.
 *
 * @author ltheussl
 * @version $Id$
 */
public class AptVerifier
    extends AbstractVerifier
{
    /** {@inheritDoc} */
    public void verify( String file )
            throws Exception
    {
        HtmlPage page = htmlPage( file );
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

        // Expected log: [APT Parser] Ambiguous link: 'cdc.html'. If this is a local link, prepend "./"!
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Anchor", a.getAttributeValue( "name" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "cdc.html", a.getAttributeValue( "name" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#Anchor", a.getAttributeValue( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#Anchor", a.getAttributeValue( "href" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Anchor_with_space", a.getAttributeValue( "name" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#Anchor_with_space", a.getAttributeValue( "href" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "http://maven.apache.org/", a.getAttributeValue( "href" ) );
        assertEquals( "externalLink", a.getAttributeValue( "class" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "http://maven.apache.org/", a.getAttributeValue( "href" ) );
        assertEquals( "externalLink", a.getAttributeValue( "class" ) );

        // Expected log: [APT Parser] Ambiguous link: 'cdc.html'. If this is a local link, prepend "./"!
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "./cdc.html", a.getAttributeValue( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#cdc.html", a.getAttributeValue( "href" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "/index.html", a.getAttributeValue( "href" ) );

        div = (HtmlDivision) elementIterator.next();
        assertEquals( "section", div.getAttributeValue( "class" ) );

        h2 = (HtmlHeader2) elementIterator.next();
        assertNotNull( h2 );
        assertEquals( "Section formatting: italic bold mono", h2.asText().trim() );

        UnknownHtmlElement unknown = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "i", unknown.getTagName() );
        assertEquals( "italic", unknown.asText().trim() );

        unknown = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "b", unknown.getTagName() );
        assertEquals( "bold", unknown.asText().trim() );

        unknown = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "tt", unknown.getTagName() );
        assertEquals( "mono", unknown.asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Section_formatting:_italic_bold_mono", a.getAttributeValue( "name" ) );

        div = (HtmlDivision) elementIterator.next();
        assertEquals( "section", div.getAttributeValue( "class" ) );

        HtmlHeader3 h3 = (HtmlHeader3) elementIterator.next();
        assertNotNull( h3 );
        assertEquals( "SubSection formatting: italic bold mono", h3.asText().trim() );

        unknown = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "i", unknown.getTagName() );
        assertEquals( "italic", unknown.asText().trim() );

        unknown = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "b", unknown.getTagName() );
        assertEquals( "bold", unknown.asText().trim() );

        unknown = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "tt", unknown.getTagName() );
        assertEquals( "mono", unknown.asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "SubSection_formatting:_italic_bold_mono", a.getAttributeValue( "name" ) );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );

        unknown = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "i", unknown.getTagName() );
        assertEquals( "italic", unknown.asText().trim() );

        unknown = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "b", unknown.getTagName() );
        assertEquals( "bold", unknown.asText().trim() );

        unknown = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "tt", unknown.getTagName() );
        assertEquals( "mono", unknown.asText().trim() );

        div = (HtmlDivision) elementIterator.next();
        assertEquals( "section", div.getAttributeValue( "class" ) );

        h2 = (HtmlHeader2) elementIterator.next();
        assertNotNull( h2 );
        assertEquals( "No Default Anchor in Section Title with Explicit Anchor", h2.asText().trim() );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "No_Default_Anchor_in_Section_Title_with_Explicit_Anchor", a.getAttributeValue( "name" ) );

        div = (HtmlDivision) elementIterator.next();
        assertEquals( "section", div.getAttributeValue( "class" ) );
    }
}
