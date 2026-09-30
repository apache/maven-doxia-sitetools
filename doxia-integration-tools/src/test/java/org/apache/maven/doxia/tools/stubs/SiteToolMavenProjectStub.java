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
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Site;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * A {@link MavenProject} read from one of the test projects under <code>src/test/resources/unit</code>. Everything
 * the site tool reads lives in the {@link Model}, as it does in a real build.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public class SiteToolMavenProjectStub extends MavenProject {
    private File basedir;

    public SiteToolMavenProjectStub(String projectName) {
        super(readModel(projectName));
        basedir = unitDir(projectName);
        setFile(new File(basedir, "pom.xml"));

        Build build = new Build();
        build.setFinalName(getArtifactId());
        build.setDirectory(System.getProperty("basedir", ".") + "/target/test/unit/" + projectName + "/target");
        build.setSourceDirectory(basedir + "/src/main/java");
        build.setOutputDirectory(build.getDirectory() + "/classes");
        build.setTestSourceDirectory(basedir + "/src/test/java");
        build.setTestOutputDirectory(build.getDirectory() + "/test-classes");
        getModel().setBuild(build);
    }

    /**
     * A project with coordinates only and no base directory, as Maven builds a parent it reads from a repository.
     */
    public SiteToolMavenProjectStub(String groupId, String artifactId, String version) {
        super(new Model());
        getModel().setModelVersion("4.0.0");
        setGroupId(groupId);
        setArtifactId(artifactId);
        setVersion(version);
    }

    private static File unitDir(String projectName) {
        return new File(System.getProperty("basedir", "."), "src/test/resources/unit/" + projectName);
    }

    private static Model readModel(String projectName) {
        try (InputStream in = Files.newInputStream(new File(unitDir(projectName), "pom.xml").toPath())) {
            return new MavenXpp3Reader().read(in);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read the test project " + projectName, e);
        }
    }

    @Override
    public File getBasedir() {
        return basedir;
    }

    public void setBasedir(File basedir) {
        this.basedir = basedir;
    }

    @Override
    public List<RemoteRepository> getRemoteProjectRepositories() {
        return Collections.singletonList(
                new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2").build());
    }

    public void setDistgributionManagementSiteUrl(String url) {
        Site site = new Site();
        site.setUrl(url);
        DistributionManagement distributionManagement = new DistributionManagement();
        distributionManagement.setSite(site);
        getModel().setDistributionManagement(distributionManagement);
    }
}
