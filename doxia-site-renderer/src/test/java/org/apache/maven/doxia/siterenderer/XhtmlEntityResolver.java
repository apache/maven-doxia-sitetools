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

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author ltheussl
 *
 * @since 1.2
 */
public class XhtmlEntityResolver
    implements EntityResolver
{
    private static final String XHTML_PUBLIC_ID = "-//W3C//DTD XHTML 1.0 Transitional//EN";

    private static final String DTD = "/dtd/xhtml1-transitional.dtd";

    private static final String LAT1_PUBLIC_ID = "-//W3C//ENTITIES Latin 1 for XHTML//EN";

    private static final String LAT1 = "/dtd/xhtml-lat1.ent";

    private static final String SYMBOL_PUBLIC_ID = "-//W3C//ENTITIES Symbols for XHTML//EN";

    private static final String SYMBOL = "/dtd/xhtml-symbol.ent";

    private static final String SPECIAL_PUBLIC_ID = "-//W3C//ENTITIES Special for XHTML//EN";

    private static final String SPECIAL = "/dtd/xhtml-special.ent";

    /** {@inheritDoc} */
    public InputSource resolveEntity( String publicId, String systemId )
        throws SAXException, IOException
    {
        if ( publicId == null )
        {
            return null;
        }

        if ( publicId.equals( XHTML_PUBLIC_ID ) )
        {
            return new InputSource( XhtmlEntityResolver.class.getResourceAsStream( DTD ) );
        }
        else if ( publicId.equals( LAT1_PUBLIC_ID ) )
        {
            return new InputSource( XhtmlEntityResolver.class.getResourceAsStream( LAT1 ) );
        }
        else if ( publicId.equals( SYMBOL_PUBLIC_ID ) )
        {
            return new InputSource( XhtmlEntityResolver.class.getResourceAsStream( SYMBOL ) );
        }
        else if ( publicId.equals( SPECIAL_PUBLIC_ID ) )
        {
            return new InputSource( XhtmlEntityResolver.class.getResourceAsStream( SPECIAL ) );
        }
        else
        {
            return null;
        }
    }
}
