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
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.doxia.Doxia;
import org.apache.maven.doxia.logging.PlexusLoggerWrapper;
import org.apache.maven.doxia.sink.render.RenderingContext;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.parser.manager.ParserNotFoundException;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.module.site.SiteModule;
import org.apache.maven.doxia.module.site.manager.SiteModuleManager;
import org.apache.maven.doxia.module.site.manager.SiteModuleNotFoundException;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.doxia.util.XmlValidator;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;

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
import org.codehaus.plexus.velocity.SiteResourceLoader;
import org.codehaus.plexus.velocity.VelocityComponent;


/**
 * <p>DefaultSiteRenderer class.</p>
 *
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id$
 * @since 1.0
 * @plexus.component role-hint="default"
 */
public class DefaultSiteRenderer
    extends AbstractLogEnabled
    implements Renderer
{
    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /** @plexus.requirement */
    private VelocityComponent velocity;

    /**
     * @plexus.requirement
     */
    private SiteModuleManager siteModuleManager;

    /** @plexus.requirement */
    private Doxia doxia;

    /** @plexus.requirement */
    private I18N i18n;

    private static final String RESOURCE_DIR = "org/apache/maven/doxia/siterenderer/resources";

    private static final String DEFAULT_TEMPLATE = RESOURCE_DIR + "/default-site.vm";

    private static final String SKIN_TEMPLATE_LOCATION = "META-INF/maven/site.vm";

    // ----------------------------------------------------------------------
    // Renderer implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void render( Collection<DocumentRenderer> documents, SiteRenderingContext siteRenderingContext,
                        File outputDirectory )
        throws RendererException, IOException
    {
        renderModule( documents, siteRenderingContext, outputDirectory );

        for ( File siteDirectory : siteRenderingContext.getSiteDirectories() )
        {
            copyResources( siteRenderingContext, new File( siteDirectory, "resources" ), outputDirectory );
        }
    }

    /** {@inheritDoc} */
    public Map<String, DocumentRenderer> locateDocumentFiles( SiteRenderingContext siteRenderingContext )
            throws IOException, RendererException
    {
        Map<String, DocumentRenderer> files = new LinkedHashMap<String, DocumentRenderer>();
        Map<String, String> moduleExcludes = siteRenderingContext.getModuleExcludes();

        for ( File siteDirectory : siteRenderingContext.getSiteDirectories() )
        {
            if ( siteDirectory.exists() )
            {
                Collection<SiteModule> modules = siteModuleManager.getSiteModules();
                for ( SiteModule module : modules )
                {
                    File moduleBasedir = new File( siteDirectory, module.getSourceDirectory() );

                    if ( moduleExcludes != null && moduleExcludes.containsKey( module.getParserId() ) )
                    {
                        addModuleFiles( moduleBasedir, module, moduleExcludes.get( module.getParserId() ),
                                files );
                    }
                    else
                    {
                        addModuleFiles( moduleBasedir, module, null, files );
                    }
                }
            }
        }

        for ( ModuleReference module : siteRenderingContext.getModules() )
        {
            try
            {
                if ( moduleExcludes != null && moduleExcludes.containsKey( module.getParserId() ) )
                {
                    addModuleFiles( module.getBasedir(), siteModuleManager.getSiteModule( module.getParserId() ),
                        moduleExcludes.get( module.getParserId() ), files );
                }
                else
                {
                    addModuleFiles( module.getBasedir(), siteModuleManager.getSiteModule( module.getParserId() ), null,
                            files );
                }
            }
            catch ( SiteModuleNotFoundException e )
            {
                throw new RendererException( "Unable to find module: " + e.getMessage(), e );
            }
        }
        return files;
    }

    private void addModuleFiles( File moduleBasedir, SiteModule module, String excludes,
                                 Map<String, DocumentRenderer> files )
            throws IOException, RendererException
    {
        if ( moduleBasedir.exists() )
        {
            @SuppressWarnings ( "unchecked" )
            List<String> allFiles = FileUtils.getFileNames( moduleBasedir, "**/*.*", excludes, false );

            String lowerCaseExtension = module.getExtension().toLowerCase( Locale.ENGLISH );
            List<String> docs = new LinkedList<String>( allFiles );
            // Take care of extension case
            for ( Iterator<String> it = docs.iterator(); it.hasNext(); )
            {
                String name = it.next().trim();

                if ( !name.toLowerCase( Locale.ENGLISH ).endsWith( "." + lowerCaseExtension ) )
                {
                    it.remove();
                }
            }

            List<String> velocityFiles = new LinkedList<String>( allFiles );
            // *.xml.vm
            for ( Iterator<String> it = velocityFiles.iterator(); it.hasNext(); )
            {
                String name = it.next().trim();

                if ( !name.toLowerCase( Locale.ENGLISH ).endsWith( lowerCaseExtension + ".vm" ) )
                {
                    it.remove();
                }
            }
            docs.addAll( velocityFiles );

            for ( String doc : docs )
            {
                String docc = doc.trim();

                RenderingContext context =
                        new RenderingContext( moduleBasedir, docc, module.getParserId(), module.getExtension() );

                // TODO: DOXIA-111: we need a general filter here that knows how to alter the context
                if ( docc.toLowerCase( Locale.ENGLISH ).endsWith( ".vm" ) )
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

                    throw new RendererException( "Files '" + module.getSourceDirectory() + File.separator + docc
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
                            throw new RendererException( "Files '" + module.getSourceDirectory() + File.separator
                                + docc + "' clashes with existing '" + originalDoc + "'." );
                        }

                        if ( getLogger().isWarnEnabled() )
                        {
                            getLogger().warn(
                                              "Files '" + module.getSourceDirectory() + File.separator + docc
                                                  + "' could clashes with existing '" + originalDoc + "'." );
                        }
                    }
                }

                files.put( key, new DoxiaDocumentRenderer( context ) );
            }
        }
    }

    private void renderModule( Collection<DocumentRenderer> docs, SiteRenderingContext siteRenderingContext,
                               File outputDirectory )
            throws IOException, RendererException
    {
        for ( DocumentRenderer docRenderer : docs )
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
                    writer = WriterFactory.newWriter( outputFile, siteRenderingContext.getOutputEncoding() );
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
    public void renderDocument( Writer writer, RenderingContext renderingContext, SiteRenderingContext context )
            throws RendererException, FileNotFoundException, UnsupportedEncodingException
    {
        SiteRendererSink sink = new SiteRendererSink( renderingContext );

        File doc = new File( renderingContext.getBasedir(), renderingContext.getInputName() );

        Reader reader = null;
        try
        {
            String resource = doc.getAbsolutePath();

            Parser parser = doxia.getParser( renderingContext.getParserId() );

            // TODO: DOXIA-111: the filter used here must be checked generally.
            if ( renderingContext.getAttribute( "velocity" ) != null )
            {
                try
                {
                    SiteResourceLoader.setResource( resource );

                    Context vc = createVelocityContext( sink, context );

                    StringWriter sw = new StringWriter();

                    velocity.getEngine().mergeTemplate( resource, context.getInputEncoding(), vc, sw );

                    reader = new StringReader( sw.toString() );
                    if ( parser.getType() == Parser.XML_TYPE && context.isValidate() )
                    {
                        reader = validate( reader, resource );
                    }
                }
                catch ( Exception e )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().error( "Error parsing " + resource + " as a velocity template, using as text.", e );
                    }
                    else
                    {
                        getLogger().error( "Error parsing " + resource + " as a velocity template, using as text." );
                    }
                }
            }
            else
            {
                switch ( parser.getType() )
                {
                    case Parser.XML_TYPE:
                        reader = ReaderFactory.newXmlReader( doc );
                        if ( context.isValidate() )
                        {
                            reader = validate( reader, resource );
                        }
                        break;

                    case Parser.TXT_TYPE:
                    case Parser.UNKNOWN_TYPE:
                    default:
                        reader = ReaderFactory.newReader( doc, context.getInputEncoding() );
                }
            }
            sink.enableLogging( new PlexusLoggerWrapper( getLogger() ) );
            doxia.parse( reader, renderingContext.getParserId(), sink );
        }
        catch ( ParserNotFoundException e )
        {
            throw new RendererException( "Error getting a parser for '" + doc + "': " + e.getMessage(), e );
        }
        catch ( ParseException e )
        {
            throw new RendererException( "Error parsing '"
                    + doc + "': line [" + e.getLineNumber() + "] " + e.getMessage(), e );
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

        generateDocument( writer, sink, context );
    }

    private Context createVelocityContext( SiteRendererSink sink, SiteRenderingContext siteRenderingContext )
    {
    	ToolManager toolManager = new ToolManager( true );
        Context context = toolManager.createContext();

        // ----------------------------------------------------------------------
        // Data objects
        // ----------------------------------------------------------------------

        RenderingContext renderingContext = sink.getRenderingContext();
        context.put( "relativePath", renderingContext.getRelativePath() );

        // Add infos from document
        context.put( "authors", sink.getAuthors() );

        context.put( "title", sink.getTitle() );

        context.put( "headContent", sink.getHead() );

        context.put( "bodyContent", sink.getBody() );

        context.put( "decoration", siteRenderingContext.getDecoration() );

        SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd" );
        if ( StringUtils.isNotEmpty( sink.getDate() ) )
        {
            try
            {
                // we support only ISO-8601 date
                context.put( "dateCreation",
                        sdf.format( new SimpleDateFormat( "yyyy-MM-dd" ).parse( sink.getDate() ) ) );
            }
            catch ( java.text.ParseException e )
            {
                getLogger().debug( "Could not parse date: " + sink.getDate() + ", ignoring!", e );
            }
        }
        context.put( "dateRevision", sdf.format( new Date() ) );

        context.put( "currentDate", new Date() );
        
        context.put( "publishDate", siteRenderingContext.getPublishDate() );

        Locale locale = siteRenderingContext.getLocale();
        context.put( "dateFormat", DateFormat.getDateInstance( DateFormat.DEFAULT, locale ) );

        String currentFileName = renderingContext.getOutputName().replace( '\\', '/' );
        context.put( "currentFileName", currentFileName );

        context.put( "alignedFileName", PathTool.calculateLink( currentFileName, renderingContext.getRelativePath() ) );

        context.put( "locale", locale );
        context.put( "supportedLocales", Collections.unmodifiableList( siteRenderingContext.getSiteLocales() ) );

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

        return context;
    }

    /** {@inheritDoc} */
    public void generateDocument( Writer writer, SiteRendererSink sink, SiteRenderingContext siteRenderingContext )
            throws RendererException
    {
        Context context = createVelocityContext( sink, siteRenderingContext );

        writeTemplate( writer, context, siteRenderingContext );
    }

    private void writeTemplate( Writer writer, Context context, SiteRenderingContext siteContext )
            throws RendererException
    {
        ClassLoader old = null;

        if ( siteContext.getTemplateClassLoader() != null )
        {
            // -------------------------------------------------------------------------
            // If no template classloader was set we'll just use the context classloader
            // -------------------------------------------------------------------------

            old = Thread.currentThread().getContextClassLoader();

            Thread.currentThread().setContextClassLoader( siteContext.getTemplateClassLoader() );
        }

        try
        {
            processTemplate( siteContext.getTemplateName(), context, writer );
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

    /**
     * @noinspection OverlyBroadCatchBlock,UnusedCatchParameter
     */
    private void processTemplate( String templateName, Context context, Writer writer )
            throws RendererException
    {
        Template template;

        try
        {
            template = velocity.getEngine().getTemplate( templateName );
        }
        catch ( Exception e )
        {
            throw new RendererException( "Could not find the template '" + templateName, e );
        }

        try
        {
            template.merge( context, writer );
        }
        catch ( Exception e )
        {
            throw new RendererException( "Error while generating code.", e );
        }
    }

    /** {@inheritDoc} */
    public SiteRenderingContext createContextForSkin( File skinFile, Map<String, ?> attributes, DecorationModel decoration,
                                                      String defaultWindowTitle, Locale locale )
            throws IOException
    {
        SiteRenderingContext context = new SiteRenderingContext();

        ZipFile zipFile = getZipFile( skinFile );

        try
        {
            if ( zipFile.getEntry( SKIN_TEMPLATE_LOCATION ) != null )
            {
                context.setTemplateName( SKIN_TEMPLATE_LOCATION );
                context.setTemplateClassLoader( new URLClassLoader( new URL[]{skinFile.toURI().toURL()} ) );
            }
            else
            {
                context.setTemplateName( DEFAULT_TEMPLATE );
                context.setTemplateClassLoader( getClass().getClassLoader() );
                context.setUsingDefaultTemplate( true );
            }
        }
        finally
        {
            closeZipFile( zipFile );
        }

        context.setTemplateProperties( attributes );
        context.setLocale( locale );
        context.setDecoration( decoration );
        context.setDefaultWindowTitle( defaultWindowTitle );
        context.setSkinJarFile( skinFile );

        return context;
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

    /** {@inheritDoc} */
    public SiteRenderingContext createContextForTemplate( File templateFile, File skinFile, Map<String, ?> attributes,
                                                          DecorationModel decoration, String defaultWindowTitle,
                                                          Locale locale )
            throws MalformedURLException
    {
        SiteRenderingContext context = new SiteRenderingContext();

        context.setTemplateName( templateFile.getName() );
        context.setTemplateClassLoader( new URLClassLoader( new URL[]{templateFile.getParentFile().toURI().toURL()} ) );

        context.setTemplateProperties( attributes );
        context.setLocale( locale );
        context.setDecoration( decoration );
        context.setDefaultWindowTitle( defaultWindowTitle );
        context.setSkinJarFile( skinFile );

        return context;
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

    /** {@inheritDoc} */
    public void copyResources( SiteRenderingContext siteRenderingContext, File resourcesDirectory, File outputDirectory )
            throws IOException
    {
        if ( siteRenderingContext.getSkinJarFile() != null )
        {
            ZipFile file = getZipFile( siteRenderingContext.getSkinJarFile() );

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
                try
                {
                    r = ReaderFactory.newReader( resourceList, ReaderFactory.UTF_8 );
                    LineNumberReader reader = new LineNumberReader( r );

                    String line = reader.readLine();

                    while ( line != null )
                    {
                        InputStream is = getClass().getClassLoader().getResourceAsStream( RESOURCE_DIR + "/" + line );

                        if ( is == null )
                        {
                            throw new IOException( "The resource " + line + " doesn't exist." );
                        }

                        File outputFile = new File( outputDirectory, line );

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

                        line = reader.readLine();
                    }
                }
                finally
                {
                    IOUtil.close( r );
                }
            }
        }

        // Copy extra site resources
        if ( resourcesDirectory != null && resourcesDirectory.exists() )
        {
            copyDirectory( resourcesDirectory, outputDirectory );
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
                    "The file '" + siteCssFile.getAbsolutePath() + "' does not exists. Creating an empty file." );
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

}
