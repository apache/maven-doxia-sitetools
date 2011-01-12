
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

import junit.framework.TestCase;

/**
 *
 * @author ltheussl
 * @since 1.1.5
 */
public class URIPathDescriptorTest
        extends TestCase
{
    /**
     * Test of resolveLink method, of class URIPathDescriptor.
     *
     * @throws Exception
     */
    public void testResolveLink()
            throws Exception
    {
        URIPathDescriptor oldPath = new URIPathDescriptor( "http://maven.apache.org/", "source" );
        assertEquals( "http://maven.apache.org/source", oldPath.resolveLink().toString() );

        oldPath = new URIPathDescriptor( "http://maven.apache.org/", "source/" );
        assertEquals( "http://maven.apache.org/source/", oldPath.resolveLink().toString() );

        oldPath = new URIPathDescriptor( "http://maven.apache.org/", "/source" );
        assertEquals( "http://maven.apache.org/source", oldPath.resolveLink().toString() );

        oldPath = new URIPathDescriptor( "http://maven.apache.org", "source" );
        assertEquals( "http://maven.apache.org/source", oldPath.resolveLink().toString() );
    }

    /**
     * Test of rebaseLink method, of class URIPathDescriptor.
     *
     * @throws Exception
     */
    public void testRebaseLink()
            throws Exception
    {
        URIPathDescriptor oldPath = new URIPathDescriptor( "http://maven.apache.org/", "source" );
        assertEquals( "../source", oldPath.rebaseLink( "http://maven.apache.org/doxia/" ).toString() );

        oldPath = new URIPathDescriptor( "scp://people.apache.org/", "source" );
        assertEquals( "../source", oldPath.rebaseLink( "scp://people.apache.org/doxia" ).toString() );

        oldPath = new URIPathDescriptor( "http://maven.apache.org/", "banner/left" );
        assertEquals( "../banner/left", oldPath.rebaseLink( "http://maven.apache.org/doxia/" ).toString() );

        oldPath = new URIPathDescriptor( "http://jakarta.apache.org/", "banner/left" );
        assertEquals( "http://jakarta.apache.org/banner/left", oldPath.rebaseLink( "http://maven.apache.org/" ).toString() );
    }

    /**
     * Test of relativizeLink method, of class URIPathDescriptor.
     *
     * @throws Exception
     */
    public void testRelativizeLink()
            throws Exception
    {
        URIPathDescriptor path = new URIPathDescriptor( "http://maven.apache.org/", "source" );
        assertEquals( "source", path.relativizeLink().toString() );

        path = new URIPathDescriptor( "http://maven.apache.org/", "http://maven.apache.org/source" );
        assertEquals( "source", path.relativizeLink().toString() );

        path = new URIPathDescriptor( "http://maven.apache.org/doxia/", "http://maven.apache.org/source/" );
        assertEquals( "../source/", path.relativizeLink().toString() );

        path = new URIPathDescriptor( "http://maven.apache.org/doxia", "http://maven.apache.org/source" );
        assertEquals( "../source", path.relativizeLink().toString() );
    }
}
