package org.apache.maven.doxia.site.decoration.inheritance;

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

import java.io.IOException;
import java.io.Reader;

import java.util.List;

import org.apache.maven.doxia.site.decoration.Banner;
import org.apache.maven.doxia.site.decoration.Body;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.site.decoration.LinkItem;
import org.apache.maven.doxia.site.decoration.Logo;
import org.apache.maven.doxia.site.decoration.Menu;
import org.apache.maven.doxia.site.decoration.io.xpp3.DecorationXpp3Reader;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the inheritance assembler.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class DecorationModelInheritanceAssemblerTest
{
    private DecorationModelInheritanceAssembler assembler = new DefaultDecorationModelInheritanceAssembler();

    private static final String NAME = "Name";

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testInheritance()
        throws IOException, XmlPullParserException
    {
        DecorationModel childModel = readModel( "inheritance-child.xml" );
        DecorationModel parentModel = readModel( "inheritance-parent.xml" );

        assembler.assembleModelInheritance( NAME, childModel, parentModel, "http://maven.apache.org/doxia",
                                            "http://maven.apache.org" );
        DecorationModel expectedModel = readModel( "inheritance-expected.xml" );

        assertEquals( "Check result", expectedModel, childModel );

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel( "inheritance-child.xml" );
        assembler.assembleModelInheritance( NAME, childModel, parentModel, "scp://people.apache.org/doxia",
                                            "scp://people.apache.org" );
        assertEquals( "Check scp result", expectedModel, childModel );

        assertEquals( "Modified parent!", readModel( "inheritance-parent.xml" ), parentModel );

        // late inheritance in links can't be rebased: check friendly message
        parentModel.getBannerLeft().setHref( "${project.url}" );
        childModel = readModel( "inheritance-child.xml" );
        try
        {
            assembler.assembleModelInheritance( NAME, childModel, parentModel, "scp://people.apache.org/doxia",
                                                "scp://people.apache.org" );
            fail( "late interpolation in link should cause IllegalArgumentException" );
        }
        catch ( IllegalArgumentException iae )
        {
            assertTrue( iae.getMessage().startsWith( "site.xml late interpolation" ) );
        }
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testSuppressedInheritance()
            throws IOException, XmlPullParserException
    {
        DecorationModel unassembledChildModel = readModel( "inheritance-child-no-inheritance.xml" );
        DecorationModel childModel = readModel( "inheritance-child-no-inheritance.xml" );
        DecorationModel parentModel = readModel( "inheritance-parent.xml" );
        assembler.assembleModelInheritance( NAME, childModel, parentModel, "http://maven.apache.org/doxia",
                                            "http://maven.apache.org" );
        assertEquals( "Check result", unassembledChildModel, childModel );

        // 2 levels of inheritance
        DecorationModel childOfchildModel = new DecorationModel();
        assembler.assembleModelInheritance( "Child of Child", childOfchildModel, childModel,
                                            "http://maven.apache.org/doxia/child", "http://maven.apache.org/doxia" );
        assembler.assembleModelInheritance( NAME, childOfchildModel, parentModel, "http://maven.apache.org/doxia",
                                            "http://maven.apache.org" );
        // check that the 3 breadcrumb items from parent.xml are not inherited
        assertEquals( "child of child no inheritance: breadcrumbs count", 0,
                childOfchildModel.getBody().getBreadcrumbs().size() );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedWhenEmpty()
        throws IOException, XmlPullParserException
    {
        // Test an empty model avoids NPEs
        DecorationModel childModel = readModel( "empty.xml" );
        DecorationModel parentModel = readModel( "empty.xml" );

        assembler.assembleModelInheritance( NAME, childModel, parentModel, "http://maven.apache.org/doxia",
                                            "http://maven.apache.org" );
        DecorationModel mergedModel = readModel( "empty.xml" );

        assertEquals( "Check result", mergedModel, childModel );

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel( "empty.xml" );
        assembler.assembleModelInheritance( NAME, childModel, parentModel, "scp://people.apache.org/doxia",
                                            "scp://people.apache.org" );
        assertEquals( "Check scp result", mergedModel, childModel );

        assertEquals( "Modified parent!", readModel( "empty.xml" ), parentModel );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsNotResolvedForExternalUrls()
        throws IOException, XmlPullParserException
    {
        DecorationModel parentModel = readModel( "external-urls.xml" );
        DecorationModel childModel = readModel( "empty.xml" );

        assembler.assembleModelInheritance( NAME, childModel, parentModel, "http://maven.apache.org/doxia",
                                            "http://maven.apache.org" );
        assertPathsNotResolvedForExternalUrls( childModel );

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel( "empty.xml" );
        assembler.assembleModelInheritance( NAME, childModel, parentModel, "scp://people.apache.org/doxia",
                                            "scp://people.apache.org" );
        assertPathsNotResolvedForExternalUrls( childModel );

        assertEquals( "Modified parent!", readModel( "external-urls.xml" ), parentModel );
    }

    private static void assertPathsNotResolvedForExternalUrls( final DecorationModel childModel )
    {
        assertEquals( "check left banner href", "http://jakarta.apache.org/",
                childModel.getBannerLeft().getHref() );
        assertEquals( "check left banner image", "http://jakarta.apache.org/images/jakarta-logo.gif",
                childModel.getBannerLeft().getSrc() );

        assertEquals( "check right banner href", "http://jakarta.apache.org/commons/sandbox",
                childModel.getBannerRight().getHref() );
        assertEquals( "check right banner image", "http://jakarta.apache.org/commons/images/logo.png",
                childModel.getBannerRight().getSrc() );

        Logo poweredBy = childModel.getPoweredBy().get( 0 );
        assertEquals( "check powered by logo href", "http://tomcat.apache.org/", poweredBy.getHref() );
        assertEquals( "check powered by logo image", "http://tomcat.apache.org/logo.gif", poweredBy.getImg() );

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get( 0 );
        assertEquals( "check breadcrumb href", "http://www.apache.org/", breadcrumb.getHref() );

        LinkItem link = childModel.getBody().getLinks().get( 0 );
        assertEquals( "check link href", "http://www.bouncycastle.org", link.getHref() );

        Menu menu = childModel.getBody().getMenus().get( 0 );
        LinkItem menuItem = menu.getItems().get( 0 );
        assertEquals( "check menu item href", "http://www.apache.org/special/", menuItem.getHref() );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedForRelativeUrls()
        throws IOException, XmlPullParserException
    {
        DecorationModel parentModel = readModel( "relative-urls.xml" );
        DecorationModel childModel = readModel( "empty.xml" );

        assembler.assembleModelInheritance( NAME, childModel, parentModel, "http://maven.apache.org/doxia/",
                                            "http://maven.apache.org" );
        assertPathsResolvedForRelativeUrls( childModel );

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel( "empty.xml" );
        assembler.assembleModelInheritance( NAME, childModel, parentModel, "scp://people.apache.org/doxia",
                                            "scp://people.apache.org" );
        assertPathsResolvedForRelativeUrls( childModel );

        assertEquals( "Modified parent!", readModel( "relative-urls.xml" ), parentModel );
    }

    private static void assertPathsResolvedForRelativeUrls( final DecorationModel childModel )
    {
        assertEquals( "check left banner href", "../banner/left", childModel.getBannerLeft().getHref() );
        assertEquals( "check left banner image", "../images/jakarta-logo.gif",
                childModel.getBannerLeft().getSrc() );

        assertEquals( "check right banner href", "../banner/right/", childModel.getBannerRight().getHref() );
        assertEquals( "check right banner image", "../commons/images/logo.png",
                childModel.getBannerRight().getSrc() );

        Logo poweredBy = childModel.getPoweredBy().get( 0 );
        assertEquals( "check powered by logo href", "../tomcat", poweredBy.getHref() );
        assertEquals( "check powered by logo image", "../tomcat/logo.gif", poweredBy.getImg() );

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get( 0 );
        assertEquals( "check breadcrumb href", "../apache", breadcrumb.getHref() );

        LinkItem link = childModel.getBody().getLinks().get( 0 );
        assertEquals( "check link href", "../bouncycastle/", link.getHref() );

        Menu menu = childModel.getBody().getMenus().get( 0 );
        LinkItem menuItem = menu.getItems().get( 0 );
        assertEquals( "check menu item href", "../special/", menuItem.getHref() );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedForSubsiteUrls()
        throws IOException, XmlPullParserException
    {
        DecorationModel parentModel = readModel( "subsite-urls.xml" );
        DecorationModel childModel = readModel( "empty.xml" );

        assembler.assembleModelInheritance( NAME, childModel, parentModel, "http://maven.apache.org/doxia/",
                                            "http://maven.apache.org" );
        assembler.resolvePaths( childModel, "http://maven.apache.org/doxia" );

        assertPathsResolvedForSubsiteUrls( childModel );

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel( "empty.xml" );
        assembler.assembleModelInheritance( NAME, childModel, parentModel, "scp://people.apache.org/doxia",
                                            "scp://people.apache.org" );
        assembler.resolvePaths( childModel, "http://maven.apache.org/doxia" );
        assertPathsResolvedForSubsiteUrls( childModel );

        assertEquals( "Modified parent!", readModel( "subsite-urls.xml" ), parentModel );
    }

    private static void assertPathsResolvedForSubsiteUrls( final DecorationModel childModel )
    {
        assertEquals( "check left banner href", "../banner/left", childModel.getBannerLeft().getHref() );
        assertEquals( "check left banner image", "../images/jakarta-logo.gif",
                childModel.getBannerLeft().getSrc() );

        assertEquals( "check right banner href", "../banner/right/", childModel.getBannerRight().getHref() );
        assertEquals( "check right banner image", "../commons/images/logo.png",
                childModel.getBannerRight().getSrc() );

        Logo poweredBy = childModel.getPoweredBy().get( 0 );
        assertEquals( "check powered by logo href", "../tomcat", poweredBy.getHref() );
        assertEquals( "check powered by logo image", "../tomcat/logo.gif", poweredBy.getImg() );

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get( 0 );
        assertEquals( "check breadcrumb href", "../apache", breadcrumb.getHref() );

        LinkItem link = childModel.getBody().getLinks().get( 0 );
        assertEquals( "check link href", "../bouncycastle/", link.getHref() );

        Menu menu = childModel.getBody().getMenus().get( 0 );
        LinkItem menuItem = menu.getItems().get( 0 );
        assertEquals( "check menu item href", "../special/", menuItem.getHref() );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedForRelativeUrlsDepthOfTwo()
        throws IOException, XmlPullParserException
    {
        DecorationModel parentModel = readModel( "relative-urls.xml" );
        DecorationModel childModel = readModel( "empty.xml" );

        assembler.assembleModelInheritance( NAME, childModel, parentModel, "http://maven.apache.org/doxia/core",
                                            "http://maven.apache.org" );
        assertPathsResolvedForRelativeUrlsDepthOfTwo( childModel );

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel( "empty.xml" );
        assembler.assembleModelInheritance( NAME, childModel, parentModel, "scp://people.apache.org/doxia/core",
                                            "scp://people.apache.org" );
        assertPathsResolvedForRelativeUrlsDepthOfTwo( childModel );

        assertEquals( "Modified parent!", readModel( "relative-urls.xml" ), parentModel );
    }

    private static void assertPathsResolvedForRelativeUrlsDepthOfTwo( final DecorationModel childModel )
    {
        assertEquals( "check left banner href", "../../banner/left", childModel.getBannerLeft().getHref() );
        assertEquals( "check left banner image", "../../images/jakarta-logo.gif",
                childModel.getBannerLeft().getSrc() );

        assertEquals( "check right banner href", "../../banner/right/", childModel.getBannerRight().getHref() );
        assertEquals( "check right banner image", "../../commons/images/logo.png",
                childModel.getBannerRight().getSrc() );

        Logo poweredBy = childModel.getPoweredBy().get( 0 );
        assertEquals( "check powered by logo href", "../../tomcat", poweredBy.getHref() );
        assertEquals( "check powered by logo image", "../../tomcat/logo.gif", poweredBy.getImg() );

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get( 0 );
        assertEquals( "check breadcrumb href", "../../apache", breadcrumb.getHref() );

        LinkItem link = childModel.getBody().getLinks().get( 0 );
        assertEquals( "check link href", "../../bouncycastle/", link.getHref() );

        Menu menu = childModel.getBody().getMenus().get( 0 );
        LinkItem menuItem = menu.getItems().get( 0 );
        assertEquals( "check menu item href", "../../special/", menuItem.getHref() );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedForReverseRelativeUrls()
        throws IOException, XmlPullParserException
    {
        DecorationModel parentModel = readModel( "relative-urls.xml" );
        DecorationModel childModel = readModel( "empty.xml" );

        assembler.assembleModelInheritance( NAME, childModel, parentModel, "http://maven.apache.org/",
                                            "http://maven.apache.org/doxia/" );
        assertPathsResolvedForReverseRelativeUrls( childModel );

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel( "empty.xml" );
        assembler.assembleModelInheritance( NAME, childModel, parentModel, "scp://people.apache.org/",
                                            "scp://people.apache.org/doxia/" );
        assertPathsResolvedForReverseRelativeUrls( childModel );

        assertEquals( "Modified parent!", readModel( "relative-urls.xml" ), parentModel );
    }

    private static void assertPathsResolvedForReverseRelativeUrls( final DecorationModel childModel )
    {
        assertEquals( "check left banner href", "doxia/banner/left", childModel.getBannerLeft().getHref() );
        assertEquals( "check left banner image", "doxia/images/jakarta-logo.gif",
                childModel.getBannerLeft().getSrc() );

        assertEquals( "check right banner href", "doxia/banner/right/", childModel.getBannerRight().getHref() );
        assertEquals( "check right banner image", "doxia/commons/images/logo.png",
                childModel.getBannerRight().getSrc() );

        Logo poweredBy = childModel.getPoweredBy().get( 0 );
        assertEquals( "check powered by logo href", "doxia/tomcat", poweredBy.getHref() );
        assertEquals( "check powered by logo image", "doxia/tomcat/logo.gif", poweredBy.getImg() );

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get( 0 );
        assertEquals( "check breadcrumb href", "doxia/apache", breadcrumb.getHref() );

        LinkItem link = childModel.getBody().getLinks().get( 0 );
        assertEquals( "check link href", "doxia/bouncycastle/", link.getHref() );

        Menu menu = childModel.getBody().getMenus().get( 0 );
        LinkItem menuItem = menu.getItems().get( 0 );
        assertEquals( "check menu item href", "doxia/special/", menuItem.getHref() );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedForReverseRelativeUrlsDepthOfTwo()
        throws IOException, XmlPullParserException
    {
        DecorationModel parentModel = readModel( "relative-urls.xml" );
        DecorationModel childModel = readModel( "empty.xml" );

        assembler.assembleModelInheritance( NAME, childModel, parentModel, "http://maven.apache.org/",
                                            "http://maven.apache.org/doxia/core/" );
        assertPathsResolvedForReverseRelativeUrlsDepthOfTwo( childModel );

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel( "empty.xml" );
        assembler.assembleModelInheritance( NAME, childModel, parentModel, "scp://people.apache.org/",
                                            "scp://people.apache.org/doxia/core/" );
        assertPathsResolvedForReverseRelativeUrlsDepthOfTwo( childModel );

        assertEquals( "Modified parent!", readModel( "relative-urls.xml" ), parentModel );
    }

    private static void assertPathsResolvedForReverseRelativeUrlsDepthOfTwo( final DecorationModel childModel )
    {
        assertEquals( "check left banner href", "doxia/core/banner/left", childModel.getBannerLeft().getHref() );
        assertEquals( "check left banner image", "doxia/core/images/jakarta-logo.gif",
                childModel.getBannerLeft().getSrc() );

        assertEquals( "check right banner href", "doxia/core/banner/right/",
                childModel.getBannerRight().getHref() );
        assertEquals( "check right banner image", "doxia/core/commons/images/logo.png",
                childModel.getBannerRight().getSrc() );

        Logo poweredBy = childModel.getPoweredBy().get( 0 );
        assertEquals( "check powered by logo href", "doxia/core/tomcat", poweredBy.getHref() );
        assertEquals( "check powered by logo image", "doxia/core/tomcat/logo.gif", poweredBy.getImg() );

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get( 0 );
        assertEquals( "check breadcrumb href", "doxia/core/apache", breadcrumb.getHref() );

        LinkItem link = childModel.getBody().getLinks().get( 0 );
        assertEquals( "check link href", "doxia/core/bouncycastle/", link.getHref() );

        Menu menu = childModel.getBody().getMenus().get( 0 );
        LinkItem menuItem = menu.getItems().get( 0 );
        assertEquals( "check menu item href", "doxia/core/special/", menuItem.getHref() );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedForUnrelatedRelativeUrls()
        throws IOException, XmlPullParserException
    {
        DecorationModel parentModel = readModel( "relative-urls.xml" );
        DecorationModel childModel = readModel( "empty.xml" );

        assembler.assembleModelInheritance( NAME, childModel, parentModel, "http://maven.apache.org",
                                            "http://jakarta.apache.org" );
        assertPathsResolvedForUnrelatedRelativeUrls( childModel );

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel( "empty.xml" );
        assembler.assembleModelInheritance( NAME, childModel, parentModel, "scp://people.apache.org/",
                                            "http://jakarta.apache.org" );
        assertPathsResolvedForUnrelatedRelativeUrls( childModel );

        assertEquals( "Modified parent!", readModel( "relative-urls.xml" ), parentModel );
    }

    private static void assertPathsResolvedForUnrelatedRelativeUrls( final DecorationModel childModel )
    {
        assertEquals( "check left banner href", "http://jakarta.apache.org/banner/left",
                childModel.getBannerLeft().getHref() );
        assertEquals( "check left banner image", "http://jakarta.apache.org/images/jakarta-logo.gif",
                childModel.getBannerLeft().getSrc() );

        assertEquals( "check right banner href", "http://jakarta.apache.org/banner/right/",
                childModel.getBannerRight().getHref() );
        assertEquals( "check right banner image", "http://jakarta.apache.org/commons/images/logo.png",
                childModel.getBannerRight().getSrc() );

        Logo poweredBy = childModel.getPoweredBy().get( 0 );
        assertEquals( "check powered by logo href", "http://jakarta.apache.org/tomcat", poweredBy.getHref() );
        assertEquals( "check powered by logo image", "http://jakarta.apache.org/tomcat/logo.gif",
                poweredBy.getImg() );

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get( 0 );
        assertEquals( "check breadcrumb href", "http://jakarta.apache.org/apache", breadcrumb.getHref() );

        LinkItem link = childModel.getBody().getLinks().get( 0 );
        assertEquals( "check link href", "http://jakarta.apache.org/bouncycastle/", link.getHref() );

        Menu menu = childModel.getBody().getMenus().get( 0 );
        LinkItem menuItem = menu.getItems().get( 0 );
        assertEquals( "check menu item href", "http://jakarta.apache.org/special/", menuItem.getHref() );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testNullParent()
        throws IOException, XmlPullParserException
    {
        DecorationModel childModel = readModel( "empty.xml" );

        assembler.assembleModelInheritance( NAME, childModel, null, "http://maven.apache.org/doxia",
                                            "http://maven.apache.org" );
        DecorationModel mergedModel = readModel( "empty.xml" );

        assertEquals( "Check result", mergedModel, childModel );

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel( "empty.xml" );
        assembler.assembleModelInheritance( NAME, childModel, null, "scp://people.apache.org/doxia",
                                            "scp://people.apache.org" );
        assertEquals( "Check scp result", mergedModel, childModel );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testFullyPopulatedChild()
        throws IOException, XmlPullParserException
    {
        DecorationModel childModel = readModel( "fully-populated-child.xml" );
        DecorationModel parentModel = readModel( "fully-populated-child.xml" );

        assembler.assembleModelInheritance( NAME, childModel, parentModel, "http://foo.apache.org/doxia",
                                            "http://foo.apache.org" );
        DecorationModel mergedModel = readModel( "fully-populated-child.xml" );

        assertEquals( "Check result", mergedModel, childModel );

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel( "fully-populated-child.xml" );
        assembler.assembleModelInheritance( NAME, childModel, parentModel, "scp://foo.apache.org/doxia",
                                            "scp://foo.apache.org" );
        assertEquals( "Check scp result", mergedModel, childModel );

        assertEquals( "Modified parent!", readModel( "fully-populated-child.xml" ), parentModel );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testFullyPopulatedParentAndEmptyChild()
        throws IOException, XmlPullParserException
    {
        DecorationModel childModel = readModel( "empty.xml" );
        DecorationModel parentModel = readModel( "fully-populated-child.xml" );

        assembler.assembleModelInheritance( NAME, childModel, parentModel, "http://maven.apache.org/doxia",
                                            "http://maven.apache.org" );

        DecorationModel unresolvedModel = readModel( "fully-populated-unresolved.xml" );
        assertEquals( "Check result", unresolvedModel, childModel );

        assembler.resolvePaths( childModel, "http://maven.apache.org/doxia" );
        DecorationModel mergedModel = readModel( "fully-populated-merged.xml" );

        assertEquals( "Check result", mergedModel, childModel );

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel( "empty.xml" );
        assembler.assembleModelInheritance( NAME, childModel, parentModel, "scp://maven.apache.org/doxia",
                                            "scp://maven.apache.org" );
        assembler.resolvePaths( childModel, "http://maven.apache.org/doxia" );
        assertEquals( "Check scp result", mergedModel, childModel );

        assertEquals( "Modified parent!", readModel( "fully-populated-child.xml" ), parentModel );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testResolvingAllExternalUrls()
        throws IOException, XmlPullParserException
    {
        DecorationModel model = readModel( "external-urls.xml" );

        assembler.resolvePaths( model, "http://foo.com/" );
        DecorationModel mergedModel = readModel( "external-urls.xml" );

        assertEquals( "Check result", mergedModel, model );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testResolvingAllRelativeUrls()
        throws IOException, XmlPullParserException
    {
        DecorationModel model = readModel( "relative-urls.xml" );

        assembler.resolvePaths( model, "http://foo.com/" );

        DecorationModel resolvedModel = readModel( "relative-urls-resolved.xml" );

        assertEquals( "Check result", resolvedModel, model );
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testResolvingAllSiteUrls()
        throws IOException, XmlPullParserException
    {
        DecorationModel model = readModel( "subsite-urls.xml" );

        assembler.resolvePaths( model, "http://maven.apache.org/" );

        DecorationModel resolvedModel = readModel( "relative-urls-resolved.xml" );
        assertEquals( "Check result", resolvedModel, model );
    }

/* [MSITE-62] This is to test the ../ relative paths, which I am inclined not to use
    public void testResolvingAllSiteChildUrls()
        throws IOException, XmlPullParserException
    {
        DecorationModel model = readModel( "subsite-urls.xml" );

        assembler.resolvePaths( model, "http://maven.apache.org/foo" );

        DecorationModel resolvedModel = readModel( "subsite-relative-urls-resolved.xml" );
        assertEquals( "Check result", resolvedModel, model );
    }

    public void testResolvingAllSiteChildUrlsMultipleLevels()
        throws IOException, XmlPullParserException
    {
        DecorationModel model = readModel( "subsite-urls.xml" );

        assembler.resolvePaths( model, "http://maven.apache.org/banner/right" );

        DecorationModel resolvedModel = readModel( "subsite-relative-urls-multiple-resolved.xml" );
        assertEquals( "Check result", resolvedModel, model );
    }

    public void testResolvingAllSiteChildFilesystemUrls()
        throws IOException, XmlPullParserException
    {
        DecorationModel model = readModel( "subsite-urls-file.xml" );

        assembler.resolvePaths( model, "file://localhost/www/maven.apache.org/foo" );

        DecorationModel resolvedModel = readModel( "subsite-relative-urls-resolved.xml" );
        assertEquals( "Check result", resolvedModel, model );
    }

*/

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testResolvingEmptyDescriptor()
        throws IOException, XmlPullParserException
    {
        DecorationModel model = readModel( "empty.xml" );
        assembler.resolvePaths( model, "http://maven.apache.org" );
        DecorationModel mergedModel = readModel( "empty.xml" );

        assertEquals( "Check result", mergedModel, model );
    }

    /**
     *
     */
    @Test
    public void testDuplicateParentElements()
    {
        DecorationModel model = new DecorationModel();
        model.setBody( new Body() );
        model.getBody().addLink( createLinkItem( "Foo", "http://foo.apache.org" ) );
        model.getBody().addLink( createLinkItem( "Foo", "http://foo.apache.org" ) );

        model.addPoweredBy( createLogo( "Foo", "http://foo.apache.org", "http://foo.apache.org/foo.jpg" ) );
        model.addPoweredBy( createLogo( "Foo", "http://foo.apache.org", "http://foo.apache.org/foo.jpg" ) );

        DecorationModel child = new DecorationModel();
        assembler.assembleModelInheritance( NAME, child, model, "http://maven.apache.org/doxia",
                                            "http://maven.apache.org" );

        assertEquals( "Check size", 1, child.getBody().getLinks().size() );
        assertEquals( "Check item", createLinkItem( "Foo", "http://foo.apache.org" ),
                child.getBody().getLinks().get( 0 ) );

        assertEquals( "Check size", 1, child.getPoweredBy().size() );
        assertEquals( "Check item",
                createLogo( "Foo", "http://foo.apache.org", "http://foo.apache.org/foo.jpg" ),
                child.getPoweredBy().get( 0 ) );
    }

    /**
     *
     */
    @Test
    public void testDuplicateChildElements()
    {
        DecorationModel model = new DecorationModel();
        model.setBody( new Body() );
        model.getBody().addLink( createLinkItem( "Foo", "http://foo.apache.org" ) );
        model.getBody().addLink( createLinkItem( "Foo", "http://foo.apache.org" ) );

        model.addPoweredBy( createLogo( "Foo", "http://foo.apache.org", "http://foo.apache.org/foo.jpg" ) );
        model.addPoweredBy( createLogo( "Foo", "http://foo.apache.org", "http://foo.apache.org/foo.jpg" ) );

        DecorationModel parent = new DecorationModel();
        assembler.assembleModelInheritance( NAME, model, parent, "http://maven.apache.org/doxia",
                                            "http://maven.apache.org" );

        assertEquals( "Check size", 1, model.getBody().getLinks().size() );
        assertEquals( "Check item", createLinkItem( "Foo", "http://foo.apache.org" ),
                model.getBody().getLinks().get( 0 ) );

        assertEquals( "Check size", 1, model.getPoweredBy().size() );
        assertEquals( "Check item",
                createLogo( "Foo", "http://foo.apache.org", "http://foo.apache.org/foo.jpg" ),
                model.getPoweredBy().get( 0 ) );

        assertEquals( "Modified parent!", new DecorationModel(), parent );
    }

    /**
     *
     */
    @Test
    public void testBadHref()
    {
        final DecorationModel model = new DecorationModel();
        model.setBody( new Body() );
        model.getBody().addBreadcrumb( createLinkItem( "Foo", "http://foo.apache.org/${property}" ) );
        assembler.resolvePaths( model, "http://foo.apache.org" );
        assertEquals( "Check size", 1, model.getBody().getBreadcrumbs().size() );
        assertEquals( "Check item", createLinkItem( "Foo", "http://foo.apache.org/${property}" ),
                model.getBody().getBreadcrumbs().get( 0 ) );
    }

    /**
     *
     */
    @Test
    public void testBreadcrumbWithoutHref()
    {
        DecorationModel model = new DecorationModel();
        model.setBody( new Body() );
        model.getBody().addBreadcrumb( createLinkItem( "Foo", null ) );
        assembler.resolvePaths( model, "http://foo.apache.org" );
        assertEquals( "Check size", 1, model.getBody().getBreadcrumbs().size() );
        assertEquals( "Check item", createLinkItem( "Foo", null ), model.getBody().getBreadcrumbs().get( 0 ) );
    }

    /**
     *
     */
    @Test
    public void testBreadcrumbs()
    {
        String parentHref = "http://parent.com/index.html";

        final DecorationModel parent = new DecorationModel();
        parent.setBody( new Body() );
        parent.getBody().addBreadcrumb( createLinkItem( "Parent", parentHref ) );

        DecorationModel child = new DecorationModel();
        assembler.assembleModelInheritance( "childName", child, parent,
                "http://parent.com/child", "http://parent.com" );
        assertBreadcrumbsCorrect( child.getBody().getBreadcrumbs(), "childName", parentHref );


        // same with trailing slash
        child = new DecorationModel();
        assembler.assembleModelInheritance( "childName", child, parent,
                "http://parent.com/child/", "http://parent.com/" );
        assertBreadcrumbsCorrect( child.getBody().getBreadcrumbs(), "childName", parentHref );

        // now mixed
        child = new DecorationModel();
        assembler.assembleModelInheritance( "childName", child, parent,
                "http://parent.com/child/", "http://parent.com" );
        assertBreadcrumbsCorrect( child.getBody().getBreadcrumbs(), "childName", parentHref );

        // and other way round
        child = new DecorationModel();
        assembler.assembleModelInheritance( "childName", child, parent,
                "http://parent.com/child", "http://parent.com/" );
        assertBreadcrumbsCorrect( child.getBody().getBreadcrumbs(), "childName", parentHref );


        // now with child breadcrumb
        child = new DecorationModel();
        child.setBody( new Body() );
        child.getBody().addBreadcrumb( createLinkItem( "Child", "index.html" ) );
        assembler.assembleModelInheritance( "childName", child, parent,
                "http://parent.com/child/", "http://parent.com/" );
        assertBreadcrumbsCorrect( child.getBody().getBreadcrumbs(), "Child", parentHref );


        // now with file url
        parentHref = "file://parent.com/index.html";
        ( parent.getBody().getBreadcrumbs().get( 0 ) ).setHref( parentHref );
        child = new DecorationModel();
        assembler.assembleModelInheritance( "childName", child, parent,
                "file://parent.com/child/", "file://parent.com/" );
        assertBreadcrumbsCorrect( child.getBody().getBreadcrumbs(), "childName", parentHref );


        // now with scp url
        parentHref = "scp://parent.com/index.html";
        ( parent.getBody().getBreadcrumbs().get( 0 ) ).setHref( parentHref );
        child = new DecorationModel();
        assembler.assembleModelInheritance( "childName", child, parent,
                "scp://parent.com/child/", "scp://parent.com/" );
        assertBreadcrumbsCorrect( child.getBody().getBreadcrumbs(), "childName", parentHref );
    }

    private static void assertBreadcrumbsCorrect( final List<LinkItem> breadcrumbs, final String childName,
            final String parentHref )
    {
        assertEquals( "Check size", 2, breadcrumbs.size() );
        assertEquals( "Check parent item", createLinkItem( "Parent", parentHref ), breadcrumbs.get( 0 ) );
        assertEquals( "Check child item", createLinkItem( childName, "index.html" ), breadcrumbs.get( 1 ) );
    }

    /**
     * https://issues.apache.org/jira/browse/DOXIASITETOOLS-62
     */
    @Test
    public void testBreadcrumbCutParentAfterDuplicate()
    {
        DecorationModel child = new DecorationModel(); // B > E
        child.setBody( new Body() );
        child.getBody().addBreadcrumb( createLinkItem( "B", null ) );
        child.getBody().addBreadcrumb( createLinkItem( "E", null ) );

        DecorationModel parent = new DecorationModel(); // A > B > C > D
        parent.setBody( new Body() );
        parent.getBody().addBreadcrumb( createLinkItem( "A", null ) );
        parent.getBody().addBreadcrumb( createLinkItem( "B", null ) );
        parent.getBody().addBreadcrumb( createLinkItem( "C", null ) );
        parent.getBody().addBreadcrumb( createLinkItem( "D", null ) );

        assembler.assembleModelInheritance( NAME, child, parent, "http://maven.apache.org/doxia",
                                            "http://maven.apache.org" );

        final List<LinkItem> breadcrumbs = child.getBody().getBreadcrumbs(); // expected: A > B > E
        assertEquals( "Check size", 3, breadcrumbs.size() );
        assertEquals( "Check item", createLinkItem( "A", null ), breadcrumbs.get( 0 ) );
        assertEquals( "Check item", createLinkItem( "B", null ), breadcrumbs.get( 1 ) );
        assertEquals( "Check item", createLinkItem( "E", null ), breadcrumbs.get( 2 ) );
    }

    /**
     *
     */
    @Test
    public void testBannerWithoutHref()
    {
        DecorationModel model = new DecorationModel();
        model.setBody( new Body() );

        Banner banner = createBanner( "Left", null, "/images/src.gif", "alt" );

        model.setBannerLeft( banner );

        assembler.resolvePaths( model, "http://foo.apache.org" );

        assertEquals( "Check banner", createBanner( "Left", null, "images/src.gif", "alt" ),
                model.getBannerLeft() );
    }

    /**
     *
     */
    @Test
    public void testLogoWithoutImage()
    {
        // This should actually be validated in the model, it doesn't really make sense
        DecorationModel model = new DecorationModel();
        model.setBody( new Body() );
        model.addPoweredBy( createLogo( "Foo", "http://foo.apache.org", null ) );
        assembler.resolvePaths( model, "http://foo.apache.org" );
        assertEquals( "Check size", 1, model.getPoweredBy().size() );
        assertEquals( "Check item", createLogo( "Foo", "./", null ), model.getPoweredBy().get( 0 ) );
    }

    private static Banner createBanner( String name, String href, String src, String alt )
    {
        Banner banner = new Banner();
        banner.setName( name );
        banner.setHref( href );
        banner.setSrc( src );
        banner.setAlt( alt );
        return banner;
    }

    private Logo createLogo( String name, String href, String img )
    {
        Logo logo = new Logo();
        logo.setHref( href );
        logo.setImg( img );
        logo.setName( name );
        return logo;
    }

    private static LinkItem createLinkItem( String name, String href )
    {
        LinkItem item = new LinkItem();
        item.setName( name );
        item.setHref( href );
        return item;
    }

    private DecorationModel readModel( String name )
        throws IOException, XmlPullParserException
    {
        Reader reader = null;
        try
        {
            reader = ReaderFactory.newXmlReader( getClass().getResourceAsStream( "/" + name ) );
            return new DecorationXpp3Reader().read( reader );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }
}
