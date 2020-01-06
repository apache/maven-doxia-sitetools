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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.doxia.sink.impl.AbstractSink;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SkinResourceLoaderTest
{
    private SkinResourceLoader skinResourceLoader = new SkinResourceLoader();

    @Test
    public void testNormalizeNewline() throws Exception
    {
        String EOL = AbstractSink.EOL;
        String EOL_MACOS9 = "\r";
        String EOL_UNIX = "\n";
        String EOL_WIN = "\r\n";
        
        assertEquals( "Hello " + EOL + " world", normalizeNewline( "Hello " + EOL_MACOS9 + " world" ) );
        assertEquals( "Hello " + EOL + " world", normalizeNewline( "Hello " + EOL_UNIX + " world" ) );
        assertEquals( "Hello " + EOL + " world", normalizeNewline( "Hello " + EOL_WIN + " world" ) );

        assertEquals( "Hello world" + EOL, normalizeNewline( "Hello world" + EOL_MACOS9 ) );
        assertEquals( "Hello world" + EOL, normalizeNewline( "Hello world" + EOL_UNIX ) );
        assertEquals( "Hello world" + EOL, normalizeNewline( "Hello world" + EOL_WIN ) );

        assertEquals( EOL + "Hello world", normalizeNewline( EOL_MACOS9 + "Hello world" ) );
        assertEquals( EOL + "Hello world", normalizeNewline( EOL_UNIX + "Hello world" ) );
        assertEquals( EOL + "Hello world", normalizeNewline( EOL_WIN + "Hello world" ) );
    }
    
    private String normalizeNewline( String  text ) throws IOException
    {
        InputStream in = new ByteArrayInputStream( text.getBytes() ); 
        InputStream out = skinResourceLoader.normalizeNewline( in );
        return IOUtil.toString( out );
    }
}
