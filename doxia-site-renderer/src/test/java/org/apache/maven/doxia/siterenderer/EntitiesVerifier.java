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
import com.gargoylesoftware.htmlunit.html.HtmlHeader4;
import com.gargoylesoftware.htmlunit.html.HtmlMeta;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlPreformattedText;

import java.util.Iterator;

/**
 * Verify the <code>site/xdoc/entityTest.xml</code>
 *
 * @author ltheussl
 * @version $Id$
 */
public class EntitiesVerifier
    extends AbstractVerifier
{
    /** {@inheritDoc} */
    public void verify( String file )
            throws Exception
    {
        HtmlPage page = htmlPage( file );
        assertNotNull( page );

        HtmlMeta author = (HtmlMeta) page.getHtmlElementsByName( "author" ).get( 0 );
        assertNotNull( author );
        assertTrue( author.toString().indexOf( "Ligature &#198;" ) > 0 );
        assertEquals( "Ligature \u00C6", author.getContentAttribute() );

        author = (HtmlMeta) page.getHtmlElementsByName( "author" ).get( 1 );
        assertNotNull( author );
        assertTrue( author.toString().indexOf( "Ampersand &amp;" ) > 0 );
        assertEquals( "Ampersand &", author.getContentAttribute() );

        author = (HtmlMeta) page.getHtmlElementsByName( "author" ).get( 2 );
        assertNotNull( author );
        assertTrue( author.toString().indexOf( "Less than &lt;" ) > 0 );
        assertEquals( "Less than <", author.getContentAttribute() );

        author = (HtmlMeta) page.getHtmlElementsByName( "author" ).get( 3 );
        assertNotNull( author );
        assertTrue( author.toString().indexOf( "Greater than &gt;" ) > 0 );
        assertEquals( "Greater than >", author.getContentAttribute() );

        author = (HtmlMeta) page.getHtmlElementsByName( "author" ).get( 4 );
        assertNotNull( author );
        assertTrue( author.getContentAttribute().equals( "Apostrophe '" ) );
        assertEquals( "Apostrophe '", author.getContentAttribute() );

        author = (HtmlMeta) page.getHtmlElementsByName( "author" ).get( 5 );
        assertNotNull( author );
        assertTrue( author.toString().indexOf( "Quote &quot;" ) > 0 );
        assertEquals( "Quote \"", author.getContentAttribute() );

        author = (HtmlMeta) page.getHtmlElementsByName( "author" ).get( 6 );
        assertNotNull( author );
        assertTrue( author.toString().indexOf( "test@email.com" ) > 0 );
        assertEquals( "test@email.com", author.getContentAttribute() );

        author = (HtmlMeta) page.getHtmlElementsByName( "author" ).get( 7 );
        assertNotNull( author );
        assertTrue( author.toString().indexOf( "test&#169;email.com" ) > 0 );
        assertEquals( "test©email.com", author.getContentAttribute() );

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
        assertEquals( h2.asText().trim(), "section name with entities: '&' '\u0391' ' ' '\uD7ED'" );

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( a.getAttributeValue( "name" ), "section_name_with_entities:____x1d7ed" );

        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        HtmlHeader4 h4 = (HtmlHeader4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( h4.asText().trim(), "Entities" );

        a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( a.getAttributeValue( "name" ), "Entities" );

        div = (HtmlDivision) elementIterator.next();

        HtmlHeader3 h3 = (HtmlHeader3) elementIterator.next();
        assertNotNull( h3 );
        assertEquals( h3.asText().trim(), "Generic Entities: '&' '<' '>' '\"' '''" );

        a = (HtmlAnchor) elementIterator.next();

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "'&' '<' '>' '\"' '''" );

        div = (HtmlDivision) elementIterator.next();

        h3 = (HtmlHeader3) elementIterator.next();
        assertNotNull( h3 );
        assertEquals( h3.asText().trim(), "Local Entities: '\u0391' '\u0392' '\u0393' '\uD7ED'" );

        a = (HtmlAnchor) elementIterator.next();

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "'\u0391' '\u0392' '\u0393' '\uD7ED\uD7ED' '\u0159\u0159'" );

        div = (HtmlDivision) elementIterator.next();

        h3 = (HtmlHeader3) elementIterator.next();
        assertNotNull( h3 );
        assertEquals( h3.asText().trim(), "DTD Entities: ' ' '\u00A1' '\u00A2'" );

        a = (HtmlAnchor) elementIterator.next();

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "' ' '\u00A1' '\u00A2'" );

        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "section" );

        h4 = (HtmlHeader4) elementIterator.next();
        assertNotNull( h4 );
        assertEquals( h4.asText().trim(), "CDATA" );

        a = (HtmlAnchor) elementIterator.next();
        assertNotNull( a );
        assertEquals( a.getAttributeValue( "name" ), "CDATA" );

        div = (HtmlDivision) elementIterator.next();
        assertNotNull( div );
        assertEquals( div.getAttributeValue( "class" ), "source" );

        HtmlPreformattedText pre = (HtmlPreformattedText) elementIterator.next();
        assertNotNull( pre );
        assertEquals( pre.asText().trim(), "<project xmlns:ant=\"jelly:ant\">" );

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull( p );
        assertEquals( p.asText().trim(), "'&nbsp;' '&iexcl;'" );

        assertFalse( elementIterator.hasNext() );
    }
}
