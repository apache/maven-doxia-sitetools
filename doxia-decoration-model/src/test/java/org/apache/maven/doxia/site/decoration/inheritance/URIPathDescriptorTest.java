
package org.apache.maven.doxia.site.decoration.inheritance;

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

import java.net.URI;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author ltheussl
 *
 * @since 1.2
 */
public class URIPathDescriptorTest
{
    private static final String BASE_URL = "http://maven.apache.org/";

    /**
     * Test of constructor, of class URIPathDescriptor.
     *
     * @throws Exception
     */
    @Test
    public void testConstructor()
        throws Exception
    {
        final String expected = BASE_URL + "doxia";

        final URIPathDescriptor path = new URIPathDescriptor( BASE_URL, "doxia" );
        assertEquals( expected, path.toString() );
        assertEquals( BASE_URL, path.getBaseURI().toString() );
        assertEquals( "doxia", path.getLink().toString() );

        URIPathDescriptor compare = new URIPathDescriptor( "http://maven.apache.org", "/doxia" );
        assertEquals( expected, compare.toString() );

        compare = new URIPathDescriptor( "http://maven.apache.org/./doxia/../", "/sub/./sub/../../doxia" );
        assertEquals( expected, compare.toString() );

        compare = new URIPathDescriptor( "http://maven.apache.org/doxia", "" );
        assertEquals( expected + "/", compare.toString() );

        compare = new URIPathDescriptor( "file:///C:\\Foo\\bar1", "" );
        assertEquals( "file:///C:/Foo/bar1/", compare.getBaseURI().toString() );
        // toString() calls resolve() which removes two slashes because authority is empty
        assertEquals( "file:/C:/Foo/bar1/", compare.toString() );

        compare = new URIPathDescriptor( "file:///C:/Documents%20and%20Settings/foo/", "bar" );
        assertEquals( "file:/C:/Documents%20and%20Settings/foo/bar", compare.toString() );

        compare = new URIPathDescriptor( "file:////Users/", "user" );
        assertEquals( "file:/Users/user", compare.toString() );

        compare = new URIPathDescriptor( "file:/C:/Documents%20and%20Settings/foo/", "bar" );
        assertEquals( "file:/C:/Documents%20and%20Settings/foo/bar", compare.toString() );

        compare = new URIPathDescriptor( "file://C:/Documents%20and%20Settings/foo/", "bar" );
        // toString() calls resolve() which removes the colon if port is empty, C is the host here!
        assertEquals( "file://C/Documents%20and%20Settings/foo/bar", compare.toString() );

        compare = new URIPathDescriptor( "file://C:8080/Documents%20and%20Settings/foo/", "bar" );
        assertEquals( "file://C:8080/Documents%20and%20Settings/foo/bar", compare.toString() );

        compare = new URIPathDescriptor( "C:\\Foo\\bar", "bar" );
        assertEquals( "C:/Foo/bar/bar", compare.toString() ); // NOTE: C: is the scheme here!

        assertFailure( "/doxia", BASE_URL );
        assertFailure( "file:///C:/Documents and Settings/foo/", "bar" );
    }

    /**
     * Test of resolveLink method, of class URIPathDescriptor.
     *
     * @throws Exception
     */
    @Test
    public void testResolveLink()
        throws Exception
    {
        final String expected = BASE_URL + "source";

        URIPathDescriptor oldPath = new URIPathDescriptor( BASE_URL, "source" );
        assertEquals( expected, oldPath.resolveLink().toString() );

        oldPath = new URIPathDescriptor( BASE_URL, "source/" );
        assertEquals( expected + "/", oldPath.resolveLink().toString() );

        oldPath = new URIPathDescriptor( BASE_URL, "/source" );
        assertEquals( expected, oldPath.resolveLink().toString() );

        oldPath = new URIPathDescriptor( "http://maven.apache.org", "source" );
        assertEquals( expected, oldPath.resolveLink().toString() );

        oldPath = new URIPathDescriptor( BASE_URL, "source/index.html" );
        assertEquals( expected + "/index.html", oldPath.resolveLink().toString() );

        oldPath = new URIPathDescriptor( BASE_URL, "source/index.html?var=foo&amp;var2=bar" );
        assertEquals( expected + "/index.html?var=foo&amp;var2=bar", oldPath.resolveLink().toString() );

        oldPath = new URIPathDescriptor( "file:////Users/", "user" );
        assertEquals( "file:/Users/user", oldPath.resolveLink().toString() );

        oldPath = new URIPathDescriptor( "file:///C:/Documents%20and%20Settings/", "source" );
        // resolve() removes two slashes because authority is empty
        assertEquals( "file:/C:/Documents%20and%20Settings/source", oldPath.resolveLink().toString() );

        oldPath = new URIPathDescriptor( "file://C:/Documents%20and%20Settings/", "source" );
        // resolve() removes the colon if port is empty
        assertEquals( "file://C/Documents%20and%20Settings/source", oldPath.resolveLink().toString() );

        oldPath = new URIPathDescriptor( "file:/C:/Documents%20and%20Settings/", "source" );
        assertEquals( "file:/C:/Documents%20and%20Settings/source", oldPath.resolveLink().toString() );
    }

