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
import java.util.StringTokenizer;

/**
 * Utilitites that allow conversion of old and new pathes and URLs relative to each other.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public abstract class PathUtils
{
    private PathUtils()
    {
    }

    public static final PathDescriptor convertPath( final PathDescriptor oldPath, final PathDescriptor newPath )
        throws MalformedURLException
    {
        String relative = getRelativePath( oldPath, newPath );

        if ( relative == null )
        {
            return oldPath;
        }

        return new PathDescriptor( relative );
    }

    public static final String getRelativePath( final PathDescriptor oldPathDescriptor,
                                                final PathDescriptor newPathDescriptor ) throws MalformedURLException
    {
        // Cannot convert from URL to file.
        if ( oldPathDescriptor.isFile() )
        {
            if ( !newPathDescriptor.isFile() )
            {
                // We want to convert from a file to an URL. This is normally not possible...
                if ( oldPathDescriptor.isRelative() )
                {
                    // unless the old path is a relative path. Then we might convert an existing
                    // site into a new URL using resolvePaths()...
                    return oldPathDescriptor.getPath();
                }
                else
                {
                    // The old path is not relative. Bail out.
                    return null;
                }
            }
        }

        // Don't optimize to else. This might also be old.isFile && new.isFile ...
        if ( !oldPathDescriptor.isFile() )
        {
            // URLs, determine if they share protocol and domain info
            URL oldUrl = oldPathDescriptor.getPathUrl();
            URL newUrl = newPathDescriptor.getPathUrl();

            if ( oldUrl == null || newUrl == null )
            {
                // One of the sites has a strange URL. no relative path possible, bail out.
                return null;
            }

            if ( ( newUrl.getProtocol().equalsIgnoreCase( oldUrl.getProtocol() ) )
                            && ( newUrl.getHost().equalsIgnoreCase( oldUrl.getHost() ) )
                            && ( newUrl.getPort() == oldUrl.getPort() ) )
            {
                // Both pathes point to the same site. So we can use relative pathes.

                String oldPath = oldPathDescriptor.getPath();
                String newPath = newPathDescriptor.getPath();

                return getRelativeWebPath( newPath, oldPath );
            }
            else
            {
                // Different sites. No relative Path possible.
                return null;
            }
        }
        else
        {
            // Both Descriptors point to a path. We can build a relative path.
            String oldPath = oldPathDescriptor.getPath();
            String newPath = newPathDescriptor.getPath();

            if ( oldPath == null || newPath == null )
            {
                // One of the sites has a strange URL. no relative path possible, bail out.
                return null;
            }

            return getRelativeFilePath( oldPath, newPath );
        }
    }

    /**
     * This method can calculate the relative path between two pathes on a web site.
     */
    public static final String getRelativeWebPath( final String oldPath, final String newPath )
    {
        String resultPath = buildRelativePath( newPath, oldPath, '/' );

        if ( newPath.endsWith( "/" ) && !resultPath.endsWith( "/" ) )
        {
            return resultPath + "/";
        }
        else
        {
            return resultPath;
        }
    }

    /**
     * This method can calculate the relative path between two pathes on a file system.
     */
    public static final String getRelativeFilePath( final String oldPath, final String newPath )
    {
        // normalise the path delimiters
        String fromPath = new File( oldPath ).getPath();
        String toPath = new File( newPath ).getPath();

        // strip any leading slashes if its a windows path
        if ( toPath.matches( "^\\[a-zA-Z]:" ) )
        {
            toPath = toPath.substring( 1 );
        }
        if ( fromPath.matches( "^\\[a-zA-Z]:" ) )
        {
            fromPath = fromPath.substring( 1 );
        }

        // lowercase windows drive letters.
        if ( fromPath.startsWith( ":", 1 ) )
        {
            fromPath = fromPath.substring( 0, 1 ).toLowerCase() + fromPath.substring( 1 );
        }
        if ( toPath.startsWith( ":", 1 ) )
        {
            toPath = toPath.substring( 0, 1 ).toLowerCase() + toPath.substring( 1 );
        }

        // check for the presence of windows drives. No relative way of
        // traversing from one to the other.
        if ( ( toPath.startsWith( ":", 1 ) && fromPath.startsWith( ":", 1 ) )
                        && ( !toPath.substring( 0, 1 ).equals( fromPath.substring( 0, 1 ) ) ) )
        {
            // they both have drive path element but they dont match, no
            // relative path
            return null;
        }

        if ( ( toPath.startsWith( ":", 1 ) && !fromPath.startsWith( ":", 1 ) )
                        || ( !toPath.startsWith( ":", 1 ) && fromPath.startsWith( ":", 1 ) ) )
        {
            // one has a drive path element and the other doesnt, no relative
            // path.
            return null;
        }

        String resultPath = buildRelativePath( toPath, fromPath, File.separatorChar );

        if ( newPath.endsWith( File.separator ) && !resultPath.endsWith( File.separator ) )
        {
            return resultPath + File.separator;
        }
        else
        {
            return resultPath;
        }
    }

    private static final String buildRelativePath( final String toPath, final String fromPath, final char separatorChar )
    {
        // use tokeniser to traverse paths and for lazy checking
        StringTokenizer toTokeniser = new StringTokenizer( toPath, String.valueOf( separatorChar ) );
        StringTokenizer fromTokeniser = new StringTokenizer( fromPath, String.valueOf( separatorChar ) );

        int count = 0;

        // walk along the to path looking for divergence from the from path
        while ( toTokeniser.hasMoreTokens() && fromTokeniser.hasMoreTokens() )
        {
            if ( separatorChar == '\\' )
            {
                if ( !fromTokeniser.nextToken().equalsIgnoreCase( toTokeniser.nextToken() ) )
                {
                    break;
                }
            }
            else
            {
                if ( !fromTokeniser.nextToken().equals( toTokeniser.nextToken() ) )
                {
                    break;
                }
            }

            count++;
        }

        // reinitialise the tokenisers to count positions to retrieve the
        // gobbled token

        toTokeniser = new StringTokenizer( toPath, String.valueOf( separatorChar ) );
        fromTokeniser = new StringTokenizer( fromPath, String.valueOf( separatorChar ) );

        while ( count-- > 0 )
        {
            fromTokeniser.nextToken();
            toTokeniser.nextToken();
        }

        String relativePath = "";

        // add back refs for the rest of from location.
        while ( fromTokeniser.hasMoreTokens() )
        {
            fromTokeniser.nextToken();

            relativePath += "..";

            if ( fromTokeniser.hasMoreTokens() )
            {
                relativePath += separatorChar;
            }
        }

        if ( relativePath.length() != 0 && toTokeniser.hasMoreTokens() )
        {
            relativePath += separatorChar;
        }

        // add fwd fills for whatevers left of newPath.
        while ( toTokeniser.hasMoreTokens() )
        {
            relativePath += toTokeniser.nextToken();

            if ( toTokeniser.hasMoreTokens() )
            {
                relativePath += separatorChar;
            }
        }
        return relativePath;
    }
}
