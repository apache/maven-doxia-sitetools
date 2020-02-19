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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;

/**
 * Abstract base class for verifiers.
 *
 * @author ltheussl
 */
public abstract class AbstractVerifier
    extends PlexusTestCase
{
    /**
     * Get a HtmlPage from a file.
     *
     * @param htmlFile the file to parse.
     *
     * @return a HtmlPage.
     *
     * @throws Exception if something goes wrong.
     */
    protected HtmlPage htmlPage( String htmlFile )
            throws Exception
    {
        File file = getTestFile( htmlFile );
        assertNotNull( file );
        assertTrue( file.exists() );

        // HtmlUnit
        try ( WebClient webClient = new WebClient() ) {
            webClient.getOptions().setCssEnabled( false );
    
            return (HtmlPage) webClient.getPage( file.toURI().toURL() );
        }
    }

    /**
     * Verify a HtmlPage.
     *
     * @param file the file to verify.
     *
     * @throws java.lang.Exception if something goes wrong
     */
    public abstract void verify( String file )
            throws Exception;
}
