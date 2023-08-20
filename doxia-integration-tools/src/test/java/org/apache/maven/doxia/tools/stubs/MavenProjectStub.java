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
package org.apache.maven.doxia.tools.stubs;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.model.Build;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Developer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Extension;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Prerequisites;
import org.apache.maven.model.Profile;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Resource;
import org.apache.maven.model.Scm;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.testing.PlexusExtension;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Very simple stub of <code>MavenProject</code> object, going to take a lot of work to make it
 * useful as a stub though.
 *
 * @author jesse
 * @version $Id$
 */
public class MavenProjectStub extends MavenProject {
    private String groupId;

    private String artifactId;

    private String name;

    private Model model;

    private MavenProject parent;

    private File file;

    private List<MavenProject> collectedProjects;

    private List<Artifact> attachedArtifacts;

    private List<String> compileSourceRoots;

    private List<String> testCompileSourceRoots;

    private List<String> scriptSourceRoots;

    private List<ArtifactRepository> pluginArtifactRepositories;

    private ArtifactRepository releaseArtifactRepository;

    private ArtifactRepository snapshotArtifactRepository;

    private List<Profile> activeProfiles;

    private Set<Artifact> dependencyArtifacts;

    private Artifact artifact;

    private Map<String, Artifact> artifactMap;

    private Model originalModel;

    private Map<String, Artifact> pluginArtifactMap;

    private Map<String, Artifact> reportArtifactMap;

    private Map<String, Artifact> extensionArtifactMap;

    private Map<String, MavenProject> projectReferences;

    private Build buildOverlay;

    private boolean executionRoot;

    private List<Artifact> compileArtifacts;

    private List<Dependency> compileDependencies;

    private List<Dependency> systemDependencies;

    private List<String> testClasspathElements;

    private List<Dependency> testDependencies;

    private List<String> systemClasspathElements;

    private List<Artifact> systemArtifacts;

    private List<Artifact> testArtifacts;

    private List<Artifact> runtimeArtifacts;

    private List<Dependency> runtimeDependencies;

    private List<String> runtimeClasspathElements;

    private String modelVersion;

    private String packaging;

    private String inceptionYear;

    private String url;

    private String description;

    private String version;

    private String defaultGoal;

    private List<License> licenses;

    private Build build;

    /**
     * Default constructor
     */
    public MavenProjectStub() {
        this(new Model());
    }

    /**
     * @param model the given model
     */
    public MavenProjectStub(Model model) {
        super((Model) null);
        this.model = model;
    }

