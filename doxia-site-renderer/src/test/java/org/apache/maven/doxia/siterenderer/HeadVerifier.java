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

import com.gargoylesoftware.htmlunit.html.HtmlBase;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlMeta;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTitle;

import java.util.Iterator;
import java.util.List;

/**
 * Verify correct rendering of <code>site/xdoc/head.xml</code>.
 *
 * @author ltheussl
 */
public class HeadVerifier
    extends AbstractVerifier
{

    /** {@inheritDoc} */
    public void verify( String file )
            throws Exception
    {
        HtmlPage page = htmlPage( file );
        assertNotNull( page );

        HtmlElement html = page.getDocumentElement();
        assertNotNull( html );

        assertEquals( "en", html.getAttribute( "lang" ) );

        List<HtmlElement> heads = html.getElementsByTagName( "head" );
        assertEquals( 1, heads.size() );
        HtmlElement head = heads.get( 0 );
        assertNotNull( head );

        Iterator<HtmlElement> elementIterator = head.getHtmlElementDescendants().iterator();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        HtmlMeta meta = (HtmlMeta) elementIterator.next();
        assertEquals( "UTF-8", meta.getAttribute( "charset" ) );

        // Skip viewport
        elementIterator.next();

        meta = (HtmlMeta) elementIterator.next();
        assertEquals( "Unexpected meta entry found generated resource " + file, "generator", meta.getAttribute( "name" ) );
        String generator = meta.getAttribute("content");
        assertEquals("Unexpected value found for generator meta entry in generated resource " + file, "Apache Maven Doxia Site Renderer", generator);

        meta = (HtmlMeta) elementIterator.next();
        assertEquals( "author", meta.getAttribute( "name" ) );
        assertEquals( "John Doe", meta.getAttribute( "content" ).trim() );

        HtmlTitle title = (HtmlTitle) elementIterator.next();
        assertNotNull( title );

        HtmlLink link = (HtmlLink) elementIterator.next();
        assertNotNull( link );
        link = (HtmlLink) elementIterator.next();
        assertNotNull( link );
        link = (HtmlLink) elementIterator.next();
        assertNotNull( link );
        link = (HtmlLink) elementIterator.next();
        assertNotNull( link );

        meta = (HtmlMeta) elementIterator.next();
        assertEquals( "description", meta.getAttribute( "name" ) );
        assertEquals( "Free Web tutorials", meta.getAttribute( "content" ) );

        meta = (HtmlMeta) elementIterator.next();
        assertEquals( "keywords", meta.getAttribute( "name" ) );
        assertEquals( "HTML,CSS,XML,JavaScript", meta.getAttribute( "content" ) );

        HtmlBase base = (HtmlBase) elementIterator.next();
        assertEquals( "http://maven.apache.org/", base.getAttribute( "href" ) );
    }
}
