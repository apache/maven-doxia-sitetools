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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.Restriction;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.doxia.Doxia;
import org.apache.maven.doxia.logging.PlexusLoggerWrapper;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.parser.manager.ParserNotFoundException;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.site.decoration.PublishDate;
import org.apache.maven.doxia.site.skin.SkinModel;
import org.apache.maven.doxia.site.skin.io.xpp3.SkinXpp3Reader;
import org.apache.maven.doxia.parser.module.ParserModule;
import org.apache.maven.doxia.parser.module.ParserModuleManager;
import org.apache.maven.doxia.parser.module.ParserModuleNotFoundException;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.doxia.util.XmlValidator;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.config.ConfigurationUtils;
import org.apache.velocity.tools.config.EasyFactoryConfiguration;
import org.apache.velocity.tools.config.FactoryConfiguration;
import org.apache.velocity.tools.generic.AlternatorTool;
import org.apache.velocity.tools.generic.ClassTool;
import org.apache.velocity.tools.generic.ComparisonDateTool;
import org.apache.velocity.tools.generic.ContextTool;
import org.apache.velocity.tools.generic.ConversionTool;
import org.apache.velocity.tools.generic.DisplayTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.FieldTool;
import org.apache.velocity.tools.generic.LinkTool;
import org.apache.velocity.tools.generic.LoopTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.RenderTool;
import org.apache.velocity.tools.generic.ResourceTool;
import org.apache.velocity.tools.generic.SortTool;
import org.apache.velocity.tools.generic.XmlTool;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.i18n.I18N;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.PathTool;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.velocity.VelocityComponent;

/**
 * <p>DefaultSiteRenderer class.</p>
 *
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @since 1.0
 */