    /**
     * Test of rebaseLink method, of class URIPathDescriptor.
     *
     * @throws Exception
     */
    @Test
    public void testRebaseLink()
        throws Exception
    {
        URIPathDescriptor oldPath = new URIPathDescriptor( BASE_URL, "source" );
        assertEquals( "../source", oldPath.rebaseLink( "http://maven.apache.org/doxia/" ).toString() );
        assertEquals( "http://maven.apache.org/source", oldPath.rebaseLink( null ).toString() );
        assertEquals( "http://maven.apache.org/source",
                oldPath.rebaseLink( "C:/Documents and Settings/" ).toString() );

        oldPath = new URIPathDescriptor( BASE_URL, "./" );
        assertEquals( "", oldPath.rebaseLink( "http://maven.apache.org/" ).toString() );

        oldPath = new URIPathDescriptor( BASE_URL, "" );
        assertEquals( "", oldPath.rebaseLink( "http://maven.apache.org/" ).toString() );

        oldPath = new URIPathDescriptor( BASE_URL, "source/index.html" );
        assertEquals( "../source/index.html",
                oldPath.rebaseLink( "http://maven.apache.org/doxia/" ).toString() );

        oldPath = new URIPathDescriptor( BASE_URL, "source/index.html?var=foo&amp;var2=bar" );
        assertEquals( "../source/index.html?var=foo&amp;var2=bar",
                oldPath.rebaseLink( "http://maven.apache.org/doxia/" ).toString() );

        oldPath = new URIPathDescriptor( "scp://people.apache.org/", "source" );
        assertEquals( "../source", oldPath.rebaseLink( "scp://people.apache.org/doxia" ).toString() );

        oldPath = new URIPathDescriptor( BASE_URL, "banner/left" );
        assertEquals( "../banner/left", oldPath.rebaseLink( "http://maven.apache.org/doxia/" ).toString() );

        oldPath = new URIPathDescriptor( BASE_URL, "index.html?var=foo&amp;var2=bar" );
        assertEquals( "../index.html?var=foo&amp;var2=bar",
                oldPath.rebaseLink( "http://maven.apache.org/doxia/" ).toString() );

        oldPath = new URIPathDescriptor( "http://jakarta.apache.org/", "banner/left" );
        assertEquals( "http://jakarta.apache.org/banner/left", oldPath.rebaseLink( BASE_URL ).toString() );

        oldPath = new URIPathDescriptor( "file:////Users/", "user" );
        assertEquals( "../user", oldPath.rebaseLink( "file:////Users/target" ).toString() );
        assertEquals( "../user", oldPath.rebaseLink( "file:/Users/target" ).toString() );

        oldPath = new URIPathDescriptor( "file:///C:/Documents%20and%20Settings/", "source" );
        assertEquals( "../source",
                oldPath.rebaseLink( "file:///C:/Documents%20and%20Settings/target" ).toString() );

        oldPath = new URIPathDescriptor( "file://C:/Documents%20and%20Settings/", "source" );
        assertEquals( "../source",
                oldPath.rebaseLink( "file://C:/Documents%20and%20Settings/target" ).toString() );

        oldPath = new URIPathDescriptor( "file:/C:/Documents%20and%20Settings/", "source" );
        assertEquals( "../source",
                oldPath.rebaseLink( "file:/C:/Documents%20and%20Settings/target" ).toString() );
    }

