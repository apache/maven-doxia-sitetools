package org.apache.maven.doxia.tools;

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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.site.decoration.Menu;
import org.apache.maven.doxia.site.decoration.MenuItem;
import org.apache.maven.doxia.site.decoration.Skin;
import org.apache.maven.doxia.site.decoration.inheritance.DecorationModelInheritanceAssembler;
import org.apache.maven.doxia.site.decoration.io.xpp3.DecorationXpp3Reader;
import org.apache.maven.doxia.site.decoration.io.xpp3.DecorationXpp3Writer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Site;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.reporting.MavenReport;
import org.codehaus.plexus.i18n.I18N;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.interpolation.EnvarBasedValueSource;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.PrefixedObjectValueSource;
import org.codehaus.plexus.interpolation.PrefixedPropertiesValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the site tool.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
@Singleton
@Named
public class DefaultSiteTool
    implements SiteTool
{
    private static final Logger LOGGER = LoggerFactory.getLogger( DefaultSiteTool.class );

    // ----------------------------------------------------------------------
    // Components
    // ----------------------------------------------------------------------

    /**
     * The component that is used to resolve additional artifacts required.
     */
    @Inject
    private ArtifactResolver artifactResolver;

    /**
     * The component used for creating artifact instances.
     */
    @Inject
    private ArtifactFactory artifactFactory;

    /**
     * Internationalization.
     */
    @Inject
    protected I18N i18n;

    /**
     * The component for assembling inheritance.
     */
    @Inject
    protected DecorationModelInheritanceAssembler assembler;

    /**
     * Project builder.
     */
    @Inject
    protected ProjectBuilder projectBuilder;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Artifact getSkinArtifactFromRepository( ArtifactRepository localRepository,
                                                   List<ArtifactRepository> remoteArtifactRepositories,
                                                   DecorationModel decoration )
        throws SiteToolException
    {
        Objects.requireNonNull( localRepository, "localRepository cannot be null" );
        Objects.requireNonNull( remoteArtifactRepositories, "remoteArtifactRepositories cannot be null" );
        Objects.requireNonNull( decoration, "decoration cannot be null" );
        Skin skin = Objects.requireNonNull( decoration.getSkin(), "decoration.skin cannot be null" );

        String version = skin.getVersion();
        Artifact artifact;
        try
        {
            if ( version == null )
            {
                version = Artifact.RELEASE_VERSION;
            }
            VersionRange versionSpec = VersionRange.createFromVersionSpec( version );
            artifact = artifactFactory.createDependencyArtifact( skin.getGroupId(), skin.getArtifactId(), versionSpec,
                                                                 "jar", null, null );

            artifactResolver.resolve( artifact, remoteArtifactRepositories, localRepository );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            throw new SiteToolException( "The skin version '" + version + "' is not valid", e );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new SiteToolException( "Unable to find skin", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new SiteToolException( "The skin does not exist", e );
        }

        return artifact;
    }

    /**
     * This method is not implemented according to the URI specification and has many weird
     * corner cases where it doesn't do the right thing. Please consider using a better
     * implemented method from a different library such as org.apache.http.client.utils.URIUtils#resolve.
     */
    @Deprecated
    public String getRelativePath( String to, String from )
    {
        Objects.requireNonNull( to, "to cannot be null" );
        Objects.requireNonNull( from, "from cannot be null" );

        if ( to.contains( ":" ) && from.contains( ":" ) )
        {
            String toScheme = to.substring( 0, to.lastIndexOf( ':' ) );
            String fromScheme = from.substring( 0, from.lastIndexOf( ':' ) );
            if ( !toScheme.equals( fromScheme ) )
            {
                return to;
            }
        }

        URL toUrl = null;
        URL fromUrl = null;

        String toPath = to;
        String fromPath = from;

        try
        {
            toUrl = new URL( to );
        }
        catch ( MalformedURLException e )
        {
            try
            {
                toUrl = new File( getNormalizedPath( to ) ).toURI().toURL();
            }
            catch ( MalformedURLException e1 )
            {
                LOGGER.warn( "Unable to load a URL for '" + to + "'", e );
                return to;
            }
        }

        try
        {
            fromUrl = new URL( from );
        }
        catch ( MalformedURLException e )
        {
            try
            {
                fromUrl = new File( getNormalizedPath( from ) ).toURI().toURL();
            }
            catch ( MalformedURLException e1 )
            {
                LOGGER.warn( "Unable to load a URL for '" + from + "'", e );
                return to;
            }
        }

        if ( toUrl != null && fromUrl != null )
        {
            // URLs, determine if they share protocol and domain info

            if ( ( toUrl.getProtocol().equalsIgnoreCase( fromUrl.getProtocol() ) )
                && ( toUrl.getHost().equalsIgnoreCase( fromUrl.getHost() ) )
                && ( toUrl.getPort() == fromUrl.getPort() ) )
            {
                // shared URL domain details, use URI to determine relative path

                toPath = toUrl.getFile();
                fromPath = fromUrl.getFile();
            }
            else
            {
                // don't share basic URL information, no relative available

                return to;
            }
        }
        else if ( ( toUrl != null && fromUrl == null ) || ( toUrl == null && fromUrl != null ) )
        {
            // one is a URL and the other isn't, no relative available.

            return to;
        }

        // either the two locations are not URLs or if they are they
        // share the common protocol and domain info and we are left
        // with their URI information

        String relativePath = getRelativeFilePath( fromPath, toPath );

        if ( relativePath == null )
        {
            relativePath = to;
        }

        if ( LOGGER.isDebugEnabled() && !relativePath.toString().equals( to ) )
        {
            LOGGER.debug( "Mapped url: " + to + " to relative path: " + relativePath );
        }

        return relativePath;
    }

    private static String getRelativeFilePath( final String oldPath, final String newPath )
    {
        // normalize the path delimiters

        String fromPath = new File( oldPath ).getPath();
        String toPath = new File( newPath ).getPath();

        // strip any leading slashes if its a windows path
        if ( toPath.matches( "^\\[a-zA-Z]:" ) )
        {
            toPath = toPath.substring( 1 );
        }
        if ( fromPath.matches( "^\\[a-zA-Z]:" ) )
        {
            fromPath = fromPath.substring( 1 );
        }

        // lowercase windows drive letters.
        if ( fromPath.startsWith( ":", 1 ) )
        {
            fromPath = Character.toLowerCase( fromPath.charAt( 0 ) ) + fromPath.substring( 1 );
        }
        if ( toPath.startsWith( ":", 1 ) )
        {
            toPath = Character.toLowerCase( toPath.charAt( 0 ) ) + toPath.substring( 1 );
        }

        // check for the presence of windows drives. No relative way of
        // traversing from one to the other.

        if ( ( toPath.startsWith( ":", 1 ) && fromPath.startsWith( ":", 1 ) )
            && ( !toPath.substring( 0, 1 ).equals( fromPath.substring( 0, 1 ) ) ) )
        {
            // they both have drive path element but they don't match, no
            // relative path

            return null;
        }

        if ( ( toPath.startsWith( ":", 1 ) && !fromPath.startsWith( ":", 1 ) )
            || ( !toPath.startsWith( ":", 1 ) && fromPath.startsWith( ":", 1 ) ) )
        {

            // one has a drive path element and the other doesn't, no relative
            // path.

            return null;

        }

        final String relativePath = buildRelativePath( toPath, fromPath, File.separatorChar );

        return relativePath.toString();
    }

    /** {@inheritDoc} */
    public File getSiteDescriptor( File siteDirectory, Locale locale )
    {
        Objects.requireNonNull( siteDirectory, "siteDirectory cannot be null" );
        Objects.requireNonNull( locale, "locale cannot be null" );

        String variant = locale.getVariant();
        String country = locale.getCountry();
        String language = locale.getLanguage();

        File siteDescriptor = null;

        if ( !variant.isEmpty() )
        {
            siteDescriptor = new File( siteDirectory, "site_" + language + "_" + country + "_" + variant + ".xml" );
        }

        if ( ( siteDescriptor == null || !siteDescriptor.isFile() ) && !country.isEmpty() )
        {
            siteDescriptor = new File( siteDirectory, "site_" + language + "_" + country + ".xml" );
        }

        if ( ( siteDescriptor == null || !siteDescriptor.isFile() ) && !language.isEmpty() )
        {
            siteDescriptor = new File( siteDirectory, "site_" + language + ".xml" );
        }

        if ( siteDescriptor == null || !siteDescriptor.isFile() )
        {
            siteDescriptor = new File( siteDirectory, "site.xml" );
        }

        return siteDescriptor;
    }

    /**
     * Get a site descriptor from one of the repositories.
     *
     * @param project the Maven project, not null.
     * @param localRepository the Maven local repository, not null.
     * @param repositories the Maven remote repositories, not null.
     * @param locale the locale wanted for the site descriptor, not null.
     * See {@link #getSiteDescriptor(File, Locale)} for details.
     * @return the site descriptor into the local repository after download of it from repositories or null if not
     * found in repositories.
     * @throws SiteToolException if any
     */
    File getSiteDescriptorFromRepository( MavenProject project, ArtifactRepository localRepository,
                                                 List<ArtifactRepository> repositories, Locale locale )
        throws SiteToolException
    {
        Objects.requireNonNull( project, "project cannot be null" );
        Objects.requireNonNull( localRepository, "localRepository cannot be null" );
        Objects.requireNonNull( repositories, "repositories cannot be null" );
        Objects.requireNonNull( locale, "locale cannot be null" );

        try
        {
            return resolveSiteDescriptor( project, localRepository, repositories, locale );
        }
        catch ( ArtifactNotFoundException e )
        {
            LOGGER.debug( "Unable to locate site descriptor", e );
            return null;
        }
        catch ( ArtifactResolutionException e )
        {
            throw new SiteToolException( "Unable to locate site descriptor", e );
        }
        catch ( IOException e )
        {
            throw new SiteToolException( "Unable to locate site descriptor", e );
        }
    }

    /** {@inheritDoc} */
    public DecorationModel getDecorationModel( File siteDirectory, Locale locale, MavenProject project,
                                               List<MavenProject> reactorProjects, ArtifactRepository localRepository,
                                               List<ArtifactRepository> repositories )
        throws SiteToolException
    {
        Objects.requireNonNull( locale, "locale cannot be null" );
        Objects.requireNonNull( project, "project cannot be null" );
        Objects.requireNonNull( reactorProjects, "reactorProjects cannot be null" );
        Objects.requireNonNull( localRepository, "localRepository cannot be null" );
        Objects.requireNonNull( repositories, "repositories cannot be null" );

        LOGGER.debug( "Computing decoration model of '" + project.getId() + "' for "
                + ( locale.equals( SiteTool.DEFAULT_LOCALE ) ? "default locale" : "locale '" + locale + "'" ) );

        Map.Entry<DecorationModel, MavenProject> result =
            getDecorationModel( 0, siteDirectory, locale, project, reactorProjects, localRepository, repositories );
        DecorationModel decorationModel = result.getKey();
        MavenProject parentProject = result.getValue();

        if ( decorationModel == null )
        {
            LOGGER.debug( "Using default site descriptor" );
            decorationModel = getDefaultDecorationModel();
        }

        // DecorationModel back to String to interpolate, then go back to DecorationModel
        String siteDescriptorContent = decorationModelToString( decorationModel );

        // "classical" late interpolation, after full inheritance
        siteDescriptorContent = getInterpolatedSiteDescriptorContent( project, siteDescriptorContent, false );

        decorationModel = readDecorationModel( siteDescriptorContent );

        if ( parentProject != null )
        {
            populateParentMenu( decorationModel, locale, project, parentProject, true );
        }

        try
        {
            populateModulesMenu( decorationModel, locale, project, reactorProjects, localRepository, true );
        }
        catch ( IOException e )
        {
            throw new SiteToolException( "Error while populating modules menu", e );
        }

        return decorationModel;
    }

    /** {@inheritDoc} */
    public String getInterpolatedSiteDescriptorContent( Map<String, String> props, MavenProject aProject,
                                                        String siteDescriptorContent )
        throws SiteToolException
    {
        Objects.requireNonNull( props, "props cannot be null" );

        // "classical" late interpolation
        return getInterpolatedSiteDescriptorContent( aProject, siteDescriptorContent, false );
    }

    private String getInterpolatedSiteDescriptorContent( MavenProject aProject,
                                                        String siteDescriptorContent, boolean isEarly )
        throws SiteToolException
    {
        Objects.requireNonNull( aProject, "aProject cannot be null" );
        Objects.requireNonNull( siteDescriptorContent, "siteDescriptorContent cannot be null" );

        RegexBasedInterpolator interpolator = new RegexBasedInterpolator();

        if ( isEarly )
        {
            interpolator.addValueSource( new PrefixedObjectValueSource( "this.", aProject ) );
            interpolator.addValueSource( new PrefixedPropertiesValueSource( "this.", aProject.getProperties() ) );
        }
        else
        {
            interpolator.addValueSource( new PrefixedObjectValueSource( "project.", aProject ) );
            interpolator.addValueSource( new MapBasedValueSource( aProject.getProperties() ) );

            try
            {
                interpolator.addValueSource( new EnvarBasedValueSource() );
            }
            catch ( IOException e )
            {
                // Prefer logging?
                throw new SiteToolException( "Cannot interpolate environment properties", e );
            }
        }

        try
        {
            // FIXME: this does not escape xml entities, see MSITE-226, PLXCOMP-118
            return interpolator.interpolate( siteDescriptorContent );
        }
        catch ( InterpolationException e )
        {
            throw new SiteToolException( "Cannot interpolate site descriptor", e );
        }
    }

    /** {@inheritDoc} */
    public MavenProject getParentProject( MavenProject aProject, List<MavenProject> reactorProjects,
                                          ArtifactRepository localRepository )
    {
        Objects.requireNonNull( aProject, "aProject cannot be null" );
        Objects.requireNonNull( reactorProjects, "reactorProjects cannot be null" );
        Objects.requireNonNull( localRepository, "localRepository cannot be null" );

        return aProject.getParent();
    }

    /**
     * Populate the pre-defined <code>parent</code> menu of the decoration model,
     * if used through <code>&lt;menu ref="parent"/&gt;</code>.
     *
     * @param decorationModel the Doxia Sitetools DecorationModel, not null.
     * @param locale the locale used for the i18n in DecorationModel, not null.
     * @param project a Maven project, not null.
     * @param parentProject a Maven parent project, not null.
     * @param keepInheritedRefs used for inherited references.
     */
    private void populateParentMenu( DecorationModel decorationModel, Locale locale, MavenProject project,
                                    MavenProject parentProject, boolean keepInheritedRefs )
    {
        Objects.requireNonNull( decorationModel, "decorationModel cannot be null" );
        Objects.requireNonNull( locale, "locale cannot be null" );
        Objects.requireNonNull( project, "project cannot be null" );
        Objects.requireNonNull( parentProject, "parentProject cannot be null" );

        Menu menu = decorationModel.getMenuRef( "parent" );

        if ( menu == null )
        {
            return;
        }

        if ( keepInheritedRefs && menu.isInheritAsRef() )
        {
            return;
        }

        String parentUrl = getDistMgmntSiteUrl( parentProject );

        if ( parentUrl != null )
        {
            if ( parentUrl.endsWith( "/" ) )
            {
                parentUrl += "index.html";
            }
            else
            {
                parentUrl += "/index.html";
            }

            parentUrl = getRelativePath( parentUrl, getDistMgmntSiteUrl( project ) );
        }
        else
        {
            // parent has no url, assume relative path is given by site structure
            File parentBasedir = parentProject.getBasedir();
            // First make sure that the parent is available on the file system
            if ( parentBasedir != null )
            {
                // Try to find the relative path to the parent via the file system
                String parentPath = parentBasedir.getAbsolutePath();
                String projectPath = project.getBasedir().getAbsolutePath();
                parentUrl = getRelativePath( parentPath, projectPath ) + "/index.html";
            }
        }

        // Only add the parent menu if we were able to find a URL for it
        if ( parentUrl == null )
        {
            LOGGER.warn( "Unable to find a URL to the parent project. The parent menu will NOT be added." );
        }
        else
        {
            if ( menu.getName() == null )
            {
                menu.setName( i18n.getString( "site-tool", locale, "decorationModel.menu.parentproject" ) );
            }

            MenuItem item = new MenuItem();
            item.setName( parentProject.getName() );
            item.setHref( parentUrl );
            menu.addItem( item );
        }
    }

    /**
     * Populate the pre-defined <code>modules</code> menu of the decoration model,
     * if used through <code>&lt;menu ref="modules"/&gt;</code>.
     *
     * @param decorationModel the Doxia Sitetools DecorationModel, not null.
     * @param locale the locale used for the i18n in DecorationModel, not null.
     * @param project a Maven project, not null.
     * @param reactorProjects the Maven reactor projects, not null.
     * @param localRepository the Maven local repository, not null.
     * @param keepInheritedRefs used for inherited references.
     * @throws SiteToolException if any
     * @throws IOException
     */
    private void populateModulesMenu( DecorationModel decorationModel, Locale locale, MavenProject project,
                                     List<MavenProject> reactorProjects, ArtifactRepository localRepository,
                                     boolean keepInheritedRefs )
        throws SiteToolException, IOException
    {
        Objects.requireNonNull( decorationModel, "decorationModel cannot be null" );
        Objects.requireNonNull( locale, "locale cannot be null" );
        Objects.requireNonNull( project, "project cannot be null" );
        Objects.requireNonNull( reactorProjects, "reactorProjects cannot be null" );
        Objects.requireNonNull( localRepository, "localRepository cannot be null" );

        Menu menu = decorationModel.getMenuRef( "modules" );

        if ( menu == null )
        {
            return;
        }

        if ( keepInheritedRefs && menu.isInheritAsRef() )
        {
            return;
        }

        // we require child modules and reactors to process module menu
        if ( project.getModules().size() > 0 )
        {
            if ( menu.getName() == null )
            {
                menu.setName( i18n.getString( "site-tool", locale, "decorationModel.menu.projectmodules" ) );
            }

            for ( String module : (List<String>) project.getModules() )
            {
                MavenProject moduleProject = getModuleFromReactor( project, reactorProjects, module );

                if ( moduleProject == null )
                {
                    LOGGER.warn( "Module " + module
                        + " not found in reactor: loading locally" );

                    File f = new File( project.getBasedir(), module + "/pom.xml" );
                    if ( f.exists() )
                    {
                        try
                        {
                            ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
                            request.setLocalRepository( localRepository );

                            ProjectBuildingResult result = projectBuilder.build( f, request );
                            moduleProject = result.getProject();
                        }
                        catch ( ProjectBuildingException e )
                        {
                            throw new SiteToolException( "Unable to read local module POM", e );
                        }
                    }
                    else
                    {
                        LOGGER.warn( "No filesystem module POM available" );

                        moduleProject = new MavenProject();
                        moduleProject.setName( module );
                        moduleProject.setDistributionManagement( new DistributionManagement() );
                        moduleProject.getDistributionManagement().setSite( new Site() );
                        moduleProject.getDistributionManagement().getSite().setUrl( module );
                    }
                }

                String siteUrl = getDistMgmntSiteUrl( moduleProject );
                String itemName =
                    ( moduleProject.getName() == null ) ? moduleProject.getArtifactId() : moduleProject.getName();

                appendMenuItem( project, menu, itemName, siteUrl, moduleProject.getArtifactId() );
            }
        }
        else if ( decorationModel.getMenuRef( "modules" ).getInherit() == null )
        {
            // only remove if project has no modules AND menu is not inherited, see MSHARED-174
            decorationModel.removeMenuRef( "modules" );
        }
    }

    private static MavenProject getModuleFromReactor( MavenProject project, List<MavenProject> reactorProjects,
                                                      String module )
        throws IOException
    {
        File moduleBasedir = new File( project.getBasedir(), module ).getCanonicalFile();

        for ( MavenProject reactorProject : reactorProjects )
        {
            if ( moduleBasedir.equals( reactorProject.getBasedir() ) )
            {
                return reactorProject;
            }
        }

        // module not found in reactor
        return null;
    }

    /** {@inheritDoc} */
    public void populateReportsMenu( DecorationModel decorationModel, Locale locale,
                                     Map<String, List<MavenReport>> categories )
    {
        Objects.requireNonNull( decorationModel, "decorationModel cannot be null" );
        Objects.requireNonNull( locale, "locale cannot be null" );
        Objects.requireNonNull( categories, "categories cannot be null" );

        Menu menu = decorationModel.getMenuRef( "reports" );

        if ( menu == null )
        {
            return;
        }

        if ( menu.getName() == null )
        {
            menu.setName( i18n.getString( "site-tool", locale, "decorationModel.menu.projectdocumentation" ) );
        }

        boolean found = false;
        if ( menu.getItems().isEmpty() )
        {
            List<MavenReport> categoryReports = categories.get( MavenReport.CATEGORY_PROJECT_INFORMATION );
            if ( !isEmptyList( categoryReports ) )
            {
                MenuItem item = createCategoryMenu(
                                                    i18n.getString( "site-tool", locale,
                                                                    "decorationModel.menu.projectinformation" ),
                                                    "/project-info.html", categoryReports, locale );
                menu.getItems().add( item );
                found = true;
            }

            categoryReports = categories.get( MavenReport.CATEGORY_PROJECT_REPORTS );
            if ( !isEmptyList( categoryReports ) )
            {
                MenuItem item =
                    createCategoryMenu( i18n.getString( "site-tool", locale, "decorationModel.menu.projectreports" ),
                                        "/project-reports.html", categoryReports, locale );
                menu.getItems().add( item );
                found = true;
            }
        }
        if ( !found )
        {
            decorationModel.removeMenuRef( "reports" );
        }
    }

    /** {@inheritDoc} */
    public List<Locale> getSiteLocales( String locales )
    {
        if ( locales == null )
        {
            return Collections.singletonList( DEFAULT_LOCALE );
        }

        String[] localesArray = StringUtils.split( locales, "," );
        List<Locale> localesList = new ArrayList<Locale>( localesArray.length );
        List<Locale> availableLocales = Arrays.asList( Locale.getAvailableLocales() );

        for ( String localeString : localesArray )
        {
            Locale locale = codeToLocale( localeString );

            if ( locale == null )
            {
                continue;
            }

            if ( !availableLocales.contains( locale ) )
            {
                if ( LOGGER.isWarnEnabled() )
                {
                    LOGGER.warn( "The locale defined by '" + locale
                        + "' is not available in this Java Virtual Machine ("
                        + System.getProperty( "java.version" )
                        + " from " + System.getProperty( "java.vendor" ) + ") - IGNORING" );
                }
                continue;
            }

            Locale bundleLocale = i18n.getBundle( "site-tool", locale ).getLocale();
            if ( !( bundleLocale.equals( locale ) || bundleLocale.getLanguage().equals( locale.getLanguage() ) ) )
            {
                if ( LOGGER.isWarnEnabled() )
                {
                    LOGGER.warn( "The locale '" + locale + "' (" + locale.getDisplayName( Locale.ENGLISH )
                        + ") is not currently supported by Maven Site - IGNORING."
                        + System.lineSeparator() + "Contributions are welcome and greatly appreciated!"
                        + System.lineSeparator() + "If you want to contribute a new translation, please visit "
                        + "https://maven.apache.org/plugins/localization.html for detailed instructions." );
                }

                continue;
            }

            localesList.add( locale );
        }

        if ( localesList.isEmpty() )
        {
            localesList = Collections.singletonList( DEFAULT_LOCALE );
        }

        return localesList;
    }

    /**
     * Converts a locale code like "en", "en_US" or "en_US_win" to a <code>java.util.Locale</code>
     * object.
     * <p>If localeCode = <code>system</code>, return the current value of the default locale for this instance
     * of the Java Virtual Machine.</p>
     * <p>If localeCode = <code>default</code>, return the root locale.</p>
     *
     * @param localeCode the locale code string.
     * @return a java.util.Locale object instanced or null if errors occurred
     * @see Locale#getDefault()
     * @see SiteTool#DEFAULT_LOCALE
     */
    private Locale codeToLocale( String localeCode )
    {
        if ( localeCode == null )
        {
            return null;
        }

        if ( "system".equalsIgnoreCase( localeCode ) )
        {
            return Locale.getDefault();
        }

        if ( "default".equalsIgnoreCase( localeCode ) )
        {
            return SiteTool.DEFAULT_LOCALE;
        }

        String language = "";
        String country = "";
        String variant = "";

        StringTokenizer tokenizer = new StringTokenizer( localeCode, "_" );
        final int maxTokens = 3;
        if ( tokenizer.countTokens() > maxTokens )
        {
            if ( LOGGER.isWarnEnabled() )
            {
                LOGGER.warn( "Invalid java.util.Locale format for '" + localeCode + "' entry - IGNORING" );
            }
            return null;
        }

        if ( tokenizer.hasMoreTokens() )
        {
            language = tokenizer.nextToken();
            if ( tokenizer.hasMoreTokens() )
            {
                country = tokenizer.nextToken();
                if ( tokenizer.hasMoreTokens() )
                {
                    variant = tokenizer.nextToken();
                }
            }
        }

        return new Locale( language, country, variant );
    }

    // ----------------------------------------------------------------------
    // Protected methods
    // ----------------------------------------------------------------------

    /**
     * @param path could be null.
     * @return the path normalized, i.e. by eliminating "/../" and "/./" in the path.
     * @see FilenameUtils#normalize(String)
     */
    protected static String getNormalizedPath( String path )
    {
        String normalized = FilenameUtils.normalize( path );
        if ( normalized == null )
        {
            normalized = path;
        }
        return ( normalized == null ) ? null : normalized.replace( '\\', '/' );
    }

    // ----------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------

    /**
     * @param project not null
     * @param localRepository not null
     * @param repositories not null
     * @param locale not null
     * @return the resolved site descriptor
     * @throws IOException if any
     * @throws ArtifactResolutionException if any
     * @throws ArtifactNotFoundException if any
     */
    private File resolveSiteDescriptor( MavenProject project, ArtifactRepository localRepository,
                                        List<ArtifactRepository> repositories, Locale locale )
        throws IOException, ArtifactResolutionException, ArtifactNotFoundException
    {
        String variant = locale.getVariant();
        String country = locale.getCountry();
        String language = locale.getLanguage();

        Artifact artifact = null;
        File siteDescriptor = null;
        boolean found = false;

        if ( !variant.isEmpty() )
        {
            String localeStr = language + "_" + country + "_" + variant;
            // TODO: this is a bit crude - proper type, or proper handling as metadata rather than an artifact in 2.1?
            artifact = artifactFactory.createArtifactWithClassifier( project.getGroupId(),
                                                                     project.getArtifactId(),
                                                                     project.getVersion(), "xml",
                                                                     "site_" + localeStr );

            try
            {
                artifactResolver.resolve( artifact, repositories, localRepository );

                siteDescriptor = artifact.getFile();

                // we use zero length files to avoid re-resolution (see below)
                if ( siteDescriptor.length() > 0 )
                {
                    found = true;
                }
                else
                {
                    LOGGER.debug( "No site descriptor found for '" + project.getId() + "' for locale '"
                        + localeStr + "', trying without variant..." );
                }
            }
            catch ( ArtifactNotFoundException e )
            {
                LOGGER.debug( "Unable to locate site descriptor for locale '" + localeStr + "'", e );

                // we can afford to write an empty descriptor here as we don't expect it to turn up later in the
                // remote repository, because the parent was already released (and snapshots are updated
                // automatically if changed)
                siteDescriptor = new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );
                siteDescriptor.getParentFile().mkdirs();
                siteDescriptor.createNewFile();
            }
        }

        if ( !found && !country.isEmpty() )
        {
            String localeStr = language + "_" + country;
            // TODO: this is a bit crude - proper type, or proper handling as metadata rather than an artifact in 2.1?
            artifact = artifactFactory.createArtifactWithClassifier( project.getGroupId(),
                                                                     project.getArtifactId(),
                                                                     project.getVersion(), "xml",
                                                                     "site_" + localeStr );

            try
            {
                artifactResolver.resolve( artifact, repositories, localRepository );

                siteDescriptor = artifact.getFile();

                // we use zero length files to avoid re-resolution (see below)
                if ( siteDescriptor.length() > 0 )
                {
                    found = true;
                }
                else
                {
                    LOGGER.debug( "No site descriptor found for '" + project.getId() + "' for locale '"
                        + localeStr + "', trying without country..." );
                }
            }
            catch ( ArtifactNotFoundException e )
            {
                LOGGER.debug( "Unable to locate site descriptor for locale '" + localeStr + "'", e );

                // we can afford to write an empty descriptor here as we don't expect it to turn up later in the
                // remote repository, because the parent was already released (and snapshots are updated
                // automatically if changed)
                siteDescriptor = new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );
                siteDescriptor.getParentFile().mkdirs();
                siteDescriptor.createNewFile();
            }
        }


        if ( !found && !language.isEmpty() )
        {
            String localeStr = language;
            // TODO: this is a bit crude - proper type, or proper handling as metadata rather than an artifact in 2.1?
            artifact = artifactFactory.createArtifactWithClassifier( project.getGroupId(),
                                                                     project.getArtifactId(),
                                                                     project.getVersion(), "xml",
                                                                     "site_" + localeStr );

            try
            {
                artifactResolver.resolve( artifact, repositories, localRepository );

                siteDescriptor = artifact.getFile();

                // we use zero length files to avoid re-resolution (see below)
                if ( siteDescriptor.length() > 0 )
                {
                    found = true;
                }
                else
                {
                    LOGGER.debug( "No site descriptor found for '" + project.getId() + "' for locale '"
                        + localeStr + "', trying default locale..." );
                }
            }
            catch ( ArtifactNotFoundException e )
            {
                LOGGER.debug( "Unable to locate site descriptor for locale '" + localeStr + "'", e );

                // we can afford to write an empty descriptor here as we don't expect it to turn up later in the
                // remote repository, because the parent was already released (and snapshots are updated
                // automatically if changed)
                siteDescriptor = new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );
                siteDescriptor.getParentFile().mkdirs();
                siteDescriptor.createNewFile();
            }
        }

        if ( !found )
        {
            artifact = artifactFactory.createArtifactWithClassifier( project.getGroupId(),
                                                                     project.getArtifactId(),
                                                                     project.getVersion(), "xml",
                                                                     "site" );
            try
            {
                artifactResolver.resolve( artifact, repositories, localRepository );
            }
            catch ( ArtifactNotFoundException e )
            {
                // see above regarding this zero length file
                siteDescriptor = new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );
                siteDescriptor.getParentFile().mkdirs();
                siteDescriptor.createNewFile();

                throw e;
            }

            siteDescriptor = artifact.getFile();

            // we use zero length files to avoid re-resolution (see below)
            if ( siteDescriptor.length() == 0 )
            {
                LOGGER.debug( "No site descriptor found for '" + project.getId() + "' with default locale." );
                siteDescriptor = null;
            }
        }

        return siteDescriptor;
    }

    /**
     * @param depth depth of project
     * @param siteDirectory, can be null if project.basedir is null, ie POM from repository
     * @param locale not null
     * @param project not null
     * @param reactorProjects not null
     * @param localRepository not null
     * @param repositories not null
     * @return the decoration model depending the locale and the parent project
     * @throws SiteToolException if any
     */
    private Map.Entry<DecorationModel, MavenProject> getDecorationModel( int depth, File siteDirectory, Locale locale,
                                                                         MavenProject project,
                                                                         List<MavenProject> reactorProjects,
                                                                         ArtifactRepository localRepository,
                                                                         List<ArtifactRepository> repositories )
        throws SiteToolException
    {
        // 1. get site descriptor File
        File siteDescriptor;
        if ( project.getBasedir() == null )
        {
            // POM is in the repository: look into the repository for site descriptor
            try
            {
                siteDescriptor = getSiteDescriptorFromRepository( project, localRepository, repositories, locale );
            }
            catch ( SiteToolException e )
            {
                throw new SiteToolException( "The site descriptor cannot be resolved from the repository", e );
            }
        }
        else
        {
            // POM is in build directory: look for site descriptor as local file
            siteDescriptor = getSiteDescriptor( siteDirectory, locale );
        }

        // 2. read DecorationModel from site descriptor File and do early interpolation (${this.*})
        DecorationModel decorationModel = null;
        Reader siteDescriptorReader = null;
        try
        {
            if ( siteDescriptor != null && siteDescriptor.exists() )
            {
                LOGGER.debug( "Reading" + ( depth == 0 ? "" : ( " parent level " + depth ) )
                    + " site descriptor from " + siteDescriptor );

                siteDescriptorReader = ReaderFactory.newXmlReader( siteDescriptor );

                String siteDescriptorContent = IOUtil.toString( siteDescriptorReader );

                // interpolate ${this.*} = early interpolation
                siteDescriptorContent = getInterpolatedSiteDescriptorContent( project, siteDescriptorContent, true );

                decorationModel = readDecorationModel( siteDescriptorContent );
                decorationModel.setLastModified( siteDescriptor.lastModified() );
            }
            else
            {
                LOGGER.debug( "No" + ( depth == 0 ? "" : ( " parent level " + depth ) ) + " site descriptor." );
            }
        }
        catch ( IOException e )
        {
            throw new SiteToolException( "The site descriptor for '" + project.getId() + "' cannot be read from "
                + siteDescriptor, e );
        }
        finally
        {
            IOUtil.close( siteDescriptorReader );
        }

        // 3. look for parent project
        MavenProject parentProject = getParentProject( project, reactorProjects, localRepository );

        // 4. merge with parent project DecorationModel
        if ( parentProject != null && ( decorationModel == null || decorationModel.isMergeParent() ) )
        {
            depth++;
            LOGGER.debug( "Looking for site descriptor of level " + depth + " parent project: "
                + parentProject.getId() );

            File parentSiteDirectory = null;
            if ( parentProject.getBasedir() != null )
            {
                // extrapolate parent project site directory
                String siteRelativePath = getRelativeFilePath( project.getBasedir().getAbsolutePath(),
                                                               siteDescriptor.getParentFile().getAbsolutePath() );

                parentSiteDirectory = new File( parentProject.getBasedir(), siteRelativePath );
                // notice: using same siteRelativePath for parent as current project; may be wrong if site plugin
                // has different configuration. But this is a rare case (this only has impact if parent is from reactor)
            }

            DecorationModel parentDecorationModel =
                getDecorationModel( depth, parentSiteDirectory, locale, parentProject, reactorProjects, localRepository,
                                    repositories ).getKey();

            // MSHARED-116 requires an empty decoration model (instead of a null one)
            // MSHARED-145 requires us to do this only if there is a parent to merge it with
            if ( decorationModel == null && parentDecorationModel != null )
            {
                // we have no site descriptor: merge the parent into an empty one because the default one
                // (default-site.xml) will break menu and breadcrumb composition.
                decorationModel = new DecorationModel();
            }

            String name = project.getName();
            if ( decorationModel != null && StringUtils.isNotEmpty( decorationModel.getName() ) )
            {
                name = decorationModel.getName();
            }

            // Merge the parent and child DecorationModels
            String projectDistMgmnt = getDistMgmntSiteUrl( project );
            String parentDistMgmnt = getDistMgmntSiteUrl( parentProject );
            if ( LOGGER.isDebugEnabled() )
            {
                LOGGER.debug( "Site decoration model inheritance: assembling child with level " + depth
                    + " parent: distributionManagement.site.url child = " + projectDistMgmnt + " and parent = "
                    + parentDistMgmnt );
            }
            assembler.assembleModelInheritance( name, decorationModel, parentDecorationModel, projectDistMgmnt,
                                                parentDistMgmnt == null ? projectDistMgmnt : parentDistMgmnt );
        }

        return new AbstractMap.SimpleEntry<DecorationModel, MavenProject>( decorationModel, parentProject );
    }

    /**
     * @param siteDescriptorContent not null
     * @return the decoration model object
     * @throws SiteToolException if any
     */
    private DecorationModel readDecorationModel( String siteDescriptorContent )
        throws SiteToolException
    {
        try
        {
            return new DecorationXpp3Reader().read( new StringReader( siteDescriptorContent ) );
        }
        catch ( XmlPullParserException e )
        {
            throw new SiteToolException( "Error parsing site descriptor", e );
        }
        catch ( IOException e )
        {
            throw new SiteToolException( "Error reading site descriptor", e );
        }
    }

    private DecorationModel getDefaultDecorationModel()
        throws SiteToolException
    {
        String siteDescriptorContent;

        Reader reader = null;
        try
        {
            reader = ReaderFactory.newXmlReader( getClass().getResourceAsStream( "/default-site.xml" ) );
            siteDescriptorContent = IOUtil.toString( reader );
        }
        catch ( IOException e )
        {
            throw new SiteToolException( "Error reading default site descriptor", e );
        }
        finally
        {
            IOUtil.close( reader );
        }

        return readDecorationModel( siteDescriptorContent );
    }

    private String decorationModelToString( DecorationModel decoration )
        throws SiteToolException
    {
        StringWriter writer = new StringWriter();

        try
        {
            new DecorationXpp3Writer().write( writer, decoration );
            return writer.toString();
        }
        catch ( IOException e )
        {
            throw new SiteToolException( "Error reading site descriptor", e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    private static String buildRelativePath( final String toPath,  final String fromPath, final char separatorChar )
    {
        // use tokenizer to traverse paths and for lazy checking
        StringTokenizer toTokeniser = new StringTokenizer( toPath, String.valueOf( separatorChar ) );
        StringTokenizer fromTokeniser = new StringTokenizer( fromPath, String.valueOf( separatorChar ) );

        int count = 0;

        // walk along the to path looking for divergence from the from path
        while ( toTokeniser.hasMoreTokens() && fromTokeniser.hasMoreTokens() )
        {
            if ( separatorChar == '\\' )
            {
                if ( !fromTokeniser.nextToken().equalsIgnoreCase( toTokeniser.nextToken() ) )
                {
                    break;
                }
            }
            else
            {
                if ( !fromTokeniser.nextToken().equals( toTokeniser.nextToken() ) )
                {
                    break;
                }
            }

            count++;
        }

        // reinitialize the tokenizers to count positions to retrieve the
        // gobbled token

        toTokeniser = new StringTokenizer( toPath, String.valueOf( separatorChar ) );
        fromTokeniser = new StringTokenizer( fromPath, String.valueOf( separatorChar ) );

        while ( count-- > 0 )
        {
            fromTokeniser.nextToken();
            toTokeniser.nextToken();
        }

        StringBuilder relativePath = new StringBuilder();

        // add back refs for the rest of from location.
        while ( fromTokeniser.hasMoreTokens() )
        {
            fromTokeniser.nextToken();

            relativePath.append( ".." );

            if ( fromTokeniser.hasMoreTokens() )
            {
                relativePath.append( separatorChar );
            }
        }

        if ( relativePath.length() != 0 && toTokeniser.hasMoreTokens() )
        {
            relativePath.append( separatorChar );
        }

        // add fwd fills for whatever's left of to.
        while ( toTokeniser.hasMoreTokens() )
        {
            relativePath.append( toTokeniser.nextToken() );

            if ( toTokeniser.hasMoreTokens() )
            {
                relativePath.append( separatorChar );
            }
        }
        return relativePath.toString();
    }

    /**
     * @param project not null
     * @param menu not null
     * @param name not null
     * @param href could be null
     * @param defaultHref not null
     */
    private void appendMenuItem( MavenProject project, Menu menu, String name, String href, String defaultHref )
    {
        String selectedHref = href;

        if ( selectedHref == null )
        {
            selectedHref = defaultHref;
        }

        MenuItem item = new MenuItem();
        item.setName( name );

        String baseUrl = getDistMgmntSiteUrl( project );
        if ( baseUrl != null )
        {
            selectedHref = getRelativePath( selectedHref, baseUrl );
        }

        if ( selectedHref.endsWith( "/" ) )
        {
            item.setHref( selectedHref + "index.html" );
        }
        else
        {
            item.setHref( selectedHref + "/index.html" );
        }
        menu.addItem( item );
    }

    /**
     * @param name not null
     * @param href not null
     * @param categoryReports not null
     * @param locale not null
     * @return the menu item object
     */
    private MenuItem createCategoryMenu( String name, String href, List<MavenReport> categoryReports, Locale locale )
    {
        MenuItem item = new MenuItem();
        item.setName( name );
        item.setCollapse( true );
        item.setHref( href );

        // MSHARED-172, allow reports to define their order in some other way?
        //Collections.sort( categoryReports, new ReportComparator( locale ) );

        for ( MavenReport report : categoryReports )
        {
            MenuItem subitem = new MenuItem();
            subitem.setName( report.getName( locale ) );
            subitem.setHref( report.getOutputName() + ".html" );
            item.getItems().add( subitem );
        }

        return item;
    }

    // ----------------------------------------------------------------------
    // static methods
    // ----------------------------------------------------------------------

    /**
     * Convenience method.
     *
     * @param list could be null
     * @return true if the list is <code>null</code> or empty
     */
    private static boolean isEmptyList( List<?> list )
    {
        return list == null || list.isEmpty();
    }

    /**
     * Return distributionManagement.site.url if defined, null otherwise.
     *
     * @param project not null
     * @return could be null
     */
    private static String getDistMgmntSiteUrl( MavenProject project )
    {
        return getDistMgmntSiteUrl( project.getDistributionManagement() );
    }

    private static String getDistMgmntSiteUrl( DistributionManagement distMgmnt )
    {
        if ( distMgmnt != null && distMgmnt.getSite() != null && distMgmnt.getSite().getUrl() != null )
        {
            // TODO This needs to go, it is just logically wrong
            return urlEncode( distMgmnt.getSite().getUrl() );
        }

        return null;
    }

    private static String urlEncode( final String url )
    {
        if ( url == null )
        {
            return null;
        }

        try
        {
            return new File( url ).toURI().toURL().toExternalForm();
        }
        catch ( MalformedURLException ex )
        {
            return url; // this will then throw somewhere else
        }
    }
}
