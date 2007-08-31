package org.apache.maven.doxia.docrenderer.itext;

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

import org.apache.maven.doxia.docrenderer.DocRenderer;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id$
 */
public class DefaultPdfRendererTest
    extends PlexusTestCase
{
    private static final String OUTPUT = "target/output";

    private static final String SITE = "src/test/resources/site";

    private static final String DESCRIPTOR_WITHOUT_TOC = "src/test/resources/doc-without-TOC.xml";

    private File outputDirectory;

    private File siteDirectoryFile;

    private DocRenderer docRenderer;

    /** {@inheritDoc} */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        docRenderer = (DocRenderer) lookup( DocRenderer.ROLE );

        outputDirectory = getTestFile( OUTPUT );
        outputDirectory.mkdirs();

        siteDirectoryFile = getTestFile( SITE );

        FileUtils.copyDirectory( new File( siteDirectoryFile, "resources/css" ), new File( outputDirectory, "css" ),
                                 "*.css", ".svn" );
        FileUtils.copyDirectory( new File( siteDirectoryFile, "resources/images" ),
                                 new File( outputDirectory, "images" ), "*.png", ".svn" );
    }

    /** {@inheritDoc} */
    protected void tearDown()
        throws Exception
    {
        release( docRenderer );
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testRenderFileFile()
        throws Exception
    {
        docRenderer.render( siteDirectoryFile, outputDirectory );

        assertTrue( new File( outputDirectory, "faq.pdf" ).exists() );
        assertTrue( new File( outputDirectory, "faq.pdf" ).length() > 0 );
        assertTrue( new File( outputDirectory, "index.pdf" ).exists() );
        assertTrue( new File( outputDirectory, "index.pdf" ).length() > 0 );
        assertTrue( new File( outputDirectory, "overview.pdf" ).exists() );
        assertTrue( new File( outputDirectory, "overview.pdf" ).length() > 0 );
        assertTrue( new File( outputDirectory, "resources.pdf" ).exists() );
        assertTrue( new File( outputDirectory, "resources.pdf" ).length() > 0 );

    }

    /**
     * @throws Exception
     */
    public void testRenderFileFileFile()
        throws Exception
    {
        File descriptor = getTestFile( DESCRIPTOR_WITHOUT_TOC );

        docRenderer.render( siteDirectoryFile, outputDirectory, descriptor );

        assertTrue( new File( outputDirectory, "doc-with-merged.pdf" ).exists() );
        assertTrue( new File( outputDirectory, "doc-with-merged.pdf" ).length() > 0 );
    }
}
