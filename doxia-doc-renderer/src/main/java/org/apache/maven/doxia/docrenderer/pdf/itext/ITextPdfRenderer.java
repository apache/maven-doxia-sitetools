package org.apache.maven.doxia.docrenderer.pdf.itext;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.doxia.docrenderer.DocumentRendererException;
import org.apache.maven.doxia.docrenderer.pdf.AbstractPdfRenderer;
import org.apache.maven.doxia.document.DocumentModel;
import org.apache.maven.doxia.module.itext.ITextSink;
import org.apache.maven.doxia.module.itext.ITextSinkFactory;
import org.apache.maven.doxia.module.itext.ITextUtil;
import org.apache.maven.doxia.module.site.SiteModule;
import org.apache.xml.utils.DefaultErrorHandler;
import org.codehaus.plexus.util.WriterFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.lowagie.text.ElementTags;

/**
 * Abstract <code>document</code> render with the <code>iText</code> framework
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @author ltheussl
 * @version $Id$
 * @since 1.1
 * @plexus.component role="org.apache.maven.doxia.docrenderer.pdf.PdfRenderer" role-hint="itext"
 */
public class ITextPdfRenderer
    extends AbstractPdfRenderer
{
    /** The xslt style sheet used to transform a Document to an iText file. */
    private static final String XSLT_RESOURCE = "org/apache/maven/doxia/docrenderer/itext/xslt/TOC.xslt";

    /** The TransformerFactory. */
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    /** The DocumentBuilderFactory. */
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    static
    {
        TRANSFORMER_FACTORY.setErrorListener( new DefaultErrorHandler() );
    }

    /** {@inheritDoc} */
    public void generatePdf( File inputFile, File pdfFile )
        throws DocumentRendererException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Generating : " + pdfFile );
        }

        try
        {
            ITextUtil.writePdf( new FileInputStream( inputFile ), new FileOutputStream( pdfFile ) );
        }
        catch ( IOException e )
        {
            throw new DocumentRendererException( "Cannot create PDF from " + inputFile + ": " + e.getMessage(), e );
        }
        catch ( RuntimeException e )
        {
            throw new DocumentRendererException( "Error creating PDF from " + inputFile + ": " + e.getMessage(), e );
        }
    }

    /** {@inheritDoc} */
    public void render( Map filesToProcess, File outputDirectory, DocumentModel documentModel    )
        throws DocumentRendererException, IOException
    {
        String outputName = documentModel.getOutputName();

        if ( outputName == null )
        {
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "No outputName is defined in the document descriptor. Using 'target.pdf'" );
            }

            documentModel.setOutputName( "target" );
        }
        else if ( outputName.lastIndexOf( "." ) != -1 )
        {
            documentModel.setOutputName( outputName.substring( 0, outputName.lastIndexOf( "." ) ) );
        }

// TODO: adjust from o.a.m.d.docrenderer.itext.AbstractITextRender
//        if ( ( documentModel.getToc() == null ) || ( documentModel.getToc().getItems() == null ) )
//        {
//            getLogger().info( "No TOC is defined in the document descriptor. Generating all documents." );

            for ( Iterator j = filesToProcess.keySet().iterator(); j.hasNext(); )
            {
                String key = (String) j.next();

                SiteModule module = (SiteModule) filesToProcess.get( key );


                String fullDocPath = getBaseDir() + File.separator
                            + module.getSourceDirectory() + File.separator + key;

                String iTextFileName = key.substring( 0, key.indexOf( "." ) + 1 ) + "xml";

                File iTextFile = new File( outputDirectory, iTextFileName );
                if ( !iTextFile.getParentFile().exists() )
                {
                    iTextFile.getParentFile().mkdirs();
                }

                String pdfFileName = key.substring( 0, key.indexOf( "." ) + 1 ) + getOutputExtension();

                File pdfFile = new File( outputDirectory, pdfFileName );
                if ( !pdfFile.getParentFile().exists() )
                {
                    pdfFile.getParentFile().mkdirs();
                }

                parse( fullDocPath, module, iTextFile );

                generatePdf( iTextFile, pdfFile );
            }
