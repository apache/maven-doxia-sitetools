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
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlPreformattedText;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableHeaderCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.UnknownHtmlElement;

import java.util.Iterator;


/**
 *
 *
 * @author ltheussl
 * @version $Id$
 */
public class AttributesVerifier
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
        assertEquals( "u", unk.getTagName() );
        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "s", unk.getTagName() );
        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "sub", unk.getTagName() );
        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "sup", unk.getTagName() );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );

        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "b", unk.getTagName() );
        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "i", unk.getTagName() );
        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "i", unk.getTagName() );
        unk = (UnknownHtmlElement) elementIterator.next();
        assertEquals( "b", unk.getTagName() );

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

        div = (HtmlDivision) elementIterator.next();
        assertEquals( "source", div.getAttributeValue( "class" ) );
        HtmlPreformattedText pre = (HtmlPreformattedText) elementIterator.next();
        assertEquals( "pretty", pre.getAttributeValue( "class" ) );

        div = (HtmlDivision) elementIterator.next();
        assertEquals( "source", div.getAttributeValue( "class" ) );
        assertEquals( "", div.getAttributeValue( "id" ) );
        pre = (HtmlPreformattedText) elementIterator.next();
        assertEquals( "pretty", pre.getAttributeValue( "id" ) );

        div = (HtmlDivision) elementIterator.next();
        assertEquals( "section", div.getAttributeValue( "class" ) );
        h2 = (HtmlHeader2) elementIterator.next();
        assertEquals( "Section without id", h2.asText().trim() );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Section_without_id", a.getAttributeValue( "name" ) );

        div = (HtmlDivision) elementIterator.next();
        assertEquals( "section", div.getAttributeValue( "class" ) );
        HtmlHeader3 h3 = (HtmlHeader3) elementIterator.next();
        assertEquals( "Subsection without id", h3.asText().trim() );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Subsection_without_id", a.getAttributeValue( "name" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "section-id", a.getAttributeValue( "name" ) );
        div = (HtmlDivision) elementIterator.next();
        assertEquals( "section", div.getAttributeValue( "class" ) );
        h2 = (HtmlHeader2) elementIterator.next();
        assertEquals( "Section with id", h2.asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "subsection-id", a.getAttributeValue( "name" ) );
        div = (HtmlDivision) elementIterator.next();
        assertEquals( "section", div.getAttributeValue( "class" ) );
        h3 = (HtmlHeader3) elementIterator.next();
        assertEquals( "Subsection with id", h3.asText().trim() );

        assertFalse( elementIterator.hasNext() );
    }
}
