package org.apache.maven.doxia.docrenderer;

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
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.doxia.Doxia;
import org.apache.maven.doxia.document.DocumentModel;
import org.apache.maven.doxia.document.io.xpp3.DocumentXpp3Reader;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.parser.manager.ParserNotFoundException;
import org.apache.maven.doxia.logging.PlexusLoggerWrapper;
import org.apache.maven.doxia.module.site.SiteModule;
import org.apache.maven.doxia.module.site.manager.SiteModuleManager;

import org.codehaus.plexus.logging.AbstractLogEnabled;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Abstract <code>document</code> renderer.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @author ltheussl
 * @version $Id$
 * @since 1.1
 */
public abstract class AbstractDocumentRenderer
    extends AbstractLogEnabled
    implements DocumentRenderer
{
    /** @plexus.requirement */
    protected SiteModuleManager siteModuleManager;

    /** @plexus.requirement */
    protected Doxia doxia;

    /**
     * The common base directory of source files.
     */
    private String baseDir;

      //--------------------------------------------
     //
    //--------------------------------------------

    /**
     * Render a document from the files found in a Map.
     *
     * @param filesToProcess the Map of Files to process. The Map should contain as keys the paths of the
     *      source files (relative to {@link #getBaseDir() baseDir}), and the corresponding SiteModule as values.
     * @param outputDirectory the output directory where the document should be generated.
     * @param documentModel the document model, containing all the metadata, etc.
     * @throws org.apache.maven.doxia.docrenderer.DocumentRendererException if any
     * @throws java.io.IOException if any
     */
    public abstract void render( Map filesToProcess, File outputDirectory, DocumentModel documentModel )
        throws DocumentRendererException, IOException;

      //--------------------------------------------
     //
    //--------------------------------------------

    /** {@inheritDoc} */
    public void render( Collection files, File outputDirectory, DocumentModel documentModel )
        throws DocumentRendererException, IOException
    {
        render( getFilesToProcess( files ), outputDirectory, documentModel );
    }

    /** {@inheritDoc} */
    public void render( File baseDirectory, File outputDirectory, DocumentModel documentModel )
        throws DocumentRendererException, IOException
    {
        render( getFilesToProcess( baseDirectory ), outputDirectory, documentModel );
    }

    /**
     * Render a document from the files found in baseDirectory. This just forwards to
     *              {@link #render(File,File,DocumentModel)} with a new DocumentModel.
     *
     * @param baseDirectory the directory containing the source files.
     *              This should follow the standard Maven convention, ie containing all the site modules.
     * @param outputDirectory the output directory where the document should be generated.
     * @throws org.apache.maven.doxia.docrenderer.DocumentRendererException if any
     * @throws java.io.IOException if any
     */
    public void render( File baseDirectory, File outputDirectory )
        throws DocumentRendererException, IOException
    {
        render( baseDirectory, outputDirectory, new DocumentModel() );
    }

    /**
     * Render a document from the files found in baseDirectory.
     *
     * @param baseDirectory the directory containing the source files.
     *              This should follow the standard Maven convention, ie containing all the site modules.
     * @param outputDirectory the output directory where the document should be generated.
     * @param documentDescriptor a file containing the document model.
     *              If this file does not exist or is null, some default settings will be used.
     * @throws org.apache.maven.doxia.docrenderer.DocumentRendererException if any
     * @throws java.io.IOException if any
     */
    public void render( File baseDirectory, File outputDirectory, File documentDescriptor )
        throws DocumentRendererException, IOException
    {
        if ( ( documentDescriptor == null ) || ( !documentDescriptor.exists() ) )
        {
            if ( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "No documentDescriptor found: using default settings!" );
            }

            render( baseDirectory, outputDirectory );
        }
        else
        {
            render( getFilesToProcess( baseDirectory ), outputDirectory, readDocumentModel( documentDescriptor ) );
        }
    }

    /**
     * Returns a Map of files to process. The Map contains as keys the paths of the source files
     *      (relative to {@link #getBaseDir() baseDir}), and the corresponding SiteModule as values.
     *
     * @param baseDirectory the directory containing the source files.
     *              This should follow the standard Maven convention, ie containing all the site modules.
     * @return a Map of files to process.
     * @throws java.io.IOException in case of a problem reading the files under baseDirectory.
     */
    public Map getFilesToProcess( File baseDirectory )
        throws IOException
    {
        if ( !baseDirectory.isDirectory() )
        {
            if ( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "No files found to process!" );
            }

            return new HashMap();
        }

        setBaseDir( baseDirectory.getAbsolutePath() );

        Map filesToProcess = new HashMap();

        for ( Iterator i = siteModuleManager.getSiteModules().iterator(); i.hasNext(); )
        {
            SiteModule module = (SiteModule) i.next();

            File moduleBasedir = new File( baseDirectory, module.getSourceDirectory() );

            if ( moduleBasedir.exists() )
            {
                // TODO: handle in/excludes
                List allFiles = FileUtils.getFileNames( moduleBasedir, "**/*.*", null, false );

                String lowerCaseExtension = module.getExtension().toLowerCase( Locale.ENGLISH );
                List docs = new LinkedList( allFiles );
                // Take care of extension case
                for ( Iterator it = docs.iterator(); it.hasNext(); )
                {
                    String name = it.next().toString().trim();

                    if ( !name.toLowerCase( Locale.ENGLISH ).endsWith( "." + lowerCaseExtension ) )
                    {
                        it.remove();
                    }
                }

                for ( Iterator j = docs.iterator(); j.hasNext(); )
                {
                    String filePath = ( (File) j.next() ).getPath();

                    filesToProcess.put( filePath, module );
                }
            }
        }

        return filesToProcess;
    }

    /**
     * Returns a Map of files to process. The Map contains as keys the paths of the source files
     *      (relative to {@link #getBaseDir() baseDir}), and the corresponding SiteModule as values.
     *
     * @param files The Collection of source files.
     * @return a Map of files to process.
     */
    public Map getFilesToProcess( Collection files )
    {
        // ----------------------------------------------------------------------
        // Map all the file names to parser ids
        // ----------------------------------------------------------------------

        Map filesToProcess = new HashMap();

        for ( Iterator it = siteModuleManager.getSiteModules().iterator(); it.hasNext(); )
        {
            SiteModule siteModule = (SiteModule) it.next();

            String extension = "." + siteModule.getExtension();

            String sourceDirectory = File.separator + siteModule.getSourceDirectory() + File.separator;

            for ( Iterator j = files.iterator(); j.hasNext(); )
            {
                String file = (String) j.next();

                // first check if the file path contains one of the recognized source dir identifiers
                // (there's trouble if a pathname contains 2 identifiers), then match file extensions (not unique).

                if ( file.indexOf( sourceDirectory ) != -1 )
                {
                    filesToProcess.put( file, siteModule );
                }
                else if ( file.toLowerCase( Locale.ENGLISH ).endsWith( extension ) )
                {
                    // don't overwrite if it's there already
                    if ( !filesToProcess.containsKey( file ) )
                    {
                        filesToProcess.put( file, siteModule );
                    }
                }
            }
        }

        return filesToProcess;
    }

    /** {@inheritDoc} */
    public DocumentModel readDocumentModel( File documentDescriptor )
        throws DocumentRendererException, IOException
    {
        DocumentModel documentModel;

        Reader reader = null;
        try
        {
            reader = ReaderFactory.newXmlReader( documentDescriptor );
            documentModel = new DocumentXpp3Reader().read( reader );
        }
        catch ( XmlPullParserException e )
        {
            throw new DocumentRendererException( "Error parsing document descriptor", e );
        }
        finally
        {
            IOUtil.close( reader );
        }

        return documentModel;
    }

    /**
     * Sets the current base directory.
     *
     * @param newDir the absolute path to the base directory to set.
     */
    public void setBaseDir( String newDir )
    {
        this.baseDir = newDir;
    }

    /**
     * Return the current base directory.
     *
     * @return the current base directory.
     */
    public String getBaseDir()
    {
        return this.baseDir;
    }

      //--------------------------------------------
     //
    //--------------------------------------------

    /**
     * Parse a source document into a sink.
     *
     * @param fullDocPath absolute path to the source document.
     * @param parserId determines the parser to use.
     * @param sink the sink to receive the events.
     * @throws org.apache.maven.doxia.docrenderer.DocumentRendererException in case of a parsing error.
     * @throws java.io.IOException if the source document cannot be opened.
     */
    protected void parse( String fullDocPath, String parserId, Sink sink )
        throws DocumentRendererException, IOException
    {
        Reader reader = null;
        try
        {
            File f = new File( fullDocPath );

            Parser parser = doxia.getParser( parserId );
            switch ( parser.getType() )
            {
                case Parser.XML_TYPE:
                    reader = ReaderFactory.newXmlReader( f );
                    break;

                case Parser.TXT_TYPE:
                case Parser.UNKNOWN_TYPE:
                default:
                    // TODO Platform dependent?
                    reader = ReaderFactory.newPlatformReader( f );
            }

            sink.enableLogging( new PlexusLoggerWrapper( getLogger() ) );

            doxia.parse( reader, parserId, sink );
        }
        catch ( ParserNotFoundException e )
        {
            throw new DocumentRendererException( "No parser '" + parserId
                        + "' found for " + fullDocPath + ": " + e.getMessage() );
        }
        catch ( ParseException e )
        {
            throw new DocumentRendererException( "Error parsing " + fullDocPath + ": " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( reader );

            sink.flush();
        }
    }

    /**
     * Copies the contents of the resource directory to an output folder.
     *
     * @param outputDirectory the destination folder.
     * @throws java.io.IOException if any.
     */
    protected void copyResources( File outputDirectory )
            throws IOException
    {
        File resourcesDirectory = new File( getBaseDir(), "resources" );

        if ( resourcesDirectory.isDirectory() && outputDirectory.isDirectory() )
        {
            copyDirectory( resourcesDirectory, outputDirectory );
        }
    }

    /**
     * Copy content of a directory, excluding scm-specific files.
     *
     * @param source directory that contains the files and sub-directories to be copied.
     * @param destination destination folder.
     * @throws java.io.IOException if any.
     */
    protected void copyDirectory( File source, File destination )
            throws IOException
    {
        if ( source.isDirectory() && destination.isDirectory() )
        {
            DirectoryScanner scanner = new DirectoryScanner();

            String[] includedResources = {"**/**"};

            scanner.setIncludes( includedResources );

            scanner.addDefaultExcludes();

            scanner.setBasedir( source );

            scanner.scan();

            List includedFiles = Arrays.asList( scanner.getIncludedFiles() );

            for ( Iterator j = includedFiles.iterator(); j.hasNext(); )
            {
                String name = (String) j.next();

                File sourceFile = new File( source, name );

                File destinationFile = new File( destination, name );

                FileUtils.copyFile( sourceFile, destinationFile );
            }
        }
    }
}
