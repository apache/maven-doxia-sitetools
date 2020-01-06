package org.apache.maven.doxia.site.decoration;

import org.codehaus.plexus.util.xml.Xpp3Dom;

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

import org.junit.Test;

import static org.junit.Assert.*;

public class DecorationUtilsTest
{
    @Test
    public void testIsLink()
    {
        assertFalse( DecorationUtils.isLink( null ) );
        assertFalse( DecorationUtils.isLink( "" ) );
        assertFalse( DecorationUtils.isLink( " " ) );
        assertTrue( DecorationUtils.isLink( "http://maven.apache.org/" ) );
        assertTrue( DecorationUtils.isLink( "https://maven.apache.org/" ) );
        assertTrue( DecorationUtils.isLink( "ftp://maven.apache.org/pub/" ) );
        assertTrue( DecorationUtils.isLink( "file:///home" ) );
        assertTrue( DecorationUtils.isLink( "mailto:toto@maven.org" ) );
        assertTrue( DecorationUtils.isLink( "any-protocol://" ) );
    }

    @Test
    public void testGetCustomChild()
    {
        Xpp3Dom dom = new Xpp3Dom( "root" );
        Xpp3Dom level1 = new Xpp3Dom( "level1" );
        dom.addChild( level1 );
        Xpp3Dom level2 = new Xpp3Dom( "level2" );
        level2.setValue( "value" );
        level1.addChild( level2 );

        assertEquals( level1, DecorationUtils.getCustomChild( dom, "level1" ) );
        assertEquals( level2, DecorationUtils.getCustomChild( dom, "level1.level2" ) );
        assertNull( DecorationUtils.getCustomChild( dom, "no.level2" ) );
        assertNull( DecorationUtils.getCustomChild( dom, "level1.no" ) );

        assertEquals( "value", DecorationUtils.getCustomValue( dom, "level1.level2" ) );
        assertNull( DecorationUtils.getCustomValue( dom, "no.level2" ) );
        assertNull( DecorationUtils.getCustomValue( dom, "level1.no" ) );

        assertEquals( "value", DecorationUtils.getCustomValue( dom, "level1.level2", "default" ) );
        assertEquals( "default", DecorationUtils.getCustomValue( dom, "no.level2", "default" ) );
        assertEquals( "default", DecorationUtils.getCustomValue( dom, "level1.no", "default" ) );
    }
}
