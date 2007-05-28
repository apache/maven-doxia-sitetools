package org.apache.maven.doxia.site.decoration.inheritance;

import junit.framework.TestCase;

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

/**
 * Testcase for DOXIA-91 problems. All tests make sure that a passed in null will not generate any path conversion but
 * just returns the old path.
 * 
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 */

public class Doxia91Test extends TestCase
{

    public void testOldPathNull() throws Exception
    {
        PathDescriptor oldPath = new PathDescriptor( null );
        PathDescriptor newPath = new PathDescriptor( "http://www.apache.org/" );

        PathDescriptor diff = PathUtils.convertPath( oldPath, newPath );

        assertEquals( diff, oldPath );
    }

    public void testNewPathNull() throws Exception
    {
        PathDescriptor oldPath = new PathDescriptor( "http://www.apache.org/", "file:///home/henning/foo" );
        PathDescriptor newPath = new PathDescriptor( null );

        PathDescriptor diff = PathUtils.convertPath( oldPath, newPath );

        assertEquals( diff, oldPath );
    }

    public void testBothPathNull() throws Exception
    {
        PathDescriptor oldPath = new PathDescriptor( null );
        PathDescriptor newPath = new PathDescriptor( null );

        PathDescriptor diff = PathUtils.convertPath( oldPath, newPath );

        assertEquals( diff, oldPath );
    }
}
