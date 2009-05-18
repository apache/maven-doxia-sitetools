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
import com.gargoylesoftware.htmlunit.html.HtmlHeader2;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlOrderedList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlPreformattedText;

import java.util.Iterator;


/**
 * 
 *
 * @author ltheussl
 * @version $Id$
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

        Iterator elementIterator = division.getAllHtmlChildElements();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "name" ), "top" );

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertEquals( div.getAttributeValue( "class" ), "section" );

        HtmlHeader2 h2 = (HtmlHeader2) elementIterator.next();
        assertEquals( h2.asText().trim(), "Oft Asked Questions" );

        a = (HtmlAnchor) elementIterator.next();
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

        p = (HtmlParagraph) elementIterator.next();
        assertEquals( p.getAttributeValue( "align" ), "right" );

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

        p = (HtmlParagraph) elementIterator.next();
        assertEquals( p.getAttributeValue( "align" ), "right" );

        a = (HtmlAnchor) elementIterator.next();
        assertEquals( a.getAttributeValue( "href" ), "#top" );
        assertEquals( a.asText().trim(), "[top]" );

        assertFalse( elementIterator.hasNext() );
    }
}
