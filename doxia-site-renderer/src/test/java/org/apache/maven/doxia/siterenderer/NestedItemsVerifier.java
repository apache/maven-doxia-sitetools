package org.apache.maven.doxia.siterenderer;

import java.util.Iterator;

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
import com.gargoylesoftware.htmlunit.html.HtmlSection;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

/**
 *
 * @author ltheussl
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

        Iterator<HtmlElement> elementIterator = division.getHtmlElementDescendants().iterator();

        // ----------------------------------------------------------------------
        // Verify link
        // ----------------------------------------------------------------------

        HtmlSection section = (HtmlSection) elementIterator.next();
        assertNotNull( section );

        HtmlHeading2 h2 = (HtmlHeading2) elementIterator.next();
        assertNotNull( h2 );
        assertEquals( "List Section", h2.asText().trim() );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( "List_Section", a.getAttribute( "name" ) );

        // ----------------------------------------------------------------------
        // Unordered lists
        // ----------------------------------------------------------------------

        section = (HtmlSection) elementIterator.next();
        assertNotNull( section );

        section = (HtmlSection) elementIterator.next();
        assertNotNull( section );

        HtmlHeading4 h4 = (HtmlHeading4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( "Unordered lists", h4.asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( "Unordered_lists", a.getAttribute( "name" ) );

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Below is an unordered list, followed by six paragraphs.", p.asText().trim() );

        HtmlUnorderedList ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        HtmlListItem li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 1.", li.getFirstChild().asText().trim() );

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Item 11.", p.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Item 12.", p.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 13.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 14.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 2.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 3.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 4.", li.getFirstChild().asText().trim() );

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 41.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 42.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 43.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 44.", li.getFirstChild().asText().trim() );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 1 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 2 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 3 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 4 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 5 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 6 below list.", p.asText().trim() );

        // ----------------------------------------------------------------------
        // Ordered lists
        // ----------------------------------------------------------------------

        section = (HtmlSection) elementIterator.next();
        assertNotNull( section );

        h4 = (HtmlHeading4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( "Ordered lists", h4.asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( "Ordered_lists", a.getAttribute( "name" ) );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Below is an ordered list, followed by six paragraphs.", p.asText().trim() );

        HtmlOrderedList ol = (HtmlOrderedList) elementIterator.next();
        assertNotNull( ol );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 1.", li.getFirstChild().asText().trim() );

        ol = (HtmlOrderedList) elementIterator.next();
        assertNotNull( ol );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 11.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 12.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 13.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 14.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 2.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 3.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 4.", li.getFirstChild().asText().trim() );

        ol = (HtmlOrderedList) elementIterator.next();
        assertNotNull( ol );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 41.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 42.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 43.", li.getFirstChild().asText().trim() );
        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "Item 44.", li.getFirstChild().asText().trim() );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 1 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 2 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 3 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 4 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 5 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 6 below list.", p.asText().trim() );

        // ----------------------------------------------------------------------
        // Definition lists
        // ----------------------------------------------------------------------

        section = (HtmlSection) elementIterator.next();
        assertNotNull( section );

        h4 = (HtmlHeading4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( "Definition lists", h4.asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( "Definition_lists", a.getAttribute( "name" ) );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Below is a definition list, followed by six paragraphs.", p.asText().trim() );

        HtmlDefinitionList dl = (HtmlDefinitionList) elementIterator.next();
        assertNotNull( dl );

        HtmlDefinitionTerm dt = (HtmlDefinitionTerm) elementIterator.next();
        assertNotNull( dt );
        assertEquals( "Term 1.", dt.getFirstChild().asText().trim() );
        HtmlDefinitionDescription dd = (HtmlDefinitionDescription) elementIterator.next();
        assertNotNull( dd );
        assertEquals( "Description 1.", dd.getFirstChild().asText().trim() );

        dt = (HtmlDefinitionTerm) elementIterator.next();
        assertNotNull( dt );
        assertEquals( "Term 2.", dt.getFirstChild().asText().trim() );
        dd = (HtmlDefinitionDescription) elementIterator.next();
        assertNotNull( dd );
        assertEquals( "Description 2.", dd.getFirstChild().asText().trim() );

        dl = (HtmlDefinitionList) elementIterator.next();
        assertNotNull( dl );
        dt = (HtmlDefinitionTerm) elementIterator.next();
        assertNotNull( dt );
        assertEquals( "Term 21.", dt.getFirstChild().asText().trim() );
        dd = (HtmlDefinitionDescription) elementIterator.next();
        assertNotNull( dd );
        assertEquals( "Description 21.", dd.getFirstChild().asText().trim() );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 1 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 2 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 3 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 4 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 5 below list.", p.asText().trim() );
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "Paragraph 6 below list.", p.asText().trim() );

        assertFalse( elementIterator.hasNext() );
    }
}
