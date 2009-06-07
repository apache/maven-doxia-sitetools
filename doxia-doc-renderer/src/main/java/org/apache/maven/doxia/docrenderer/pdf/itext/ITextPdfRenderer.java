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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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
import org.apache.maven.doxia.document.DocumentTOCItem;
import org.apache.maven.doxia.module.itext.ITextSink;
import org.apache.maven.doxia.module.itext.ITextSinkFactory;
import org.apache.maven.doxia.module.itext.ITextUtil;
import org.apache.maven.doxia.module.site.SiteModule;
import org.apache.xml.utils.DefaultErrorHandler;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
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

        File outputITextFile = new File( outputDirectory, outputName + ".xml" );
        if ( !outputITextFile.getParentFile().exists() )
        {
            outputITextFile.getParentFile().mkdirs();
        }

        File pdfOutputFile = new File( outputDirectory, outputName + ".pdf" );
        if ( !pdfOutputFile.getParentFile().exists() )
        {
            pdfOutputFile.getParentFile().mkdirs();
        }

        List iTextFiles;
        if ( ( documentModel.getToc() == null ) || ( documentModel.getToc().getItems() == null ) )
        {
            getLogger().info( "No TOC is defined in the document descriptor. Merging all documents." );

            iTextFiles = parseAllFiles( filesToProcess, outputDirectory );
        }
        else
        {
            getLogger().debug( "Using TOC defined in the document descriptor." );

            iTextFiles = parseTOCFiles( filesToProcess, outputDirectory, documentModel );
        }

        File iTextFile = new File( outputDirectory, outputName + ".xml" );
        File iTextOutput = new File( outputDirectory, outputName + "." + getOutputExtension() );
        Document document = generateDocument( iTextFiles );
        transform( documentModel, document, iTextFile );
        generatePdf( iTextFile, iTextOutput );
    }

    /** {@inheritDoc} */
    public void renderIndividual( Map filesToProcess, File outputDirectory )
        throws DocumentRendererException, IOException
    {
        for ( Iterator it = filesToProcess.keySet().iterator(); it.hasNext(); )
        {
            String key = (String) it.next();
            SiteModule module = (SiteModule) filesToProcess.get( key );
            File fullDoc = new File( getBaseDir(), module.getSourceDirectory() + File.separator + key );

            String output = key;
            String lowerCaseExtension = module.getExtension().toLowerCase( Locale.ENGLISH );
            if ( output.toLowerCase( Locale.ENGLISH ).indexOf( "." + lowerCaseExtension ) != -1 )
            {
                output =
                    output.substring( 0, output.toLowerCase( Locale.ENGLISH ).indexOf( "." + lowerCaseExtension ) );
            }

            File outputITextFile = new File( outputDirectory, output + ".xml" );
            if ( !outputITextFile.getParentFile().exists() )
            {
                outputITextFile.getParentFile().mkdirs();
            }

            File pdfOutputFile = new File( outputDirectory, output + ".pdf" );
            if ( !pdfOutputFile.getParentFile().exists() )
            {
                pdfOutputFile.getParentFile().mkdirs();
            }

            parse( fullDoc, module, outputITextFile );

            generatePdf( outputITextFile, pdfOutputFile );
        }
    }

      //--------------------------------------------
     //
    //--------------------------------------------


    /**
     * Parse a source document and emit results into a sink.
     *
     * @param fullDocPath file to the source document.
     * @param module the site module associated with the source document (determines the parser to use).
     * @param iTextFile the resulting iText xml file.
     * @throws DocumentRendererException in case of a parsing problem.
     * @throws IOException if the source and/or target document cannot be opened.
     */
    private void parse( File fullDoc, SiteModule module, File iTextFile )
        throws DocumentRendererException, IOException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Parsing file " + fullDoc.getAbsolutePath() );
        }

        System.setProperty( "itext.basedir", iTextFile.getParentFile().getAbsolutePath() );

        Writer writer = null;
        ITextSink sink = null;
        try
        {
            writer = WriterFactory.newXmlWriter( iTextFile );
            sink = (ITextSink) new ITextSinkFactory().createSink( writer );

            sink.setClassLoader( new URLClassLoader( new URL[] { iTextFile.getParentFile().toURI().toURL() } ) );

            parse( fullDoc.getAbsolutePath(), module.getParserId(), sink );
        }
        finally
        {
            if ( sink != null )
            {
                sink.flush();
                sink.close();
            }
            IOUtil.close( writer );
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
     * @param documentModel the DocumentModel to take the parameters from, could be null.
     */
    private void addTransformerParameters( Transformer transformer, DocumentModel documentModel )
    {
        if ( documentModel == null )
        {
            return;
        }

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
     * @param documentModel the DocumentModel to take the parameters from, could be null.
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

    /**
     * @param filesToProcess not null
     * @param outputDirectory not null
     * @return a list of all parsed files.
     * @throws DocumentRendererException if any
     * @throws IOException if any
     * @since 1.1.1
     */
    private List parseAllFiles( Map filesToProcess, File outputDirectory )
        throws DocumentRendererException, IOException
    {
        List iTextFiles = new LinkedList();
        for ( Iterator it = filesToProcess.keySet().iterator(); it.hasNext(); )
        {
            String key = (String) it.next();
            SiteModule module = (SiteModule) filesToProcess.get( key );
            File fullDoc = new File( getBaseDir(), module.getSourceDirectory() + File.separator + key );

            String outputITextName = key.substring( 0, key.lastIndexOf( "." ) + 1 ) + "xml";
            File outputITextFileTmp = new File( outputDirectory, outputITextName );
            if ( !outputITextFileTmp.getParentFile().exists() )
            {
                outputITextFileTmp.getParentFile().mkdirs();
            }

            iTextFiles.add( outputITextFileTmp );
            parse( fullDoc, module, outputITextFileTmp );
        }

        return iTextFiles;
    }

    /**
     * @param filesToProcess not null
     * @param outputDirectory not null
     * @return a list of all parsed files.
     * @throws DocumentRendererException if any
     * @throws IOException if any
     * @since 1.1.1
     */
    private List parseTOCFiles( Map filesToProcess, File outputDirectory, DocumentModel documentModel )
        throws DocumentRendererException, IOException
    {
        List iTextFiles = new LinkedList();
        for ( Iterator it = documentModel.getToc().getItems().iterator(); it.hasNext(); )
        {
            DocumentTOCItem tocItem = (DocumentTOCItem) it.next();

            if ( tocItem.getRef() == null )
            {
                getLogger().debug(
                                   "No ref defined for the tocItem '" + tocItem.getName()
                                       + "' in the document descriptor. IGNORING" );
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
                        String outputITextName = doc.substring( 0, doc.lastIndexOf( "." ) + 1 ) + "xml";
                        File outputITextFileTmp = new File( outputDirectory, outputITextName );
                        if ( !outputITextFileTmp.getParentFile().exists() )
                        {
                            outputITextFileTmp.getParentFile().mkdirs();
                        }

                        iTextFiles.add( outputITextFileTmp );
                        parse( source, module, outputITextFileTmp );
                    }
                }
            }
        }

        return iTextFiles;
    }
}
