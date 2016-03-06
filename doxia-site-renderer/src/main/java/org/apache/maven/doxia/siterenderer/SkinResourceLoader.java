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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.commons.collections.ExtendedProperties;
import org.codehaus.plexus.util.IOUtil;

/**
 * Skin resource loader: gets content from context classloader, which should contain skin artifact,
 * and normalizes newlines (see <a href="https://issues.apache.org/jira/browse/DOXIASITETOOLS-87">DOXIASITETOOLS-87</a>.
 *
 * @author Hervé Boutemy
 */
@Deprecated
public class SkinResourceLoader
    extends ResourceLoader
{
    public void init( ExtendedProperties configuration )
    {
    }

    public synchronized InputStream getResourceStream( String name )
        throws ResourceNotFoundException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if ( name.startsWith( "/" ) )
        {
            name = name.substring( 1 );
        }

        return normalizeNewline( classLoader.getResourceAsStream( name ) );
    }

    InputStream normalizeNewline( InputStream in )
        throws ResourceNotFoundException
    {
        if ( in == null )
        {
            return null;
        }

        try
        {
            byte[] content = IOUtil.toByteArray( in );

            // following code based on org.apache.maven.doxia.sink.AbstractSink.unifyEOLs(String)

            byte[] eol = System.getProperty( "line.separator" ).getBytes();

            final int size = content.length;

            ByteArrayOutputStream out = new ByteArrayOutputStream( size );

            for ( int i = 0; i < size; i++ )
            {
                byte b = content[i];

                if ( b == '\r' )
                {
                    if ( ( i + 1 ) < size && content[i + 1] == '\n' )
                    {
                        i++;
                    }

                    out.write( eol );
                }
                else if ( b == '\n' )
                {
                    out.write( eol );
                }
                else
                {
                    out.write( b );
                }
            }

            return new ByteArrayInputStream( out.toByteArray() );
        }
        catch ( IOException ioe )
        {
            throw new ResourceNotFoundException( "cannot read resource", ioe );
        }
        finally
        {
            IOUtil.close( in );
        }
    }

    public boolean isSourceModified( Resource resource )
    {
        return false;
    }

    public long getLastModified( Resource resource )
    {
        return 0;
    }
}
