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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.site.decoration.LinkItem;
import org.apache.maven.doxia.site.decoration.Skin;
import org.apache.maven.doxia.site.decoration.io.xpp3.DecorationXpp3Writer;
import org.apache.maven.doxia.tools.stubs.SiteToolMavenProjectStub;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public class SiteToolTest
    extends PlexusTestCase
{
    /**
     * @return the repo.
     *
     * @throws Exception
     */
    protected ArtifactRepository getLocalRepo()
        throws Exception
    {
        String updatePolicyFlag = ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS;
        String checksumPolicyFlag = ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN;
        ArtifactRepositoryPolicy snapshotsPolicy = new ArtifactRepositoryPolicy( true, updatePolicyFlag,
                                                                                 checksumPolicyFlag );
        ArtifactRepositoryPolicy releasesPolicy = new ArtifactRepositoryPolicy( true, updatePolicyFlag,
                                                                                checksumPolicyFlag );
        ArtifactRepositoryFactory artifactRepositoryFactory = (ArtifactRepositoryFactory) lookup( ArtifactRepositoryFactory.ROLE );
        ArtifactRepositoryLayout defaultArtifactRepositoryLayout = (ArtifactRepositoryLayout) lookup(
                                                                                                      ArtifactRepositoryLayout.ROLE,
                                                                                                      "default" );
        return artifactRepositoryFactory.createArtifactRepository( "local", getTestFile( "target/local-repo" ).toURI().toURL()
            .toString(), defaultArtifactRepositoryLayout, snapshotsPolicy, releasesPolicy );
    }

    /**
     * @return the local repo directory.
     *
     * @throws Exception
     */
    protected File getLocalRepoDir()
        throws Exception
    {
        return new File( getLocalRepo().getBasedir() );
    }

    /**
     * @throws Exception
     */
    public void testGetDefaultSkinArtifact()
        throws Exception
    {
        SiteTool tool = (SiteTool) lookup( SiteTool.ROLE );
        assertNotNull( tool );

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub( "site-tool-test" );
        assertNotNull( tool.getDefaultSkinArtifact( getLocalRepo(), project.getRemoteArtifactRepositories() ) );
    }

    /**
     * @throws Exception
     */
    public void testGetSkinArtifactFromRepository()
        throws Exception
    {
        SiteTool tool = (SiteTool) lookup( SiteTool.ROLE );
        assertNotNull( tool );

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub( "site-tool-test" );
        DecorationModel decorationModel = new DecorationModel();
        Skin skin = new Skin();
        skin.setGroupId( "org.apache.maven.skins" );
        skin.setArtifactId( "maven-stylus-skin" );
        decorationModel.setSkin( skin );
        assertNotNull( tool.getSkinArtifactFromRepository( getLocalRepo(), project.getRemoteArtifactRepositories(),
                                                           decorationModel ) );
    }

    private void checkGetRelativePathDirectory( SiteTool tool, String relative, String to, String from )
    {
        assertEquals( relative, tool.getRelativePath( to, from ) );
        assertEquals( relative, tool.getRelativePath( to + '/', from ) );
        assertEquals( relative, tool.getRelativePath( to, from + '/' ) );
        assertEquals( relative, tool.getRelativePath( to + '/', from + '/' ) );
    }

    /**
     * @throws Exception
     */
    public void testGetRelativePath()
        throws Exception
    {
        SiteTool tool = (SiteTool) lookup( SiteTool.ROLE );
        assertNotNull( tool );

        checkGetRelativePathDirectory( tool, "", "http://maven.apache.org", "http://maven.apache.org" );

        checkGetRelativePathDirectory( tool, ".." + File.separator + "..", "http://maven.apache.org",
                                       "http://maven.apache.org/plugins/maven-site-plugin" );

        checkGetRelativePathDirectory( tool, "plugins" + File.separator + "maven-site-plugin",
                                       "http://maven.apache.org/plugins/maven-site-plugin", "http://maven.apache.org"                         );

        checkGetRelativePathDirectory( tool, "", "dav:https://maven.apache.org", "dav:https://maven.apache.org" );

        checkGetRelativePathDirectory( tool, "plugins" + File.separator + "maven-site-plugin",
                                       "dav:http://maven.apache.org/plugins/maven-site-plugin",
                                       "dav:http://maven.apache.org" );

        checkGetRelativePathDirectory( tool, "", "scm:svn:https://maven.apache.org", "scm:svn:https://maven.apache.org" );

        checkGetRelativePathDirectory( tool, "plugins" + File.separator + "maven-site-plugin",
                                       "scm:svn:https://maven.apache.org/plugins/maven-site-plugin",
                                       "scm:svn:https://maven.apache.org" );

        String to = "http://maven.apache.org/downloads.html";
        String from = "http://maven.apache.org/index.html";

        // MSITE-600, MSHARED-203
        to = "file:///tmp/bloop";
        from = "scp://localhost:/tmp/blop";
        assertEquals( tool.getRelativePath( to, from ), to );

        // note: 'tmp' is the host here which is probably not the intention, but at least the result is correct
        to = "file://tmp/bloop";
        from = "scp://localhost:/tmp/blop";
        assertEquals( to, tool.getRelativePath( to, from ) );

        // Tests between files as described in MIDEA-102
        to = "C:/dev/voca/gateway/parser/gateway-parser.iml";
        from = "C:/dev/voca/gateway/";
        assertEquals( "Child file using Windows drive letter",
                      "parser" + File.separator + "gateway-parser.iml", tool.getRelativePath( to, from ) );
        to = "C:/foo/child";
        from = "C:/foo/master";
        assertEquals( "Sibling directory using Windows drive letter",
                      ".." + File.separator + "child", tool.getRelativePath( to, from ) );
        to = "/myproject/myproject-module1";
        from = "/myproject/myproject";
        assertEquals( "Sibling directory with similar name",
                      ".." + File.separator + "myproject-module1", tool.getRelativePath( to, from ) );

        // Normalized paths as described in MSITE-284
        assertEquals( ".." + File.separator + "project-module-1" + File.separator + "src" + File.separator + "site",
                      tool.getRelativePath( "Z:\\dir\\project\\project-module-1\\src\\site",
                                            "Z:\\dir\\project\\project-module-1\\..\\project-parent" ) );
        assertEquals( ".." + File.separator + ".." + File.separator + ".." + File.separator + "project-parent",
                      tool.getRelativePath( "Z:\\dir\\project\\project-module-1\\..\\project-parent",
                                            "Z:\\dir\\project\\project-module-1\\src\\site" ) );

        assertEquals( ".." + File.separator + "foo", tool.getRelativePath( "../../foo/foo", "../../foo/bar" ) );
    }

    /**
     * @throws Exception
     */
    public void testGetSiteDescriptorFromBasedir()
        throws Exception
    {
        SiteTool tool = (SiteTool) lookup( SiteTool.ROLE );
        assertNotNull( tool );

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub( "site-tool-test" );
        assertEquals( tool.getSiteDescriptor( new File( project.getBasedir(), "src/site" ), null ).toString(),
            project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml" );
        assertEquals( tool.getSiteDescriptor( new File( project.getBasedir(), "src/site" ), Locale.ENGLISH ).toString(),
            project.getBasedir() + File.separator + "src" + File.separator + "site" + File.separator + "site.xml" );
        String siteDir = "src/blabla";
        assertEquals( tool.getSiteDescriptor( new File( project.getBasedir(), siteDir ), null ).toString(),
            project.getBasedir() + File.separator + "src" + File.separator + "blabla" + File.separator + "site.xml" );
    }

    /**
     * @throws Exception
     */
    public void testGetSiteDescriptorFromRepository()
        throws Exception
    {
        DefaultSiteTool tool = (DefaultSiteTool) lookup( SiteTool.ROLE );
        assertNotNull( tool );

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub( "site-tool-test" );
        project.setGroupId( "org.apache.maven" );
        project.setArtifactId( "maven-site" );
        project.setVersion( "1.0" );
        String result = getLocalRepoDir() + File.separator + "org" + File.separator + "apache" + File.separator
            + "maven" + File.separator + "maven-site" + File.separator + "1.0" + File.separator
            + "maven-site-1.0-site.xml";

        assertEquals( tool.getSiteDescriptorFromRepository( project, getLocalRepo(),
                                                            project.getRemoteArtifactRepositories(), Locale.ENGLISH )
            .toString(), result );
    }

    /**
     * @throws Exception
     */
    public void testGetDecorationModel()
        throws Exception
    {
        SiteTool tool = (SiteTool) lookup( SiteTool.ROLE );
        assertNotNull( tool );

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub( "site-tool-test" );
        List<MavenProject> reactorProjects = new ArrayList<MavenProject>();

        // model from current local build
        DecorationModel model =
            tool.getDecorationModel( new File( project.getBasedir(), "src/site" ), Locale.getDefault(), project,
                                     reactorProjects, getLocalRepo(), project.getRemoteArtifactRepositories() );
        assertNotNull( model );
        assertNotNull( model.getBannerLeft() );
        assertEquals( "Maven Site", model.getBannerLeft().getName() );
        assertEquals( "http://maven.apache.org/images/apache-maven-project.png", model.getBannerLeft().getSrc() );
        assertEquals( "http://maven.apache.org/", model.getBannerLeft().getHref() );
        assertNotNull( model.getBannerRight() );
        assertNull( model.getBannerRight().getName() );
        assertEquals( "http://maven.apache.org/images/maven-small.gif", model.getBannerRight().getSrc() );
        assertNull( model.getBannerRight().getHref() );

        // model from repo: https://repo1.maven.org/maven2/org/apache/maven/maven-site/1.0/maven-site-1.0-site.xml
        // TODO Enable this test as soon as we haven a site.xml with head content as string
        /*project.setBasedir( null );
        project.setGroupId( "org.apache.maven" );
        project.setArtifactId( "maven-site" );
        project.setVersion( "1.0" );
        DecorationModel modelFromRepo =
            tool.getDecorationModel( null, Locale.getDefault(), project, reactorProjects, getLocalRepo(),
                                     project.getRemoteArtifactRepositories() );
        assertNotNull( modelFromRepo );
        assertNotNull( modelFromRepo.getBannerLeft() );
        assertEquals( "Maven", modelFromRepo.getBannerLeft().getName() );
        assertEquals( "images/apache-maven-project-2.png", modelFromRepo.getBannerLeft().getSrc() );
        assertEquals( "http://maven.apache.org/", modelFromRepo.getBannerLeft().getHref() );
        assertNotNull( modelFromRepo.getBannerRight() );
        assertNull( modelFromRepo.getBannerRight().getName() );
        assertEquals( "images/maven-logo-2.gif", modelFromRepo.getBannerRight().getSrc() );
        assertNull( modelFromRepo.getBannerRight().getHref() );*/
    }

    /**
     * @throws Exception
     */
    public void testGetDefaultDecorationModel()
        throws Exception
    {
        SiteTool tool = (SiteTool) lookup( SiteTool.ROLE );
        assertNotNull( tool );

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub( "no-site-test" );
        String siteDirectory = "src/site";
        List<MavenProject> reactorProjects = new ArrayList<MavenProject>();

        DecorationModel model =
            tool.getDecorationModel( new File( project.getBasedir(), siteDirectory ), Locale.getDefault(), project,
                                     reactorProjects, getLocalRepo(), project.getRemoteArtifactRepositories() );
        assertNotNull( model );
    }

    public void testGetAvailableLocales()
                    throws Exception
    {
        SiteTool tool = (SiteTool) lookup( SiteTool.ROLE );

        assertEquals( Arrays.asList( new Locale[] { SiteTool.DEFAULT_LOCALE } ), tool.getSiteLocales( "en" ) );

        assertEquals( Arrays.asList( new Locale[] { SiteTool.DEFAULT_LOCALE, Locale.FRENCH, Locale.ITALIAN } ),
                      tool.getSiteLocales( "en,fr,it" ) );

        // by default, only DEFAULT_LOCALE
        assertEquals( Arrays.asList( new Locale[] { SiteTool.DEFAULT_LOCALE } ), tool.getSiteLocales( "" ) );
    }

    public void testGetInterpolatedSiteDescriptorContent()
        throws Exception
    {
        SiteTool tool = (SiteTool) lookup( SiteTool.ROLE );
        assertNotNull( tool );

        File pomXmlFile = getTestFile( "src/test/resources/unit/interpolated-site/pom.xml" );
        assertNotNull( pomXmlFile );
        assertTrue( pomXmlFile.exists() );

        File descriptorFile = getTestFile( "src/test/resources/unit/interpolated-site/src/site/site.xml" );
        assertNotNull( descriptorFile );
        assertTrue( descriptorFile.exists() );

        String siteDescriptorContent = FileUtils.fileRead( descriptorFile );
        assertNotNull( siteDescriptorContent );
        assertTrue( siteDescriptorContent.contains( "${project.name}" ) );
        assertFalse( siteDescriptorContent.contains( "Interpolatesite" ) );

        SiteToolMavenProjectStub project = new SiteToolMavenProjectStub( "interpolated-site" );

        SiteTool siteTool = (SiteTool) lookup( SiteTool.ROLE );
        siteDescriptorContent =
            siteTool.getInterpolatedSiteDescriptorContent( new HashMap<String, String>(), project,
                                                           siteDescriptorContent );
        assertNotNull( siteDescriptorContent );
        assertFalse( siteDescriptorContent.contains( "${project.name}" ) );
        assertTrue( siteDescriptorContent.contains( "Interpolatesite" ) );
    }

    // MSHARED-217 -> DOXIATOOLS-34 -> DOXIASITETOOLS-118
    public void testDecorationModelInheritanceAndInterpolation()
        throws Exception
    {
        SiteTool tool = (SiteTool) lookup( SiteTool.ROLE );
        assertNotNull( tool );

        SiteToolMavenProjectStub parentProject = new SiteToolMavenProjectStub( "interpolation-parent-test" );
        parentProject.setDistgributionManagementSiteUrl( "dav:https://davs.codehaus.org/site" );

        SiteToolMavenProjectStub childProject = new SiteToolMavenProjectStub( "interpolation-child-test" );
        childProject.setParent( parentProject );
        childProject.setDistgributionManagementSiteUrl( "dav:https://davs.codehaus.org/site/child" );

        List<MavenProject> reactorProjects = Collections.<MavenProject>singletonList( parentProject );

        DecorationModel model = tool.getDecorationModel( new File( childProject.getBasedir(), "src/site" ),
                                                         Locale.getDefault(), childProject, reactorProjects,
                                                         getLocalRepo(), childProject.getRemoteArtifactRepositories() );
        assertNotNull( model );

        writeModel( model, "unit/interpolation-child-test/effective-site.xml" );

        assertEquals( "MSHARED-217 Child", model.getName() );
        // late (classical) interpolation
        assertEquals( "project.artifactId = mshared-217-child", model.getBannerLeft().getName() );
        // early interpolation: DOXIASITETOOLS-158
        assertEquals( "this.artifactId = mshared-217-parent", model.getBannerRight().getName() );
        // href rebase
        assertEquals( "../../index.html", model.getBody().getBreadcrumbs().iterator().next().getHref() );
        Iterator<LinkItem> links = model.getBody().getLinks().iterator();
        // late interpolation of pom content (which happens first: properties can't override)
        assertEquals( "project.name = MSHARED-217 Child", links.next().getName() );
        assertEquals( "name = MSHARED-217 Child", links.next().getName() );
        // early interpolation: DOXIASITETOOLS-158
        assertEquals( "this.name = MSHARED-217 Parent", links.next().getName() );

        // late interpolation of project properties
        assertEquals( "my_property = from child pom.xml", links.next().getName() );
        // early interpolation of project properties: DOXIASITETOOLS-158
        assertEquals( "this.my_property = from parent pom.xml", links.next().getName() );

        // Env Var interpolation
        String envPath = links.next().getName();
        assertTrue( envPath.startsWith( "env.PATH = " ) );
        assertFalse( envPath.contains( "${" ) );
        assertNotSame( "env.PATH = PATH property from pom", envPath );

        // property overrides env
        assertEquals( "PATH = PATH property from pom", links.next().getName() );
    }

    private void writeModel( DecorationModel model, String to )
        throws Exception
    {
        Writer writer = WriterFactory.newXmlWriter( getTestFile( "target/test-classes/" + to ) );
        try
        {
            new DecorationXpp3Writer().write( writer, model );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }
}
