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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.api.DependencyCoordinates;
import org.apache.maven.api.Packaging;
import org.apache.maven.api.ProducedArtifact;
import org.apache.maven.api.Project;
import org.apache.maven.api.model.DistributionManagement;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.Profile;
import org.apache.maven.api.model.Site;
import org.apache.maven.model.v4.MavenStaxReader;

import static org.codehaus.plexus.testing.PlexusExtension.getTestFile;

/**
 * A {@link Project} backed by nothing but a {@link Model} and a base directory: the Maven 4 API has no
 * public implementation of it outside of Maven core, so the site tool tests supply their own.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public class SiteToolProjectStub implements Project {
    private Model model;

    private Path basedir;

    private Project parent;

    /**
     * A project reading its POM from <code>src/test/resources/unit/&lt;projectName&gt;/pom.xml</code>.
     */
    public SiteToolProjectStub(String projectName) {
        File dir = getTestFile("src/test/resources/unit/" + projectName);
        basedir = dir.toPath();
        try (InputStream in = Files.newInputStream(basedir.resolve("pom.xml"))) {
            model = new MavenStaxReader().read(in);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * A project which only exists as coordinates, ie has no base directory (as if its POM came from a repository).
     */
    public SiteToolProjectStub(String groupId, String artifactId, String version) {
        model = Model.newBuilder()
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .build();
    }

    public void setBasedir(Path basedir) {
        this.basedir = basedir;
    }

    public void setGroupId(String groupId) {
        model = model.withGroupId(groupId);
    }

    public void setArtifactId(String artifactId) {
        model = model.withArtifactId(artifactId);
    }

    public void setVersion(String version) {
        model = model.withVersion(version);
    }

    public void setProperties(Map<String, String> properties) {
        model = model.withProperties(new HashMap<>(properties));
    }

    public void setParent(Project parent) {
        this.parent = parent;
    }

    public void setDistgributionManagementSiteUrl(String url) {
        model = model.withDistributionManagement(DistributionManagement.newBuilder()
                .site(Site.newBuilder().url(url).build())
                .build());
    }

    @Override
    public String getGroupId() {
        return model.getGroupId();
    }

    @Override
    public String getArtifactId() {
        return model.getArtifactId();
    }

    @Override
    public String getVersion() {
        return model.getVersion();
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public Path getBasedir() {
        return basedir;
    }

    @Override
    public Optional<Project> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Packaging getPackaging() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ProducedArtifact> getArtifacts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getPomPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DependencyCoordinates> getDependencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DependencyCoordinates> getManagedDependencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTopProject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRootProject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getRootDirectory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Profile> getDeclaredProfiles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Profile> getEffectiveProfiles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Profile> getDeclaredActiveProfiles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Profile> getEffectiveActiveProfiles() {
        throw new UnsupportedOperationException();
    }
}
