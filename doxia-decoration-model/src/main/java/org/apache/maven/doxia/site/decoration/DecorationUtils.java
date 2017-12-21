package org.apache.maven.doxia.site.decoration;

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

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Decoration model utilities.
 * 
 * @since 1.7
 */
public class DecorationUtils
{
    public static boolean isLink( String href )
    {
        return StringUtils.isNotBlank( href )
            && ( startsWithAnyIgnoreCase( href, "http:/", "https:/", "ftp:/", "mailto:", "file:/" )
                || href.contains( "://" ) );
    }

    private static boolean startsWithIgnoreCase( String str, String prefix )
    {
        if ( str == null || prefix == null )
        {
            return ( str == null && prefix == null );
        }
        if ( prefix.length() > str.length() )
        {
            return false;
        }
        return str.regionMatches( true, 0, prefix, 0, prefix.length() );
    }

    public static boolean startsWithAnyIgnoreCase( String string, String... searchStrings )
    {
        for ( int i = 0; i < searchStrings.length; i++ )
        {
            String searchString = searchStrings[i];
            if ( startsWithIgnoreCase( string, searchString ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper to get decoration custom DOM element by simply specifying a dotted path.
     *
     * @param custom the custom DOM element
     * @param path the dotted path to the child
     * @return <code>null</code> if any element in the path does not exist
     * @since 1.8
     */
    public static Xpp3Dom getCustomChild( Xpp3Dom custom, String path )
    {
        String[] elements = path.split( "\\." );
        for ( String element : elements )
        {
            if ( custom == null )
            {
                return null;
            }
            custom = custom.getChild( element );
        }
        return custom;
    }

    /**
     * Helper to get decoration custom DOM element value by simply specifying a dotted path.
     *
     * @param custom the custom DOM element
     * @param path the dotted path to the child
     * @return the element value or <code>null</code> if any element in the path does not exist
     * @since 1.8
     */
    public static String getCustomValue( Xpp3Dom custom, String path )
    {
        custom = getCustomChild( custom, path );
        return ( custom == null ) ? null : custom.getValue();
    }

    /**
     * Helper to get decoration custom DOM element value by simply specifying a dotted path.
     *
     * @param custom the custom DOM element
     * @param path the dotted path to the child
     * @param defaultValue default value
     * @return the element value or the default value if any element in the path does not exist
     * @since 1.8
     */
    public static String getCustomValue( Xpp3Dom custom, String path, String defaultValue )
    {
        custom = getCustomChild( custom, path );
        return ( custom == null ) ? defaultValue : custom.getValue();
    }
}
