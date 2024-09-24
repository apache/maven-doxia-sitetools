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

import java.io.File;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.maven.doxia.site.LinkItem;
import org.apache.maven.doxia.site.SiteModel;
import org.apache.maven.doxia.site.Skin;
import org.apache.maven.doxia.site.io.xpp3.SiteXpp3Reader;
import org.apache.maven.doxia.site.io.xpp3.SiteXpp3Writer;
import org.apache.maven.doxia.tools.stubs.MavenProjectStub;
import org.apache.maven.doxia.tools.stubs.SiteToolMavenProjectStub;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.testing.PlexusTest;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.junit.jupiter.api.Test;

import static org.codehaus.plexus.testing.PlexusExtension.getTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
@SuppressWarnings("javadoc")
@PlexusTest
public class SiteToolTest {

    @Inject
    private DefaultSiteTool tool;

    /**
     * @return the local repo directory.
     *
     * @throws Exception
     */
    protected File getLocalRepoDir() throws Exception {
        return getTestFile("target/local-repo");
    }

    protected RepositorySystemSession newRepoSession() throws Exception {
        DefaultRepositorySystemSession repoSession = MavenRepositorySystemUtils.newSession();
        repoSession.setLocalRepositoryManager(new SimpleLocalRepositoryManagerFactory()
                .newInstance(repoSession, new LocalRepository(getLocalRepoDir())));
        return repoSession;
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetSkinArtifactFromRepository() throws Exception {
        assertNotNull(tool);

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub("site-tool-test");
        Skin skin = new Skin();
        skin.setGroupId("org.apache.maven.skins");
        skin.setArtifactId("maven-fluido-skin");
        assertNotNull(
                tool.getSkinArtifactFromRepository(newRepoSession(), project.getRemoteProjectRepositories(), skin));
    }

    private void checkGetRelativePathDirectory(SiteTool tool, String relative, String to, String from) {
        assertEquals(relative, tool.getRelativePath(to, from));
        assertEquals(relative, tool.getRelativePath(to + '/', from));
        assertEquals(relative, tool.getRelativePath(to, from + '/'));
        assertEquals(relative, tool.getRelativePath(to + '/', from + '/'));
    }

    /**
     * @throws Exception
     */
    @Test
    @SuppressWarnings({"deprecation"})
    public void testGetRelativePath() throws Exception {
        assertNotNull(tool);

        checkGetRelativePathDirectory(tool, "", "http://maven.apache.org", "http://maven.apache.org");

        checkGetRelativePathDirectory(
                tool,
                ".." + File.separator + "..",
                "http://maven.apache.org",
                "http://maven.apache.org/plugins/maven-site-plugin");

        checkGetRelativePathDirectory(
                tool,
                "plugins" + File.separator + "maven-site-plugin",
                "http://maven.apache.org/plugins/maven-site-plugin",
                "http://maven.apache.org");

        checkGetRelativePathDirectory(tool, "", "dav:https://maven.apache.org", "dav:https://maven.apache.org");

        checkGetRelativePathDirectory(
                tool,
                "plugins" + File.separator + "maven-site-plugin",
                "dav:http://maven.apache.org/plugins/maven-site-plugin",
                "dav:http://maven.apache.org");

        checkGetRelativePathDirectory(tool, "", "scm:svn:https://maven.apache.org", "scm:svn:https://maven.apache.org");

        checkGetRelativePathDirectory(
                tool,
                "plugins" + File.separator + "maven-site-plugin",
                "scm:svn:https://maven.apache.org/plugins/maven-site-plugin",
                "scm:svn:https://maven.apache.org");

        String to = "http://maven.apache.org/downloads.html";
        String from = "http://maven.apache.org/index.html";

        // MSITE-600, MSHARED-203
        to = "file:///tmp/bloop";
        from = "scp://localhost:/tmp/blop";
        assertEquals(tool.getRelativePath(to, from), to);

        // note: 'tmp' is the host here which is probably not the intention, but at least the result is correct
        to = "file://tmp/bloop";
        from = "scp://localhost:/tmp/blop";
        assertEquals(to, tool.getRelativePath(to, from));

        // Tests between files as described in MIDEA-102
        to = "C:/dev/voca/gateway/parser/gateway-parser.iml";
        from = "C:/dev/voca/gateway/";
        assertEquals(
                "parser" + File.separator + "gateway-parser.iml",
                tool.getRelativePath(to, from),
                "Child file using Windows drive letter");
        to = "C:/foo/child";
        from = "C:/foo/master";
        assertEquals(
                ".." + File.separator + "child",
                tool.getRelativePath(to, from),
                "Sibling directory using Windows drive letter");
        to = "/myproject/myproject-module1";
        from = "/myproject/myproject";
        assertEquals(
                ".." + File.separator + "myproject-module1",
                tool.getRelativePath(to, from),
                "Sibling directory with similar name");

        // Normalized paths as described in MSITE-284
        assertEquals(
                ".." + File.separator + "project-module-1" + File.separator + "src" + File.separator + "site",
                tool.getRelativePath(
                        "Z:\\dir\\project\\project-module-1\\src\\site",
                        "Z:\\dir\\project\\project-module-1\\..\\project-parent"));
        assertEquals(
                ".." + File.separator + ".." + File.separator + ".." + File.separator + "project-parent",
                tool.getRelativePath(
                        "Z:\\dir\\project\\project-module-1\\..\\project-parent",
                        "Z:\\dir\\project\\project-module-1\\src\\site"));

        assertEquals(".." + File.separator + "foo", tool.getRelativePath("../../foo/foo", "../../foo/bar"));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetSiteDescriptorFromBasedir() throws Exception {
        assertNotNull(tool);

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub("site-tool-test");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), SiteTool.DEFAULT_LOCALE)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), Locale.ENGLISH)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        String siteDir = "src/blabla";
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), siteDir), SiteTool.DEFAULT_LOCALE)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "blabla" + File.separator
                        + "site.xml");

        project = new SiteToolMavenProjectStub("site-tool-locales-test/full");
        final Locale BAVARIAN = new Locale("de", "DE", "BY");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), SiteTool.DEFAULT_LOCALE)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), BAVARIAN)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
                        + "site_de_DE_BY.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), Locale.GERMANY)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), Locale.ENGLISH)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), Locale.GERMAN)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");

        project = new SiteToolMavenProjectStub("site-tool-locales-test/language_country");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), SiteTool.DEFAULT_LOCALE)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), BAVARIAN)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
                        + "site_de_DE.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), Locale.GERMANY)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
                        + "site_de_DE.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), Locale.ENGLISH)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), Locale.GERMAN)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");

        project = new SiteToolMavenProjectStub("site-tool-locales-test/language");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), SiteTool.DEFAULT_LOCALE)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), BAVARIAN)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
                        + "site_de.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), Locale.GERMANY)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
                        + "site_de.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), Locale.ENGLISH)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(new File(project.getBasedir(), "src/site"), Locale.GERMAN)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
                        + "site_de.xml");
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetSiteDescriptorFromRepository() throws Exception {
        assertNotNull(tool);

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub("site-tool-test");
        project.setGroupId("org.apache.maven");
        project.setArtifactId("maven-site");
        project.setVersion("1.0");
        String result = getLocalRepoDir() + File.separator + "org" + File.separator + "apache" + File.separator
                + "maven" + File.separator + "maven-site" + File.separator + "1.0" + File.separator
                + "maven-site-1.0-site.xml";

        assertEquals(
                tool.getSiteDescriptorFromRepository(
                                project,
                                newRepoSession(),
                                project.getRemoteProjectRepositories(),
                                SiteTool.DEFAULT_LOCALE)
                        .toString(),
                result);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetSiteModel() throws Exception {
        assertNotNull(tool);

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub("site-tool-test");
        List<MavenProject> reactorProjects = new ArrayList<>();

        // model from current local build
        SiteModel model = tool.getSiteModel(
                new File(project.getBasedir(), "src/site"),
                SiteTool.DEFAULT_LOCALE,
                project,
                reactorProjects,
                newRepoSession(),
                project.getRemoteProjectRepositories());
        assertNotNull(model);
        assertNotNull(model.getBannerLeft());
        assertEquals("Maven Site", model.getBannerLeft().getName());
        assertEquals(
                "http://maven.apache.org/images/apache-maven-project.png",
                model.getBannerLeft().getImage().getSrc());
        assertEquals("http://maven.apache.org/", model.getBannerLeft().getHref());
        assertNotNull(model.getBannerRight());
        assertNull(model.getBannerRight().getName());
        assertEquals(
                "http://maven.apache.org/images/maven-small.gif",
                model.getBannerRight().getImage().getSrc());
        assertNull(model.getBannerRight().getHref());

        // model from repo: https://repo1.maven.org/maven2/org/apache/maven/maven/3.8.6/maven-3.8.6-site.xml
        project.setBasedir(null);
        project.setGroupId("org.apache.maven");
        project.setArtifactId("maven");
        project.setVersion("3.8.6");
        SiteModel modelFromRepo = tool.getSiteModel(
                null,
                SiteTool.DEFAULT_LOCALE,
                project,
                reactorProjects,
                newRepoSession(),
                project.getRemoteProjectRepositories());
        assertNotNull(modelFromRepo);
        assertNotNull(modelFromRepo.getBannerLeft());
        assertEquals("dummy", modelFromRepo.getBannerLeft().getName());
        assertEquals(
                "https://maven.apache.org/images/apache-maven-project.png",
                modelFromRepo.getBannerLeft().getImage().getSrc());
        assertEquals("https://maven.apache.org/", modelFromRepo.getBannerLeft().getHref());
        assertNull(modelFromRepo.getBannerRight());
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetDefaultSiteModel() throws Exception {
        assertNotNull(tool);

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub("no-site-test");
        String siteDirectory = "src/site";
        List<MavenProject> reactorProjects = new ArrayList<>();

        SiteModel model = tool.getSiteModel(
                new File(project.getBasedir(), siteDirectory),
                SiteTool.DEFAULT_LOCALE,
                project,
                reactorProjects,
                newRepoSession(),
                project.getRemoteProjectRepositories());
        assertNotNull(model);
    }

    @Test
    public void testGetAvailableLocales() throws Exception {
        assertEquals(Collections.singletonList(SiteTool.DEFAULT_LOCALE), tool.getSiteLocales("default"));

        assertEquals(
                Arrays.asList(SiteTool.DEFAULT_LOCALE, Locale.FRENCH, Locale.ITALIAN),
                tool.getSiteLocales("default,fr,it"));

        // by default, only DEFAULT_LOCALE
        assertEquals(Collections.singletonList(SiteTool.DEFAULT_LOCALE), tool.getSiteLocales(""));
    }

    @Test
    public void testGetInterpolatedSiteDescriptorContent() throws Exception {
        assertNotNull(tool);

        File pomXmlFile = getTestFile("src/test/resources/unit/interpolated-site/pom.xml");
        assertNotNull(pomXmlFile);
        assertTrue(pomXmlFile.exists());

        File descriptorFile = getTestFile("src/test/resources/unit/interpolated-site/src/site/site.xml");
        assertNotNull(descriptorFile);
        assertTrue(descriptorFile.exists());

        String siteDescriptorContent = FileUtils.fileRead(descriptorFile);
        assertNotNull(siteDescriptorContent);
        assertTrue(siteDescriptorContent.contains("${project.name}"));
        assertFalse(siteDescriptorContent.contains(
                "Interpolatesite &quot;quoted&quot; &amp; &apos;quoted&apos; &lt;sdf&gt;"));

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub("interpolated-site");
        List<MavenProject> reactorProjects = Collections.<MavenProject>singletonList(project);

        siteDescriptorContent =
                tool.getInterpolatedSiteDescriptorContent(new HashMap<>(), project, siteDescriptorContent);
        assertNotNull(siteDescriptorContent);
        assertFalse(siteDescriptorContent.contains("${project.name}"));
        assertTrue(siteDescriptorContent.contains(
                "Interpolatesite &quot;quoted&quot; &amp; &apos;quoted&apos; &lt;sdf&gt;"));

        SiteModel model = tool.getSiteModel(
                new File(project.getBasedir(), "src/site"),
                SiteTool.DEFAULT_LOCALE,
                project,
                reactorProjects,
                newRepoSession(),
                project.getRemoteProjectRepositories());
        assertNotNull(model);

        assertEquals(
                "Test " + project.getName(),
                model.getBody().getMenus().get(0).getItems().get(1).getName());
    }

    // MSHARED-217 -> DOXIATOOLS-34 -> DOXIASITETOOLS-118
    @Test
    public void testSiteModelInheritanceAndInterpolation() throws Exception {
        assertNotNull(tool);

        SiteToolMavenProjectStub parentProject = new SiteToolMavenProjectStub("interpolation-parent-test");
        parentProject.setDistgributionManagementSiteUrl("dav+https://davs.codehaus.org/site");

        SiteToolMavenProjectStub childProject = new SiteToolMavenProjectStub("interpolation-child-test");
        childProject.setParent(parentProject);
        childProject.setDistgributionManagementSiteUrl("dav+https://davs.codehaus.org/site/child");

        List<MavenProject> reactorProjects = Collections.<MavenProject>singletonList(parentProject);

        SiteModel model = tool.getSiteModel(
                new File(childProject.getBasedir(), "src/site"),
                SiteTool.DEFAULT_LOCALE,
                childProject,
                reactorProjects,
                newRepoSession(),
                childProject.getRemoteProjectRepositories());
        assertNotNull(model);

        writeModel(model, "unit/interpolation-child-test/effective-site.xml");

        assertEquals("MSHARED-217 Child", model.getName());
        // late (classical) interpolation
        assertEquals(
                "project.artifactId = mshared-217-child", model.getBannerLeft().getName());
        // early interpolation: DOXIASITETOOLS-158
        assertEquals(
                "this.artifactId = mshared-217-parent", model.getBannerRight().getName());
        // href rebase
        assertEquals(
                "../../index.html",
                model.getBody().getBreadcrumbs().iterator().next().getHref());
        Iterator<LinkItem> links = model.getBody().getLinks().iterator();
        // late interpolation of pom content
        assertEquals("project.name = MSHARED-217 Child", links.next().getName());
        assertEquals("name = name property", links.next().getName());
        // early interpolation: DOXIASITETOOLS-158
        assertEquals("this.name = MSHARED-217 Parent", links.next().getName());

        // late interpolation of project properties
        assertEquals("my_property = from child pom.xml", links.next().getName());
        // early interpolation of project properties: DOXIASITETOOLS-158
        assertEquals("this.my_property = from parent pom.xml", links.next().getName());

        // Env Var interpolation
        String envPath = links.next().getName();
        assertTrue(envPath.startsWith("env.PATH = "));
        assertFalse(envPath.contains("${"));
        assertNotSame("env.PATH = PATH property from pom", envPath);

        // property overrides env
        assertEquals("PATH = PATH property from pom", links.next().getName());
    }

    /**
     * @throws Exception
     */
    @Test
    public void testConvertOldToNewSiteModel() throws Exception {
        assertNotNull(tool);

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub("old-to-new-site-model-conversion-test");
        List<MavenProject> reactorProjects = new ArrayList<MavenProject>();

        // model from current local build
        SiteModel model = tool.getSiteModel(
                new File(project.getBasedir(), "src/site"),
                SiteTool.DEFAULT_LOCALE,
                project,
                reactorProjects,
                newRepoSession(),
                project.getRemoteProjectRepositories());
        assertNotNull(model);

        File descriptorFile =
                getTestFile("src/test/resources/unit/old-to-new-site-model-conversion-test/src/site/new-site.xml");
        assertNotNull(descriptorFile);
        assertTrue(descriptorFile.exists());

        String siteDescriptorContent = FileUtils.fileRead(descriptorFile);
        SiteModel newModel = new SiteXpp3Reader().read(new StringReader(siteDescriptorContent));
        assertNotNull(newModel);
        assertEquals(newModel, model);
    }

    @Test
    public void testRequireParent() throws SiteToolException, Exception {
        assertNotNull(tool);

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub("require-parent-test");
        MavenProjectStub parentProject = new MavenProjectStub() {
            @Override
            public File getBasedir() {
                return null; // this should be a non reactor/local project
            }
        };
        parentProject.setGroupId("org.apache.maven.shared.its");
        parentProject.setArtifactId("mshared-217-parent");
        parentProject.setVersion("1.0-SNAPSHOT");
        project.setParent(parentProject);
        List<MavenProject> reactorProjects = new ArrayList<MavenProject>();

        RepositorySystemSession repoSession = newRepoSession();
        // coordinates for site descriptor: <groupId>:<artifactId>:xml:site:<version>
        new SiteToolMavenProjectStub("require-parent-test");
        org.eclipse.aether.artifact.Artifact parentArtifact = new org.eclipse.aether.artifact.DefaultArtifact(
                "org.apache.maven.shared.its:mshared-217-parent:xml:site:1.0-SNAPSHOT");
        File parentArtifactInRepoFile = new File(
                repoSession.getLocalRepository().getBasedir(),
                repoSession.getLocalRepositoryManager().getPathForLocalArtifact(parentArtifact));

        // model from current local build
        assertThrows(
                SiteToolException.class,
                () -> tool.getSiteModel(
                        new File(project.getBasedir(), "src/site"),
                        SiteTool.DEFAULT_LOCALE,
                        project,
                        reactorProjects,
                        repoSession,
                        project.getRemoteProjectRepositories()));

        // now copy parent site descriptor to repo
        FileUtils.copyFile(
                getTestFile("src/test/resources/unit/require-parent-test/parent-site.xml"), parentArtifactInRepoFile);
        try {
            tool.getSiteModel(
                    new File(project.getBasedir(), "src/site"),
                    SiteTool.DEFAULT_LOCALE,
                    project,
                    reactorProjects,
                    repoSession,
                    project.getRemoteProjectRepositories());
        } finally {
            parentArtifactInRepoFile.delete();
        }
    }

    private void writeModel(SiteModel model, String to) throws Exception {
        Writer writer = WriterFactory.newXmlWriter(getTestFile("target/test-classes/" + to));
        try {
            new SiteXpp3Writer().write(writer, model);
        } finally {
            IOUtil.close(writer);
        }
    }
}
