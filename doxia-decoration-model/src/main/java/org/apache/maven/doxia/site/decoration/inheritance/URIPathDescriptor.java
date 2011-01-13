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
import java.net.URISyntaxException;

import org.codehaus.plexus.util.PathTool;

/**
 * Describes a link that may be absolute or relative, and that is anchored to an absolute URI.
 *
 * @author ltheussl
 * @since 1.1.5
 */
public class URIPathDescriptor
{
    private final URI baseURI;
    private final URI link;

    /**
     * A URIPathDescriptor consists of a base URI and a link.
     * Both arguments to this constructor have to be parsable to URIs.
     * The baseURI parameter has to be absolute in the sense of {@link URI#isAbsolute()}.
     *
     * @param baseURI The base URI. Has to be a valid absolute URI.
     *      In addition, the path of the URI should not have any file part,
     *      ie <code>http://maven.apache.org/</code> is valid,
     *      <code>http://maven.apache.org/index.html</code> is not.
     * @param link the link. This may be a relative link or an absolute link.
     *      Note that URIs that start with a "/", ie don't specify a scheme, are considered relative.
     *
     * @throws URISyntaxException if either argument is not parsable as a URI,
     *      or if baseURI is not absolute.
     */
    public URIPathDescriptor( final String baseURI, final String link )
            throws URISyntaxException
    {
        final String llink = link.startsWith( "/" ) ? link.substring( 1 ) : link;
        final String bbase = baseURI.endsWith( "/" ) ? baseURI : baseURI + "/";

        this.baseURI = new URI( bbase ).normalize();
        this.link = new URI( llink ).normalize();

        if ( !this.baseURI.isAbsolute() )
        {
            throw new URISyntaxException( baseURI, "Base must be an absolute URI!" );
        }
    }

    /**
     * Return the base of this URIPathDescriptor as a URI.
     *
     * @return the base URI.
     */
    public URI getBaseURI()
    {
        return baseURI;
    }

    /**
     * Return the link of this URIPathDescriptor as a URI.
     *
     * @return the link URI.
     */
    public URI getLink()
    {
        return link;
    }

    /**
     * Resolve the link to the base.
     * This always returns an absolute URI. If link is absolute, link is returned.
     *
     * @return the resolved link. This is equivalent to calling
     *      {@link #getBaseURI()}.{@link URI#resolve(java.net.URI) resolve}( {@link #getLink()} ).
     */
    public URI resolveLink()
    {
        return baseURI.resolve( link );
    }

    /**
     * Calculate the relative link with respect to the base.
     * The original link is returned if either
     *      link is relative;
     *      or link and base do not share the {@link #sameSite(java.net.URI) same site}.
     *
     * @return the link as a relative URI.
     */
    public URI relativizeLink()
    {
        return relativizeLink( baseURI.toString(), link );
    }

    private static URI relativizeLink( final String base, final URI link )
    {
        if ( !link.isAbsolute() )
        {
            return link;
        }

        final URI newBaseURI;

        try
        {
            newBaseURI = new URI( base );
        }
        catch ( URISyntaxException ex )
        {
            return link;
        }

        if ( !sameSite( link, newBaseURI ) )
        {
            return link;
        }

        final String relativePath = PathTool.getRelativeFilePath( newBaseURI.getPath(), link.getPath() );

        try
        {
            return new URI( relativePath );
        }
        catch ( URISyntaxException ex )
        {
            return link;
        }
    }

    /**
     * Calculate the link as viewed from a different base.
     * This returns the original link if link is absolute.
     * This returns {@link #resolveLink()} if
     *      newBase == null
     *      or newBase is not parsable as a URI.
     *
     * @param newBase the new base URI. Has to be parsable as a URI.
     *.
     * @return a new relative link.
     */
    public URI rebaseLink( final String newBase )
    {
        if ( link.isAbsolute() )
        {
            return link;
        }

        if ( newBase == null )
        {
            return resolveLink();
        }

        final URI newBaseURI;

        try
        {
            newBaseURI = new URI( newBase );
        }
        catch ( URISyntaxException ex )
        {
            return resolveLink();
        }

        if ( !sameSite( newBaseURI ) )
        {
            return resolveLink();
        }

        final String relativeBasePath = PathTool.getRelativeFilePath( newBaseURI.getPath(), baseURI.getPath() );

        try
        {
            return new URI( relativeBasePath ).resolve( link );
        }
        catch ( URISyntaxException ex )
        {
            return resolveLink();
        }
    }

    /**
     * Check if this URIPathDescriptor lives on the same site as the given URI.
     *
     * @param uri a URI to compare with.
     *      May be null, in which case false is returned.
     *
     * @return true if {@link #getBaseURI()} shares the same scheme, host and port with the given URI
     *      where null values are allowed.
     */
    public boolean sameSite( final URI uri )
    {
        if ( uri == null )
        {
            return false;
        }

        return sameSite( this.baseURI, uri );
    }

    private static boolean sameSite( final URI baseURI, final URI newBaseURI )
    {
        final boolean equalScheme = ( baseURI.getScheme() == null ? newBaseURI.getScheme() == null
                : baseURI.getScheme().equalsIgnoreCase( newBaseURI.getScheme() ) );
        final boolean equalHost = ( baseURI.getHost() == null ? newBaseURI.getHost() == null
                : baseURI.getHost().equalsIgnoreCase( newBaseURI.getHost() ) );
        final boolean equalPort = ( baseURI.getPort() == newBaseURI.getPort() );

        return ( equalScheme && equalPort && equalHost );
    }

    /**
     * Construct a string representation of this URIPathDescriptor.
     * This is equivalent to calling {@link #resolveLink()}.toString().
     *
     * @return this URIPathDescriptor as a String.
     */
    public String toString()
    {
        return resolveLink().toString();
    }

}