@Component( role = Renderer.class )
public class DefaultSiteRenderer
    extends AbstractLogEnabled
    implements Renderer
{
    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    @Requirement
    private VelocityComponent velocity;

    @Requirement
    private ParserModuleManager parserModuleManager;

    @Requirement
    private Doxia doxia;

    @Requirement
    private I18N i18n;

    @Requirement
    private PlexusContainer plexus;

    private static final String RESOURCE_DIR = "org/apache/maven/doxia/siterenderer/resources";

    private static final String DEFAULT_TEMPLATE = RESOURCE_DIR + "/default-site.vm";

    private static final String SKIN_TEMPLATE_LOCATION = "META-INF/maven/site.vm";

    private static final String TOOLS_LOCATION = "META-INF/maven/site-tools.xml";

    // ----------------------------------------------------------------------
    // Renderer implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public Map<String, DocumentRenderer> locateDocumentFiles( SiteRenderingContext siteRenderingContext )
            throws IOException, RendererException
    {
        return locateDocumentFiles( siteRenderingContext, false );
    }

    /** {@inheritDoc} */
    public Map<String, DocumentRenderer> locateDocumentFiles( SiteRenderingContext siteRenderingContext,
                                                              boolean editable )
        throws IOException, RendererException
    {
        Map<String, DocumentRenderer> files = new LinkedHashMap<String, DocumentRenderer>();
        Map<String, String> moduleExcludes = siteRenderingContext.getModuleExcludes();

        // look in every site directory (in general src/site or target/generated-site)
        for ( File siteDirectory : siteRenderingContext.getSiteDirectories() )
        {
            if ( siteDirectory.exists() )
            {
                Collection<ParserModule> modules = parserModuleManager.getParserModules();
                // use every Doxia parser module
                for ( ParserModule module : modules )
                {
                    File moduleBasedir = new File( siteDirectory, module.getSourceDirectory() );

                    String excludes = ( moduleExcludes == null ) ? null : moduleExcludes.get( module.getParserId() );

                    addModuleFiles( siteRenderingContext.getRootDirectory(), moduleBasedir, module, excludes, files,
                                    editable );
                }
            }
        }

        // look in specific modules directories (used for old Maven 1.x site layout: xdoc and fml docs in /xdocs)
        for ( ExtraDoxiaModuleReference module : siteRenderingContext.getModules() )
        {
            try
            {
                ParserModule parserModule = parserModuleManager.getParserModule( module.getParserId() );

                String excludes = ( moduleExcludes == null ) ? null : moduleExcludes.get( module.getParserId() );

                addModuleFiles( siteRenderingContext.getRootDirectory(), module.getBasedir(), parserModule, excludes,
                                files, editable );
            }
            catch ( ParserModuleNotFoundException e )
            {
                throw new RendererException( "Unable to find module: " + e.getMessage(), e );
            }
        }
        return files;
    }

    private List<String> filterExtensionIgnoreCase( List<String> fileNames, String extension )
    {
        List<String> filtered = new LinkedList<String>( fileNames );
        for ( Iterator<String> it = filtered.iterator(); it.hasNext(); )
        {
            String name = it.next();

            // Take care of extension case
            if ( !endsWithIgnoreCase( name, extension ) )
            {
                it.remove();
            }
        }
        return filtered;
    }

    private void addModuleFiles( File rootDir, File moduleBasedir, ParserModule module, String excludes,
                                 Map<String, DocumentRenderer> files, boolean editable )
            throws IOException, RendererException
    {
        if ( !moduleBasedir.exists() || ArrayUtils.isEmpty( module.getExtensions() ) )
        {
            return;
        }

        String moduleRelativePath =
            PathTool.getRelativeFilePath( rootDir.getAbsolutePath(), moduleBasedir.getAbsolutePath() );

        List<String> allFiles = FileUtils.getFileNames( moduleBasedir, "**/*.*", excludes, false );

        for ( String extension : module.getExtensions() )
        {
            String fullExtension = "." + extension;

            List<String> docs = filterExtensionIgnoreCase( allFiles, fullExtension );

            // *.<extension>.vm
            List<String> velocityFiles = filterExtensionIgnoreCase( allFiles, fullExtension + ".vm" );

            docs.addAll( velocityFiles );

            for ( String doc : docs )
            {
                RenderingContext context = new RenderingContext( moduleBasedir, moduleRelativePath, doc,
                                                                 module.getParserId(), extension, editable );

                // TODO: DOXIA-111: we need a general filter here that knows how to alter the context
                if ( endsWithIgnoreCase( doc, ".vm" ) )
                {
                    context.setAttribute( "velocity", "true" );
                }

                String key = context.getOutputName();
                key = StringUtils.replace( key, "\\", "/" );

                if ( files.containsKey( key ) )
                {
                    DocumentRenderer renderer = files.get( key );

                    RenderingContext originalContext = renderer.getRenderingContext();

                    File originalDoc = new File( originalContext.getBasedir(), originalContext.getInputName() );

                    throw new RendererException( "File '" + module.getSourceDirectory() + File.separator + doc
                        + "' clashes with existing '" + originalDoc + "'." );
                }
                // -----------------------------------------------------------------------
                // Handle key without case differences
                // -----------------------------------------------------------------------
                for ( Map.Entry<String, DocumentRenderer> entry : files.entrySet() )
                {
                    if ( entry.getKey().equalsIgnoreCase( key ) )
                    {
                        RenderingContext originalContext = entry.getValue().getRenderingContext();

                        File originalDoc = new File( originalContext.getBasedir(), originalContext.getInputName() );

                        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
                        {
                            throw new RendererException( "File '" + module.getSourceDirectory() + File.separator
                                + doc + "' clashes with existing '" + originalDoc + "'." );
                        }

                        if ( getLogger().isWarnEnabled() )
                        {
                            getLogger().warn( "File '" + module.getSourceDirectory() + File.separator + doc
                                + "' could clash with existing '" + originalDoc + "'." );
                        }
                    }
                }

                files.put( key, new DoxiaDocumentRenderer( context ) );
            }
        }
    }

    /** {@inheritDoc} */
    public void render( Collection<DocumentRenderer> documents, SiteRenderingContext siteRenderingContext,
                        File outputDirectory )
        throws RendererException, IOException
    {
        for ( DocumentRenderer docRenderer : documents )
        {
            RenderingContext renderingContext = docRenderer.getRenderingContext();

            File outputFile = new File( outputDirectory, docRenderer.getOutputName() );

            File inputFile = new File( renderingContext.getBasedir(), renderingContext.getInputName() );

            boolean modified = !outputFile.exists() || ( inputFile.lastModified() > outputFile.lastModified() )
                || ( siteRenderingContext.getDecoration().getLastModified() > outputFile.lastModified() );

            if ( modified || docRenderer.isOverwrite() )
            {
                if ( !outputFile.getParentFile().exists() )
                {
                    outputFile.getParentFile().mkdirs();
                }

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Generating " + outputFile );
                }

                Writer writer = null;
                try
                {
                    if ( !docRenderer.isExternalReport() )
                    {
                        writer = WriterFactory.newWriter( outputFile, siteRenderingContext.getOutputEncoding() );
                    }
                    docRenderer.renderDocument( writer, this, siteRenderingContext );
                }
                finally
                {
                    IOUtil.close( writer );
                }
            }
            else
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( inputFile + " unchanged, not regenerating..." );
                }
            }
        }
    }

    /** {@inheritDoc} */
    public void renderDocument( Writer writer, RenderingContext docRenderingContext, SiteRenderingContext siteContext )
            throws RendererException, FileNotFoundException, UnsupportedEncodingException
    {
        SiteRendererSink sink = new SiteRendererSink( docRenderingContext );

        File doc = new File( docRenderingContext.getBasedir(), docRenderingContext.getInputName() );

        Reader reader = null;
        try
        {
            String resource = doc.getAbsolutePath();

            Parser parser = doxia.getParser( docRenderingContext.getParserId() );
            // DOXIASITETOOLS-146 don't render comments from source markup
            parser.setEmitComments( false );

            // TODO: DOXIA-111: the filter used here must be checked generally.
            if ( docRenderingContext.getAttribute( "velocity" ) != null )
            {
                getLogger().debug( "Processing Velocity for " + docRenderingContext.getDoxiaSourcePath() );
                try
                {
                    Context vc = createDocumentVelocityContext( docRenderingContext, siteContext );

                    StringWriter sw = new StringWriter();

                    velocity.getEngine().mergeTemplate( resource, siteContext.getInputEncoding(), vc, sw );

                    String doxiaContent = sw.toString();

                    if ( siteContext.getProcessedContentOutput() != null )
                    {
                        // save Velocity processing result, ie the Doxia content that will be parsed after
                        saveVelocityProcessedContent( docRenderingContext, siteContext, doxiaContent );
                    }

                    reader = new StringReader( doxiaContent );
                }
                catch ( VelocityException e )
                {
                    throw new RendererException( "Error parsing " + docRenderingContext.getDoxiaSourcePath()
                        + " as a Velocity template: " + e.getMessage(), e );
                }

                if ( parser.getType() == Parser.XML_TYPE && siteContext.isValidate() )
                {
                    reader = validate( reader, resource );
                }
            }
            else
            {
                switch ( parser.getType() )
                {
                    case Parser.XML_TYPE:
                        reader = ReaderFactory.newXmlReader( doc );
                        if ( siteContext.isValidate() )
                        {
                            reader = validate( reader, resource );
                        }
                        break;

                    case Parser.TXT_TYPE:
                    case Parser.UNKNOWN_TYPE:
                    default:
                        reader = ReaderFactory.newReader( doc, siteContext.getInputEncoding() );
                }
            }
            sink.enableLogging( new PlexusLoggerWrapper( getLogger() ) );

            doxia.parse( reader, docRenderingContext.getParserId(), sink );
        }
        catch ( ParserNotFoundException e )
        {
            throw new RendererException( "Error getting a parser for '" + doc + "': " + e.getMessage(), e );
        }
        catch ( ParseException e )
        {
            StringBuilder errorMsgBuilder = new StringBuilder();
            errorMsgBuilder.append( "Error parsing '" ).append( doc ).append( "': " );
            if ( e.getLineNumber() > 0 )
            {
                errorMsgBuilder.append( "line [" ).append( e.getLineNumber() ).append( "] " );
            }
            errorMsgBuilder.append( e.getMessage() );
            throw new RendererException( errorMsgBuilder.toString(), e );
        }
        catch ( IOException e )
        {
            throw new RendererException( "IOException when processing '" + doc + "'", e );
        }
        finally
        {
            sink.flush();

            sink.close();

            IOUtil.close( reader );
        }

        mergeDocumentIntoSite( writer, (DocumentContent) sink, siteContext );
    }

    private void saveVelocityProcessedContent( RenderingContext docRenderingContext, SiteRenderingContext siteContext,
                                               String doxiaContent )
        throws IOException
    {
        if ( !siteContext.getProcessedContentOutput().exists() )
        {
            siteContext.getProcessedContentOutput().mkdirs();
        }

        String input = docRenderingContext.getInputName();
        File outputFile = new File( siteContext.getProcessedContentOutput(),
                                    input.substring( 0, input.length() - 3 ) );

        File outputParent = outputFile.getParentFile();
        if ( !outputParent.exists() )
        {
            outputParent.mkdirs();
        }

        FileUtils.fileWrite( outputFile, siteContext.getInputEncoding(), doxiaContent );
    }

    /**
     * Creates a Velocity Context with all generic tools configured wit the site rendering context.
     *
     * @param siteRenderingContext the site rendering context
     * @return a Velocity tools managed context
     */
    protected Context createToolManagedVelocityContext( SiteRenderingContext siteRenderingContext )
    {
        Locale locale = siteRenderingContext.getLocale();
        String dateFormat = siteRenderingContext.getDecoration().getPublishDate().getFormat();

        EasyFactoryConfiguration config = new EasyFactoryConfiguration( false );
        config.property( "safeMode", Boolean.FALSE );
        config.toolbox( Scope.REQUEST )
            .tool( ContextTool.class )
            .tool( LinkTool.class )
            .tool( LoopTool.class )
            .tool( RenderTool.class );
        config.toolbox( Scope.APPLICATION ).property( "locale", locale )
            .tool( AlternatorTool.class )
            .tool( ClassTool.class )
            .tool( ComparisonDateTool.class ).property( "format", dateFormat )
            .tool( ConversionTool.class ).property( "dateFormat", dateFormat )
            .tool( DisplayTool.class )
            .tool( EscapeTool.class )
            .tool( FieldTool.class )
            .tool( MathTool.class )
            .tool( NumberTool.class )
            .tool( ResourceTool.class ).property( "bundles", new String[] { "site-renderer" } )
            .tool( SortTool.class )
            .tool( XmlTool.class );

        FactoryConfiguration customConfig = ConfigurationUtils.findInClasspath( TOOLS_LOCATION );

        if ( customConfig != null )
        {
            config.addConfiguration( customConfig );
        }

        ToolManager manager = new ToolManager( false, false );
        manager.configure( config );

        return manager.createContext();
    }

    /**
     * Create a Velocity Context for a Doxia document, containing every information about rendered document.
     *
     * @param renderingContext the document's RenderingContext
     * @param siteRenderingContext the site rendering context
     * @return a Velocity tools managed context
     */
    protected Context createDocumentVelocityContext( RenderingContext renderingContext,
                                                     SiteRenderingContext siteRenderingContext )
    {
        Context context = createToolManagedVelocityContext( siteRenderingContext );
        // ----------------------------------------------------------------------
        // Data objects
        // ----------------------------------------------------------------------

        context.put( "relativePath", renderingContext.getRelativePath() );

        String currentFileName = renderingContext.getOutputName().replace( '\\', '/' );
        context.put( "currentFileName", currentFileName );

        context.put( "alignedFileName", PathTool.calculateLink( currentFileName, renderingContext.getRelativePath() ) );

        context.put( "decoration", siteRenderingContext.getDecoration() );

        Locale locale = siteRenderingContext.getLocale();
        context.put( "locale", locale );
        context.put( "supportedLocales", Collections.unmodifiableList( siteRenderingContext.getSiteLocales() ) );

        context.put( "currentDate", new Date() );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd" );
        context.put( "dateRevision", sdf.format( new Date() ) );

        context.put( "publishDate", siteRenderingContext.getPublishDate() );

        PublishDate publishDate = siteRenderingContext.getDecoration().getPublishDate();
        DateFormat dateFormat = new SimpleDateFormat( publishDate.getFormat(), locale );
        context.put( "dateFormat", dateFormat );

        // doxiaSiteRendererVersion
        InputStream inputStream = this.getClass().getResourceAsStream( "/META-INF/"
            + "maven/org.apache.maven.doxia/doxia-site-renderer/pom.properties" );
        if ( inputStream == null )
        {
            getLogger().debug( "pom.properties for doxia-site-renderer could not be found." );
        }
        else
        {
            Properties properties = new Properties();
            try ( InputStream in = inputStream )
            {
                properties.load( in );
                context.put( "doxiaSiteRendererVersion", properties.getProperty( "version" ) );
            }
            catch ( IOException e )
            {
                getLogger().debug( "Failed to load pom.properties, so doxiaVersion is not available"
                        + " in the Velocity context." );
            }
        }

        // Add user properties
        Map<String, ?> templateProperties = siteRenderingContext.getTemplateProperties();

        if ( templateProperties != null )
        {
            for ( Map.Entry<String, ?> entry : templateProperties.entrySet() )
            {
                context.put( entry.getKey(), entry.getValue() );
            }
        }

        // ----------------------------------------------------------------------
        // Tools
        // ----------------------------------------------------------------------

        context.put( "PathTool", new PathTool() );

        context.put( "FileUtils", new FileUtils() );

        context.put( "StringUtils", new StringUtils() );

        context.put( "i18n", i18n );

        context.put( "plexus", plexus );
        return context;
    }

    /**
     * Create a Velocity Context for the site template decorating the document. In addition to all the informations
     * from the document, this context contains data gathered in {@link SiteRendererSink} during document rendering.
     *
     * @param content the document content to be merged into the template
     * @param siteRenderingContext the site rendering context
     * @return a Velocity tools managed context
     */
    protected Context createSiteTemplateVelocityContext( DocumentContent content,
                                                         SiteRenderingContext siteRenderingContext )
    {
        // first get the context from document
        Context context = createDocumentVelocityContext( content.getRenderingContext(), siteRenderingContext );

        // then add data objects from rendered document

        // Add infos from document
        context.put( "authors", content.getAuthors() );

        context.put( "shortTitle", content.getTitle() );

        // DOXIASITETOOLS-70: Prepend the project name to the title, if any
        String title = "";
        if ( siteRenderingContext.getDecoration() != null
                && siteRenderingContext.getDecoration().getName() != null )
        {
            title = siteRenderingContext.getDecoration().getName();
        }
        else if ( siteRenderingContext.getDefaultWindowTitle() != null )
        {
            title = siteRenderingContext.getDefaultWindowTitle();
        }

        if ( title.length() > 0 )
        {
            title += " &#x2013; "; // Symbol Name: En Dash, Html Entity: &ndash;
        }
        title += content.getTitle();

        context.put( "title", title );

        context.put( "headContent", content.getHead() );

        context.put( "bodyContent", content.getBody() );

        // document date (got from Doxia Sink date() API)
        String documentDate = content.getDate();
        if ( StringUtils.isNotEmpty( documentDate ) )
        {
            context.put( "documentDate", documentDate );

            // deprecated variables that rework the document date, suppose one semantics over others
            // (ie creation date, while it may be last modification date if the document writer decided so)
            // see DOXIASITETOOLS-20 for the beginning and DOXIASITETOOLS-164 for the end of this story
            try
            {
                // we support only ISO 8601 date
                Date creationDate = new SimpleDateFormat( "yyyy-MM-dd" ).parse( documentDate );

                context.put( "creationDate", creationDate );
                SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd" );
                context.put( "dateCreation", sdf.format( creationDate ) );
            }
            catch ( java.text.ParseException e )
            {
                getLogger().warn( "Could not parse date '" + documentDate + "' from "
                    + content.getRenderingContext().getInputName()
                    + " (expected yyyy-MM-dd format), ignoring!" );
            }
        }

        // document rendering context, to get eventual inputName
        context.put( "docRenderingContext", content.getRenderingContext() );

        return context;
    }

    /** {@inheritDoc} */
    public void generateDocument( Writer writer, SiteRendererSink sink, SiteRenderingContext siteRenderingContext )
            throws RendererException
    {
        mergeDocumentIntoSite( writer, sink, siteRenderingContext );
    }

    /** {@inheritDoc} */
    public void mergeDocumentIntoSite( Writer writer, DocumentContent content,
                                           SiteRenderingContext siteRenderingContext )
        throws RendererException
    {
        String templateName = siteRenderingContext.getTemplateName();

        getLogger().debug( "Processing Velocity for template " + templateName + " on "
            + content.getRenderingContext().getInputName() );

        Context context = createSiteTemplateVelocityContext( content, siteRenderingContext );

        ClassLoader old = null;

        if ( siteRenderingContext.getTemplateClassLoader() != null )
        {
            // -------------------------------------------------------------------------
            // If no template classloader was set we'll just use the context classloader
            // -------------------------------------------------------------------------

            old = Thread.currentThread().getContextClassLoader();

            Thread.currentThread().setContextClassLoader( siteRenderingContext.getTemplateClassLoader() );
        }

        try
        {
            Template template;
            Artifact skin = siteRenderingContext.getSkin();

            try
            {
                SkinModel skinModel = siteRenderingContext.getSkinModel();
                String encoding = ( skinModel == null ) ? null : skinModel.getEncoding();

                template = ( encoding == null ) ? velocity.getEngine().getTemplate( templateName )
                                : velocity.getEngine().getTemplate( templateName, encoding );
            }
            catch ( ParseErrorException pee )
            {
                throw new RendererException( "Velocity parsing error while reading the site decoration template "
                    + ( ( skin == null ) ? ( "'" + templateName + "'" ) : ( "from " + skin.getId() + " skin" ) ),
                                             pee );
            }
            catch ( ResourceNotFoundException rnfe )
            {
                throw new RendererException( "Could not find the site decoration template "
                    + ( ( skin == null ) ? ( "'" + templateName + "'" ) : ( "from " + skin.getId() + " skin" ) ),
                                             rnfe );
            }

            try
            {
                StringWriter sw = new StringWriter();
                template.merge( context, sw );
                writer.write( sw.toString().replaceAll( "\r?\n", SystemUtils.LINE_SEPARATOR ) );
            }
            catch ( VelocityException ve )
            {
                throw new RendererException( "Velocity error while merging site decoration template.", ve );
            }
            catch ( IOException ioe )
            {
                throw new RendererException( "IO exception while merging site decoration template.", ioe );
            }
        }
        finally
        {
            IOUtil.close( writer );

            if ( old != null )
            {
                Thread.currentThread().setContextClassLoader( old );
            }
        }
    }

    private SiteRenderingContext createSiteRenderingContext( Map<String, ?> attributes, DecorationModel decoration,
                                                             String defaultWindowTitle, Locale locale )
    {
        SiteRenderingContext context = new SiteRenderingContext();

        context.setTemplateProperties( attributes );
        context.setLocale( locale );
        context.setDecoration( decoration );
        context.setDefaultWindowTitle( defaultWindowTitle );

        return context;
    }

    /** {@inheritDoc} */
    public SiteRenderingContext createContextForSkin( Artifact skin, Map<String, ?> attributes,
                                                      DecorationModel decoration, String defaultWindowTitle,
                                                      Locale locale )
            throws IOException, RendererException
    {
        SiteRenderingContext context = createSiteRenderingContext( attributes, decoration, defaultWindowTitle, locale );

        context.setSkin( skin );

        ZipFile zipFile = getZipFile( skin.getFile() );
        InputStream in = null;

        try
        {
            if ( zipFile.getEntry( SKIN_TEMPLATE_LOCATION ) != null )
            {
                context.setTemplateName( SKIN_TEMPLATE_LOCATION );
                context.setTemplateClassLoader( new URLClassLoader( new URL[]{skin.getFile().toURI().toURL()} ) );
            }
            else
            {
                context.setTemplateName( DEFAULT_TEMPLATE );
                context.setTemplateClassLoader( getClass().getClassLoader() );
                context.setUsingDefaultTemplate( true );
            }

            ZipEntry skinDescriptorEntry = zipFile.getEntry( SkinModel.SKIN_DESCRIPTOR_LOCATION );
            if ( skinDescriptorEntry != null )
            {
                in = zipFile.getInputStream( skinDescriptorEntry );

                SkinModel skinModel = new SkinXpp3Reader().read( in );
                context.setSkinModel( skinModel );

                String toolsPrerequisite =
                    skinModel.getPrerequisites() == null ? null : skinModel.getPrerequisites().getDoxiaSitetools();

                Package p = DefaultSiteRenderer.class.getPackage();
                String current = ( p == null ) ? null : p.getImplementationVersion();

                if ( StringUtils.isNotBlank( toolsPrerequisite ) && ( current != null )
                    && !matchVersion( current, toolsPrerequisite ) )
                {
                    throw new RendererException( "Cannot use skin: has " + toolsPrerequisite
                        + " Doxia Sitetools prerequisite, but current is " + current );
                }
            }
        }
        catch ( XmlPullParserException e )
        {
            throw new RendererException( "Failed to parse " + SkinModel.SKIN_DESCRIPTOR_LOCATION
                + " skin descriptor from " + skin.getId() + " skin", e );
        }
        finally
        {
            IOUtil.close( in );
            closeZipFile( zipFile );
        }

        return context;
    }

    boolean matchVersion( String current, String prerequisite )
        throws RendererException
    {
        try
        {
            ArtifactVersion v = new DefaultArtifactVersion( current );
            VersionRange vr = VersionRange.createFromVersionSpec( prerequisite );

            boolean matched = false;
            ArtifactVersion recommendedVersion = vr.getRecommendedVersion();
            if ( recommendedVersion == null )
            {
                List<Restriction> restrictions = vr.getRestrictions();
                for ( Restriction restriction : restrictions )
                {
                    if ( restriction.containsVersion( v ) )
                    {
                        matched = true;
                        break;
                    }
                }
            }
            else
            {
                // only singular versions ever have a recommendedVersion
                @SuppressWarnings( "unchecked" )
                int compareTo = recommendedVersion.compareTo( v );
                matched = ( compareTo <= 0 );
            }

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Skin doxia-sitetools prerequisite: " + prerequisite + ", current: " + current
                    + ", matched = " + matched );
            }

            return matched;
        }
        catch ( InvalidVersionSpecificationException e )
        {
            throw new RendererException( "Invalid skin doxia-sitetools prerequisite: " + prerequisite, e );
        }
    }

    /** {@inheritDoc} */
    @Deprecated
    public SiteRenderingContext createContextForTemplate( File templateFile, Map<String, ?> attributes,
                                                          DecorationModel decoration, String defaultWindowTitle,
                                                          Locale locale )
            throws MalformedURLException
    {
        SiteRenderingContext context = createSiteRenderingContext( attributes, decoration, defaultWindowTitle, locale );

        context.setTemplateName( templateFile.getName() );
        context.setTemplateClassLoader( new URLClassLoader( new URL[]{templateFile.getParentFile().toURI().toURL()} ) );

        return context;
    }

    /** {@inheritDoc} */
    public void copyResources( SiteRenderingContext siteRenderingContext, File resourcesDirectory,
                               File outputDirectory )
        throws IOException
    {
        throw new AssertionError( "copyResources( SiteRenderingContext, File, File ) is deprecated." );
    }

    /** {@inheritDoc} */
    public void copyResources( SiteRenderingContext siteRenderingContext, File outputDirectory )
        throws IOException
    {
        if ( siteRenderingContext.getSkin() != null )
        {
            ZipFile file = getZipFile( siteRenderingContext.getSkin().getFile() );

            try
            {
                for ( Enumeration<? extends ZipEntry> e = file.entries(); e.hasMoreElements(); )
                {
                    ZipEntry entry = e.nextElement();

                    if ( !entry.getName().startsWith( "META-INF/" ) )
                    {
                        File destFile = new File( outputDirectory, entry.getName() );
                        if ( !entry.isDirectory() )
                        {
                            if ( destFile.exists() )
                            {
                                // don't override existing content: avoids extra rewrite with same content or extra site
                                // resource
                                continue;
                            }

                            destFile.getParentFile().mkdirs();

                            copyFileFromZip( file, entry, destFile );
                        }
                        else
                        {
                            destFile.mkdirs();
                        }
                    }
                }
            }
            finally
            {
                closeZipFile( file );
            }
        }

        if ( siteRenderingContext.isUsingDefaultTemplate() )
        {
            InputStream resourceList = getClass().getClassLoader()
                    .getResourceAsStream( RESOURCE_DIR + "/resources.txt" );

            if ( resourceList != null )
            {
                Reader r = null;
                LineNumberReader reader = null;
                try
                {
                    r = ReaderFactory.newReader( resourceList, ReaderFactory.UTF_8 );
                    reader = new LineNumberReader( r );

                    String line;

                    while ( ( line = reader.readLine() ) != null )
                    {
                        if ( line.startsWith( "#" ) || line.trim().length() == 0 )
                        {
                            continue;
                        }

                        InputStream is = getClass().getClassLoader().getResourceAsStream( RESOURCE_DIR + "/" + line );

                        if ( is == null )
                        {
                            throw new IOException( "The resource " + line + " doesn't exist." );
                        }

                        File outputFile = new File( outputDirectory, line );

                        if ( outputFile.exists() )
                        {
                            // don't override existing content: avoids extra rewrite with same content or extra site
                            // resource
                            continue;
                        }

                        if ( !outputFile.getParentFile().exists() )
                        {
                            outputFile.getParentFile().mkdirs();
                        }

                        OutputStream os = null;
                        try
                        {
                            // for the images
                            os = new FileOutputStream( outputFile );
                            IOUtil.copy( is, os );
                        }
                        finally
                        {
                            IOUtil.close( os );
                        }

                        IOUtil.close( is );
                    }
                }
                finally
                {
                    IOUtil.close( reader );
                    IOUtil.close( r );
                }
            }
        }

        // Copy extra site resources
        for ( File siteDirectory : siteRenderingContext.getSiteDirectories() )
        {
            File resourcesDirectory = new File( siteDirectory, "resources" );

            if ( resourcesDirectory != null && resourcesDirectory.exists() )
            {
                copyDirectory( resourcesDirectory, outputDirectory );
            }
        }

        // Check for the existence of /css/site.css
        File siteCssFile = new File( outputDirectory, "/css/site.css" );
        if ( !siteCssFile.exists() )
        {
            // Create the subdirectory css if it doesn't exist, DOXIA-151
            File cssDirectory = new File( outputDirectory, "/css/" );
            boolean created = cssDirectory.mkdirs();
            if ( created && getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "The directory '" + cssDirectory.getAbsolutePath() + "' did not exist. It was created." );
            }

            // If the file is not there - create an empty file, DOXIA-86
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "The file '" + siteCssFile.getAbsolutePath() + "' does not exist. Creating an empty file." );
            }
            Writer writer = null;
            try
            {
                writer = WriterFactory.newWriter( siteCssFile, siteRenderingContext.getOutputEncoding() );
                //DOXIA-290...the file should not be 0 bytes.
                writer.write( "/* You can override this file with your own styles */"  );
            }
            finally
            {
                IOUtil.close( writer );
            }
        }
    }

    private static void copyFileFromZip( ZipFile file, ZipEntry entry, File destFile )
            throws IOException
    {
        FileOutputStream fos = new FileOutputStream( destFile );

        try
        {
            IOUtil.copy( file.getInputStream( entry ), fos );
        }
        finally
        {
            IOUtil.close( fos );
        }
    }

    /**
     * Copy the directory
     *
     * @param source      source file to be copied
     * @param destination destination file
     * @throws java.io.IOException if any
     */
    protected void copyDirectory( File source, File destination )
            throws IOException
    {
        if ( source.exists() )
        {
            DirectoryScanner scanner = new DirectoryScanner();

            String[] includedResources = {"**/**"};

            scanner.setIncludes( includedResources );

            scanner.addDefaultExcludes();

            scanner.setBasedir( source );

            scanner.scan();

            List<String> includedFiles = Arrays.asList( scanner.getIncludedFiles() );

            for ( String name : includedFiles )
            {
                File sourceFile = new File( source, name );

                File destinationFile = new File( destination, name );

                FileUtils.copyFile( sourceFile, destinationFile );
            }
        }
    }

    private Reader validate( Reader source, String resource )
            throws ParseException, IOException
    {
        getLogger().debug( "Validating: " + resource );

        try
        {
            String content = IOUtil.toString( new BufferedReader( source ) );

            new XmlValidator( new PlexusLoggerWrapper( getLogger() ) ).validate( content );

            return new StringReader( content );
        }
        finally
        {
            IOUtil.close( source );
        }
    }

    // TODO replace with StringUtils.endsWithIgnoreCase() from maven-shared-utils 0.7
    static boolean endsWithIgnoreCase( String str, String searchStr )
    {
        if ( str.length() < searchStr.length() )
        {
            return false;
        }

        return str.regionMatches( true, str.length() - searchStr.length(), searchStr, 0, searchStr.length() );
    }

    private static ZipFile getZipFile( File file )
        throws IOException
    {
        if ( file == null )
        {
            throw new IOException( "Error opening ZipFile: null" );
        }

        try
        {
            // TODO: plexus-archiver, if it could do the excludes
            return new ZipFile( file );
        }
        catch ( ZipException ex )
        {
            IOException ioe = new IOException( "Error opening ZipFile: " + file.getAbsolutePath() );
            ioe.initCause( ex );
            throw ioe;
        }
    }

    private static void closeZipFile( ZipFile zipFile )
    {
        // TODO: move to plexus utils
        try
        {
            zipFile.close();
        }
        catch ( IOException e )
        {
            // ignore
        }
    }
}
