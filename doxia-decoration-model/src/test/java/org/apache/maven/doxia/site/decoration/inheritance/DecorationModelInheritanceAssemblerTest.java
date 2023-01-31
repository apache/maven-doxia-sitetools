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
package org.apache.maven.doxia.site.decoration.inheritance;

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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test the inheritance assembler.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
@SuppressWarnings("javadoc")
public class DecorationModelInheritanceAssemblerTest {
    private final DecorationModelInheritanceAssembler assembler = new DefaultDecorationModelInheritanceAssembler();

    private static final String NAME = "Name";

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testInheritance() throws IOException, XmlPullParserException {
        DecorationModel childModel = readModel("inheritance-child.xml");
        DecorationModel parentModel = readModel("inheritance-parent.xml");

        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "http://maven.apache.org/doxia", "http://maven.apache.org");
        DecorationModel expectedModel = readModel("inheritance-expected.xml");

        assertEquals(expectedModel, childModel, "Check result");

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel("inheritance-child.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "scp://people.apache.org/doxia", "scp://people.apache.org");
        assertEquals(expectedModel, childModel, "Check scp result");

        assertEquals(readModel("inheritance-parent.xml"), parentModel, "Modified parent!");

        // late inheritance in links can't be rebased: check friendly message
        parentModel.getBannerLeft().setHref("${project.url}");
        childModel = readModel("inheritance-child.xml");
        try {
            assembler.assembleModelInheritance(
                    NAME, childModel, parentModel, "scp://people.apache.org/doxia", "scp://people.apache.org");
            fail("late interpolation in link should cause IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().startsWith("site.xml late interpolation"));
        }
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testSuppressedInheritance() throws IOException, XmlPullParserException {
        DecorationModel unassembledChildModel = readModel("inheritance-child-no-inheritance.xml");
        DecorationModel childModel = readModel("inheritance-child-no-inheritance.xml");
        DecorationModel parentModel = readModel("inheritance-parent.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "http://maven.apache.org/doxia", "http://maven.apache.org");
        assertEquals(unassembledChildModel, childModel, "Check result");

        // 2 levels of inheritance
        DecorationModel childOfchildModel = new DecorationModel();
        assembler.assembleModelInheritance(
                "Child of Child",
                childOfchildModel,
                childModel,
                "http://maven.apache.org/doxia/child",
                "http://maven.apache.org/doxia");
        assembler.assembleModelInheritance(
                NAME, childOfchildModel, parentModel, "http://maven.apache.org/doxia", "http://maven.apache.org");
        // check that the 3 breadcrumb items from parent.xml are not inherited
        assertEquals(
                0,
                childOfchildModel.getBody().getBreadcrumbs().size(),
                "child of child no inheritance: breadcrumbs count");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedWhenEmpty() throws IOException, XmlPullParserException {
        // Test an empty model avoids NPEs
        DecorationModel childModel = readModel("empty.xml");
        DecorationModel parentModel = readModel("empty.xml");

        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "http://maven.apache.org/doxia", "http://maven.apache.org");
        DecorationModel mergedModel = readModel("empty.xml");

        assertEquals(mergedModel, childModel, "Check result");

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel("empty.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "scp://people.apache.org/doxia", "scp://people.apache.org");
        assertEquals(mergedModel, childModel, "Check scp result");

        assertEquals(readModel("empty.xml"), parentModel, "Modified parent!");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsNotResolvedForExternalUrls() throws IOException, XmlPullParserException {
        DecorationModel parentModel = readModel("external-urls.xml");
        DecorationModel childModel = readModel("empty.xml");

        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "http://maven.apache.org/doxia", "http://maven.apache.org");
        assertPathsNotResolvedForExternalUrls(childModel);

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel("empty.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "scp://people.apache.org/doxia", "scp://people.apache.org");
        assertPathsNotResolvedForExternalUrls(childModel);

        assertEquals(readModel("external-urls.xml"), parentModel, "Modified parent!");
    }

    private static void assertPathsNotResolvedForExternalUrls(final DecorationModel childModel) {
        assertEquals("http://jakarta.apache.org/", childModel.getBannerLeft().getHref(), "check left banner href");
        assertEquals(
                "http://jakarta.apache.org/images/jakarta-logo.gif",
                childModel.getBannerLeft().getSrc(),
                "check left banner image");

        assertEquals(
                "http://jakarta.apache.org/commons/sandbox",
                childModel.getBannerRight().getHref(),
                "check right banner href");
        assertEquals(
                "http://jakarta.apache.org/commons/images/logo.png",
                childModel.getBannerRight().getSrc(),
                "check right banner image");

        Logo poweredBy = childModel.getPoweredBy().get(0);
        assertEquals("http://tomcat.apache.org/", poweredBy.getHref(), "check powered by logo href");
        assertEquals("http://tomcat.apache.org/logo.gif", poweredBy.getImg(), "check powered by logo image");

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get(0);
        assertEquals("http://www.apache.org/", breadcrumb.getHref(), "check breadcrumb href");

        LinkItem link = childModel.getBody().getLinks().get(0);
        assertEquals("http://www.bouncycastle.org", link.getHref(), "check link href");

        Menu menu = childModel.getBody().getMenus().get(0);
        LinkItem menuItem = menu.getItems().get(0);
        assertEquals("http://www.apache.org/special/", menuItem.getHref(), "check menu item href");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedForRelativeUrls() throws IOException, XmlPullParserException {
        DecorationModel parentModel = readModel("relative-urls.xml");
        DecorationModel childModel = readModel("empty.xml");

        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "http://maven.apache.org/doxia/", "http://maven.apache.org");
        assertPathsResolvedForRelativeUrls(childModel);

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel("empty.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "scp://people.apache.org/doxia", "scp://people.apache.org");
        assertPathsResolvedForRelativeUrls(childModel);

        assertEquals(readModel("relative-urls.xml"), parentModel, "Modified parent!");
    }

    private static void assertPathsResolvedForRelativeUrls(final DecorationModel childModel) {
        assertEquals("../banner/left", childModel.getBannerLeft().getHref(), "check left banner href");
        assertEquals("../images/jakarta-logo.gif", childModel.getBannerLeft().getSrc(), "check left banner image");

        assertEquals("../banner/right/", childModel.getBannerRight().getHref(), "check right banner href");
        assertEquals("../commons/images/logo.png", childModel.getBannerRight().getSrc(), "check right banner image");

        Logo poweredBy = childModel.getPoweredBy().get(0);
        assertEquals("../tomcat", poweredBy.getHref(), "check powered by logo href");
        assertEquals("../tomcat/logo.gif", poweredBy.getImg(), "check powered by logo image");

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get(0);
        assertEquals("../apache", breadcrumb.getHref(), "check breadcrumb href");

        LinkItem link = childModel.getBody().getLinks().get(0);
        assertEquals("../bouncycastle/", link.getHref(), "check link href");

        Menu menu = childModel.getBody().getMenus().get(0);
        LinkItem menuItem = menu.getItems().get(0);
        assertEquals("../special/", menuItem.getHref(), "check menu item href");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedForSubsiteUrls() throws IOException, XmlPullParserException {
        DecorationModel parentModel = readModel("subsite-urls.xml");
        DecorationModel childModel = readModel("empty.xml");

        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "http://maven.apache.org/doxia/", "http://maven.apache.org");
        assembler.resolvePaths(childModel, "http://maven.apache.org/doxia");

        assertPathsResolvedForSubsiteUrls(childModel);

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel("empty.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "scp://people.apache.org/doxia", "scp://people.apache.org");
        assembler.resolvePaths(childModel, "http://maven.apache.org/doxia");
        assertPathsResolvedForSubsiteUrls(childModel);

        assertEquals(readModel("subsite-urls.xml"), parentModel, "Modified parent!");
    }

    private static void assertPathsResolvedForSubsiteUrls(final DecorationModel childModel) {
        assertEquals("../banner/left", childModel.getBannerLeft().getHref(), "check left banner href");
        assertEquals("../images/jakarta-logo.gif", childModel.getBannerLeft().getSrc(), "check left banner image");

        assertEquals("../banner/right/", childModel.getBannerRight().getHref(), "check right banner href");
        assertEquals("../commons/images/logo.png", childModel.getBannerRight().getSrc(), "check right banner image");

        Logo poweredBy = childModel.getPoweredBy().get(0);
        assertEquals("../tomcat", poweredBy.getHref(), "check powered by logo href");
        assertEquals("../tomcat/logo.gif", poweredBy.getImg(), "check powered by logo image");

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get(0);
        assertEquals("../apache", breadcrumb.getHref(), "check breadcrumb href");

        LinkItem link = childModel.getBody().getLinks().get(0);
        assertEquals("../bouncycastle/", link.getHref(), "check link href");

        Menu menu = childModel.getBody().getMenus().get(0);
        LinkItem menuItem = menu.getItems().get(0);
        assertEquals("../special/", menuItem.getHref(), "check menu item href");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedForRelativeUrlsDepthOfTwo() throws IOException, XmlPullParserException {
        DecorationModel parentModel = readModel("relative-urls.xml");
        DecorationModel childModel = readModel("empty.xml");

        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "http://maven.apache.org/doxia/core", "http://maven.apache.org");
        assertPathsResolvedForRelativeUrlsDepthOfTwo(childModel);

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel("empty.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "scp://people.apache.org/doxia/core", "scp://people.apache.org");
        assertPathsResolvedForRelativeUrlsDepthOfTwo(childModel);

        assertEquals(readModel("relative-urls.xml"), parentModel, "Modified parent!");
    }

    private static void assertPathsResolvedForRelativeUrlsDepthOfTwo(final DecorationModel childModel) {
        assertEquals("../../banner/left", childModel.getBannerLeft().getHref(), "check left banner href");
        assertEquals("../../images/jakarta-logo.gif", childModel.getBannerLeft().getSrc(), "check left banner image");

        assertEquals("../../banner/right/", childModel.getBannerRight().getHref(), "check right banner href");
        assertEquals(
                "../../commons/images/logo.png", childModel.getBannerRight().getSrc(), "check right banner image");

        Logo poweredBy = childModel.getPoweredBy().get(0);
        assertEquals("../../tomcat", poweredBy.getHref(), "check powered by logo href");
        assertEquals("../../tomcat/logo.gif", poweredBy.getImg(), "check powered by logo image");

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get(0);
        assertEquals("../../apache", breadcrumb.getHref(), "check breadcrumb href");

        LinkItem link = childModel.getBody().getLinks().get(0);
        assertEquals("../../bouncycastle/", link.getHref(), "check link href");

        Menu menu = childModel.getBody().getMenus().get(0);
        LinkItem menuItem = menu.getItems().get(0);
        assertEquals("../../special/", menuItem.getHref(), "check menu item href");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedForReverseRelativeUrls() throws IOException, XmlPullParserException {
        DecorationModel parentModel = readModel("relative-urls.xml");
        DecorationModel childModel = readModel("empty.xml");

        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "http://maven.apache.org/", "http://maven.apache.org/doxia/");
        assertPathsResolvedForReverseRelativeUrls(childModel);

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel("empty.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "scp://people.apache.org/", "scp://people.apache.org/doxia/");
        assertPathsResolvedForReverseRelativeUrls(childModel);

        assertEquals(readModel("relative-urls.xml"), parentModel, "Modified parent!");
    }

    private static void assertPathsResolvedForReverseRelativeUrls(final DecorationModel childModel) {
        assertEquals("doxia/banner/left", childModel.getBannerLeft().getHref(), "check left banner href");
        assertEquals("doxia/images/jakarta-logo.gif", childModel.getBannerLeft().getSrc(), "check left banner image");

        assertEquals("doxia/banner/right/", childModel.getBannerRight().getHref(), "check right banner href");
        assertEquals(
                "doxia/commons/images/logo.png", childModel.getBannerRight().getSrc(), "check right banner image");

        Logo poweredBy = childModel.getPoweredBy().get(0);
        assertEquals("doxia/tomcat", poweredBy.getHref(), "check powered by logo href");
        assertEquals("doxia/tomcat/logo.gif", poweredBy.getImg(), "check powered by logo image");

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get(0);
        assertEquals("doxia/apache", breadcrumb.getHref(), "check breadcrumb href");

        LinkItem link = childModel.getBody().getLinks().get(0);
        assertEquals("doxia/bouncycastle/", link.getHref(), "check link href");

        Menu menu = childModel.getBody().getMenus().get(0);
        LinkItem menuItem = menu.getItems().get(0);
        assertEquals("doxia/special/", menuItem.getHref(), "check menu item href");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedForReverseRelativeUrlsDepthOfTwo() throws IOException, XmlPullParserException {
        DecorationModel parentModel = readModel("relative-urls.xml");
        DecorationModel childModel = readModel("empty.xml");

        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "http://maven.apache.org/", "http://maven.apache.org/doxia/core/");
        assertPathsResolvedForReverseRelativeUrlsDepthOfTwo(childModel);

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel("empty.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "scp://people.apache.org/", "scp://people.apache.org/doxia/core/");
        assertPathsResolvedForReverseRelativeUrlsDepthOfTwo(childModel);

        assertEquals(readModel("relative-urls.xml"), parentModel, "Modified parent!");
    }

    private static void assertPathsResolvedForReverseRelativeUrlsDepthOfTwo(final DecorationModel childModel) {
        assertEquals("doxia/core/banner/left", childModel.getBannerLeft().getHref(), "check left banner href");
        assertEquals(
                "doxia/core/images/jakarta-logo.gif", childModel.getBannerLeft().getSrc(), "check left banner image");

        assertEquals("doxia/core/banner/right/", childModel.getBannerRight().getHref(), "check right banner href");
        assertEquals(
                "doxia/core/commons/images/logo.png",
                childModel.getBannerRight().getSrc(),
                "check right banner image");

        Logo poweredBy = childModel.getPoweredBy().get(0);
        assertEquals("doxia/core/tomcat", poweredBy.getHref(), "check powered by logo href");
        assertEquals("doxia/core/tomcat/logo.gif", poweredBy.getImg(), "check powered by logo image");

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get(0);
        assertEquals("doxia/core/apache", breadcrumb.getHref(), "check breadcrumb href");

        LinkItem link = childModel.getBody().getLinks().get(0);
        assertEquals("doxia/core/bouncycastle/", link.getHref(), "check link href");

        Menu menu = childModel.getBody().getMenus().get(0);
        LinkItem menuItem = menu.getItems().get(0);
        assertEquals("doxia/core/special/", menuItem.getHref(), "check menu item href");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testPathsResolvedForUnrelatedRelativeUrls() throws IOException, XmlPullParserException {
        DecorationModel parentModel = readModel("relative-urls.xml");
        DecorationModel childModel = readModel("empty.xml");

        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "http://maven.apache.org", "http://jakarta.apache.org");
        assertPathsResolvedForUnrelatedRelativeUrls(childModel);

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel("empty.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "scp://people.apache.org/", "http://jakarta.apache.org");
        assertPathsResolvedForUnrelatedRelativeUrls(childModel);

        assertEquals(readModel("relative-urls.xml"), parentModel, "Modified parent!");
    }

    private static void assertPathsResolvedForUnrelatedRelativeUrls(final DecorationModel childModel) {
        assertEquals(
                "http://jakarta.apache.org/banner/left",
                childModel.getBannerLeft().getHref(),
                "check left banner href");
        assertEquals(
                "http://jakarta.apache.org/images/jakarta-logo.gif",
                childModel.getBannerLeft().getSrc(),
                "check left banner image");

        assertEquals(
                "http://jakarta.apache.org/banner/right/",
                childModel.getBannerRight().getHref(),
                "check right banner href");
        assertEquals(
                "http://jakarta.apache.org/commons/images/logo.png",
                childModel.getBannerRight().getSrc(),
                "check right banner image");

        Logo poweredBy = childModel.getPoweredBy().get(0);
        assertEquals("http://jakarta.apache.org/tomcat", poweredBy.getHref(), "check powered by logo href");
        assertEquals("http://jakarta.apache.org/tomcat/logo.gif", poweredBy.getImg(), "check powered by logo image");

        LinkItem breadcrumb = childModel.getBody().getBreadcrumbs().get(0);
        assertEquals("http://jakarta.apache.org/apache", breadcrumb.getHref(), "check breadcrumb href");

        LinkItem link = childModel.getBody().getLinks().get(0);
        assertEquals("http://jakarta.apache.org/bouncycastle/", link.getHref(), "check link href");

        Menu menu = childModel.getBody().getMenus().get(0);
        LinkItem menuItem = menu.getItems().get(0);
        assertEquals("http://jakarta.apache.org/special/", menuItem.getHref(), "check menu item href");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testNullParent() throws IOException, XmlPullParserException {
        DecorationModel childModel = readModel("empty.xml");

        assembler.assembleModelInheritance(
                NAME, childModel, null, "http://maven.apache.org/doxia", "http://maven.apache.org");
        DecorationModel mergedModel = readModel("empty.xml");

        assertEquals(mergedModel, childModel, "Check result");

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel("empty.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, null, "scp://people.apache.org/doxia", "scp://people.apache.org");
        assertEquals(mergedModel, childModel, "Check scp result");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testFullyPopulatedChild() throws IOException, XmlPullParserException {
        DecorationModel childModel = readModel("fully-populated-child.xml");
        DecorationModel parentModel = readModel("fully-populated-child.xml");

        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "http://foo.apache.org/doxia", "http://foo.apache.org");
        DecorationModel mergedModel = readModel("fully-populated-child.xml");

        assertEquals(mergedModel, childModel, "Check result");

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel("fully-populated-child.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "scp://foo.apache.org/doxia", "scp://foo.apache.org");
        assertEquals(mergedModel, childModel, "Check scp result");

        assertEquals(readModel("fully-populated-child.xml"), parentModel, "Modified parent!");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testFullyPopulatedParentAndEmptyChild() throws IOException, XmlPullParserException {
        DecorationModel childModel = readModel("empty.xml");
        DecorationModel parentModel = readModel("fully-populated-child.xml");

        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "http://maven.apache.org/doxia", "http://maven.apache.org");

        DecorationModel unresolvedModel = readModel("fully-populated-unresolved.xml");
        assertEquals(unresolvedModel, childModel, "Check result");

        assembler.resolvePaths(childModel, "http://maven.apache.org/doxia");
        DecorationModel mergedModel = readModel("fully-populated-merged.xml");

        assertEquals(mergedModel, childModel, "Check result");

        // same with scp url, DOXIASITETOOLS-47
        childModel = readModel("empty.xml");
        assembler.assembleModelInheritance(
                NAME, childModel, parentModel, "scp://maven.apache.org/doxia", "scp://maven.apache.org");
        assembler.resolvePaths(childModel, "http://maven.apache.org/doxia");
        assertEquals(mergedModel, childModel, "Check scp result");

        assertEquals(readModel("fully-populated-child.xml"), parentModel, "Modified parent!");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testResolvingAllExternalUrls() throws IOException, XmlPullParserException {
        DecorationModel model = readModel("external-urls.xml");

        assembler.resolvePaths(model, "http://foo.com/");
        DecorationModel mergedModel = readModel("external-urls.xml");

        assertEquals(mergedModel, model, "Check result");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testResolvingAllRelativeUrls() throws IOException, XmlPullParserException {
        DecorationModel model = readModel("relative-urls.xml");

        assembler.resolvePaths(model, "http://foo.com/");

        DecorationModel resolvedModel = readModel("relative-urls-resolved.xml");

        assertEquals(resolvedModel, model, "Check result");
    }

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testResolvingAllSiteUrls() throws IOException, XmlPullParserException {
        DecorationModel model = readModel("subsite-urls.xml");

        assembler.resolvePaths(model, "http://maven.apache.org/");

        DecorationModel resolvedModel = readModel("relative-urls-resolved.xml");
        assertEquals(resolvedModel, model, "Check result");
    }

    /* [MSITE-62] This is to test the ../ relative paths, which I am inclined not to use
        public void testResolvingAllSiteChildUrls()
            throws IOException, XmlPullParserException
        {
            DecorationModel model = readModel( "subsite-urls.xml" );

            assembler.resolvePaths( model, "http://maven.apache.org/foo" );

            DecorationModel resolvedModel = readModel( "subsite-relative-urls-resolved.xml" );
            assertEquals( resolvedModel, model, "Check result" );
        }

        public void testResolvingAllSiteChildUrlsMultipleLevels()
            throws IOException, XmlPullParserException
        {
            DecorationModel model = readModel( "subsite-urls.xml" );

            assembler.resolvePaths( model, "http://maven.apache.org/banner/right" );

            DecorationModel resolvedModel = readModel( "subsite-relative-urls-multiple-resolved.xml" );
            assertEquals( resolvedModel, model, "Check result" );
        }

        public void testResolvingAllSiteChildFilesystemUrls()
            throws IOException, XmlPullParserException
        {
            DecorationModel model = readModel( "subsite-urls-file.xml" );

            assembler.resolvePaths( model, "file://localhost/www/maven.apache.org/foo" );

            DecorationModel resolvedModel = readModel( "subsite-relative-urls-resolved.xml" );
            assertEquals( resolvedModel, model, "Check result" );
        }

    */

    /**
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void testResolvingEmptyDescriptor() throws IOException, XmlPullParserException {
        DecorationModel model = readModel("empty.xml");
        assembler.resolvePaths(model, "http://maven.apache.org");
        DecorationModel mergedModel = readModel("empty.xml");

        assertEquals(mergedModel, model, "Check result");
    }

    /**
     *
     */
    @Test
    public void testDuplicateParentElements() {
        DecorationModel model = new DecorationModel();
        model.setBody(new Body());
        model.getBody().addLink(createLinkItem("Foo", "http://foo.apache.org"));
        model.getBody().addLink(createLinkItem("Foo", "http://foo.apache.org"));

        model.addPoweredBy(createLogo("Foo", "http://foo.apache.org", "http://foo.apache.org/foo.jpg"));
        model.addPoweredBy(createLogo("Foo", "http://foo.apache.org", "http://foo.apache.org/foo.jpg"));

        DecorationModel child = new DecorationModel();
        assembler.assembleModelInheritance(
                NAME, child, model, "http://maven.apache.org/doxia", "http://maven.apache.org");

        assertEquals(1, child.getBody().getLinks().size(), "Check size");
        assertEquals(
                createLinkItem("Foo", "http://foo.apache.org"),
                child.getBody().getLinks().get(0),
                "Check item");

        assertEquals(1, child.getPoweredBy().size(), "Check size");
        assertEquals(
                createLogo("Foo", "http://foo.apache.org", "http://foo.apache.org/foo.jpg"),
                child.getPoweredBy().get(0),
                "Check item");
    }

    /**
     *
     */
    @Test
    public void testDuplicateChildElements() {
        DecorationModel model = new DecorationModel();
        model.setBody(new Body());
        model.getBody().addLink(createLinkItem("Foo", "http://foo.apache.org"));
        model.getBody().addLink(createLinkItem("Foo", "http://foo.apache.org"));

        model.addPoweredBy(createLogo("Foo", "http://foo.apache.org", "http://foo.apache.org/foo.jpg"));
        model.addPoweredBy(createLogo("Foo", "http://foo.apache.org", "http://foo.apache.org/foo.jpg"));

        DecorationModel parent = new DecorationModel();
        assembler.assembleModelInheritance(
                NAME, model, parent, "http://maven.apache.org/doxia", "http://maven.apache.org");

        assertEquals(1, model.getBody().getLinks().size(), "Check size");
        assertEquals(
                createLinkItem("Foo", "http://foo.apache.org"),
                model.getBody().getLinks().get(0),
                "Check item");

        assertEquals(1, model.getPoweredBy().size(), "Check size");
        assertEquals(
                createLogo("Foo", "http://foo.apache.org", "http://foo.apache.org/foo.jpg"),
                model.getPoweredBy().get(0),
                "Check item");

        assertEquals(new DecorationModel(), parent, "Modified parent!");
    }

    /**
     *
     */
    @Test
    public void testBadHref() {
        final DecorationModel model = new DecorationModel();
        model.setBody(new Body());
        model.getBody().addBreadcrumb(createLinkItem("Foo", "http://foo.apache.org/${property}"));
        assembler.resolvePaths(model, "http://foo.apache.org");
        assertEquals(1, model.getBody().getBreadcrumbs().size(), "Check size");
        assertEquals(
                createLinkItem("Foo", "http://foo.apache.org/${property}"),
                model.getBody().getBreadcrumbs().get(0),
                "Check item");
    }

    /**
     *
     */
    @Test
    public void testBreadcrumbWithoutHref() {
        DecorationModel model = new DecorationModel();
        model.setBody(new Body());
        model.getBody().addBreadcrumb(createLinkItem("Foo", null));
        assembler.resolvePaths(model, "http://foo.apache.org");
        assertEquals(1, model.getBody().getBreadcrumbs().size(), "Check size");
        assertEquals(
                createLinkItem("Foo", null), model.getBody().getBreadcrumbs().get(0), "Check item");
    }

    /**
     *
     */
    @Test
    public void testBreadcrumbs() {
        String parentHref = "http://parent.com/index.html";

        final DecorationModel parent = new DecorationModel();
        parent.setBody(new Body());
        parent.getBody().addBreadcrumb(createLinkItem("Parent", parentHref));

        DecorationModel child = new DecorationModel();
        assembler.assembleModelInheritance("childName", child, parent, "http://parent.com/child", "http://parent.com");
        assertBreadcrumbsCorrect(child.getBody().getBreadcrumbs(), "childName", parentHref);

        // same with trailing slash
        child = new DecorationModel();
        assembler.assembleModelInheritance(
                "childName", child, parent, "http://parent.com/child/", "http://parent.com/");
        assertBreadcrumbsCorrect(child.getBody().getBreadcrumbs(), "childName", parentHref);

        // now mixed
        child = new DecorationModel();
        assembler.assembleModelInheritance("childName", child, parent, "http://parent.com/child/", "http://parent.com");
        assertBreadcrumbsCorrect(child.getBody().getBreadcrumbs(), "childName", parentHref);

        // and other way round
        child = new DecorationModel();
        assembler.assembleModelInheritance("childName", child, parent, "http://parent.com/child", "http://parent.com/");
        assertBreadcrumbsCorrect(child.getBody().getBreadcrumbs(), "childName", parentHref);

        // now with child breadcrumb
        child = new DecorationModel();
        child.setBody(new Body());
        child.getBody().addBreadcrumb(createLinkItem("Child", "index.html"));
        assembler.assembleModelInheritance(
                "childName", child, parent, "http://parent.com/child/", "http://parent.com/");
        assertBreadcrumbsCorrect(child.getBody().getBreadcrumbs(), "Child", parentHref);

        // now with file url
        parentHref = "file://parent.com/index.html";
        (parent.getBody().getBreadcrumbs().get(0)).setHref(parentHref);
        child = new DecorationModel();
        assembler.assembleModelInheritance(
                "childName", child, parent, "file://parent.com/child/", "file://parent.com/");
        assertBreadcrumbsCorrect(child.getBody().getBreadcrumbs(), "childName", parentHref);

        // now with scp url
        parentHref = "scp://parent.com/index.html";
        (parent.getBody().getBreadcrumbs().get(0)).setHref(parentHref);
        child = new DecorationModel();
        assembler.assembleModelInheritance("childName", child, parent, "scp://parent.com/child/", "scp://parent.com/");
        assertBreadcrumbsCorrect(child.getBody().getBreadcrumbs(), "childName", parentHref);
    }

    private static void assertBreadcrumbsCorrect(
            final List<LinkItem> breadcrumbs, final String childName, final String parentHref) {
        assertEquals(2, breadcrumbs.size(), "Check size");
        assertEquals(createLinkItem("Parent", parentHref), breadcrumbs.get(0), "Check parent item");
        assertEquals(createLinkItem(childName, "index.html"), breadcrumbs.get(1), "Check child item");
    }

    /**
     * https://issues.apache.org/jira/browse/DOXIASITETOOLS-62
     */
    @Test
    public void testBreadcrumbCutParentAfterDuplicate() {
        DecorationModel child = new DecorationModel(); // B > E
        child.setBody(new Body());
        child.getBody().addBreadcrumb(createLinkItem("B", null));
        child.getBody().addBreadcrumb(createLinkItem("E", null));

        DecorationModel parent = new DecorationModel(); // A > B > C > D
        parent.setBody(new Body());
        parent.getBody().addBreadcrumb(createLinkItem("A", null));
        parent.getBody().addBreadcrumb(createLinkItem("B", null));
        parent.getBody().addBreadcrumb(createLinkItem("C", null));
        parent.getBody().addBreadcrumb(createLinkItem("D", null));

        assembler.assembleModelInheritance(
                NAME, child, parent, "http://maven.apache.org/doxia", "http://maven.apache.org");

        final List<LinkItem> breadcrumbs = child.getBody().getBreadcrumbs(); // expected: A > B > E
        assertEquals(3, breadcrumbs.size(), "Check size");
        assertEquals(createLinkItem("A", null), breadcrumbs.get(0), "Check item");
        assertEquals(createLinkItem("B", null), breadcrumbs.get(1), "Check item");
        assertEquals(createLinkItem("E", null), breadcrumbs.get(2), "Check item");
    }

    /**
     *
     */
    @Test
    public void testBannerWithoutHref() {
        DecorationModel model = new DecorationModel();
        model.setBody(new Body());

        Banner banner = createBanner("Left", null, "/images/src.gif", "alt");

        model.setBannerLeft(banner);

        assembler.resolvePaths(model, "http://foo.apache.org");

        assertEquals(createBanner("Left", null, "images/src.gif", "alt"), model.getBannerLeft(), "Check banner");
    }

    /**
     *
     */
    @Test
    public void testLogoWithoutImage() {
        // This should actually be validated in the model, it doesn't really make sense
        DecorationModel model = new DecorationModel();
        model.setBody(new Body());
        model.addPoweredBy(createLogo("Foo", "http://foo.apache.org", null));
        assembler.resolvePaths(model, "http://foo.apache.org");
        assertEquals(1, model.getPoweredBy().size(), "Check size");
        assertEquals(createLogo("Foo", "./", null), model.getPoweredBy().get(0), "Check item");
    }

    private static Banner createBanner(String name, String href, String src, String alt) {
        Banner banner = new Banner();
        banner.setName(name);
        banner.setHref(href);
        banner.setSrc(src);
        banner.setAlt(alt);
        return banner;
    }

    private Logo createLogo(String name, String href, String img) {
        Logo logo = new Logo();
        logo.setHref(href);
        logo.setImg(img);
        logo.setName(name);
        return logo;
    }

    private static LinkItem createLinkItem(String name, String href) {
        LinkItem item = new LinkItem();
        item.setName(name);
        item.setHref(href);
        return item;
    }

    private DecorationModel readModel(String name) throws IOException, XmlPullParserException {
        Reader reader = null;
        try {
            reader = ReaderFactory.newXmlReader(getClass().getResourceAsStream("/" + name));
            return new DecorationXpp3Reader().read(reader);
        } finally {
            IOUtil.close(reader);
        }
    }
}
