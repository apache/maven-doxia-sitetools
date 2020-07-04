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

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReport;

/**
 * Tool to play with <a href="http://maven.apache.org/doxia/">Doxia</a> objects
 * like <code>DecorationModel</code>.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public interface SiteTool
{
    /** Plexus Role */
    String ROLE = SiteTool.class.getName();

    /**
     * The locale by default for a Maven Site
     * @see Locale#ENGLISH
     */
    Locale DEFAULT_LOCALE = Locale.ENGLISH;

    /**
     * Get a skin artifact from one of the repositories.
     *
     * @param localRepository the Maven local repository, not null.
     * @param remoteArtifactRepositories the Maven remote repositories, not null.
     * @param decoration the Doxia site descriptor model, not null.
     * @return the <code>Skin</code> artifact defined in a <code>DecorationModel</code> from a given project and a
     * local repository
     * @throws SiteToolException if any
     */
    Artifact getSkinArtifactFromRepository( ArtifactRepository localRepository,
                                            List<ArtifactRepository> remoteArtifactRepositories,
                                            DecorationModel decoration )
        throws SiteToolException;

    /**
     * Get the default skin artifact for a project from one of the repositories.
     *
     * @param localRepository the Maven local repository, not null.
     * @param remoteArtifactRepositories the Maven remote repositories, not null.
     * @return the default <code>Skin</code> artifact from a given project and a local repository
     * @throws SiteToolException if any
     * @see org.apache.maven.doxia.site.decoration.Skin#getDefaultSkin()
     * @see #getSkinArtifactFromRepository(ArtifactRepository, List, DecorationModel)
     */
    Artifact getDefaultSkinArtifact( ArtifactRepository localRepository,
                                     List<ArtifactRepository> remoteArtifactRepositories )
        throws SiteToolException;

    /**
     * Get a site descriptor from the project's site directory.
     *
     * @param siteDirectory the site directory, not null
     * @param locale the locale wanted for the site descriptor. If not null, searching for
     * <code>site_<i>localeLanguage</i>.xml</code>, otherwise searching for <code>site.xml</code>.
     * @return the site descriptor file
     */ // used by maven-pdf-plugin (should not?)
    File getSiteDescriptor( File siteDirectory, Locale locale );

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
     */ // used by maven-pdf-plugin (should not?)
    String getInterpolatedSiteDescriptorContent( Map<String, String> props, MavenProject aProject,
                                                 String siteDescriptorContent )
        throws SiteToolException;

    /**
     * Get a decoration model for a project.
     *
     * @param siteDirectory the site directory, may be null if project from repository
     * @param locale the locale used for the i18n in DecorationModel. If null, using the default locale in the jvm.
     * @param project the Maven project, not null.
     * @param reactorProjects the Maven reactor projects, not null.
     * @param localRepository the Maven local repository, not null.
     * @param repositories the Maven remote repositories, not null.
     * @return the <code>DecorationModel</code> object corresponding to the <code>site.xml</code> file with some
     * interpolations.
     * @throws SiteToolException if any
     * @since 1.7, was previously with other parameter types and order
     */
    DecorationModel getDecorationModel( File siteDirectory, Locale locale, MavenProject project,
                                        List<MavenProject> reactorProjects, ArtifactRepository localRepository,
                                        List<ArtifactRepository> repositories )
        throws SiteToolException;

    /**
     * Populate the pre-defined <code>reports</code> menu of the decoration model,
     * if used through <code>&lt;menu ref="reports"/&gt;</code>. Notice this menu reference is translated into
     * 2 separate menus: "Project Information" and "Project Reports".
     *
     * @param decorationModel the Doxia Sitetools DecorationModel, not null.
     * @param locale the locale used for the i18n in DecorationModel. If null, using the default locale in the jvm.
     * @param reportsPerCategory reports per category to put in "Reports" or "Information" menus, not null.
     * @see MavenReport#CATEGORY_PROJECT_INFORMATION
     * @see MavenReport#CATEGORY_PROJECT_REPORTS
     */
    void populateReportsMenu( DecorationModel decorationModel, Locale locale,
                              Map<String, List<MavenReport>> reportsPerCategory );

    /**
     * Extracts from a comma-separated list the locales that are available in <code>site-tool</code>
     * resource bundle. Notice that <code>default</code> value will be changed to the default locale of
     * the JVM.
     *
     * @param locales A comma separated list of locales
     * @return a list of <code>Locale</code>, which at least contains the Maven default locale which is english
     * @since 1.7, was previously getAvailableLocales(String)
     */
    List<Locale> getSiteLocales( String locales );

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
    String getRelativePath( String to, String from );

    /**
     * Returns the parent POM with interpolated URLs.
     * If called from Maven 3, just returns <code>project.getParent()</code>, which is already
     * interpolated. But when called from Maven 2, attempts to source this value from the
     * <code>reactorProjects</code> parameters if available (reactor env model attributes
     * are interpolated), or if the reactor is unavailable (-N) resorts to the
     * <code>project.getParent().getUrl()</code> value which will NOT have been interpolated.
     *
     * @param aProject a Maven project, not null.
     * @param reactorProjects the Maven reactor projects, not null.
     * @param localRepository the Maven local repository, not null.
     * @return the parent project with interpolated URLs.
     */
    MavenProject getParentProject( MavenProject aProject, List<MavenProject> reactorProjects,
                                   ArtifactRepository localRepository );
}