/*
        }
        else
        {
            // TODO: adjust from o.a.m.d.docrenderer.itext.AbstractITextRender
        }
*/

    }


      //--------------------------------------------
     //
    //--------------------------------------------


    /**
     * Parse a source document and emit results into a sink.
     *
     * @param fullDocPath absolute path to the source document.
     * @param module the site module associated with the source document (determines the parser to use).
     * @param iTextFile the resulting iText xml file.
     * @throws DocumentRendererException in case of a parsing problem.
     * @throws IOException if the source and/or target document cannot be opened.
     */
    private void parse( String fullDocPath, SiteModule module, File iTextFile )
        throws DocumentRendererException, IOException
    {
        System.setProperty( "itext.basedir", iTextFile.getParentFile().getAbsolutePath() );

        try
        {
            Writer writer = WriterFactory.newXmlWriter( iTextFile );
            ITextSink sink = (ITextSink) new ITextSinkFactory().createSink( writer );

            sink.setClassLoader( new URLClassLoader( new URL[] { iTextFile.getParentFile().toURI().toURL() } ) );

            parse( fullDocPath, module.getParserId(), sink );

            sink.close();
        }
        finally
        {
            System.getProperties().remove( "itext.basedir" );
        }
    }

    /**
     * Merge all iTextFiles to a single one.
     *
     * @param iTextFiles list of iText xml files.
     * @return Document.
     * @throws DocumentRendererException if any.
     * @throws IOException if any.
     */
    private Document generateDocument( List iTextFiles )
        throws DocumentRendererException, IOException
    {
        Document document;

        try
        {
            document = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().newDocument();
        }
        catch ( ParserConfigurationException e )
        {
            throw new DocumentRendererException( "Error building document :" + e.getMessage() );
        }

        document.appendChild( document.createElement( ElementTags.ITEXT ) ); // Used only to set a root

        for ( int i = 0; i < iTextFiles.size(); i++ )
        {
            File iTextFile = (File) iTextFiles.get( i );

            Document iTextDocument;

            try
            {
                iTextDocument = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().parse( iTextFile );
            }
            catch ( SAXException e )
            {
                throw new DocumentRendererException( "SAX Error : " + e.getMessage() );
            }
            catch ( ParserConfigurationException e )
            {
                throw new DocumentRendererException( "Error parsing configuration : " + e.getMessage() );
            }

            // Only one chapter per doc
            Node chapter = iTextDocument.getElementsByTagName( ElementTags.CHAPTER ).item( 0 );

            try
            {
                document.getDocumentElement().appendChild( document.importNode( chapter, true ) );
            }
            catch ( DOMException e )
            {
                throw new DocumentRendererException( "Error appending chapter for "
                        + iTextFile + " : " + e.getMessage() );
            }
        }

        return document;
    }

    /**
     * Initialize the transformer object.
     *
     * @return an instance of a transformer object.
     * @throws DocumentRendererException if any.
     */
    private Transformer initTransformer()
        throws DocumentRendererException
    {
        try
        {
            Transformer transformer = TRANSFORMER_FACTORY.newTransformer( new StreamSource( AbstractPdfRenderer.class
                .getResourceAsStream( "/" + XSLT_RESOURCE ) ) );

            transformer.setErrorListener( TRANSFORMER_FACTORY.getErrorListener() );

            transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "false" );

            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

            transformer.setOutputProperty( OutputKeys.METHOD, "xml" );

            transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );

            return transformer;
        }
        catch ( TransformerConfigurationException e )
        {
            throw new DocumentRendererException( "Error configuring Transformer for " + XSLT_RESOURCE + ": "
                + e.getMessage() );
        }
        catch ( IllegalArgumentException e )
        {
            throw new DocumentRendererException( "Error configuring Transformer for " + XSLT_RESOURCE + ": "
                + e.getMessage() );
        }
    }

    /**
     * Add transformer parameters from a DocumentModel.
     *
     * @param transformer the Transformer to set the parameters.
     * @param documentModel the DocumentModel to take the parameters from.
     */
    private void addTransformerParameters( Transformer transformer, DocumentModel documentModel )
    {
        if ( documentModel.getMeta().getTitle() != null )
        {
            transformer.setParameter( "title", documentModel.getMeta().getTitle() );
        }

        if ( documentModel.getMeta().getAuthor() != null )
        {
            transformer.setParameter( "author", documentModel.getMeta().getAuthor() );
        }

        transformer.setParameter( "creationdate", new Date().toString() );

        if ( documentModel.getMeta().getSubject() != null )
        {
            transformer.setParameter( "subject", documentModel.getMeta().getSubject() );
        }

        if ( documentModel.getMeta().getKeywords() != null )
        {
            transformer.setParameter( "keywords", documentModel.getMeta().getKeywords() );
        }

        transformer.setParameter( "producer", "Generated with Doxia by " + System.getProperty( "user.name" ) );

        if ( ITextUtil.isPageSizeSupported( documentModel.getMeta().getTitle() ) )
        {
            transformer.setParameter( "pagesize", documentModel.getMeta().getPageSize() );
        }
        else
        {
            transformer.setParameter( "pagesize", "A4" );
        }

        transformer.setParameter( "frontPageHeader", "" );

        if ( documentModel.getMeta().getTitle() != null )
        {
            transformer.setParameter( "frontPageTitle", documentModel.getMeta().getTitle() );
        }

        transformer.setParameter( "frontPageFooter", "Generated date " + new Date().toString() );
    }

    /**
     * Transform a document to an iTextFile.
     *
     * @param documentModel the DocumentModel to take the parameters from.
     * @param document the Document to transform.
     * @param iTextFile the resulting iText xml file.
     * @throws DocumentRendererException in case of a transformation error.
     */
    private void transform( DocumentModel documentModel, Document document, File iTextFile )
        throws DocumentRendererException
    {
        Transformer transformer = initTransformer();

        addTransformerParameters( transformer, documentModel );

        try
        {
            transformer.transform( new DOMSource( document ), new StreamResult( iTextFile ) );
        }
        catch ( TransformerException e )
        {
            throw new DocumentRendererException( "Error transforming Document " + document + ": " + e.getMessage() );
        }
    }
}
