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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author ltheussl
 */
public class PathUtilsTest
{
    private static final String SLASH = File.separator;

    /** @throws Exception */
    @Test
    public void testConvertPath()
        throws Exception
    {
        PathDescriptor oldPath = new PathDescriptor( (String) null, "base" );
        PathDescriptor newPath = new PathDescriptor( "/tmp", "target" );
        assertEquals( oldPath, PathUtils.convertPath( oldPath, newPath ) );
        assertEquals( newPath, PathUtils.convertPath( newPath, oldPath ) );
    }

    /** @throws Exception */
    @Test
    public void testGetRelativePath()
        throws Exception
    {
        PathDescriptor oldPath = new PathDescriptor( "/tmp/foo", "base" );
        PathDescriptor newPath = new PathDescriptor( "/tmp", "target" );
        assertEquals( ".." + SLASH + ".." + SLASH + "target", PathUtils.getRelativePath( oldPath, newPath ) );

        oldPath = new PathDescriptor( (String) null, "base" );
        assertNull( PathUtils.getRelativePath( oldPath, newPath ) );
        assertNull( PathUtils.getRelativePath( newPath, oldPath ) );

        oldPath = new PathDescriptor( "/tmp/foo", null );
        assertEquals( ".." + SLASH + "target", PathUtils.getRelativePath( oldPath, newPath ) );
        assertEquals( ".." + SLASH + "foo", PathUtils.getRelativePath( newPath, oldPath ) );
    }

    /** @throws Exception */
    @Test
    public void testRelativePathScpBase()
        throws Exception
    {
        PathDescriptor oldPath = new PathDescriptor( "http://maven.apache.org/", "source" );
        PathDescriptor newPath = new PathDescriptor( "http://maven.apache.org/", "target" );
        assertEquals( "../source", PathUtils.getRelativePath( oldPath, newPath ) );

        oldPath = new PathDescriptor( "scp://people.apache.org/", "source" );
        newPath = new PathDescriptor( "scp://people.apache.org/", "target" );
        // same with scp URLs fails?! DOXIASITETOOLS-47
        //assertEquals( "../source", PathUtils.getRelativePath( oldPath, newPath ) );
    }
}
