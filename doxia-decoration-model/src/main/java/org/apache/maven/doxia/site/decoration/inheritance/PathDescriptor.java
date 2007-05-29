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
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.util.StringUtils;

/**
 * This class holds an instance of a maven path. This consists of a relative path (e.g. images/maven-logo.png) and a
 * base reference which can also be a relative path (e.g. '.' or '../doxia') or an URL that is used for an absolute
 * anchor.
 *
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */

public class PathDescriptor
{
    private final URL baseUrl;

    private final URL pathUrl;

    private final String relativePath;

    public PathDescriptor( final String path ) throws MalformedURLException
    {
        this( (URL) null, path );
    }

    public PathDescriptor( final String base, final String path ) throws MalformedURLException
    {
        this( PathDescriptor.buildBaseUrl( base ), path );
    }

    public PathDescriptor( final URL baseUrl, final String path ) throws MalformedURLException
    {
        this.baseUrl = baseUrl;

        URL pathUrl = null;
        String relativePath = null;
        try
        {
            pathUrl = new URL( path );
        }
        catch ( MalformedURLException e )
        {
            try
            {
                pathUrl = buildUrl( baseUrl, path );
            }
            catch ( MalformedURLException e2 )
            {
                // If we got an absolute path passed in and end here, then the path
                // is converted to relative because we have no reference URL anyway
                // to which it has been anchored.
                if ( path != null && path.startsWith( "/" ) )
                {
                    relativePath = path.substring( 1 );
                }
                else
                {
                    relativePath = path;
                }
            }
        }
        this.pathUrl = pathUrl;
        this.relativePath = relativePath;
    }

    private static final URL buildBaseUrl( final String base ) throws MalformedURLException
    {
        if ( base == null )
        {
            return null;
        }

        try
        {
            return new URL( base );
        }
        catch ( MalformedURLException e )
        {
            return new File( base ).toURL();
        }
    }

    private static final URL buildUrl( final URL baseUrl, final String path ) throws MalformedURLException
    {
        if ( baseUrl == null )
        {
            throw new MalformedURLException( "Base is null!" );
        }

        if ( path == null )
        {
            return baseUrl;
        }

        if ( baseUrl.getProtocol().equals( "file" ) )
        {
            return new File( baseUrl.getFile(), path ).toURL();
        }

        if ( path.startsWith( "/" ) && baseUrl.getPath().endsWith( "/" ) )
        {
            return new URL( baseUrl, path.substring( 1 ) );
        }

        return new URL( baseUrl, path );
    }

    public boolean isFile()
    {
        return isRelative() || pathUrl.getProtocol().equals( "file" );
    }

    public boolean isRelative()
    {
        return pathUrl == null;
    }

    public URL getBaseUrl()
    {
        return baseUrl;
    }

    public URL getPathUrl()
    {
        return pathUrl;
    }

    public String getPath()
    {
        if ( getPathUrl() != null )
        {
            if ( isFile() )
            {
                return StringUtils.stripEnd( getPathUrl().getPath(), "/" );
            }
            else
            {
                return getPathUrl().getPath();
            }
        }
        else
        {
            return relativePath;
        }
    }

    public String getLocation()
    {
        if ( isFile() )
        {
            if ( getPathUrl() != null )
            {
                return StringUtils.stripEnd( getPathUrl().getFile(), "/" );
            }
            else
            {
                return relativePath;
            }
        }
        else
        {
            return getPathUrl().toExternalForm();
        }
    }

    public String toString()
    {
        StringBuffer res =
            new StringBuffer( ( StringUtils.isNotEmpty( relativePath ) ) ? relativePath : String.valueOf( pathUrl ) );
        res.append( " (Base: " ).append( baseUrl ).append( ") Location: " ).append( getLocation() );
        return res.toString();
    }
}
