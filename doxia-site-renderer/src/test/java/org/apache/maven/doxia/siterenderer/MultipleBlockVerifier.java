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
import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlSection;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

import java.util.Iterator;

/**
 *
 * @author ltheussl
 */
public class MultipleBlockVerifier
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
        assertEquals( "section name", h2.asText().trim() );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( "section_name", a.getAttribute( "name" ) );

        // ----------------------------------------------------------------------
        // Paragraph
        // ----------------------------------------------------------------------

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "text", p.asText().trim() );

        // ----------------------------------------------------------------------
        // Unordered list
        // ----------------------------------------------------------------------

        HtmlUnorderedList ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        HtmlListItem li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "list1", li.getFirstChild().asText().trim() );

        // ----------------------------------------------------------------------
        // Paragraph
        // ----------------------------------------------------------------------

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "text2", p.asText().trim() );

        // ----------------------------------------------------------------------
        // Unordered list
        // ----------------------------------------------------------------------

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "list1", li.getFirstChild().asText().trim() );

        // ----------------------------------------------------------------------
        // Paragraph
        // ----------------------------------------------------------------------

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "text3", p.asText().trim() );

        // ----------------------------------------------------------------------
        // Unordered list
        // ----------------------------------------------------------------------

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull( ul );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( "list1", p.getFirstChild().asText().trim() );

        assertFalse( elementIterator.hasNext() );
    }
}
