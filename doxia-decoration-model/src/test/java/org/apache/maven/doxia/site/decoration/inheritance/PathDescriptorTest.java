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

import java.io.File;

import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the PathDescriptor creation under various circumstances.
 *
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 */
public class PathDescriptorTest
{
    /** @throws Exception */
    @Test
    public void testAbsPath()
        throws Exception
    {
        String path = "absolutePath";

        PathDescriptor desc = new PathDescriptor( "/" + path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testRelPath()
        throws Exception
    {
        String path = "relativePath";

        PathDescriptor desc = new PathDescriptor( path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testEmptyAbsPath()
        throws Exception
    {
        String path = "";

        PathDescriptor desc = new PathDescriptor( "/" + path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testEmptyRelPath()
        throws Exception
    {
        String path = "";

        PathDescriptor desc = new PathDescriptor( path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testNullPath()
        throws Exception
    {
        String path = null;

        PathDescriptor desc = new PathDescriptor( path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNull( desc.getPath() );
        assertNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testNullBaseAbsPath()
        throws Exception
    {
        String base = null;
        String path = "absolutePath";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testNullBaseRelPath()
        throws Exception
    {
        String base = null;
        String path = "relativePath";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testNullBaseEmptyAbsPath()
        throws Exception
    {
        String base = null;
        String path = "";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testNullBaseEmptyRelPath()
        throws Exception
    {
        String base = null;
        String path = "";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testNullBaseNullPath()
        throws Exception
    {
        String base = null;
        String path = null;

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertTrue( desc.isRelative() );
        assertNull( desc.getBaseUrl() );
        assertNull( desc.getPathUrl() );
        assertNull( desc.getPath() );
        assertNull( desc.getLocation() );
        assertEquals( "wrong path", path, desc.getPath() );
        assertEquals( "wrong location", path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testUrlBaseAbsPath()
        throws Exception
    {
        String base = "http://maven.apache.org/";
        String path = "absolutePath";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertFalse( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", "/" + path, desc.getPath() );
        assertEquals( "wrong location", base + path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testUrlBaseRelPath()
        throws Exception
    {
        String base = "http://maven.apache.org/";
        String path = "relativePath";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertFalse( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", "/" + path, desc.getPath() );
        assertEquals( "wrong location", base + path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testUrlBaseEmptyAbsPath()
        throws Exception
    {
        String base = "http://maven.apache.org/";
        String path = "";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertFalse( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", "/" + path, desc.getPath() );
        assertEquals( "wrong location", base + path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testUrlBaseEmptyRelPath()
        throws Exception
    {
        String base = "http://maven.apache.org/";
        String path = "";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertFalse( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", "/" + path, desc.getPath() );
        assertEquals( "wrong location", base + path, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testUrlBaseNullPath()
        throws Exception
    {
        String base = "http://maven.apache.org/";
        String path = null;

        PathDescriptor desc = new PathDescriptor( base, path );

        assertFalse( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", "/", desc.getPath() );
        assertEquals( "wrong location", base, desc.getLocation() );
    }

    /** @throws Exception */
    @Test
    public void testFileBaseAbsPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "absolutePath";

        PathDescriptor desc = new PathDescriptor( "file://" + base, "/" + path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            String s = StringUtils.replace( new File( base + "/" + path ).toURI().toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base + "/" + path, desc.getPath() );
            assertEquals( "wrong location", base + "/" + path, desc.getLocation() );
        }
    }

    /** @throws Exception */
    @Test
    public void testFileBaseRelPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "relativePath";

        PathDescriptor desc = new PathDescriptor( "file://" + base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            String s = StringUtils.replace( new File( base + "/" + path ).toURI().toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base + "/" + path, desc.getPath() );
            assertEquals( "wrong location", base + "/" + path, desc.getLocation() );
        }
    }

    /** @throws Exception */
    @Test
    public void testFileBaseEmptyAbsPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "";

        PathDescriptor desc = new PathDescriptor( "file://" + base, "/" + path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            String s = StringUtils.replace( new File( base ).toURI().toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base, desc.getPath() );
            assertEquals( "wrong location", base, desc.getLocation() );
        }
    }

    /** @throws Exception */
    @Test
    public void testFileBaseEmptyRelPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "";

        PathDescriptor desc = new PathDescriptor( "file://" + base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            String s = StringUtils.replace( new File( base ).toURI().toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base, desc.getPath() );
            assertEquals( "wrong location", base, desc.getLocation() );
        }
    }

    /** @throws Exception */
    @Test
    public void testFileBaseNullPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = null;

        PathDescriptor desc = new PathDescriptor( "file://" + base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", base, desc.getPath() );
        assertEquals( "wrong location", base, desc.getLocation() );
    }

/*
    // same as testUrlBaseAbsPath with scp, this fails!? DOXIASITETOOLS-47
    public void testUriBaseAbsPath()
        throws Exception
    {
        String base = "scp://people.apache.org/";
        String path = "absolutePath";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertFalse( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( "wrong path", "/" + path, desc.getPath() );
        assertEquals( "wrong location", base + path, desc.getLocation() );
    }
*/

    /** @throws Exception */
    @Test
    public void testPathBaseAbsPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "absolutePath";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            String s = StringUtils.replace( new File( base + "/" + path ).toURI().toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base + "/" + path, desc.getPath() );
            assertEquals( "wrong location", base + "/" + path, desc.getLocation() );
        }
    }

    /** @throws Exception */
    @Test
    public void testPathBaseRelPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "relativePath";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            String s = StringUtils.replace( new File( base + "/" + path ).toURI().toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base + "/" + path, desc.getPath() );
            assertEquals( "wrong location", base + "/" + path, desc.getLocation() );
        }
    }

    /** @throws Exception */
    @Test
    public void testPathBaseEmptyAbsPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "";

        PathDescriptor desc = new PathDescriptor( base, "/" + path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            String s = StringUtils.replace( new File( base ).toURI().toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base, desc.getPath() );
            assertEquals( "wrong location", base, desc.getLocation() );
        }
    }

    /** @throws Exception */
    @Test
    public void testPathBaseEmptyRelPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = "";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            String s = StringUtils.replace( new File( base ).toURI().toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base, desc.getPath() );
            assertEquals( "wrong location", base, desc.getLocation() );
        }
    }

    /** @throws Exception */
    @Test
    public void testPathBaseNullPath()
        throws Exception
    {
        String base = "/tmp/foo";
        String path = null;

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            String s = StringUtils.replace( new File( base ).toURI().toURL().toString(), "file:", "" );
            assertEquals( "wrong path", s, desc.getPath() );
            assertEquals( "wrong location", s, desc.getLocation() );
        }
        else
        {
            assertEquals( "wrong path", base, desc.getPath() );
            assertEquals( "wrong location", base, desc.getLocation() );
        }
    }

    /** @throws Exception */
    @Test
    public void testPathRelBase()
        throws Exception
    {
        String base = "../msite-404";
        String path = "index.html";

        PathDescriptor desc = new PathDescriptor( base, path );

        assertTrue( desc.isFile() );
        assertFalse( desc.isRelative() );
        assertNotNull( desc.getBaseUrl() );
        assertNotNull( desc.getPathUrl() );
        assertNotNull( desc.getPath() );
        assertNotNull( desc.getLocation() );
        assertEquals( desc.getPath(), desc.getLocation() );
        // Hudson doesn't like this?
        //assertEquals( desc.getPathUrl().toString(), desc.getBaseUrl().toString() + "/" + path );
    }
}
