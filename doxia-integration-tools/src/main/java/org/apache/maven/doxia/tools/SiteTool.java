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

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.doxia.site.SiteModel;
import org.apache.maven.doxia.site.Skin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReport;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * Tool to play with <a href="http://maven.apache.org/doxia/">Doxia</a> objects
 * like <code>SiteModel</code>.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public interface SiteTool {
    /**
     * The locale by default for a Maven Site.
     *
     * @see Locale#ROOT
     */
    Locale DEFAULT_LOCALE = Locale.ROOT;

    /**
     * Get a skin artifact from one of the repositories.
     *
     * @param repoSession the repository system session, not null.
     * @param remoteProjectRepositories the Maven remote project repositories, not null.
     * @param skin the Skin model, not null.
     * @return the <code>Skin</code> artifact defined in a <code>SiteModel</code> from a given project
     * @throws SiteToolException if any
     */
    Artifact getSkinArtifactFromRepository(
            RepositorySystemSession repoSession, List<RemoteRepository> remoteProjectRepositories, Skin skin)
            throws SiteToolException;

    /**
     * Get a site descriptor from the project's site directory.
     *
     * @param siteDirectory the site directory, not null
     * @param locale the locale wanted for the site descriptor, not null. Most specific
     * to least specific lookup from <code>site_language_country_variant.xml</code>,
     * <code>site_language_country.xml</code>, <code>site_language.xml}</code>,
     * to <code>site.xml</code> as last resort for {@link Locale#ROOT}, if provided
     * locale defines a variant and/or a country and/or a language.
     * @return the most specific site descriptor file for the given locale
     */
    File getSiteDescriptor(File siteDirectory, Locale locale);

    /**
     * Interpolating several expressions in the site descriptor content. Actually, the expressions can be in
     * the project, the environment variables and the specific properties like <code>encoding</code>.
     * <p>
     * For instance:
     * <dl>
     * <dt>${project.name}</dt>
     * <dd>The value from the POM of:
     * <p>
     * &lt;project&gt;<br>
     * &nbsp;&nbsp;&lt;name&gt;myProjectName&lt;/name&gt;<br>
     * &lt;/project&gt;
     * </p></dd>
     * <dt>${my.value}</dt>
     * <dd>The value from the POM of:
     * <p>
     * &lt;properties&gt;<br>
     * &nbsp;&nbsp;&lt;my.value&gt;hello&lt;/my.value&gt;<br>
     * &lt;/properties&gt;
     * </p></dd>
     * <dt>${JAVA_HOME}</dt>
     * <dd>The value of JAVA_HOME in the environment variables</dd>
     * </dl>
     *
     * @param props a map used for interpolation, not null.
     * @param aProject a Maven project, not null.
     * @param siteDescriptorContent the site descriptor file, not null.
     * @return the interpolated site descriptor content.
     * @throws SiteToolException if errors happened during the interpolation.
     */
    // used by maven-pdf-plugin (should not?)
    String getInterpolatedSiteDescriptorContent(
            Map<String, String> props, MavenProject aProject, String siteDescriptorContent) throws SiteToolException;

    /**
     * Get a site model for a project.
     *
     * @param siteDirectory the site directory, may be null if project from repository
     * @param locale the locale used for the i18n in SiteModel, not null.
     * See {@link #getSiteDescriptor(File, Locale)} for details.
     * @param project the Maven project, not null.
     * @param reactorProjects the Maven reactor projects, not null.
     * @param repoSession the repository system session, not null.
     * @param remoteProjectRepositories the Maven remote project repositories, not null.
     * @return the <code>SiteModel</code> object corresponding to the <code>site.xml</code> file with some
     * interpolations.
     * @throws SiteToolException if any
     * @since 1.7, was previously with other parameter types and order
     */
    SiteModel getSiteModel(
            File siteDirectory,
            Locale locale,
            MavenProject project,
            List<MavenProject> reactorProjects,
            RepositorySystemSession repoSession,
            List<RemoteRepository> remoteProjectRepositories)
            throws SiteToolException;

    /**
     * Populate the pre-defined <code>reports</code> menu of the site model,
     * if used through <code>&lt;menu ref="reports"/&gt;</code>. Notice this menu reference is translated into
     * 2 separate menus: "Project Information" and "Project Reports".
     *
     * @param siteModel the Doxia Sitetools SiteModel, not null.
     * @param locale the locale used for the i18n in SiteModel, not null.
     * See {@link #getSiteDescriptor(File, Locale)} for details.
     * @param reportsPerCategory reports per category to put in "Reports" or "Information" menus, not null.
     * @see MavenReport#CATEGORY_PROJECT_INFORMATION
     * @see MavenReport#CATEGORY_PROJECT_REPORTS
     */
    void populateReportsMenu(SiteModel siteModel, Locale locale, Map<String, List<MavenReport>> reportsPerCategory);

    /**
     * Extracts from a comma-separated list the locales that are available in <code>site-tool</code>
     * resource bundle.
     *
     * @param locales A comma separated list of locales
     * @return a list of <code>Locale</code>s.
     * @since 1.7, was previously getAvailableLocales(String)
     */
    List<Locale> getSiteLocales(String locales);

    /**
     * Calculate the relative path between two URLs or between two files.
     *
     * For example:
     * <dl>
     * <dt>to = "http://maven.apache.org" and from = "http://maven.apache.org"</dt>
     * <dd>return ""</dd>
     * <dt>to = "http://maven.apache.org" and from = "http://maven.apache.org/plugins/maven-site-plugin/"</dt>
     * <dd>return "../.."</dd>
     * <dt>to = "http://maven.apache.org/plugins/maven-site-plugin/" and from = "http://maven.apache.org"</dt>
     * <dd>return "plugins/maven-site-plugin"</dd>
     * <dt>to = "/myproject/myproject-module1" and from = "/myproject/myproject"</dt>
     * <dd>return "../myproject-module1"</dd>
     * </dl>
     * <b>Note</b>: The file separator depends on the system.
     * Maven-specific urls are supported, like <code>dav:https://dav.codehaus.org/</code> or
     * <code>scm:svn:https://svn.apache.org/repos/asf</code>.
     *
     * @param to the <code>to</code> url of file as string
     * @param from the <code>from</code> url of file as string
     * @return a relative path from <code>from</code> to <code>to</code>.
     */
    String getRelativePath(String to, String from);
}
