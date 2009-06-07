package org.apache.maven.doxia.docrenderer.pdf.fo;

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
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.maven.doxia.docrenderer.DocumentRendererException;
import org.apache.maven.doxia.docrenderer.pdf.AbstractPdfRenderer;
import org.apache.maven.doxia.document.DocumentModel;
import org.apache.maven.doxia.document.DocumentTOC;
import org.apache.maven.doxia.document.DocumentTOCItem;
import org.apache.maven.doxia.module.fo.FoAggregateSink;
import org.apache.maven.doxia.module.fo.FoSink;
import org.apache.maven.doxia.module.fo.FoSinkFactory;
import org.apache.maven.doxia.module.fo.FoUtils;
import org.apache.maven.doxia.module.site.SiteModule;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;

import org.xml.sax.SAXParseException;

/**
 * PDF renderer that uses Doxia's FO module.
 *
 * @author ltheussl
 * @version $Id$
 * @since 1.1
 * @plexus.component role="org.apache.maven.doxia.docrenderer.pdf.PdfRenderer" role-hint="fo"
 */
public class FoPdfRenderer
    extends AbstractPdfRenderer
{
    /**
     * {@inheritDoc}
     * @see org.apache.maven.doxia.module.fo.FoUtils#convertFO2PDF(File, File, String)
     */
    public void generatePdf( File inputFile, File pdfFile )
        throws DocumentRendererException
    {
        // Should take care of the document model for the metadata...
        generatePdf( inputFile, pdfFile, null );
    }

    /** {@inheritDoc} */
    public void render( Map filesToProcess, File outputDirectory, DocumentModel documentModel )
        throws DocumentRendererException, IOException
    {
        // copy resources, images, etc.
        copyResources( outputDirectory );

        if ( documentModel == null )
        {
            getLogger().debug( "No document model, generating all documents individually." );

            renderIndividual( filesToProcess, outputDirectory );
            return;
        }

        String outputName = getOutputName( documentModel );

        File outputFOFile = new File( outputDirectory, outputName + ".fo" );
        if ( !outputFOFile.getParentFile().exists() )
        {
            outputFOFile.getParentFile().mkdirs();
        }

        File pdfOutputFile = new File( outputDirectory, outputName + ".pdf" );
        if ( !pdfOutputFile.getParentFile().exists() )
        {
            pdfOutputFile.getParentFile().mkdirs();
        }

        Writer writer = null;
        try
        {
            writer = WriterFactory.newXmlWriter( outputFOFile );

            FoAggregateSink sink = new FoAggregateSink( writer );

            File fOConfigFile = new File( outputDirectory, "pdf-config.xml" );

            if ( fOConfigFile.exists() )
            {
                sink.load( fOConfigFile );
                getLogger().debug( "Loaded pdf config file: " + fOConfigFile.getAbsolutePath() );
            }

            sink.setDocumentModel( documentModel  );

            sink.beginDocument();

            sink.coverPage();

            sink.toc();

            if ( ( documentModel.getToc() == null ) || ( documentModel.getToc().getItems() == null ) )
            {
                getLogger().info( "No TOC is defined in the document descriptor. Merging all documents." );

                mergeAllSources( filesToProcess, sink );
            }
            else
            {
                getLogger().debug( "Using TOC defined in the document descriptor." );

                mergeSourcesFromTOC( documentModel.getToc(), sink );
            }

            sink.endDocument();
        }
        finally
        {
            IOUtil.close( writer );
        }

        generatePdf( outputFOFile, pdfOutputFile, documentModel );
    }

    /** {@inheritDoc} */
    public void renderIndividual( Map filesToProcess, File outputDirectory )
        throws DocumentRendererException, IOException
    {
        for ( Iterator j = filesToProcess.keySet().iterator(); j.hasNext(); )
        {
            String key = (String) j.next();
            SiteModule module = (SiteModule) filesToProcess.get( key );

            File fullDoc = new File( getBaseDir(), module.getSourceDirectory() + File.separator + key );

            String output = key;
            String lowerCaseExtension = module.getExtension().toLowerCase( Locale.ENGLISH );
            if ( output.toLowerCase( Locale.ENGLISH ).indexOf( "." + lowerCaseExtension ) != -1 )
            {
                output =
                    output.substring( 0, output.toLowerCase( Locale.ENGLISH ).indexOf( "." + lowerCaseExtension ) );
            }

            File outputFOFile = new File( outputDirectory, output + ".fo" );
            if ( !outputFOFile.getParentFile().exists() )
            {
                outputFOFile.getParentFile().mkdirs();
            }

            File pdfOutputFile = new File( outputDirectory, output + ".pdf" );
            if ( !pdfOutputFile.getParentFile().exists() )
            {
                pdfOutputFile.getParentFile().mkdirs();
            }

            FoSink sink =
                (FoSink) new FoSinkFactory().createSink( outputFOFile.getParentFile(), outputFOFile.getName() );
            sink.beginDocument();
            parse( fullDoc.getAbsolutePath(), module.getParserId(), sink );
            sink.endDocument();

            generatePdf( outputFOFile, pdfOutputFile, null );
        }
    }

    private void mergeAllSources( Map filesToProcess, FoAggregateSink sink )
            throws DocumentRendererException, IOException
    {
        for ( Iterator j = filesToProcess.keySet().iterator(); j.hasNext(); )
        {
            String key = (String) j.next();
            SiteModule module = (SiteModule) filesToProcess.get( key );
            sink.setDocumentName( key );
            File fullDoc = new File( getBaseDir(), module.getSourceDirectory() + File.separator + key );

            parse( fullDoc.getAbsolutePath(), module.getParserId(), sink );
        }
    }

    private void mergeSourcesFromTOC( DocumentTOC toc, FoAggregateSink sink )
            throws IOException, DocumentRendererException
    {
        parseTocItems( toc.getItems(), sink );
    }

    private void parseTocItems( List items, FoAggregateSink sink )
            throws IOException, DocumentRendererException
    {
        for ( Iterator k = items.iterator(); k.hasNext(); )
        {
            DocumentTOCItem tocItem = (DocumentTOCItem) k.next();

            if ( tocItem.getRef() == null )
            {
                if ( getLogger().isInfoEnabled() )
                {
                    getLogger().info( "No ref defined for tocItem " + tocItem.getName() );
                }

                continue;
            }

            String href = StringUtils.replace( tocItem.getRef(), "\\", "/" );
            if ( href.lastIndexOf( "." ) != -1 )
            {
                href = href.substring( 0, href.lastIndexOf( "." ) );
            }

            renderModules( href, sink, tocItem );

            if ( tocItem.getItems() != null )
            {
                parseTocItems( tocItem.getItems(), sink );
            }
        }
    }

    private void renderModules( String href, FoAggregateSink sink, DocumentTOCItem tocItem )
            throws DocumentRendererException, IOException
    {
        for ( Iterator i = siteModuleManager.getSiteModules().iterator(); i.hasNext(); )
        {
            SiteModule module = (SiteModule) i.next();
            File moduleBasedir = new File( getBaseDir(), module.getSourceDirectory() );

            if ( moduleBasedir.exists() )
            {
                String doc = href + "." + module.getExtension();
                File source = new File( moduleBasedir, doc );

                if ( source.exists() )
                {
                    sink.setDocumentName( doc );
                    sink.setDocumentTitle( tocItem.getName() );

                    parse( source.getPath(), module.getParserId(), sink );
                }
            }
        }
    }

    /**
     * @param inputFile
     * @param pdfFile
     * @param documentModel could be null
     * @throws DocumentRendererException if any
     * @since 1.1.1
     */
    private void generatePdf( File inputFile, File pdfFile, DocumentModel documentModel )
        throws DocumentRendererException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Generating: " + pdfFile );
        }

        try
        {
            FoUtils.convertFO2PDF( inputFile, pdfFile, null, documentModel );
        }
        catch ( TransformerException e )
        {
            if ( ( e.getCause() != null ) && ( e.getCause() instanceof SAXParseException ) )
            {
                SAXParseException sax = (SAXParseException) e.getCause();

                StringBuffer sb = new StringBuffer();
                sb.append( "Error creating PDF from " ).append( inputFile.getAbsolutePath() ).append( ":" )
                  .append( sax.getLineNumber() ).append( ":" ).append( sax.getColumnNumber() ).append( "\n" );
                sb.append( e.getMessage() );

                throw new DocumentRendererException( sb.toString() );
            }

            throw new DocumentRendererException( "Error creating PDF from " + inputFile + ": " + e.getMessage() );
        }
    }
}