    /**
     * Loads the model for this stub from the specified POM. For convenience, any checked exception caused by I/O or
     * parser errors will be wrapped into an unchecked exception.
     *
     * @param pomFile The path to the POM file to load, must not be <code>null</code>. If this path is relative, it
     *            is resolved against the return value of {@link #getBasedir()}.
     */
    protected void readModel(File pomFile) {
        if (!pomFile.isAbsolute()) {
            pomFile = new File(getBasedir(), pomFile.getPath());
        }
        try {
            setModel(new MavenXpp3Reader().read(ReaderFactory.newXmlReader(pomFile)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read POM file: " + pomFile, e);
        } catch (XmlPullParserException e) {
            throw new RuntimeException("Failed to parse POM file: " + pomFile, e);
        }
    }

    /**
     * No project model is associated
     *
     * @param project the given project
     */
    public MavenProjectStub(MavenProject project) {
        super((Model) null);
    }

    /**
     * @param mavenProject
     * @return an empty String
     * @throws IOException if any
     */
    public String getModulePathAdjustment(MavenProject mavenProject) throws IOException {
        return "";
    }

    /** {@inheritDoc} */
    public Artifact getArtifact() {
        return artifact;
    }

    /** {@inheritDoc} */
    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    /** {@inheritDoc} */
    public Model getModel() {
        return model;
    }

    /** {@inheritDoc} */
    public MavenProject getParent() {
        return parent;
    }

    /** {@inheritDoc} */
    public void setParent(MavenProject mavenProject) {
        this.parent = mavenProject;
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setRemoteArtifactRepositories(List)
     */
    public void setRemoteArtifactRepositories(List<ArtifactRepository> list) {
        // nop
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getRemoteArtifactRepositories()
     */
    public List<ArtifactRepository> getRemoteArtifactRepositories() {
        return Collections.<ArtifactRepository>emptyList();
    }

    /** {@inheritDoc} */
    public boolean hasParent() {
        if (parent != null) {
            return true;
        }

        return false;
    }

    /** {@inheritDoc} */
    public File getFile() {
        return file;
    }

    /** {@inheritDoc} */
    public void setFile(File file) {
        this.file = file;
    }

    /** {@inheritDoc} */
    public File getBasedir() {
        return new File(PlexusExtension.getBasedir());
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setDependencies(List)
     */
    public void setDependencies(List<Dependency> list) {
        // nop
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getDependencies()
     */
    public List<Dependency> getDependencies() {
        return Collections.<Dependency>emptyList();
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getDependencyManagement()
     */
    public DependencyManagement getDependencyManagement() {
        return null;
    }

    /** {@inheritDoc} */
    public void addCompileSourceRoot(String string) {
        if (compileSourceRoots == null) {
            compileSourceRoots = new ArrayList<>(Collections.singletonList(string));
        } else {
            compileSourceRoots.add(string);
        }
    }

    /** {@inheritDoc} */
    public void addScriptSourceRoot(String string) {
        if (scriptSourceRoots == null) {
            scriptSourceRoots = new ArrayList<>(Collections.singletonList(string));
        } else {
            scriptSourceRoots.add(string);
        }
    }

    /** {@inheritDoc} */
    public void addTestCompileSourceRoot(String string) {
        if (testCompileSourceRoots == null) {
            testCompileSourceRoots = new ArrayList<>(Collections.singletonList(string));
        } else {
            testCompileSourceRoots.add(string);
        }
    }

    /** {@inheritDoc} */
    public List<String> getCompileSourceRoots() {
        return compileSourceRoots;
    }

    /** {@inheritDoc} */
    public List<String> getScriptSourceRoots() {
        return scriptSourceRoots;
    }

    /** {@inheritDoc} */
    public List<String> getTestCompileSourceRoots() {
        return testCompileSourceRoots;
    }

    /** {@inheritDoc} */
    public List<String> getCompileClasspathElements() throws DependencyResolutionRequiredException {
        return compileSourceRoots;
    }

    /**
     * @param compileArtifacts
     */
    public void setCompileArtifacts(List<Artifact> compileArtifacts) {
        this.compileArtifacts = compileArtifacts;
    }

    /** {@inheritDoc} */
    public List<Artifact> getCompileArtifacts() {
        return compileArtifacts;
    }

    /** {@inheritDoc} */
    public List<Dependency> getCompileDependencies() {
        return compileDependencies;
    }

    /** {@inheritDoc} */
    public List<String> getTestClasspathElements() throws DependencyResolutionRequiredException {
        return testClasspathElements;
    }

    /** {@inheritDoc} */
    public List<Artifact> getTestArtifacts() {
        return testArtifacts;
    }

    /** {@inheritDoc} */
    public List<Dependency> getTestDependencies() {
        return testDependencies;
    }

    /** {@inheritDoc} */
    public List<String> getRuntimeClasspathElements() throws DependencyResolutionRequiredException {
        return runtimeClasspathElements;
    }

    /** {@inheritDoc} */
    public List<Artifact> getRuntimeArtifacts() {
        return runtimeArtifacts;
    }

    /** {@inheritDoc} */
    public List<Dependency> getRuntimeDependencies() {
        return runtimeDependencies;
    }

    /** {@inheritDoc} */
    public List<String> getSystemClasspathElements() throws DependencyResolutionRequiredException {
        return systemClasspathElements;
    }

    /** {@inheritDoc} */
    public List<Artifact> getSystemArtifacts() {
        return systemArtifacts;
    }

    /**
     * @param runtimeClasspathElements
     */
    public void setRuntimeClasspathElements(List<String> runtimeClasspathElements) {
        this.runtimeClasspathElements = runtimeClasspathElements;
    }

    /**
     * @param attachedArtifacts
     */
    public void setAttachedArtifacts(List<Artifact> attachedArtifacts) {
        this.attachedArtifacts = attachedArtifacts;
    }

    /**
     * @param compileSourceRoots
     */
    public void setCompileSourceRoots(List<String> compileSourceRoots) {
        this.compileSourceRoots = compileSourceRoots;
    }

    /**
     * @param testCompileSourceRoots
     */
    public void setTestCompileSourceRoots(List<String> testCompileSourceRoots) {
        this.testCompileSourceRoots = testCompileSourceRoots;
    }

    /**
     * @param scriptSourceRoots
     */
    public void setScriptSourceRoots(List<String> scriptSourceRoots) {
        this.scriptSourceRoots = scriptSourceRoots;
    }

    /**
     * @param artifactMap
     */
    public void setArtifactMap(Map<String, Artifact> artifactMap) {
        this.artifactMap = artifactMap;
    }

    /**
     * @param pluginArtifactMap
     */
    public void setPluginArtifactMap(Map<String, Artifact> pluginArtifactMap) {
        this.pluginArtifactMap = pluginArtifactMap;
    }

    /**
     * @param reportArtifactMap
     */
    public void setReportArtifactMap(Map<String, Artifact> reportArtifactMap) {
        this.reportArtifactMap = reportArtifactMap;
    }

    /**
     * @param extensionArtifactMap
     */
    public void setExtensionArtifactMap(Map<String, Artifact> extensionArtifactMap) {
        this.extensionArtifactMap = extensionArtifactMap;
    }

    /**
     * @param projectReferences
     */
    public void setProjectReferences(Map<String, MavenProject> projectReferences) {
        this.projectReferences = projectReferences;
    }

    /**
     * @param buildOverlay
     */
    public void setBuildOverlay(Build buildOverlay) {
        this.buildOverlay = buildOverlay;
    }

    /**
     * @param compileDependencies
     */
    public void setCompileDependencies(List<Dependency> compileDependencies) {
        this.compileDependencies = compileDependencies;
    }

    /**
     * @param systemDependencies
     */
    public void setSystemDependencies(List<Dependency> systemDependencies) {
        this.systemDependencies = systemDependencies;
    }

    /**
     * @param testClasspathElements
     */
    public void setTestClasspathElements(List<String> testClasspathElements) {
        this.testClasspathElements = testClasspathElements;
    }

    /**
     * @param testDependencies
     */
    public void setTestDependencies(List<Dependency> testDependencies) {
        this.testDependencies = testDependencies;
    }

    /**
     * @param systemClasspathElements
     */
    public void setSystemClasspathElements(List<String> systemClasspathElements) {
        this.systemClasspathElements = systemClasspathElements;
    }

    /**
     * @param systemArtifacts
     */
    public void setSystemArtifacts(List<Artifact> systemArtifacts) {
        this.systemArtifacts = systemArtifacts;
    }

    /**
     * @param testArtifacts
     */
    public void setTestArtifacts(List<Artifact> testArtifacts) {
        this.testArtifacts = testArtifacts;
    }

    /**
     * @param runtimeArtifacts
     */
    public void setRuntimeArtifacts(List<Artifact> runtimeArtifacts) {
        this.runtimeArtifacts = runtimeArtifacts;
    }

    /**
     * @param runtimeDependencies
     */
    public void setRuntimeDependencies(List<Dependency> runtimeDependencies) {
        this.runtimeDependencies = runtimeDependencies;
    }

    /**
     * @param model
     */
    public void setModel(Model model) {
        this.model = model;
    }

    /** {@inheritDoc} */
    public List<Dependency> getSystemDependencies() {
        return systemDependencies;
    }

    /** {@inheritDoc} */
    public void setModelVersion(String string) {
        this.modelVersion = string;
    }

    /** {@inheritDoc} */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * By default, return an empty String.
     *
     * @see MavenProject#getId()
     */
    public String getId() {
        return "";
    }

    /** {@inheritDoc} */
    public void setGroupId(String string) {
        this.groupId = string;
    }

    /** {@inheritDoc} */
    public String getGroupId() {
        return groupId;
    }

    /** {@inheritDoc} */
    public void setArtifactId(String string) {
        this.artifactId = string;
    }

    /** {@inheritDoc} */
    public String getArtifactId() {
        return artifactId;
    }

    /** {@inheritDoc} */
    public void setName(String string) {
        this.name = string;
    }

    /** {@inheritDoc} */
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    public void setVersion(String string) {
        this.version = string;
    }

    /** {@inheritDoc} */
    public String getVersion() {
        return version;
    }

    /** {@inheritDoc} */
    public String getPackaging() {
        return packaging;
    }

    /** {@inheritDoc} */
    public void setPackaging(String string) {
        this.packaging = string;
    }

    /** {@inheritDoc} */
    public void setInceptionYear(String string) {
        this.inceptionYear = string;
    }

    /** {@inheritDoc} */
    public String getInceptionYear() {
        return inceptionYear;
    }

    /** {@inheritDoc} */
    public void setUrl(String string) {
        this.url = string;
    }

    /** {@inheritDoc} */
    public String getUrl() {
        return url;
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getPrerequisites()
     */
    public Prerequisites getPrerequisites() {
        return null;
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setIssueManagement(IssueManagement)
     */
    public void setIssueManagement(IssueManagement issueManagement) {
        // nop
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getCiManagement()
     */
    public CiManagement getCiManagement() {
        return null;
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setCiManagement(CiManagement)
     */
    public void setCiManagement(CiManagement ciManagement) {
        // nop
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getIssueManagement()
     */
    public IssueManagement getIssueManagement() {
        return null;
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setDistributionManagement(DistributionManagement)
     */
    public void setDistributionManagement(DistributionManagement distributionManagement) {
        // nop
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getDistributionManagement()
     */
    public DistributionManagement getDistributionManagement() {
        return null;
    }

    /** {@inheritDoc} */
    public void setDescription(String string) {
        this.description = string;
    }

    /** {@inheritDoc} */
    public String getDescription() {
        return description;
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setOrganization(Organization)
     */
    public void setOrganization(Organization organization) {
        // nop
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getOrganization()
     */
    public Organization getOrganization() {
        return null;
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setScm(Scm)
     */
    public void setScm(Scm scm) {
        // nop
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getScm()
     */
    public Scm getScm() {
        return null;
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setMailingLists(List)
     */
    public void setMailingLists(List<MailingList> list) {
        // nop
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getMailingLists()
     */
    public List<MailingList> getMailingLists() {
        return Collections.<MailingList>emptyList();
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#addMailingList(MailingList)
     */
    public void addMailingList(MailingList mailingList) {
        // nop
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setDevelopers(List)
     */
    public void setDevelopers(List<Developer> list) {
        // nop
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getDevelopers()
     */
    public List<Developer> getDevelopers() {
        return Collections.<Developer>emptyList();
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#addDeveloper(Developer)
     */
    public void addDeveloper(Developer developer) {
        // nop
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setContributors(List)
     */
    public void setContributors(List<Contributor> list) {
        // nop
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getContributors()
     */
    public List<Contributor> getContributors() {
        return Collections.<Contributor>emptyList();
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#addContributor(Contributor)
     */
    public void addContributor(Contributor contributor) {
        // nop
    }

    /** {@inheritDoc} */
    public void setBuild(Build build) {
        this.build = build;
    }

    /** {@inheritDoc} */
    public Build getBuild() {
        return build;
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getResources()
     */
    public List<Resource> getResources() {
        return Collections.<Resource>emptyList();
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getTestResources()
     */
    public List<Resource> getTestResources() {
        return Collections.<Resource>emptyList();
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#addResource(Resource)
     */
    public void addResource(Resource resource) {
        // nop
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#addTestResource(Resource)
     */
    public void addTestResource(Resource resource) {
        // nop
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setReporting(Reporting)
     */
    public void setReporting(Reporting reporting) {
        // nop
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getReporting()
     */
    public Reporting getReporting() {
        return null;
    }

    /** {@inheritDoc} */
    public void setLicenses(List<License> licenses) {
        this.licenses = licenses;
    }

    /** {@inheritDoc} */
    public List<License> getLicenses() {
        return licenses;
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#addLicense(License)
     */
    public void addLicense(License license) {
        // nop
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setArtifacts(Set)
     */
    public void setArtifacts(Set<Artifact> set) {
        // nop
    }

    /**
     * By default, return <code>Collections.EMPTY_SET</code>.
     *
     * @see MavenProject#getArtifacts()
     */
    public Set<Artifact> getArtifacts() {
        return Collections.<Artifact>emptySet();
    }

    /**
     * By default, return <code>Collections.EMPTY_MAP</code>.
     *
     * @see MavenProject#getArtifactMap()
     */
    public Map<String, Artifact> getArtifactMap() {
        return Collections.<String, Artifact>emptyMap();
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setPluginArtifacts(Set)
     */
    public void setPluginArtifacts(Set<Artifact> set) {
        // nop
    }

    /**
     * By default, return <code>Collections.EMPTY_SET</code>.
     *
     * @see MavenProject#getPluginArtifacts()
     */
    public Set<Artifact> getPluginArtifacts() {
        return Collections.<Artifact>emptySet();
    }

    /**
     * By default, return <code>Collections.EMPTY_MAP</code>.
     *
     * @see MavenProject#getPluginArtifactMap()
     */
    public Map<String, Artifact> getPluginArtifactMap() {
        return Collections.<String, Artifact>emptyMap();
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setReportArtifacts(Set)
     */
    public void setReportArtifacts(Set<Artifact> set) {
        // nop
    }

    /**
     * By default, return <code>Collections.EMPTY_SET</code>.
     *
     * @see MavenProject#getReportArtifacts()
     */
    public Set<Artifact> getReportArtifacts() {
        return Collections.<Artifact>emptySet();
    }

    /**
     * By default, return <code>Collections.EMPTY_MAP</code>.
     *
     * @see MavenProject#getReportArtifactMap()
     */
    public Map<String, Artifact> getReportArtifactMap() {
        return Collections.<String, Artifact>emptyMap();
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setExtensionArtifacts(Set)
     */
    public void setExtensionArtifacts(Set<Artifact> set) {
        // nop
    }

    /**
     * By default, return <code>Collections.EMPTY_SET</code>.
     *
     * @see MavenProject#getExtensionArtifacts()
     */
    public Set<Artifact> getExtensionArtifacts() {
        return Collections.<Artifact>emptySet();
    }

    /**
     * By default, return <code>Collections.EMPTY_MAP</code>.
     *
     * @see MavenProject#getExtensionArtifactMap()
     */
    public Map<String, Artifact> getExtensionArtifactMap() {
        return Collections.<String, Artifact>emptyMap();
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setParentArtifact(Artifact)
     */
    public void setParentArtifact(Artifact artifact) {
        // nop
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getParentArtifact()
     */
    public Artifact getParentArtifact() {
        return null;
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getRepositories()
     */
    public List<Repository> getRepositories() {
        return Collections.<Repository>emptyList();
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getReportPlugins()
     */
    public List<ReportPlugin> getReportPlugins() {
        return Collections.<ReportPlugin>emptyList();
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getBuildPlugins()
     */
    public List<Plugin> getBuildPlugins() {
        return Collections.<Plugin>emptyList();
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getModules()
     */
    public List<String> getModules() {
        return Collections.<String>emptyList();
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getPluginManagement()
     */
    public PluginManagement getPluginManagement() {
        return null;
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#addPlugin(Plugin)
     */
    public void addPlugin(Plugin plugin) {
        // nop
    }

    /**
     * By default, do nothing.
     *
     * @param plugin
     */
    public void injectPluginManagementInfo(Plugin plugin) {
        // nop
    }

    /** {@inheritDoc} */
    public List<MavenProject> getCollectedProjects() {
        return collectedProjects;
    }

    /** {@inheritDoc} */
    public void setCollectedProjects(List<MavenProject> list) {
        this.collectedProjects = list;
    }

    /** {@inheritDoc} */
    public void setPluginArtifactRepositories(List<ArtifactRepository> list) {
        this.pluginArtifactRepositories = list;
    }

    /** {@inheritDoc} */
    public List<ArtifactRepository> getPluginArtifactRepositories() {
        return pluginArtifactRepositories;
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getDistributionManagementArtifactRepository()
     */
    public ArtifactRepository getDistributionManagementArtifactRepository() {
        return null;
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getPluginRepositories()
     */
    public List<Repository> getPluginRepositories() {
        return Collections.<Repository>emptyList();
    }

    /** {@inheritDoc} */
    public void setActiveProfiles(List<Profile> list) {
        activeProfiles = list;
    }

    /** {@inheritDoc} */
    public List<Profile> getActiveProfiles() {
        return activeProfiles;
    }

    /** {@inheritDoc} */
    public void addAttachedArtifact(Artifact artifact) {
        if (attachedArtifacts == null) {
            this.attachedArtifacts = new ArrayList<>(Collections.singletonList(artifact));
        } else {
            attachedArtifacts.add(artifact);
        }
    }

    /** {@inheritDoc} */
    public List<Artifact> getAttachedArtifacts() {
        return attachedArtifacts;
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getGoalConfiguration(String, String, String, String)
     */
    public Xpp3Dom getGoalConfiguration(String string, String string1, String string2, String string3) {
        return null;
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getReportConfiguration(String, String, String)
     */
    public Xpp3Dom getReportConfiguration(String string, String string1, String string2) {
        return null;
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#getExecutionProject()
     */
    public MavenProject getExecutionProject() {
        return null;
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#setExecutionProject(MavenProject)
     */
    public void setExecutionProject(MavenProject mavenProject) {
        // nop
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#writeModel(Writer)
     */
    public void writeModel(Writer writer) throws IOException {
        // nop
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#writeOriginalModel(Writer)
     */
    public void writeOriginalModel(Writer writer) throws IOException {
        // nop
    }

    /** {@inheritDoc} */
    public Set<Artifact> getDependencyArtifacts() {
        return dependencyArtifacts;
    }

    /** {@inheritDoc} */
    public void setDependencyArtifacts(Set<Artifact> set) {
        this.dependencyArtifacts = set;
    }

    /** {@inheritDoc} */
    public void setReleaseArtifactRepository(ArtifactRepository artifactRepository) {
        this.releaseArtifactRepository = artifactRepository;
    }

    /** {@inheritDoc} */
    public void setSnapshotArtifactRepository(ArtifactRepository artifactRepository) {
        this.snapshotArtifactRepository = artifactRepository;
    }

    /** {@inheritDoc} */
    public void setOriginalModel(Model model) {
        this.originalModel = model;
    }

    /** {@inheritDoc} */
    public Model getOriginalModel() {
        return originalModel;
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getBuildExtensions()
     */
    public List<Extension> getBuildExtensions() {
        return Collections.<Extension>emptyList();
    }

    /**
     * By default, return <code>Collections.EMPTY_SET</code>.
     *
     * @see MavenProject#createArtifacts(ArtifactFactory, String, ArtifactFilter)
     */
    public Set<Artifact> createArtifacts(
            ArtifactFactory artifactFactory, String string, ArtifactFilter artifactFilter) {
        return Collections.<Artifact>emptySet();
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#addProjectReference(MavenProject)
     */
    public void addProjectReference(MavenProject mavenProject) {
        // nop
    }

    /**
     * By default, do nothing.
     *
     * @see MavenProject#attachArtifact(String, String, File)
     */
    public void attachArtifact(String string, String string1, File file) {
        // nop
    }

    /**
     * By default, return a new instance of <code>Properties</code>.
     *
     * @see MavenProject#getProperties()
     */
    public Properties getProperties() {
        return new Properties();
    }

    /**
     * By default, return <code>Collections.EMPTY_LIST</code>.
     *
     * @see MavenProject#getFilters()
     */
    public List<String> getFilters() {
        return Collections.<String>emptyList();
    }

    /**
     * By default, return <code>Collections.EMPTY_MAP</code>.
     *
     * @see MavenProject#getProjectReferences()
     */
    public Map<String, MavenProject> getProjectReferences() {
        return Collections.<String, MavenProject>emptyMap();
    }

    /** {@inheritDoc} */
    public boolean isExecutionRoot() {
        return executionRoot;
    }

    /** {@inheritDoc} */
    public void setExecutionRoot(boolean b) {
        this.executionRoot = b;
    }

    /** {@inheritDoc} */
    public String getDefaultGoal() {
        return defaultGoal;
    }

    /**
     * By default, return <code>null</code>.
     *
     * @see MavenProject#replaceWithActiveArtifact(Artifact)
     */
    public Artifact replaceWithActiveArtifact(Artifact artifact) {
        return null;
    }
}
