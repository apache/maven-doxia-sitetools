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
 *
 * @since 1.2
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
     * Before being parsed to {@link URI}s, the arguments are modified to catch
     * some common bad practices: first all Windows-style backslashes '\' are replaced by
     * forward slashes '/'.
     * If the baseURI does not end with '/', a slash is appended.
     * If the link starts with a '/', the first character is stripped.
     *
     * @param baseURI The base URI. Has to be a valid absolute URI.
     *      In addition, the path of the URI should not have any file part,
     *      ie <code>http://maven.apache.org/</code> is valid,
     *      <code>http://maven.apache.org/index.html</code> is not.
     *      Even though the latter form is accepted without warning,
     *      the methods in this class will not return what is probably expected,
     *      because a slash is appended during construction, as noted above.
     * @param link the link. This may be a relative link or an absolute link.
     *      Note that URIs that start with a "/", ie don't specify a scheme, are considered relative.
     *
     * @throws IllegalArgumentException if either argument is not parsable as a URI,
     *      or if baseURI is not absolute.
     */
    public URIPathDescriptor( final String baseURI, final String link )
    {
        final String llink = sanitizeLink( link );
        final String bbase = sanitizeBase( baseURI );

        this.baseURI = URI.create( bbase ).normalize();
        this.link = URI.create( llink ).normalize();

        if ( !this.baseURI.isAbsolute() )
        {
            throw new IllegalArgumentException( "Base URI is not absolute: " + baseURI );
        }
    }

    /**
     * Return the base of this URIPathDescriptor as a URI.
     * This is always {@link URI#normalize() normalized}.
     *
     * @return the normalized base URI.
     */
    public URI getBaseURI()
    {
        return baseURI;
    }

    /**
     * Return the link of this URIPathDescriptor as a URI.
     * This is always {@link URI#normalize() normalized}.
     *
     * @return the normalized link URI.
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

    // NOTE: URI.relativize does not work as expected, see
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6226081
    private static URI relativizeLink( final String base, final URI link )
    {
        if ( !link.isAbsolute() )
        {
            return link;
        }

        final URI newBaseURI = URI.create( base );

        if ( !sameSite( link, newBaseURI ) )
        {
            return link;
        }

        final String relativePath = PathTool.getRelativeWebPath( newBaseURI.toString(), link.toString() );

        return URI.create( correctRelativePath( relativePath ) );
    }

    /**
     * Calculate the link as viewed from a different base.
     * This returns the original link if link is absolute.
     * This returns {@link #resolveLink()} if either
     *      newBase == null,
     *      or newBase is not parsable as a URI,
     *      or newBase and this {@link #getBaseURI()} do not share the
     *      {@link #sameSite(java.net.URI) same site}.
     *
     * @param newBase the new base URI. Has to be parsable as a URI.
     *.
     * @return a new relative link or the original link {@link #resolveLink() resolved},
     *      i.e. as an absolute link, if the link cannot be re-based.
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

        final String relativeBasePath = PathTool.getRelativeWebPath( newBaseURI.getPath(), baseURI.getPath() );

        return URI.create( correctRelativePath( relativeBasePath ) ).resolve( link );
    }

    private static String correctRelativePath( final String relativePath )
    {
        if ( "".equals( relativePath ) || "/".equals( relativePath ) )
        {
            return "./";
        }
        else
        {
            return relativePath;
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
        return ( uri != null ) && sameSite( this.baseURI, uri );
    }

    private static boolean sameSite( final URI baseURI, final URI newBaseURI )
    {
        final boolean sameScheme =
            ( newBaseURI.getScheme() == null ? false : baseURI.getScheme().equalsIgnoreCase( newBaseURI.getScheme() ) );
        final boolean sameHost =
            ( baseURI.getHost() == null ? newBaseURI.getHost() == null
                            : baseURI.getHost().equalsIgnoreCase( newBaseURI.getHost() ) );
        final boolean samePort = ( baseURI.getPort() == newBaseURI.getPort() );

        return ( sameScheme && samePort && sameHost );
    }

    /**
     * Construct a string representation of this URIPathDescriptor.
     * This is equivalent to calling {@link #resolveLink()}.toString().
     *
     * @return this URIPathDescriptor as a String.
     */
    @Override
    public String toString()
    {
        return resolveLink().toString();
    }

    private static String sanitizeBase( final String base )
    {
        String sane = base.replace( '\\', '/' );

        if ( !sane.endsWith( "/" ) )
        {
            sane += "/";
        }

        return sane;
    }

    private static String sanitizeLink( final String link )
    {
        String sane = link.replace( '\\', '/' );

        if ( sane.startsWith( "/" ) )
        {
            sane = sane.substring( 1 );
        }

        return sane;
    }
}
