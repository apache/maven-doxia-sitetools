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
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlOrderedList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlPreformattedText;
import com.gargoylesoftware.htmlunit.html.HtmlSection;

import java.util.Iterator;

/**
 *
 * @author ltheussl
 */
public class FaqVerifier
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
        assertEquals( "Oft Asked Questions", h2.asText().trim() );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttribute( "name" ), "Oft_Asked_Questions" );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "top", a.getAttribute( "name" ) );

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        element = elementIterator.next();
        assertEquals( "b", element.getTagName() );
        assertEquals( "Contributing", element.asText().trim() );

        HtmlOrderedList ol = (HtmlOrderedList) elementIterator.next();
        assertEquals( "One stupid question & a silly answer?", ol.getFirstElementChild().asText().trim() );

        HtmlListItem li = (HtmlListItem) elementIterator.next();
        assertEquals( "One stupid question & a silly answer?", li.getFirstElementChild().asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#stupid-question", a.getAttribute( "href" ) );

        element = elementIterator.next();
        assertEquals( "b", element.getTagName() );
        assertEquals( "stupid", element.asText().trim() );

        p = (HtmlParagraph) elementIterator.next();
        element = elementIterator.next();
        assertEquals( "b", element.getTagName() );
        assertEquals( "Using Maven", element.asText().trim() );

        ol = (HtmlOrderedList) elementIterator.next();
        assertEquals( "How do I disable a report on my site?", ol.getFirstElementChild().asText().trim() );

        li = (HtmlListItem) elementIterator.next();
        assertNotNull( li );
        assertEquals( "How do I disable a report on my site?", li.getFirstElementChild().asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#disable-reports", a.getAttribute( "href" ) );

        section = (HtmlSection) elementIterator.next();

        h2 = (HtmlHeading2) elementIterator.next();
        assertEquals( "Contributing", h2.asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Contributing", a.getAttribute( "name" ) );

        HtmlDefinitionList dl = (HtmlDefinitionList) elementIterator.next();

        HtmlDefinitionTerm dt = (HtmlDefinitionTerm) elementIterator.next();
        assertEquals( "One stupid question & a silly answer?", dt.getFirstChild().asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "stupid-question", a.getAttribute( "name" ) );

        element = elementIterator.next();
        assertEquals( "b", element.getTagName() );
        assertEquals( "stupid", element.asText().trim() );

        HtmlDefinitionDescription dd = (HtmlDefinitionDescription) elementIterator.next();

        p = (HtmlParagraph) elementIterator.next();

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#Using_Maven", a.getAttribute( "href" ) );
        assertEquals( "local link", a.asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "./cdc.html", a.getAttribute( "href" ) );
        assertEquals( "source document", a.asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "http://maven.apache.org/?l=a&m=b", a.getAttribute( "href" ) );
        assertEquals( "external link", a.asText().trim() );

        element = elementIterator.next();
        assertEquals( "i", element.getTagName() );
        assertEquals( "italic", element.asText().trim() );

        element = elementIterator.next();
        assertEquals( "b", element.getTagName() );
        assertEquals( "non-US-ASCII characters: àéèç", element.asText().trim() );

        p = (HtmlParagraph) elementIterator.next();
        assertEquals( "right", p.getAttribute( "align" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#top", a.getAttribute( "href" ) );
        assertEquals( "[top]", a.asText().trim() );


        section = (HtmlSection) elementIterator.next();

        h2 = (HtmlHeading2) elementIterator.next();
        assertEquals( "Using Maven", h2.asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "Using_Maven", a.getAttribute( "name" ) );

        dl = (HtmlDefinitionList) elementIterator.next();

        dt = (HtmlDefinitionTerm) elementIterator.next();
        assertEquals( "How do I disable a report on my site?", dt.getFirstChild().asText().trim() );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "disable-reports", a.getAttribute( "name" ) );

        dd = (HtmlDefinitionDescription) elementIterator.next();

        p = (HtmlParagraph) elementIterator.next();

        element = elementIterator.next();
        assertEquals( "code", element.getTagName() );
        assertEquals( "<source></source>", element.asText().trim() );

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertEquals( "source", div.getAttribute( "class" ) );

        HtmlPreformattedText pre = (HtmlPreformattedText) elementIterator.next();
        assertEquals( "<source>1.5</source>", pre.asText().trim() );

        p = (HtmlParagraph) elementIterator.next();
        assertEquals( "right", p.getAttribute( "align" ) );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( "#top", a.getAttribute( "href" ) );
        assertEquals( "[top]", a.asText().trim() );

        assertFalse( elementIterator.hasNext() );
    }
}
