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
import java.io.FileWriter;
import java.io.IOException;

import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.maven.doxia.docrenderer.DocumentRendererException;
import org.apache.maven.doxia.document.DocumentModel;
import org.apache.maven.doxia.document.DocumentTOCItem;
import org.apache.maven.doxia.docrenderer.pdf.AbstractPdfRenderer;
import org.apache.maven.doxia.module.site.SiteModule;
import org.apache.maven.doxia.module.fo.FoAggregateSink;
import org.apache.maven.doxia.module.fo.FoUtils;

import org.codehaus.plexus.util.StringUtils;

/**
 * PDF renderer that uses Doxia's FO module.
 *
 * @author ltheussl
 * @version $Id$
 */
public class FoPdfRenderer
    extends AbstractPdfRenderer
{
    /**
     * Converts a FO file to a PDF file using FOP.
     *
     * @param foFile the FO file.
     * @param pdfFile the target PDF file.
     * @throws DocumentRendererException In case of a conversion problem.
     * @see org.apache.maven.doxia.module.fo.FoUtils#convertFO2PDF(File,File,String);
     */
    public void generatePdf( File foFile, File pdfFile )
        throws DocumentRendererException
    {
        getLogger().debug( "Generating: " + pdfFile );

        try
        {
            FoUtils.convertFO2PDF( foFile, pdfFile, null );
        }
        catch ( TransformerException e )
        {
            throw new DocumentRendererException( "Error creating PDF from " + foFile + ": " + e.getMessage() );
        }
    }


    /** {@inheritDoc} */
    public void render( Map files, File outputDirectory, DocumentModel model )
        throws DocumentRendererException, IOException
    {
        String outputName = model.getOutputName();

        if ( outputName == null )
        {
            getLogger().info( "No outputName is defined in the document descriptor. Using 'target.pdf'" );

            model.setOutputName( "target" );
        }
        else if ( outputName.lastIndexOf( "." ) != -1 )
        {
            model.setOutputName( outputName.substring( 0, outputName.lastIndexOf( "." ) ) );
        }

        outputName = model.getOutputName();

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

        FoAggregateSink sink = new FoAggregateSink( new FileWriter( outputFOFile ) );

        sink.setDocumentModel( model );

        sink.beginDocument();

        sink.coverPage();

        sink.toc();

        if ( ( model.getToc() == null ) || ( model.getToc().getItems() == null ) )
        {
            getLogger().info( "No TOC is defined in the document descriptor. Merging all documents." );

            for ( Iterator j = files.keySet().iterator(); j.hasNext(); )
            {
                String key = (String) j.next();

                SiteModule module = (SiteModule) files.get( key );

                sink.setDocumentName( key );
                // TODO: sink.setDocumentTitle( "Title" ); ???

                String fullDocPath = getBaseDir() + File.separator
                            + module.getSourceDirectory() + File.separator + key;

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Parsing file " + fullDocPath );
                }

                parse( fullDocPath, module.getParserId(), sink );
            }
        }
        else
        {
            for ( Iterator k = model.getToc().getItems().iterator(); k.hasNext(); )
            {
                DocumentTOCItem tocItem = (DocumentTOCItem) k.next();

                if ( tocItem.getRef() == null )
                {
                    getLogger().info( "No ref defined for tocItem " + tocItem.getName() );

                    continue;
                }

                String href = StringUtils.replace( tocItem.getRef(), "\\", "/" );

                if ( href.lastIndexOf( "." ) != -1 )
                {
                    href = href.substring( 0, href.lastIndexOf( "." ) );
                }

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
                            if ( getLogger().isDebugEnabled() )
                            {
                                getLogger().debug( "Parsing file " + source );
                            }

                            sink.setDocumentName( doc );

                            sink.setDocumentTitle( tocItem.getName() );

                            parse( source.getPath(), module.getParserId(), sink );
                        }
                    }
                }
            }

        }

        sink.endDocument();

        generatePdf( outputFOFile, pdfOutputFile );
    }

}
