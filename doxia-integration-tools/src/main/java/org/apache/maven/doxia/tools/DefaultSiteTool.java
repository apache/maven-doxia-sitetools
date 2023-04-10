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
package org.apache.maven.doxia.tools;

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
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.doxia.site.Menu;
import org.apache.maven.doxia.site.MenuItem;
import org.apache.maven.doxia.site.SiteModel;
import org.apache.maven.doxia.site.Skin;
import org.apache.maven.doxia.site.inheritance.SiteModelInheritanceAssembler;
import org.apache.maven.doxia.site.io.xpp3.SiteXpp3Reader;
import org.apache.maven.doxia.site.io.xpp3.SiteXpp3Writer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.reporting.MavenReport;
import org.codehaus.plexus.i18n.I18N;
import org.codehaus.plexus.interpolation.EnvarBasedValueSource;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.InterpolationPostProcessor;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.PrefixedObjectValueSource;
import org.codehaus.plexus.interpolation.PrefixedPropertiesValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the site tool.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
@Singleton
@Named
public class DefaultSiteTool implements SiteTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSiteTool.class);

    // ----------------------------------------------------------------------
    // Components
    // ----------------------------------------------------------------------

    /**
     * The component that is used to resolve additional required artifacts.
     */
    @Inject
    protected RepositorySystem repositorySystem;

    /**
     * The component used for getting artifact handlers.
     */
    @Inject
    private ArtifactHandlerManager artifactHandlerManager;

    /**
     * Internationalization.
     */
    @Inject
    protected I18N i18n;

    /**
     * The component for assembling inheritance.
     */
    @Inject
    protected SiteModelInheritanceAssembler assembler;

    /**
     * Project builder.
     */
    @Inject
    protected ProjectBuilder projectBuilder;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public Artifact getSkinArtifactFromRepository(
            RepositorySystemSession repoSession, List<RemoteRepository> remoteProjectRepositories, Skin skin)
            throws SiteToolException {
        Objects.requireNonNull(repoSession, "repoSession cannot be null");
        Objects.requireNonNull(remoteProjectRepositories, "remoteProjectRepositories cannot be null");
        Objects.requireNonNull(skin, "skin cannot be null");

        String version = skin.getVersion();
        try {
            if (version == null) {
                version = Artifact.RELEASE_VERSION;
            }
            VersionRange versionSpec = VersionRange.createFromVersionSpec(version);
            String type = "jar";
            Artifact artifact = new DefaultArtifact(
                    skin.getGroupId(),
                    skin.getArtifactId(),
                    versionSpec,
                    Artifact.SCOPE_RUNTIME,
                    type,
                    null,
                    artifactHandlerManager.getArtifactHandler(type));
            ArtifactRequest request =
                    new ArtifactRequest(RepositoryUtils.toArtifact(artifact), remoteProjectRepositories, "remote-skin");
            ArtifactResult result = repositorySystem.resolveArtifact(repoSession, request);

            return RepositoryUtils.toArtifact(result.getArtifact());
        } catch (InvalidVersionSpecificationException e) {
            throw new SiteToolException("The skin version '" + version + "' is not valid", e);
        } catch (ArtifactResolutionException e) {
            if (e.getCause() instanceof ArtifactNotFoundException) {
                throw new SiteToolException("The skin does not exist", e.getCause());
            }

            throw new SiteToolException("Unable to find skin", e);
        }
    }

    /**
     * This method is not implemented according to the URI specification and has many weird
     * corner cases where it doesn't do the right thing. Please consider using a better
     * implemented method from a different library such as org.apache.http.client.utils.URIUtils#resolve.
     */
    @Deprecated
    public String getRelativePath(String to, String from) {
        Objects.requireNonNull(to, "to cannot be null");
        Objects.requireNonNull(from, "from cannot be null");

        if (to.contains(":") && from.contains(":")) {
            String toScheme = to.substring(0, to.lastIndexOf(':'));
            String fromScheme = from.substring(0, from.lastIndexOf(':'));
            if (!toScheme.equals(fromScheme)) {
                return to;
            }
        }

        URL toUrl = null;
        URL fromUrl = null;

        String toPath = to;
        String fromPath = from;

        try {
            toUrl = new URL(to);
        } catch (MalformedURLException e) {
            try {
                toUrl = new File(getNormalizedPath(to)).toURI().toURL();
            } catch (MalformedURLException e1) {
                LOGGER.warn("Unable to load a URL for '" + to + "'", e);
                return to;
            }
        }

        try {
            fromUrl = new URL(from);
        } catch (MalformedURLException e) {
            try {
                fromUrl = new File(getNormalizedPath(from)).toURI().toURL();
            } catch (MalformedURLException e1) {
                LOGGER.warn("Unable to load a URL for '" + from + "'", e);
                return to;
            }
        }

        if (toUrl != null && fromUrl != null) {
            // URLs, determine if they share protocol and domain info

            if ((toUrl.getProtocol().equalsIgnoreCase(fromUrl.getProtocol()))
                    && (toUrl.getHost().equalsIgnoreCase(fromUrl.getHost()))
                    && (toUrl.getPort() == fromUrl.getPort())) {
                // shared URL domain details, use URI to determine relative path

                toPath = toUrl.getFile();
                fromPath = fromUrl.getFile();
            } else {
                // don't share basic URL information, no relative available

                return to;
            }
        } else if ((toUrl != null && fromUrl == null) || (toUrl == null && fromUrl != null)) {
            // one is a URL and the other isn't, no relative available.

            return to;
        }

        // either the two locations are not URLs or if they are they
        // share the common protocol and domain info and we are left
        // with their URI information

        String relativePath = getRelativeFilePath(fromPath, toPath);

        if (relativePath == null) {
            relativePath = to;
        }

        if (LOGGER.isDebugEnabled() && !relativePath.toString().equals(to)) {
            LOGGER.debug("Mapped url: " + to + " to relative path: " + relativePath);
        }

        return relativePath;
    }

    private static String getRelativeFilePath(final String oldPath, final String newPath) {
        // normalize the path delimiters

        String fromPath = new File(oldPath).getPath();
        String toPath = new File(newPath).getPath();

        // strip any leading slashes if its a windows path
        if (toPath.matches("^\\[a-zA-Z]:")) {
            toPath = toPath.substring(1);
        }
        if (fromPath.matches("^\\[a-zA-Z]:")) {
            fromPath = fromPath.substring(1);
        }

        // lowercase windows drive letters.
        if (fromPath.startsWith(":", 1)) {
            fromPath = Character.toLowerCase(fromPath.charAt(0)) + fromPath.substring(1);
        }
        if (toPath.startsWith(":", 1)) {
            toPath = Character.toLowerCase(toPath.charAt(0)) + toPath.substring(1);
        }

        // check for the presence of windows drives. No relative way of
        // traversing from one to the other.

        if ((toPath.startsWith(":", 1) && fromPath.startsWith(":", 1))
                && (!toPath.substring(0, 1).equals(fromPath.substring(0, 1)))) {
            // they both have drive path element but they don't match, no
            // relative path

            return null;
        }

        if ((toPath.startsWith(":", 1) && !fromPath.startsWith(":", 1))
                || (!toPath.startsWith(":", 1) && fromPath.startsWith(":", 1))) {

            // one has a drive path element and the other doesn't, no relative
            // path.

            return null;
        }

        final String relativePath = buildRelativePath(toPath, fromPath, File.separatorChar);

        return relativePath.toString();
    }

    /** {@inheritDoc} */
    public File getSiteDescriptor(File siteDirectory, Locale locale) {
        Objects.requireNonNull(siteDirectory, "siteDirectory cannot be null");
        Objects.requireNonNull(locale, "locale cannot be null");

        String variant = locale.getVariant();
        String country = locale.getCountry();
        String language = locale.getLanguage();

        File siteDescriptor = null;

        if (!variant.isEmpty()) {
            siteDescriptor = new File(siteDirectory, "site_" + language + "_" + country + "_" + variant + ".xml");
        }

        if ((siteDescriptor == null || !siteDescriptor.isFile()) && !country.isEmpty()) {
            siteDescriptor = new File(siteDirectory, "site_" + language + "_" + country + ".xml");
        }

        if ((siteDescriptor == null || !siteDescriptor.isFile()) && !language.isEmpty()) {
            siteDescriptor = new File(siteDirectory, "site_" + language + ".xml");
        }

        if (siteDescriptor == null || !siteDescriptor.isFile()) {
            siteDescriptor = new File(siteDirectory, "site.xml");
        }

        return siteDescriptor;
    }

    /**
     * Get a site descriptor from one of the repositories.
     *
     * @param project the Maven project, not null.
     * @param repoSession the repository system session, not null.
     * @param remoteProjectRepositories the Maven remote project repositories, not null.
     * @param locale the locale wanted for the site descriptor, not null.
     * See {@link #getSiteDescriptor(File, Locale)} for details.
     * @return the site descriptor into the local repository after download of it from repositories or null if not
     * found in repositories.
     * @throws SiteToolException if any
     */
    File getSiteDescriptorFromRepository(
            MavenProject project,
            RepositorySystemSession repoSession,
            List<RemoteRepository> remoteProjectRepositories,
            Locale locale)
            throws SiteToolException {
        Objects.requireNonNull(project, "project cannot be null");
        Objects.requireNonNull(repoSession, "repoSession cannot be null");
        Objects.requireNonNull(remoteProjectRepositories, "remoteProjectRepositories cannot be null");
        Objects.requireNonNull(locale, "locale cannot be null");

        try {
            return resolveSiteDescriptor(project, repoSession, remoteProjectRepositories, locale);
        } catch (ArtifactNotFoundException e) {
            LOGGER.debug("Unable to locate site descriptor", e);
            return null;
        } catch (ArtifactResolutionException e) {
            throw new SiteToolException("Unable to locate site descriptor", e);
        } catch (IOException e) {
            throw new SiteToolException("Unable to locate site descriptor", e);
        }
    }

    /** {@inheritDoc} */
    public SiteModel getSiteModel(
            File siteDirectory,
            Locale locale,
            MavenProject project,
            List<MavenProject> reactorProjects,
            RepositorySystemSession repoSession,
            List<RemoteRepository> remoteProjectRepositories)
            throws SiteToolException {
        Objects.requireNonNull(locale, "locale cannot be null");
        Objects.requireNonNull(project, "project cannot be null");
        Objects.requireNonNull(reactorProjects, "reactorProjects cannot be null");
        Objects.requireNonNull(repoSession, "repoSession cannot be null");
        Objects.requireNonNull(remoteProjectRepositories, "remoteProjectRepositories cannot be null");

        LOGGER.debug("Computing site model of '" + project.getId() + "' for "
                + (locale.equals(SiteTool.DEFAULT_LOCALE) ? "default locale" : "locale '" + locale + "'"));

        Map.Entry<SiteModel, MavenProject> result =
                getSiteModel(0, siteDirectory, locale, project, repoSession, remoteProjectRepositories);
        SiteModel siteModel = result.getKey();
        MavenProject parentProject = result.getValue();

        if (siteModel == null) {
            LOGGER.debug("Using default site descriptor");
            siteModel = getDefaultSiteModel();
        }

        // SiteModel back to String to interpolate, then go back to SiteModel
        String siteDescriptorContent = siteModelToString(siteModel);

        // "classical" late interpolation, after full inheritance
        siteDescriptorContent = getInterpolatedSiteDescriptorContent(project, siteDescriptorContent, false);

        siteModel = readSiteModel(siteDescriptorContent);

        if (parentProject != null) {
            populateParentMenu(siteModel, locale, project, parentProject, true);
        }

        try {
            populateModulesMenu(siteModel, locale, project, reactorProjects, true);
        } catch (IOException e) {
            throw new SiteToolException("Error while populating modules menu", e);
        }

        return siteModel;
    }

    /** {@inheritDoc} */
    public String getInterpolatedSiteDescriptorContent(
            Map<String, String> props, MavenProject aProject, String siteDescriptorContent) throws SiteToolException {
        Objects.requireNonNull(props, "props cannot be null");

        // "classical" late interpolation
        return getInterpolatedSiteDescriptorContent(aProject, siteDescriptorContent, false);
    }

    private String getInterpolatedSiteDescriptorContent(
            MavenProject aProject, String siteDescriptorContent, boolean isEarly) throws SiteToolException {
        Objects.requireNonNull(aProject, "aProject cannot be null");
        Objects.requireNonNull(siteDescriptorContent, "siteDescriptorContent cannot be null");

        RegexBasedInterpolator interpolator = new RegexBasedInterpolator();

        if (isEarly) {
            interpolator.addValueSource(new PrefixedObjectValueSource("this.", aProject));
            interpolator.addValueSource(new PrefixedPropertiesValueSource("this.", aProject.getProperties()));
        } else {
            interpolator.addValueSource(new PrefixedObjectValueSource("project.", aProject));
            interpolator.addValueSource(new MapBasedValueSource(aProject.getProperties()));

            try {
                interpolator.addValueSource(new EnvarBasedValueSource());
            } catch (IOException e) {
                // Prefer logging?
                throw new SiteToolException("Cannot interpolate environment properties", e);
            }
        }

        interpolator.addPostProcessor(new InterpolationPostProcessor() {
            @Override
            public Object execute(String expression, Object value) {
                if (value != null) {
                    // we're going to parse this back in as XML so we need to escape XML markup
                    return value.toString()
                            .replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;")
                            .replace("\"", "&quot;")
                            .replace("'", "&apos;");
                }
                return null;
            }
        });

        try {
            return interpolator.interpolate(siteDescriptorContent);
        } catch (InterpolationException e) {
            throw new SiteToolException("Cannot interpolate site descriptor", e);
        }
    }

    /**
     * Populate the pre-defined <code>parent</code> menu of the site model,
     * if used through <code>&lt;menu ref="parent"/&gt;</code>.
     *
     * @param siteModel the Doxia Sitetools SiteModel, not null.
     * @param locale the locale used for the i18n in SiteModel, not null.
     * @param project a Maven project, not null.
     * @param parentProject a Maven parent project, not null.
     * @param keepInheritedRefs used for inherited references.
     */
    private void populateParentMenu(
            SiteModel siteModel,
            Locale locale,
            MavenProject project,
            MavenProject parentProject,
            boolean keepInheritedRefs) {
        Objects.requireNonNull(siteModel, "siteModel cannot be null");
        Objects.requireNonNull(locale, "locale cannot be null");
        Objects.requireNonNull(project, "project cannot be null");
        Objects.requireNonNull(parentProject, "parentProject cannot be null");

        Menu menu = siteModel.getMenuRef("parent");

        if (menu == null) {
            return;
        }

        if (keepInheritedRefs && menu.isInheritAsRef()) {
            return;
        }

        String parentUrl = getDistMgmntSiteUrl(parentProject);

        if (parentUrl != null) {
            if (parentUrl.endsWith("/")) {
                parentUrl += "index.html";
            } else {
                parentUrl += "/index.html";
            }

            parentUrl = getRelativePath(parentUrl, getDistMgmntSiteUrl(project));
        } else {
            // parent has no url, assume relative path is given by site structure
            File parentBasedir = parentProject.getBasedir();
            // First make sure that the parent is available on the file system
            if (parentBasedir != null) {
                // Try to find the relative path to the parent via the file system
                String parentPath = parentBasedir.getAbsolutePath();
                String projectPath = project.getBasedir().getAbsolutePath();
                parentUrl = getRelativePath(parentPath, projectPath) + "/index.html";
            }
        }

        // Only add the parent menu if we were able to find a URL for it
        if (parentUrl == null) {
            LOGGER.warn("Unable to find a URL to the parent project. The parent menu will NOT be added.");
        } else {
            if (menu.getName() == null) {
                menu.setName(i18n.getString("site-tool", locale, "siteModel.menu.parentproject"));
            }

            MenuItem item = new MenuItem();
            item.setName(parentProject.getName());
            item.setHref(parentUrl);
            menu.addItem(item);
        }
    }

    /**
     * Populate the pre-defined <code>modules</code> menu of the model,
     * if used through <code>&lt;menu ref="modules"/&gt;</code>.
     *
     * @param siteModel the Doxia Sitetools SiteModel, not null.
     * @param locale the locale used for the i18n in SiteModel, not null.
     * @param project a Maven project, not null.
     * @param reactorProjects the Maven reactor projects, not null.
     * @param keepInheritedRefs used for inherited references.
     * @throws SiteToolException if any
     * @throws IOException
     */
    private void populateModulesMenu(
            SiteModel siteModel,
            Locale locale,
            MavenProject project,
            List<MavenProject> reactorProjects,
            boolean keepInheritedRefs)
            throws SiteToolException, IOException {
        Objects.requireNonNull(siteModel, "siteModel cannot be null");
        Objects.requireNonNull(locale, "locale cannot be null");
        Objects.requireNonNull(project, "project cannot be null");
        Objects.requireNonNull(reactorProjects, "reactorProjects cannot be null");

        Menu menu = siteModel.getMenuRef("modules");

        if (menu == null) {
            return;
        }

        if (keepInheritedRefs && menu.isInheritAsRef()) {
            return;
        }

        // we require child modules and reactors to process module menu
        if (!project.getModules().isEmpty()) {
            if (menu.getName() == null) {
                menu.setName(i18n.getString("site-tool", locale, "siteModel.menu.projectmodules"));
            }

            for (String module : project.getModules()) {
                MavenProject moduleProject = getModuleFromReactor(project, reactorProjects, module);

                if (moduleProject == null) {
                    LOGGER.debug("Module " + module + " not found in reactor");
                    continue;
                }

                final String pluginId = "org.apache.maven.plugins:maven-site-plugin";
                String skipFlag = getPluginParameter(moduleProject, pluginId, "skip");
                if (skipFlag == null) {
                    skipFlag = moduleProject.getProperties().getProperty("maven.site.skip");
                }

                String siteUrl = "true".equalsIgnoreCase(skipFlag) ? null : getDistMgmntSiteUrl(moduleProject);
                String itemName =
                        (moduleProject.getName() == null) ? moduleProject.getArtifactId() : moduleProject.getName();
                String defaultSiteUrl = "true".equalsIgnoreCase(skipFlag) ? null : moduleProject.getArtifactId();

                appendMenuItem(project, menu, itemName, siteUrl, defaultSiteUrl);
            }
        } else if (siteModel.getMenuRef("modules").getInherit() == null) {
            // only remove if project has no modules AND menu is not inherited, see MSHARED-174
            siteModel.removeMenuRef("modules");
        }
    }

    private MavenProject getModuleFromReactor(MavenProject project, List<MavenProject> reactorProjects, String module)
            throws IOException {
        File moduleBasedir = new File(project.getBasedir(), module).getCanonicalFile();

        for (MavenProject reactorProject : reactorProjects) {
            if (moduleBasedir.equals(reactorProject.getBasedir())) {
                return reactorProject;
            }
        }

        // module not found in reactor
        return null;
    }

    /** {@inheritDoc} */
    public void populateReportsMenu(SiteModel siteModel, Locale locale, Map<String, List<MavenReport>> categories) {
        Objects.requireNonNull(siteModel, "siteModel cannot be null");
        Objects.requireNonNull(locale, "locale cannot be null");
        Objects.requireNonNull(categories, "categories cannot be null");

        Menu menu = siteModel.getMenuRef("reports");

        if (menu == null) {
            return;
        }

        if (menu.getName() == null) {
            menu.setName(i18n.getString("site-tool", locale, "siteModel.menu.projectdocumentation"));
        }

        boolean found = false;
        if (menu.getItems().isEmpty()) {
            List<MavenReport> categoryReports = categories.get(MavenReport.CATEGORY_PROJECT_INFORMATION);
            if (!isEmptyList(categoryReports)) {
                MenuItem item = createCategoryMenu(
                        i18n.getString("site-tool", locale, "siteModel.menu.projectinformation"),
                        "/project-info.html",
                        categoryReports,
                        locale);
                menu.getItems().add(item);
                found = true;
            }

            categoryReports = categories.get(MavenReport.CATEGORY_PROJECT_REPORTS);
            if (!isEmptyList(categoryReports)) {
                MenuItem item = createCategoryMenu(
                        i18n.getString("site-tool", locale, "siteModel.menu.projectreports"),
                        "/project-reports.html",
                        categoryReports,
                        locale);
                menu.getItems().add(item);
                found = true;
            }
        }
        if (!found) {
            siteModel.removeMenuRef("reports");
        }
    }

    /** {@inheritDoc} */
    public List<Locale> getSiteLocales(String locales) {
        if (locales == null) {
            return Collections.singletonList(DEFAULT_LOCALE);
        }

        String[] localesArray = StringUtils.split(locales, ",");
        List<Locale> localesList = new ArrayList<Locale>(localesArray.length);
        List<Locale> availableLocales = Arrays.asList(Locale.getAvailableLocales());

        for (String localeString : localesArray) {
            Locale locale = codeToLocale(localeString);

            if (locale == null) {
                continue;
            }

            if (!availableLocales.contains(locale)) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("The locale defined by '" + locale
                            + "' is not available in this Java Virtual Machine ("
                            + System.getProperty("java.version")
                            + " from " + System.getProperty("java.vendor") + ") - IGNORING");
                }
                continue;
            }

            Locale bundleLocale = i18n.getBundle("site-tool", locale).getLocale();
            if (!(bundleLocale.equals(locale) || bundleLocale.getLanguage().equals(locale.getLanguage()))) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("The locale '" + locale + "' (" + locale.getDisplayName(Locale.ENGLISH)
                            + ") is not currently supported by Maven Site - IGNORING."
                            + System.lineSeparator() + "Contributions are welcome and greatly appreciated!"
                            + System.lineSeparator() + "If you want to contribute a new translation, please visit "
                            + "https://maven.apache.org/plugins/localization.html for detailed instructions.");
                }

                continue;
            }

            localesList.add(locale);
        }

        if (localesList.isEmpty()) {
            localesList = Collections.singletonList(DEFAULT_LOCALE);
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
    private Locale codeToLocale(String localeCode) {
        if (localeCode == null) {
            return null;
        }

        if ("system".equalsIgnoreCase(localeCode)) {
            return Locale.getDefault();
        }

        if ("default".equalsIgnoreCase(localeCode)) {
            return SiteTool.DEFAULT_LOCALE;
        }

        String language = "";
        String country = "";
        String variant = "";

        StringTokenizer tokenizer = new StringTokenizer(localeCode, "_");
        final int maxTokens = 3;
        if (tokenizer.countTokens() > maxTokens) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Invalid java.util.Locale format for '" + localeCode + "' entry - IGNORING");
            }
            return null;
        }

        if (tokenizer.hasMoreTokens()) {
            language = tokenizer.nextToken();
            if (tokenizer.hasMoreTokens()) {
                country = tokenizer.nextToken();
                if (tokenizer.hasMoreTokens()) {
                    variant = tokenizer.nextToken();
                }
            }
        }

        return new Locale(language, country, variant);
    }

    // ----------------------------------------------------------------------
    // Protected methods
    // ----------------------------------------------------------------------

    /**
     * @param path could be null.
     * @return the path normalized, i.e. by eliminating "/../" and "/./" in the path.
     * @see FilenameUtils#normalize(String)
     */
    protected static String getNormalizedPath(String path) {
        String normalized = FilenameUtils.normalize(path);
        if (normalized == null) {
            normalized = path;
        }
        return (normalized == null) ? null : normalized.replace('\\', '/');
    }

    // ----------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------

    /**
     * @param project not null
     * @param localeStr not null
     * @param remoteProjectRepositories not null
     * @return the site descriptor artifact request
     */
    private ArtifactRequest createSiteDescriptorArtifactRequest(
            MavenProject project, String localeStr, List<RemoteRepository> remoteProjectRepositories) {
        String type = "xml";
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler(type);
        Artifact artifact = new DefaultArtifact(
                project.getGroupId(),
                project.getArtifactId(),
                project.getVersion(),
                Artifact.SCOPE_RUNTIME,
                type,
                "site" + (localeStr.isEmpty() ? "" : "_" + localeStr),
                artifactHandler);
        return new ArtifactRequest(
                RepositoryUtils.toArtifact(artifact), remoteProjectRepositories, "remote-site-descriptor");
    }

    /**
     * @param project not null
     * @param repoSession the repository system session not null
     * @param remoteProjectRepositories not null
     * @param locale not null
     * @return the resolved site descriptor
     * @throws IOException if any
     * @throws ArtifactResolutionException if any
     * @throws ArtifactNotFoundException if any
     */
    private File resolveSiteDescriptor(
            MavenProject project,
            RepositorySystemSession repoSession,
            List<RemoteRepository> remoteProjectRepositories,
            Locale locale)
            throws IOException, ArtifactResolutionException, ArtifactNotFoundException {
        String variant = locale.getVariant();
        String country = locale.getCountry();
        String language = locale.getLanguage();

        String localeStr = null;
        File siteDescriptor = null;
        boolean found = false;

        if (!variant.isEmpty()) {
            localeStr = language + "_" + country + "_" + variant;
            ArtifactRequest request =
                    createSiteDescriptorArtifactRequest(project, localeStr, remoteProjectRepositories);

            try {
                ArtifactResult result = repositorySystem.resolveArtifact(repoSession, request);

                siteDescriptor = result.getArtifact().getFile();
                found = true;
            } catch (ArtifactResolutionException e) {
                if (e.getCause() instanceof ArtifactNotFoundException) {
                    LOGGER.debug("No site descriptor found for '" + project.getId() + "' for locale '" + localeStr
                            + "', trying without variant...");
                } else {
                    throw e;
                }
            }
        }

        if (!found && !country.isEmpty()) {
            localeStr = language + "_" + country;
            ArtifactRequest request =
                    createSiteDescriptorArtifactRequest(project, localeStr, remoteProjectRepositories);

            try {
                ArtifactResult result = repositorySystem.resolveArtifact(repoSession, request);

                siteDescriptor = result.getArtifact().getFile();
                found = true;
            } catch (ArtifactResolutionException e) {
                if (e.getCause() instanceof ArtifactNotFoundException) {
                    LOGGER.debug("No site descriptor found for '" + project.getId() + "' for locale '" + localeStr
                            + "', trying without country...");
                } else {
                    throw e;
                }
            }
        }

        if (!found && !language.isEmpty()) {
            localeStr = language;
            ArtifactRequest request =
                    createSiteDescriptorArtifactRequest(project, localeStr, remoteProjectRepositories);

            try {
                ArtifactResult result = repositorySystem.resolveArtifact(repoSession, request);

                siteDescriptor = result.getArtifact().getFile();
                found = true;
            } catch (ArtifactResolutionException e) {
                if (e.getCause() instanceof ArtifactNotFoundException) {
                    LOGGER.debug("No site descriptor found for '" + project.getId() + "' for locale '" + localeStr
                            + "', trying without language (default locale)...");
                } else {
                    throw e;
                }
            }
        }

        if (!found) {
            localeStr = SiteTool.DEFAULT_LOCALE.toString();
            ArtifactRequest request =
                    createSiteDescriptorArtifactRequest(project, localeStr, remoteProjectRepositories);
            try {
                ArtifactResult result = repositorySystem.resolveArtifact(repoSession, request);

                siteDescriptor = result.getArtifact().getFile();
            } catch (ArtifactResolutionException e) {
                if (e.getCause() instanceof ArtifactNotFoundException) {
                    LOGGER.debug("No site descriptor found for '" + project.getId() + "' with default locale");
                    throw (ArtifactNotFoundException) e.getCause();
                }

                throw e;
            }
        }

        return siteDescriptor;
    }

    /**
     * @param depth depth of project
     * @param siteDirectory, can be null if project.basedir is null, ie POM from repository
     * @param locale not null
     * @param project not null
     * @param repoSession not null
     * @param remoteProjectRepositories not null
     * @return the site model depending the locale and the parent project
     * @throws SiteToolException if any
     */
    private Map.Entry<SiteModel, MavenProject> getSiteModel(
            int depth,
            File siteDirectory,
            Locale locale,
            MavenProject project,
            RepositorySystemSession repoSession,
            List<RemoteRepository> remoteProjectRepositories)
            throws SiteToolException {
        // 1. get site descriptor File
        File siteDescriptor;
        if (project.getBasedir() == null) {
            // POM is in the repository: look into the repository for site descriptor
            try {
                siteDescriptor =
                        getSiteDescriptorFromRepository(project, repoSession, remoteProjectRepositories, locale);
            } catch (SiteToolException e) {
                throw new SiteToolException("The site descriptor cannot be resolved from the repository", e);
            }
        } else {
            // POM is in build directory: look for site descriptor as local file
            siteDescriptor = getSiteDescriptor(siteDirectory, locale);
        }

        // 2. read SiteModel from site descriptor File and do early interpolation (${this.*})
        SiteModel siteModel = null;
        Reader siteDescriptorReader = null;
        try {
            if (siteDescriptor != null && siteDescriptor.exists()) {
                LOGGER.debug("Reading" + (depth == 0 ? "" : (" parent level " + depth)) + " site descriptor from "
                        + siteDescriptor);

                siteDescriptorReader = ReaderFactory.newXmlReader(siteDescriptor);

                String siteDescriptorContent = IOUtil.toString(siteDescriptorReader);

                // interpolate ${this.*} = early interpolation
                siteDescriptorContent = getInterpolatedSiteDescriptorContent(project, siteDescriptorContent, true);

                siteModel = readSiteModel(siteDescriptorContent);
                siteModel.setLastModified(siteDescriptor.lastModified());
            } else {
                LOGGER.debug("No" + (depth == 0 ? "" : (" parent level " + depth)) + " site descriptor");
            }
        } catch (IOException e) {
            throw new SiteToolException(
                    "The site descriptor for '" + project.getId() + "' cannot be read from " + siteDescriptor, e);
        } finally {
            IOUtil.close(siteDescriptorReader);
        }

        // 3. look for parent project
        MavenProject parentProject = project.getParent();

        // 4. merge with parent project SiteModel
        if (parentProject != null && (siteModel == null || siteModel.isMergeParent())) {
            depth++;
            LOGGER.debug("Looking for site descriptor of level " + depth + " parent project: " + parentProject.getId());

            File parentSiteDirectory = null;
            if (parentProject.getBasedir() != null) {
                // extrapolate parent project site directory
                String siteRelativePath = getRelativeFilePath(
                        project.getBasedir().getAbsolutePath(),
                        siteDescriptor.getParentFile().getAbsolutePath());

                parentSiteDirectory = new File(parentProject.getBasedir(), siteRelativePath);
                // notice: using same siteRelativePath for parent as current project; may be wrong if site plugin
                // has different configuration. But this is a rare case (this only has impact if parent is from reactor)
            }

            SiteModel parentSiteModel = getSiteModel(
                            depth, parentSiteDirectory, locale, parentProject, repoSession, remoteProjectRepositories)
                    .getKey();

            // MSHARED-116 requires site model (instead of a null one)
            // MSHARED-145 requires us to do this only if there is a parent to merge it with
            if (siteModel == null && parentSiteModel != null) {
                // we have no site descriptor: merge the parent into an empty one because the default one
                // (default-site.xml) will break menu and breadcrumb composition.
                siteModel = new SiteModel();
            }

            String name = project.getName();
            if (siteModel != null && StringUtils.isNotEmpty(siteModel.getName())) {
                name = siteModel.getName();
            }

            // Merge the parent and child SiteModels
            String projectDistMgmnt = getDistMgmntSiteUrl(project);
            String parentDistMgmnt = getDistMgmntSiteUrl(parentProject);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Site model inheritance: assembling child with level " + depth
                        + " parent: distributionManagement.site.url child = " + projectDistMgmnt + " and parent = "
                        + parentDistMgmnt);
            }
            assembler.assembleModelInheritance(
                    name,
                    siteModel,
                    parentSiteModel,
                    projectDistMgmnt,
                    parentDistMgmnt == null ? projectDistMgmnt : parentDistMgmnt);
        }

        return new AbstractMap.SimpleEntry<SiteModel, MavenProject>(siteModel, parentProject);
    }

    /**
     * @param siteDescriptorContent not null
     * @return the site model object
     * @throws SiteToolException if any
     */
    private SiteModel readSiteModel(String siteDescriptorContent) throws SiteToolException {
        try {
            return new SiteXpp3Reader().read(new StringReader(siteDescriptorContent));
        } catch (XmlPullParserException e) {
            throw new SiteToolException("Error parsing site descriptor", e);
        } catch (IOException e) {
            throw new SiteToolException("Error reading site descriptor", e);
        }
    }

    private SiteModel getDefaultSiteModel() throws SiteToolException {
        String siteDescriptorContent;

        Reader reader = null;
        try {
            reader = ReaderFactory.newXmlReader(getClass().getResourceAsStream("/default-site.xml"));
            siteDescriptorContent = IOUtil.toString(reader);
        } catch (IOException e) {
            throw new SiteToolException("Error reading default site descriptor", e);
        } finally {
            IOUtil.close(reader);
        }

        return readSiteModel(siteDescriptorContent);
    }

    private String siteModelToString(SiteModel siteModel) throws SiteToolException {
        StringWriter writer = new StringWriter();

        try {
            new SiteXpp3Writer().write(writer, siteModel);
            return writer.toString();
        } catch (IOException e) {
            throw new SiteToolException("Error reading site descriptor", e);
        } finally {
            IOUtil.close(writer);
        }
    }

    private static String buildRelativePath(final String toPath, final String fromPath, final char separatorChar) {
        // use tokenizer to traverse paths and for lazy checking
        StringTokenizer toTokeniser = new StringTokenizer(toPath, String.valueOf(separatorChar));
        StringTokenizer fromTokeniser = new StringTokenizer(fromPath, String.valueOf(separatorChar));

        int count = 0;

        // walk along the to path looking for divergence from the from path
        while (toTokeniser.hasMoreTokens() && fromTokeniser.hasMoreTokens()) {
            if (separatorChar == '\\') {
                if (!fromTokeniser.nextToken().equalsIgnoreCase(toTokeniser.nextToken())) {
                    break;
                }
            } else {
                if (!fromTokeniser.nextToken().equals(toTokeniser.nextToken())) {
                    break;
                }
            }

            count++;
        }

        // reinitialize the tokenizers to count positions to retrieve the
        // gobbled token

        toTokeniser = new StringTokenizer(toPath, String.valueOf(separatorChar));
        fromTokeniser = new StringTokenizer(fromPath, String.valueOf(separatorChar));

        while (count-- > 0) {
            fromTokeniser.nextToken();
            toTokeniser.nextToken();
        }

        StringBuilder relativePath = new StringBuilder();

        // add back refs for the rest of from location.
        while (fromTokeniser.hasMoreTokens()) {
            fromTokeniser.nextToken();

            relativePath.append("..");

            if (fromTokeniser.hasMoreTokens()) {
                relativePath.append(separatorChar);
            }
        }

        if (relativePath.length() != 0 && toTokeniser.hasMoreTokens()) {
            relativePath.append(separatorChar);
        }

        // add fwd fills for whatever's left of to.
        while (toTokeniser.hasMoreTokens()) {
            relativePath.append(toTokeniser.nextToken());

            if (toTokeniser.hasMoreTokens()) {
                relativePath.append(separatorChar);
            }
        }
        return relativePath.toString();
    }

    /**
     * @param project not null
     * @param menu not null
     * @param name not null
     * @param href could be null
     * @param defaultHref could be null
     */
    private void appendMenuItem(MavenProject project, Menu menu, String name, String href, String defaultHref) {
        String selectedHref = href;

        if (selectedHref == null) {
            selectedHref = defaultHref;
        }

        MenuItem item = new MenuItem();
        item.setName(name);

        if (selectedHref != null) {
            String baseUrl = getDistMgmntSiteUrl(project);
            if (baseUrl != null) {
                selectedHref = getRelativePath(selectedHref, baseUrl);
            }

            if (selectedHref.endsWith("/")) {
                item.setHref(selectedHref + "index.html");
            } else {
                item.setHref(selectedHref + "/index.html");
            }
        }
        menu.addItem(item);
    }

    /**
     * @param name not null
     * @param href not null
     * @param categoryReports not null
     * @param locale not null
     * @return the menu item object
     */
    private MenuItem createCategoryMenu(String name, String href, List<MavenReport> categoryReports, Locale locale) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setCollapse(true);
        item.setHref(href);

        // MSHARED-172, allow reports to define their order in some other way?
        // Collections.sort( categoryReports, new ReportComparator( locale ) );

        for (MavenReport report : categoryReports) {
            MenuItem subitem = new MenuItem();
            subitem.setName(report.getName(locale));
            subitem.setHref(report.getOutputName() + ".html");
            item.getItems().add(subitem);
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
    private static boolean isEmptyList(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Return distributionManagement.site.url if defined, null otherwise.
     *
     * @param project not null
     * @return could be null
     */
    private static String getDistMgmntSiteUrl(MavenProject project) {
        return getDistMgmntSiteUrl(project.getDistributionManagement());
    }

    private static String getDistMgmntSiteUrl(DistributionManagement distMgmnt) {
        if (distMgmnt != null
                && distMgmnt.getSite() != null
                && distMgmnt.getSite().getUrl() != null) {
            // TODO This needs to go, it is just logically wrong
            return urlEncode(distMgmnt.getSite().getUrl());
        }

        return null;
    }

    /**
     * @param project the project
     * @param pluginId The id of the plugin
     * @return The information about the plugin.
     */
    private static Plugin getPlugin(MavenProject project, String pluginId) {
        if ((project.getBuild() == null) || (project.getBuild().getPluginsAsMap() == null)) {
            return null;
        }

        Plugin plugin = project.getBuild().getPluginsAsMap().get(pluginId);

        if ((plugin == null)
                && (project.getBuild().getPluginManagement() != null)
                && (project.getBuild().getPluginManagement().getPluginsAsMap() != null)) {
            plugin = project.getBuild().getPluginManagement().getPluginsAsMap().get(pluginId);
        }

        return plugin;
    }

    /**
     * @param project the project
     * @param pluginId The pluginId
     * @param param The child which should be checked.
     * @return The value of the dom tree.
     */
    private static String getPluginParameter(MavenProject project, String pluginId, String param) {
        Plugin plugin = getPlugin(project, pluginId);
        if (plugin != null) {
            Xpp3Dom xpp3Dom = (Xpp3Dom) plugin.getConfiguration();
            if (xpp3Dom != null
                    && xpp3Dom.getChild(param) != null
                    && StringUtils.isNotEmpty(xpp3Dom.getChild(param).getValue())) {
                return xpp3Dom.getChild(param).getValue();
            }
        }

        return null;
    }

    private static String urlEncode(final String url) {
        if (url == null) {
            return null;
        }

        try {
            return new File(url).toURI().toURL().toExternalForm();
        } catch (MalformedURLException ex) {
            return url; // this will then throw somewhere else
        }
    }
}
