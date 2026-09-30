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
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

import org.apache.maven.api.Artifact;
import org.apache.maven.api.Project;
import org.apache.maven.api.RemoteRepository;
import org.apache.maven.api.Session;
import org.apache.maven.api.services.LocalRepositoryManager;
import org.apache.maven.doxia.site.LinkItem;
import org.apache.maven.doxia.site.SiteModel;
import org.apache.maven.doxia.site.Skin;
import org.apache.maven.doxia.site.io.xpp3.SiteXpp3Reader;
import org.apache.maven.doxia.site.io.xpp3.SiteXpp3Writer;
import org.apache.maven.doxia.tools.stubs.SiteToolProjectStub;
import org.apache.maven.impl.standalone.ApiRunner;
import org.codehaus.plexus.testing.PlexusTest;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.XmlStreamWriter;
import org.junit.jupiter.api.BeforeEach;
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
class SiteToolTest {

    @Inject
    private DefaultSiteTool tool;

    /**
     * The tests resolve the skin and the site descriptors from a local repository seeded from
     * <code>src/test/resources/local-repo</code>: the standalone Maven 4 session (maven-impl 4.0.0-rc-7) registers
     * no transporter, so nothing can be downloaded.
     */
    @BeforeEach
    void setUp() throws Exception {
        File localRepo = getLocalRepoDir();
        FileUtils.deleteDirectory(localRepo);
        Path source = getTestFile("src/test/resources/local-repo").toPath();
        try (Stream<Path> files = Files.walk(source)) {
            for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                Path target = localRepo.toPath().resolve(source.relativize(file));
                Files.createDirectories(target.getParent());
                Files.copy(file, target);
                markAsFromCentral(target);
            }
        }
        // the skin is only looked at as a file: any jar will do
        Path skinJar = localRepo
                .toPath()
                .resolve("org/apache/maven/skins/maven-fluido-skin/2.1.0/maven-fluido-skin-2.1.0.jar");
        Files.createDirectories(skinJar.getParent());
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(skinJar))) {
            out.putNextEntry(new JarEntry("META-INF/maven/site.vm"));
            out.closeEntry();
        }
        markAsFromCentral(skinJar);

        session = newSession();
    }

    /**
     * The local repository only trusts an artifact file when its <code>_remote.repositories</code> says which
     * repository it comes from (enhanced local repository manager).
     */
    private static void markAsFromCentral(Path artifactFile) throws IOException {
        if (artifactFile.getFileName().toString().startsWith("maven-metadata-")) {
            return;
        }
        Files.write(
                artifactFile.resolveSibling("_remote.repositories"),
                (artifactFile.getFileName() + ">central=\n").getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    /**
     * @return the local repo directory.
     *
     * @throws Exception
     */
    protected File getLocalRepoDir() throws Exception {
        return getTestFile("target/local-repo");
    }

    /**
     * The Maven 4 session, standing in for the one Maven gives a plugin. Its local repository is
     * <code>target/local-repo</code>.
     */
    private Session session;

    /**
     * The session of the standalone runner has fixed user and system properties: substitute the ones under test.
     */
    private static Session withProperties(Session session, Map<String, String> user, Map<String, String> system) {
        return (Session) Proxy.newProxyInstance(
                SiteToolTest.class.getClassLoader(), new Class<?>[] {Session.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getUserProperties":
                            return user;
                        case "getSystemProperties":
                            return system;
                        default:
                            try {
                                return method.invoke(session, args);
                            } catch (InvocationTargetException e) {
                                throw e.getCause();
                            }
                    }
                });
    }

    private Session newSession() throws Exception {
        return ApiRunner.createSession(injector -> {}, getLocalRepoDir().toPath());
    }

    private List<RemoteRepository> remoteRepositories() {
        return Collections.singletonList(session.createRemoteRepository("central", "https://repo1.maven.org/maven2"));
    }

    /**
     * @throws Exception
     */
    @Test
    void getSkinArtifactFromRepository() throws Exception {
        assertNotNull(tool);

        Skin skin = new Skin();
        skin.setGroupId("org.apache.maven.skins");
        skin.setArtifactId("maven-fluido-skin");
        assertNotNull(tool.getSkinArtifactFromRepository(session, remoteRepositories(), skin));
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
    void getRelativePath() throws Exception {
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
    void getSiteDescriptorFromBasedir() throws Exception {
        assertNotNull(tool);

        SiteToolProjectStub project = new SiteToolProjectStub("site-tool-test");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), SiteTool.DEFAULT_LOCALE)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), Locale.ENGLISH)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        String siteDir = "src/blabla";
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve(siteDir).toFile(), SiteTool.DEFAULT_LOCALE)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "blabla" + File.separator
                        + "site.xml");

        project = new SiteToolProjectStub("site-tool-locales-test/full");
        Locale bavarian = new Locale("de", "DE", "BY");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), SiteTool.DEFAULT_LOCALE)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), bavarian)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
                        + "site_de_DE_BY.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), Locale.GERMANY)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), Locale.ENGLISH)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), Locale.GERMAN)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");

        project = new SiteToolProjectStub("site-tool-locales-test/language_country");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), SiteTool.DEFAULT_LOCALE)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), bavarian)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
                        + "site_de_DE.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), Locale.GERMANY)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
                        + "site_de_DE.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), Locale.ENGLISH)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), Locale.GERMAN)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");

        project = new SiteToolProjectStub("site-tool-locales-test/language");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), SiteTool.DEFAULT_LOCALE)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), bavarian)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
                        + "site_de.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), Locale.GERMANY)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
                        + "site_de.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), Locale.ENGLISH)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml");
        assertEquals(
                tool.getSiteDescriptor(project.getBasedir().resolve("src/site").toFile(), Locale.GERMAN)
                        .toString(),
                project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator
                        + "site_de.xml");
    }

    /**
     * @throws Exception
     */
    @Test
    void getSiteDescriptorFromRepository() throws Exception {
        assertNotNull(tool);

        SiteToolProjectStub project = new SiteToolProjectStub("site-tool-test");
        project.setGroupId("org.apache.maven");
        project.setArtifactId("maven-site");
        project.setVersion("1.0");
        String result = getLocalRepoDir() + File.separator + "org" + File.separator + "apache" + File.separator
                + "maven" + File.separator + "maven-site" + File.separator + "1.0" + File.separator
                + "maven-site-1.0-site.xml";

        assertEquals(
                tool.getSiteDescriptorFromRepository(project, session, remoteRepositories(), SiteTool.DEFAULT_LOCALE)
                        .toString(),
                result);
    }

    /**
     * @throws Exception
     */
    @Test
    void getSiteModel() throws Exception {
        assertNotNull(tool);

        SiteToolProjectStub project = new SiteToolProjectStub("site-tool-test");
        List<Project> reactorProjects = new ArrayList<>();

        // model from current local build
        SiteModel model = tool.getSiteModel(
                project.getBasedir().resolve("src/site").toFile(),
                SiteTool.DEFAULT_LOCALE,
                project,
                reactorProjects,
                session,
                remoteRepositories());
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
                null, SiteTool.DEFAULT_LOCALE, project, reactorProjects, session, remoteRepositories());
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
    void getDefaultSiteModel() throws Exception {
        assertNotNull(tool);

        SiteToolProjectStub project = new SiteToolProjectStub("no-site-test");
        String siteDirectory = "src/site";
        List<Project> reactorProjects = new ArrayList<>();

        SiteModel model = tool.getSiteModel(
                project.getBasedir().resolve(siteDirectory).toFile(),
                SiteTool.DEFAULT_LOCALE,
                project,
                reactorProjects,
                session,
                remoteRepositories());
        assertNotNull(model);
    }

    @Test
    void getAvailableLocales() throws Exception {
        assertEquals(Collections.singletonList(SiteTool.DEFAULT_LOCALE), tool.getSiteLocales("default"));

        assertEquals(
                Arrays.asList(SiteTool.DEFAULT_LOCALE, Locale.FRENCH, Locale.ITALIAN),
                tool.getSiteLocales("default,fr,it"));

        // by default, only DEFAULT_LOCALE
        assertEquals(Collections.singletonList(SiteTool.DEFAULT_LOCALE), tool.getSiteLocales(""));
    }

    @Test
    void interpolatedSiteDescriptor() throws Exception {
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

        SiteToolProjectStub project = new SiteToolProjectStub("interpolated-site");
        List<Project> reactorProjects = Collections.<Project>singletonList(project);

        SiteModel model = tool.getSiteModel(
                project.getBasedir().resolve("src/site").toFile(),
                SiteTool.DEFAULT_LOCALE,
                project,
                reactorProjects,
                session,
                remoteRepositories());
        assertNotNull(model);

        assertEquals(
                "Test " + project.getModel().getName(),
                model.getBody().getMenus().get(0).getItems().get(1).getName());
    }

    // MSHARED-217 -> DOXIATOOLS-34 -> DOXIASITETOOLS-118
    @Test
    void siteModelInheritanceAndInterpolation() throws Exception {
        assertNotNull(tool);

        SiteToolProjectStub parentProject = new SiteToolProjectStub("interpolation-parent-test");
        parentProject.setDistgributionManagementSiteUrl("dav+https://davs.codehaus.org/site");

        SiteToolProjectStub childProject = new SiteToolProjectStub("interpolation-child-test");
        childProject.setParent(parentProject);
        childProject.setDistgributionManagementSiteUrl("dav+https://davs.codehaus.org/site/child");
        Map<String, String> effectiveProperties = new HashMap<>();
        effectiveProperties.putAll(parentProject.getModel().getProperties());
        effectiveProperties.putAll(childProject.getModel().getProperties());
        childProject.setProperties(effectiveProperties);

        List<Project> reactorProjects = Collections.<Project>singletonList(parentProject);
        Map<String, String> userProperties = new HashMap<>();
        userProperties.put("userProp1", "from user properties");
        userProperties.put("my_property2", "from user properties");
        Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("systemProp1", "from system properties");
        systemProperties.put("my_property3", "from system properties");

        SiteModel model = tool.getSiteModel(
                childProject.getBasedir().resolve("src/site").toFile(),
                SiteTool.DEFAULT_LOCALE,
                childProject,
                reactorProjects,
                withProperties(session, userProperties, systemProperties),
                remoteRepositories());
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
        // must be overridden by user property
        assertEquals("my_property2 = from user properties", links.next().getName());
        // must not be overridden by system property
        assertEquals("my_property3 = from parent pom.xml", links.next().getName());
        // early interpolation of project properties: DOXIASITETOOLS-158
        assertEquals("this.my_property = from parent pom.xml", links.next().getName());

        // Env Var interpolation
        String envPath = links.next().getName();
        assertTrue(envPath.startsWith("env.PATH = "));
        assertFalse(envPath.contains("${"));
        assertNotSame("env.PATH = PATH property from pom", envPath);

        // property overrides env
        assertEquals("PATH = PATH property from pom", links.next().getName());

        // user properties
        assertEquals("userProp1 = from user properties", links.next().getName());

        // system properties
        assertEquals("systemProp1 = from system properties", links.next().getName());
    }

    /**
     * @throws Exception
     */
    @Test
    void convertOldToNewSiteModel() throws Exception {
        assertNotNull(tool);

        SiteToolProjectStub project = new SiteToolProjectStub("old-to-new-site-model-conversion-test");
        List<Project> reactorProjects = new ArrayList<Project>();

        // model from current local build
        SiteModel model = tool.getSiteModel(
                project.getBasedir().resolve("src/site").toFile(),
                SiteTool.DEFAULT_LOCALE,
                project,
                reactorProjects,
                session,
                remoteRepositories());
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
    void requireParent() throws Exception {
        assertNotNull(tool);

        SiteToolProjectStub project = new SiteToolProjectStub("require-parent-test");
        // no base directory: this should be a non reactor/local project
        SiteToolProjectStub parentProject =
                new SiteToolProjectStub("org.apache.maven.shared.its", "mshared-217-parent", "1.0-SNAPSHOT");
        project.setParent(parentProject);
        List<Project> reactorProjects = new ArrayList<Project>();

        // coordinates for site descriptor: <groupId>:<artifactId>:xml:site:<version>
        Artifact parentArtifact = session.createArtifact(
                "org.apache.maven.shared.its", "mshared-217-parent", "1.0-SNAPSHOT", "site", "xml", "xml");
        File parentArtifactInRepoFile = session.getService(LocalRepositoryManager.class)
                .getPathForLocalArtifact(session, session.getLocalRepository(), parentArtifact)
                .toFile();

        // model from current local build
        assertThrows(
                SiteToolException.class,
                () -> tool.getSiteModel(
                        project.getBasedir().resolve("src/site").toFile(),
                        SiteTool.DEFAULT_LOCALE,
                        project,
                        reactorProjects,
                        session,
                        remoteRepositories()));

        // now copy parent site descriptor to repo
        FileUtils.copyFile(
                getTestFile("src/test/resources/unit/require-parent-test/parent-site.xml"), parentArtifactInRepoFile);
        // the local repository only trusts files it knows the origin of (enhanced local repository manager)
        File remoteRepositoriesFile = new File(parentArtifactInRepoFile.getParentFile(), "_remote.repositories");
        Files.write(
                remoteRepositoriesFile.toPath(),
                (parentArtifactInRepoFile.getName() + ">central=\n").getBytes(StandardCharsets.UTF_8));
        try {
            // the session caches the outcome of a resolution, so the failed one above would be replayed
            tool.getSiteModel(
                    project.getBasedir().resolve("src/site").toFile(),
                    SiteTool.DEFAULT_LOCALE,
                    project,
                    reactorProjects,
                    newSession(),
                    remoteRepositories());
        } finally {
            parentArtifactInRepoFile.delete();
            remoteRepositoriesFile.delete();
        }
    }

    private void writeModel(SiteModel model, String to) throws Exception {
        Writer writer = new XmlStreamWriter(getTestFile("target/test-classes/" + to));
        try {
            new SiteXpp3Writer().write(writer, model);
        } finally {
            IOUtil.close(writer);
        }
    }
}
