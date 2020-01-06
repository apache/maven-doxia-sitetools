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
import com.gargoylesoftware.htmlunit.html.HtmlBold;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
import com.gargoylesoftware.htmlunit.html.HtmlHeading3;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlItalic;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlPreformattedText;
import com.gargoylesoftware.htmlunit.html.HtmlS;
import com.gargoylesoftware.htmlunit.html.HtmlSection;
import com.gargoylesoftware.htmlunit.html.HtmlSubscript;
import com.gargoylesoftware.htmlunit.html.HtmlSuperscript;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableHeaderCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlUnderlined;

import java.util.Iterator;


/**
 *
 *
 * @author ltheussl
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

        Iterator<HtmlElement> elementIterator = division.getHtmlElementDescendants().iterator();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        HtmlSection section = (HtmlSection) elementIterator.next();

        HtmlHeading2 h2 = (HtmlHeading2) elementIterator.next();
        assertNotNull( h2 );
        assertEquals( "section", h2.asText().trim() );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( "section", a.getAttribute( "name" ) );

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );

        assertEquals( "ID",  p.getAttribute( "id" ) );
        assertEquals( "CLASS", p.getAttribute( "class" ) );
        assertEquals( "TITLE", p.getAttribute( "title" ) );
        assertEquals( "STYLE", p.getAttribute( "style" ) );
        assertEquals( "LANG", p.getAttribute( "lang" ) );

        HtmlImage img = (HtmlImage) elementIterator.next();
        assertNotNull( img );

        assertEquals( "project.png", img.getAttribute( "src" ) );
        assertEquals( "150", img.getAttribute( "width" ) );
        assertEquals( "93", img.getAttribute( "height" ) );
        assertEquals( "border: 1px solid silver", img.getAttribute( "style" ) );
        assertEquals( "Project", img.getAttribute( "alt" ) );

        // test object identity to distinguish the case ATTRIBUTE_VALUE_EMPTY
        assertSame( img.getAttribute( "dummy" ), HtmlElement.ATTRIBUTE_NOT_DEFINED );

        HtmlTable table = (HtmlTable) elementIterator.next();
        assertEquals( "1", table.getAttribute( "border" ) );
        assertEquals( "none", table.getAttribute( "class" ) );

        element = elementIterator.next();
        // this is a htmlunit bug
        assertEquals( "tbody", element.getTagName() );

        HtmlTableRow tr = (HtmlTableRow) elementIterator.next();
        HtmlTableHeaderCell th = (HtmlTableHeaderCell) elementIterator.next();

        th = (HtmlTableHeaderCell) elementIterator.next();
        assertEquals( "center", th.getAttribute( "align" ) );
        assertEquals( "2", th.getAttribute( "colspan" ) );
        assertEquals( "50%", th.getAttribute( "width" ) );

        tr = (HtmlTableRow) elementIterator.next();

        th = (HtmlTableHeaderCell) elementIterator.next();
        assertEquals( "2", th.getAttribute( "rowspan" ) );
        assertEquals( "middle", th.getAttribute( "valign" ) );

        HtmlTableDataCell td = (HtmlTableDataCell) elementIterator.next();
        td = (HtmlTableDataCell) elementIterator.next();
        tr = (HtmlTableRow) elementIterator.next();
        td = (HtmlTableDataCell) elementIterator.next();
        td = (HtmlTableDataCell) elementIterator.next();

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );

        HtmlUnderlined u = (HtmlUnderlined) elementIterator.next();
        assertEquals( "u", u.getTagName() );
        HtmlS s = (HtmlS) elementIterator.next();
        assertEquals( "s", s.getTagName() );
        HtmlSubscript sub = (HtmlSubscript) elementIterator.next();
        assertEquals( "sub", sub.getTagName() );
        HtmlSuperscript sup = (HtmlSuperscript) elementIterator.next();
        assertEquals( "sup", sup.getTagName() );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );

        HtmlBold b = (HtmlBold) elementIterator.next();
        assertEquals( "b", b.getTagName() );
        HtmlItalic i = (HtmlItalic) elementIterator.next();
        assertEquals( "i", i.getTagName() );
        i = (HtmlItalic) elementIterator.next();
        assertEquals( "i", i.getTagName() );
        b = (HtmlBold) elementIterator.next();
        assertEquals( "b", b.getTagName() );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "color: red; margin-left: 20px", p.getAttribute( "style" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Anchor", a.getAttribute( "name" ) );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#Anchor", a.getAttribute( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#Anchor", a.getAttribute( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "http://maven.apache.org/", a.getAttribute( "href" ) );
        assertEquals( "externalLink", a.getAttribute( "class" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "./cdc.html", a.getAttribute( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "cdc.html", a.getAttribute( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "cdc.pdf", a.getAttribute( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "./cdc.txt", a.getAttribute( "href" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "/index.html", a.getAttribute( "href" ) );

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertEquals( "source", div.getAttribute( "class" ) );
        HtmlPreformattedText pre = (HtmlPreformattedText) elementIterator.next();
        assertEquals( "pretty", pre.getAttribute( "class" ) );

        div = (HtmlDivision) elementIterator.next();
        assertEquals( "source", div.getAttribute( "class" ) );
        assertEquals( "", div.getAttribute( "id" ) );
        pre = (HtmlPreformattedText) elementIterator.next();
        assertEquals( "pretty", pre.getAttribute( "id" ) );

        section = (HtmlSection) elementIterator.next();
        h2 = (HtmlHeading2) elementIterator.next();
        assertEquals( "Section without id", h2.asText().trim() );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Section_without_id", a.getAttribute( "name" ) );

        section = (HtmlSection) elementIterator.next();
        HtmlHeading3 h3 = (HtmlHeading3) elementIterator.next();
        assertEquals( "Subsection without id", h3.asText().trim() );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Subsection_without_id", a.getAttribute( "name" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "section-id", a.getAttribute( "name" ) );
        section = (HtmlSection) elementIterator.next();
        h2 = (HtmlHeading2) elementIterator.next();
        assertEquals( "Section with id", h2.asText().trim() );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Section_with_id", a.getAttribute( "name" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "subsection-id", a.getAttribute( "name" ) );
        section = (HtmlSection) elementIterator.next();
        h3 = (HtmlHeading3) elementIterator.next();
        assertEquals( "Subsection with id", h3.asText().trim() );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Subsection_with_id", a.getAttribute( "name" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "foo", a.getAttribute( "name" ) );
        section = (HtmlSection) elementIterator.next();
        assertEquals( "bar", section.getAttribute( "class" ) );
        assertEquals( "foo", section.getAttribute( "id" ) );
        h2 = (HtmlHeading2) elementIterator.next();
        assertEquals( "Section name", h2.asText().trim() );
        assertEquals( "", h2.getAttribute( "class" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Section_name", a.getAttribute( "name" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "subfoo", a.getAttribute( "name" ) );
        section = (HtmlSection) elementIterator.next();
        assertEquals( "subbar", section.getAttribute( "class" ) );
        assertEquals( "subfoo", section.getAttribute( "id" ) );
        h3 = (HtmlHeading3) elementIterator.next();
        assertEquals( "Subsection name", h3.asText().trim() );
        assertEquals( "", h3.getAttribute( "class" ) );
        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Subsection_name", a.getAttribute( "name" ) );

        assertFalse( elementIterator.hasNext() );
    }
}
