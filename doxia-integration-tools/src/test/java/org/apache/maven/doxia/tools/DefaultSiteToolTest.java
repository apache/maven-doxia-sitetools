package org.apache.maven.doxia.tools;

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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Before;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public class DefaultSiteToolTest
{
	
    private DefaultSiteTool tool = new DefaultSiteTool();
    
    @Before 
    public void setUp() {
    	Logger logger =  new ConsoleLogger(Logger.LEVEL_WARN, "tool");
        tool.enableLogging(logger);
    }
	
    /**
     * test getNormalizedPath().
     */
    @Test
    public void testGetNormalizedPath()
    {
        assertNull( DefaultSiteTool.getNormalizedPath( null ) );
        assertEquals( "", DefaultSiteTool.getNormalizedPath( "" ) );
        assertEquals( "", DefaultSiteTool.getNormalizedPath( "." ) );
        assertEquals( "", DefaultSiteTool.getNormalizedPath( "./" ) );
        assertEquals( "foo", DefaultSiteTool.getNormalizedPath( "foo" ) );
        assertEquals( "foo/bar", DefaultSiteTool.getNormalizedPath( "foo/bar" ) );
        assertEquals( "foo/bar", DefaultSiteTool.getNormalizedPath( "foo\\bar" ) );
        assertEquals( "foo/bar", DefaultSiteTool.getNormalizedPath( "foo/./bar" ) );
        assertEquals( "foo/bar", DefaultSiteTool.getNormalizedPath( "foo//bar" ) );
        assertEquals( "", DefaultSiteTool.getNormalizedPath( "foo/../" ) );
        assertEquals( "", DefaultSiteTool.getNormalizedPath( "foo/.." ) );
        assertEquals( "bar", DefaultSiteTool.getNormalizedPath( "foo/../bar" ) );
        assertEquals( "foo", DefaultSiteTool.getNormalizedPath( "./foo" ) );
        assertEquals( "../foo", DefaultSiteTool.getNormalizedPath( "../foo" ) );
        assertEquals( "../../foo", DefaultSiteTool.getNormalizedPath( "../../foo" ) );
        assertEquals( "index.html", DefaultSiteTool.getNormalizedPath( "./foo/../index.html" ) );

        // note: space is preserved and double slash is removed!
        assertEquals( "file:/Documents and Settings/",
                DefaultSiteTool.getNormalizedPath( "file://Documents and Settings/" ) );
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetRelativePath()
    {
        assertEquals(
            ".." + File.separator + "bar.html",
            tool.getRelativePath("http://example.com/foo/bar.html", "http://example.com/foo/baz.html"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetRelativePath_same()
    {
        assertTrue(
          tool.getRelativePath( "http://example.com/foo/bar.html", "http://example.com/foo/bar.html" ).isEmpty() );
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetRelativePath_differentSchemes() {
        assertEquals(
          "scp://example.com/foo/bar.html",
          tool.getRelativePath( "scp://example.com/foo/bar.html", "http://example.com/foo/bar.html" ) );
      assertEquals(
        "file:///tmp/bloop",
         tool.getRelativePath( "file:///tmp/bloop", "scp://localhost:/tmp/blop" ) );
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetRelativePath_differentDomains() {
        assertEquals(
          "https://example.org/bar.html",
          tool.getRelativePath( "https://example.org/bar.html", "https://example.com/bar.html" ) );
        assertEquals(
          "dav:https://nexus2.mysite.net:123/nexus/content/sites/site/mysite-child/2.0.0/",
          tool.getRelativePath(
            "dav:https://nexus2.mysite.net:123/nexus/content/sites/site/mysite-child/2.0.0/",
            "dav:https://nexus1.mysite.net:123/nexus/content/sites/site/mysite-parent/1.0.0/" ));
    }
}