    /**
     * Test of relativizeLink method, of class URIPathDescriptor.
     *
     * @throws Exception
     */
    @Test
    public void testRelativizeLink()
        throws Exception
    {
        URIPathDescriptor path = new URIPathDescriptor( BASE_URL, "source" );
        assertEquals( "source", path.relativizeLink().toString() );

        path = new URIPathDescriptor( BASE_URL, "http://maven.apache.org/source" );
        assertEquals( "source", path.relativizeLink().toString() );

        path = new URIPathDescriptor( BASE_URL, "http://maven.apache.org/" );
        assertEquals( "./", path.relativizeLink().toString() );

        path = new URIPathDescriptor( BASE_URL, "http://maven.apache.org" );
        assertEquals( "./", path.relativizeLink().toString() );

        path = new URIPathDescriptor( "http://maven.apache.org", BASE_URL );
        assertEquals( "./", path.relativizeLink().toString() );

        path = new URIPathDescriptor( "http://maven.apache.org", "http://maven.apache.org" );
        assertEquals( "./", path.relativizeLink().toString() );

        path = new URIPathDescriptor( "http://maven.apache.org/doxia/", "http://maven.apache.org/source/" );
        assertEquals( "../source/", path.relativizeLink().toString() );

        path = new URIPathDescriptor( "http://maven.apache.org/doxia", "http://maven.apache.org/source" );
        assertEquals( "../source", path.relativizeLink().toString() );

        path = new URIPathDescriptor( BASE_URL, "http://maven.apache.org/index.html" );
        assertEquals( "index.html", path.relativizeLink().toString() );

        path = new URIPathDescriptor( BASE_URL, "http://maven.apache.org/index.html?var=foo&amp;var2=bar" );
        assertEquals( "index.html?var=foo&amp;var2=bar", path.relativizeLink().toString() );

        path = new URIPathDescriptor( "file:////Users/", "index.html" );
        assertEquals( "index.html", path.relativizeLink().toString() );

        path = new URIPathDescriptor( "file:///C:/Documents%20and%20Settings/", "index.html" );
        assertEquals( "index.html", path.relativizeLink().toString() );

        path = new URIPathDescriptor( "file://C:/Documents%20and%20Settings/", "index.html" );
        assertEquals( "index.html", path.relativizeLink().toString() );

        path = new URIPathDescriptor( "file:/C:/Documents%20and%20Settings/", "index.html" );
        assertEquals( "index.html", path.relativizeLink().toString() );
    }

    /**
     * Test of sameSite method, of class URIPathDescriptor.
     *
     * @throws Exception
     */
    @Test
    public void testSameSite()
        throws Exception
    {
        final URIPathDescriptor path = new URIPathDescriptor( BASE_URL, "doxia" );

        assertTrue( path.sameSite( new URI( "http://maven.apache.org/" ) ) );
        assertTrue( path.sameSite( new URI( "http://maven.apache.org" ) ) );
        assertTrue( path.sameSite( new URI( "HTTP://maven.apache.org/" ) ) );
        assertTrue( path.sameSite( new URI( "http://MAVEN.apache.org/" ) ) );
        assertTrue( path.sameSite( new URI( "http://maven.apache.org/wagon/index.html" ) ) );

        assertFalse( path.sameSite( null ) );
        assertFalse( path.sameSite( new URI( "https://maven.apache.org/" ) ) );
        assertFalse( path.sameSite( new URI( "http://ant.apache.org/" ) ) );
        assertFalse( path.sameSite( new URI( "http://maven.apache.org:80" ) ) );
        assertFalse( path.sameSite( new URI( "/usr/share/bin/" ) ) );
        assertFalse( path.sameSite( new URI( "http:///maven.apache.org/" ) ) );

        final URIPathDescriptor nullHost = new URIPathDescriptor( "http:///maven.apache.org/", "doxia" );
        assertTrue( nullHost.sameSite( new URI( "http:///maven.apache.org/" ) ) );
        assertFalse( nullHost.sameSite( new URI( "http://maven.apache.org/" ) ) );

        URIPathDescriptor newPath = new URIPathDescriptor( "file:///C:/Documents%20and%20Settings/", "source" );
        assertTrue( newPath.sameSite( new URI( "file:///C:/Documents%20and%20Settings/" ) ) );
        assertFalse( newPath.sameSite( new URI( "file://C:/Documents%20and%20Settings/" ) ) );
        // authority is empty
        assertTrue( newPath.sameSite( new URI( "file:/C:/Documents%20and%20Settings/" ) ) );
    }

    private static void assertFailure( final String base, final String link )
    {
        try
        {
            final URIPathDescriptor test = new URIPathDescriptor( base, link );
            fail( "Should fail: " + test.toString() );
        }
        catch ( IllegalArgumentException ex )
        {
            assertNotNull( ex );
        }
    }
}
