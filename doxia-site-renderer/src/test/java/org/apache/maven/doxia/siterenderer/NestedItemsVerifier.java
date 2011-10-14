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
import com.gargoylesoftware.htmlunit.html.HtmlDefinitionDescription;
import com.gargoylesoftware.htmlunit.html.HtmlDefinitionList;
import com.gargoylesoftware.htmlunit.html.HtmlDefinitionTerm;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
import com.gargoylesoftware.htmlunit.html.HtmlHeading4;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlOrderedList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

import java.util.Iterator;


/**
 *
 * @author ltheussl
 * @version $Id$
 */
public class NestedItemsVerifier
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

        Iterator<HtmlElement> elementIterator = division.getAllHtmlChildElements().iterator();

        // ----------------------------------------------------------------------
        // Verify link
        // ----------------------------------------------------------------------

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttribute( "class" ), "section" );

        HtmlHeading2 h2 = (HtmlHeading2) elementIterator.next();
        assertNotNull( h2 );
        assertEquals( h2.asText().trim(), "List Section" );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( a.getAttribute( "name" ), "List_Section" );

        // ----------------------------------------------------------------------
        // Unordered lists
        // ----------------------------------------------------------------------
        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttribute( "class" ), "section" );

        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttribute( "class" ), "section" );

        HtmlHeading4 h4 = (HtmlHeading4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( h4.asText().trim(), "Unordered lists" );

        a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( a.getAttribute( "name" ), "Unordered_lists" );

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Below is an unordered list, followed by six paragraphs." );

        HtmlUnorderedList ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        HtmlListItem li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 1." );

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.getFirstChild().asText().trim(), "Item 11." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.getFirstChild().asText().trim(), "Item 12." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 13." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 14." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 2." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 3." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 4." );

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 41." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 42." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 43." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 44." );

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
        assertEquals( div.getAttribute( "class" ), "section" );

        h4 = (HtmlHeading4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( h4.asText().trim(), "Ordered lists" );

        a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( a.getAttribute( "name" ), "Ordered_lists" );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Below is an ordered list, followed by six paragraphs." );

        HtmlOrderedList ol = (HtmlOrderedList) elementIterator.next();
        assertNotNull( ol );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 1." );

        ol = (HtmlOrderedList) elementIterator.next();
        assertNotNull( ol );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 11." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 12." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 13." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 14." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 2." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 3." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 4." );

        ol = (HtmlOrderedList) elementIterator.next();
        assertNotNull( ol );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 41." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 42." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 43." );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( li.getFirstChild().asText().trim(), "Item 44." );

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
        assertEquals( div.getAttribute( "class" ), "section" );

        h4 = (HtmlHeading4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( h4.asText().trim(), "Definition lists" );

        a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( a.getAttribute( "name" ), "Definition_lists" );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "Below is a definition list, followed by six paragraphs." );

        HtmlDefinitionList dl = (HtmlDefinitionList) elementIterator.next();
        assertNotNull( dl );

        HtmlDefinitionTerm dt = (HtmlDefinitionTerm) elementIterator.next();
        assertNotNull( dt );
        assertEquals( dt.getFirstChild().asText().trim(), "Term 1." );
        HtmlDefinitionDescription dd = (HtmlDefinitionDescription) elementIterator.next();
        assertNotNull( dd );
        assertEquals( dd.getFirstChild().asText().trim(), "Description 1." );

        dt = (HtmlDefinitionTerm) elementIterator.next();
        assertNotNull( dt );
        assertEquals( dt.getFirstChild().asText().trim(), "Term 2." );
        dd = (HtmlDefinitionDescription) elementIterator.next();
        assertNotNull( dd );
        assertEquals( dd.getFirstChild().asText().trim(), "Description 2." );

        dl = (HtmlDefinitionList) elementIterator.next();
        assertNotNull( dl );
        dt = (HtmlDefinitionTerm) elementIterator.next();
        assertNotNull( dt );
        assertEquals( dt.getFirstChild().asText().trim(), "Term 21." );
        dd = (HtmlDefinitionDescription) elementIterator.next();
        assertNotNull( dd );
        assertEquals( dd.getFirstChild().asText().trim(), "Description 21." );

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
}
