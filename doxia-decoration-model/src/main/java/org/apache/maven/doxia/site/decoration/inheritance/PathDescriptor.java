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
 * @deprecated use {@link URIPathDescriptor} instead.
 */

public class PathDescriptor
{
    private final URL baseUrl;

    private final URL pathUrl;

    private final String relativePath;

    /**
     * Construct a PathDescriptor from a path.
     *
     * @param path the path.
     * @throws java.net.MalformedURLException if a URL cannot be formed from the path.
     */
    public PathDescriptor( final String path )
        throws MalformedURLException
    {
        this( (URL) null, path );
    }

    /**
     * Construct a PathDescriptor from a path and a base.
     *
     * @param base a base reference.
     * @param path the path.
     * @throws java.net.MalformedURLException if a URL cannot be formed from the path.
     */
    public PathDescriptor( final String base, final String path )
        throws MalformedURLException
    {
        this( PathDescriptor.buildBaseUrl( base ), path );
    }

    /**
     * Construct a PathDescriptor from a path and a base.
     *
     * @param baseUrl a base reference.
     * @param path the path.
     * @throws java.net.MalformedURLException if a URL cannot be formed from the path.
     */
    public PathDescriptor( final URL baseUrl, final String path )
        throws MalformedURLException
    {
        this.baseUrl = baseUrl;

        URL pathURL = null;
        String relPath = null;

        try
        {
            pathURL = new URL( path );
        }
        catch ( MalformedURLException e )
        {
            try
            {
                pathURL = buildUrl( baseUrl, path );
            }
            catch ( MalformedURLException e2 )
            {
                // If we got an absolute path passed in and end here, then the path
                // is converted to relative because we have no reference URL anyway
                // to which it has been anchored.
                if ( path != null && path.startsWith( "/" ) )
                {
                    relPath = path.substring( 1 );
                }
                else
                {
                    relPath = path;
                }
            }
        }

        this.pathUrl = pathURL;
        this.relativePath = relPath;
    }

    private static URL buildBaseUrl( final String base )
        throws MalformedURLException
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
            return new File( base ).toURI().toURL();
        }
    }

    private static URL buildUrl( final URL baseUrl, final String path )
        throws MalformedURLException
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
            return new File( baseUrl.getFile(), path ).toURI().toURL();
        }

        if ( path.startsWith( "/" ) && baseUrl.getPath().endsWith( "/" ) )
        {
            return new URL( baseUrl, path.substring( 1 ) );
        }

        return new URL( baseUrl, path );
    }

    /**
     * Check if this PathDescriptor describes a file.
     *
     * @return true for file, false otherwise.
     */
    public boolean isFile()
    {
        return isRelative() || pathUrl.getProtocol().equals( "file" );
    }

    /**
     * Check if this PathDescriptor describes a relative path.
     *
     * @return true if {@link #getPathUrl()} returns null.
     */
    public boolean isRelative()
    {
        return pathUrl == null;
    }

    /**
     * Get the base URL.
     *
     * @return the base URL.
     */
    public URL getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * Get the path as a URL.
     *
     * @return the path as a URL.
     */
    public URL getPathUrl()
    {
        return pathUrl;
    }

    /**
     * Get the path.
     *
     * @return the path.
     */
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

    /**
     * Get the location for files.
     *
     * @return the location.
     */
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

    /** {@inheritDoc} */
    public String toString()
    {
        StringBuilder res =
            new StringBuilder( ( StringUtils.isNotEmpty( relativePath ) ) ? relativePath : String.valueOf( pathUrl ) );
        res.append( " (Base: " ).append( baseUrl ).append( ") Location: " ).append( getLocation() );
        return res.toString();
    }
}
