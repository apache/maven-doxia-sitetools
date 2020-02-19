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

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlScript;
import com.gargoylesoftware.htmlunit.html.HtmlSection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Verify javascript code.
 *
 * @author ltheussl
 */
public class JavascriptVerifier
    extends AbstractVerifier
{
    /**
     * Verifies a HtmlPage.
     *
     * @param file the file to verify.
     *
     * @throws Exception if something goes wrong.
     */
    public void verify( String file )
            throws Exception
    {
        File jsTest = getTestFile( "target/output/javascript.html" );
        assertNotNull( jsTest );
        assertTrue( jsTest.exists() );

        // HtmlUnit
        try ( WebClient webClient = new WebClient() ) {
            webClient.getOptions().setCssEnabled( false );
    
            final List<String> collectedAlerts = new ArrayList<String>( 4 );
            webClient.setAlertHandler( new CollectingAlertHandler( collectedAlerts ) );
    
            HtmlPage page = (HtmlPage) webClient.getPage( jsTest.toURI().toURL() );
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
            assertNotNull( section );
    
            HtmlHeading2 h2 = (HtmlHeading2) elementIterator.next();
            assertNotNull( h2 );
            assertEquals( "Test", h2.asText().trim() );
    
            HtmlAnchor a = (HtmlAnchor) elementIterator.next();
            assertNotNull( a );
            assertEquals( "Test", a.getAttribute( "name" ) );
    
            HtmlParagraph p = (HtmlParagraph) elementIterator.next();
            assertNotNull( p );
            assertEquals( "You should see a JavaScript alert...", p.asText().trim() );
    
            HtmlScript script = (HtmlScript) elementIterator.next();
            assertNotNull( script  );
            assertEquals( "text/javascript", script.getAttribute( "type" ) );
            assertEquals( "", script.asText().trim() );
            List<String> expectedAlerts = Collections.singletonList( "Hello!" );
            assertEquals( expectedAlerts, collectedAlerts );
        }
    }
}
