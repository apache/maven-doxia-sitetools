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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.doxia.site.decoration.Banner;
import org.apache.maven.doxia.site.decoration.Body;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.site.decoration.LinkItem;
import org.apache.maven.doxia.site.decoration.Logo;
import org.apache.maven.doxia.site.decoration.Menu;
import org.apache.maven.doxia.site.decoration.MenuItem;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Manage inheritance of the decoration model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.doxia.site.decoration.inheritance.DecorationModelInheritanceAssembler"
 */
public class DefaultDecorationModelInheritanceAssembler implements DecorationModelInheritanceAssembler
{
    /** {@inheritDoc} */
    public void assembleModelInheritance( String name, DecorationModel child, DecorationModel parent,
                                          String childBaseUrl, String parentBaseUrl )
    {
        URLContainer urlContainer = new URLContainer( parentBaseUrl, childBaseUrl );

        // cannot inherit from null parent.
        if ( parent != null )
        {
            if ( child.getBannerLeft() == null )
            {
                child.setBannerLeft( parent.getBannerLeft() );
                resolveBannerPaths( child.getBannerLeft(), urlContainer );
            }

            if ( child.getBannerRight() == null )
            {
                child.setBannerRight( parent.getBannerRight() );

                resolveBannerPaths( child.getBannerRight(), urlContainer );
            }

            if ( child.getPublishDate() == null )
            {
                child.setPublishDate( parent.getPublishDate() );
            }

            if ( child.getVersion() == null )
            {
                child.setVersion( parent.getVersion() );
            }

            if ( child.getSkin() == null )
            {
                child.setSkin( parent.getSkin() );
            }

            child.setPoweredBy( mergePoweredByLists( child.getPoweredBy(), parent.getPoweredBy(), urlContainer ) );

            if ( parent.getLastModified() > child.getLastModified() )
            {
                child.setLastModified( parent.getLastModified() );
            }

            assembleBodyInheritance( name, child, parent, urlContainer );

            assembleCustomInheritance( child, parent );
        }
    }

    /** {@inheritDoc} */
    public void resolvePaths( final DecorationModel decoration, final String baseUrl )
    {
        URLContainer urlContainer = new URLContainer( null, baseUrl );

        if ( decoration.getBannerLeft() != null )
        {
            resolveBannerPaths( decoration.getBannerLeft(), urlContainer );
        }

        if ( decoration.getBannerRight() != null )
        {
            resolveBannerPaths( decoration.getBannerRight(), urlContainer );
        }

        for ( Iterator i = decoration.getPoweredBy().iterator(); i.hasNext(); )
        {
            Logo logo = (Logo) i.next();

            resolveLogoPaths( logo, urlContainer );
        }

        if ( decoration.getBody() != null )
        {
            for ( Iterator i = decoration.getBody().getLinks().iterator(); i.hasNext(); )
            {
                LinkItem linkItem = (LinkItem) i.next();

                resolveLinkItemPaths( linkItem, urlContainer );
            }

            for ( Iterator i = decoration.getBody().getBreadcrumbs().iterator(); i.hasNext(); )
            {
                LinkItem linkItem = (LinkItem) i.next();

                resolveLinkItemPaths( linkItem, urlContainer );
            }

            for ( Iterator i = decoration.getBody().getMenus().iterator(); i.hasNext(); )
            {
                Menu menu = (Menu) i.next();

                resolveMenuPaths( menu.getItems(), urlContainer );
            }
        }
    }

    /**
     * Resolves all relative paths between the elements in a banner. The banner element might contain relative paths
     * to the oldBaseUrl, these are changed to the newBannerUrl.
     *
     * @param banner
     * @param urlContainer
     */
    private void resolveBannerPaths( final Banner banner, final URLContainer urlContainer )
    {
        if ( banner != null )
        {
            if ( StringUtils.isNotEmpty( banner.getHref() ) )
            {
                banner.setHref( convertPath( banner.getHref(), urlContainer ) );
            }
            if ( StringUtils.isNotEmpty( banner.getSrc() ) )
            {
                banner.setSrc( convertPath( banner.getSrc(), urlContainer ) );
            }
        }
    }

    private void assembleCustomInheritance( final DecorationModel child, final DecorationModel parent )
    {
        if ( child.getCustom() == null )
        {
            child.setCustom( parent.getCustom() );
        }
        else
        {
            child.setCustom( Xpp3Dom.mergeXpp3Dom( (Xpp3Dom) child.getCustom(), (Xpp3Dom) parent.getCustom() ) );
        }
    }

    private void assembleBodyInheritance( final String name, final DecorationModel child, final DecorationModel parent,
                                          final URLContainer urlContainer )
    {
        Body cBody = child.getBody();
        Body pBody = parent.getBody();

        if ( cBody != null || pBody != null )
        {
            if ( cBody == null )
            {
                cBody = new Body();
                child.setBody( cBody );
            }

            if ( pBody == null )
            {
                pBody = new Body();
            }

            if ( cBody.getHead() == null )
            {
                cBody.setHead( pBody.getHead() );
            }
            else
            {
                cBody.setHead( Xpp3Dom.mergeXpp3Dom( (Xpp3Dom) cBody.getHead(), (Xpp3Dom) pBody.getHead() ) );
            }

            cBody.setLinks( mergeLinkItemLists( cBody.getLinks(), pBody.getLinks(), urlContainer ) );

            if ( cBody.getBreadcrumbs().isEmpty() && !pBody.getBreadcrumbs().isEmpty() )
            {
                LinkItem breadcrumb = new LinkItem();
                breadcrumb.setName( name );
                breadcrumb.setHref( convertPath( urlContainer.getNewPath(), urlContainer ) );
                cBody.getBreadcrumbs().add( breadcrumb );
            }
            cBody.setBreadcrumbs( mergeLinkItemLists( cBody.getBreadcrumbs(), pBody.getBreadcrumbs(), urlContainer ) );

            cBody.setMenus( mergeMenus( cBody.getMenus(), pBody.getMenus(), urlContainer ) );
        }
    }

    private List mergeMenus( final List childMenus, final List parentMenus, final URLContainer urlContainer )
    {
        List menus = new ArrayList();

        for ( Iterator it = childMenus.iterator(); it.hasNext(); )
        {
            Menu menu = (Menu) it.next();

            menus.add( menu );
        }

        int topCounter = 0;
        for ( Iterator it = parentMenus.iterator(); it.hasNext(); )
        {
            Menu menu = (Menu) it.next();

            if ( "top".equals( menu.getInherit() ) )
            {
                menus.add( topCounter, menu );
                topCounter++;

                resolveMenuPaths( menu.getItems(), urlContainer );
            }
            else if ( "bottom".equals( menu.getInherit() ) )
            {
                menus.add( menu );

                resolveMenuPaths( menu.getItems(), urlContainer );
            }
        }

        return menus;
    }

    private void resolveMenuPaths( final List items, final URLContainer urlContainer )
    {
        for ( Iterator i = items.iterator(); i.hasNext(); )
        {
            MenuItem item = (MenuItem) i.next();
            resolveLinkItemPaths( item, urlContainer );
            resolveMenuPaths( item.getItems(), urlContainer );
        }
    }

    private void resolveLinkItemPaths( final LinkItem item, final URLContainer urlContainer )
    {
        if ( StringUtils.isNotEmpty( item.getHref() ) )
        {
            String href = convertPath( item.getHref(), urlContainer );
            if ( StringUtils.isNotEmpty( href ) )
            {
                item.setHref( href );
            }
        }
        else
        {
            item.setHref( convertPath( "", urlContainer ) );
        }
    }

    private void resolveLogoPaths( final Logo logo, final URLContainer urlContainer )
    {
        logo.setImg( convertPath( logo.getImg(), urlContainer ) );
        resolveLinkItemPaths( logo, urlContainer );
    }

    private List mergeLinkItemLists( final List childList, final List parentList, final URLContainer urlContainer )
    {
        List items = new ArrayList();

        for ( Iterator it = parentList.iterator(); it.hasNext(); )
        {
            LinkItem item = (LinkItem) it.next();

            resolveLinkItemPaths( item, urlContainer );

            if ( !items.contains( item ) )
            {
                items.add( item );
            }
        }

        for ( Iterator it = childList.iterator(); it.hasNext(); )
        {
            LinkItem item = (LinkItem) it.next();

            if ( !items.contains( item ) )
            {
                items.add( item );
            }
        }

        return items;
    }

    private List mergePoweredByLists( final List childList, final List parentList, final URLContainer urlContainer )
    {
        List logos = new ArrayList();

        for ( Iterator it = parentList.iterator(); it.hasNext(); )
        {
            Logo logo = (Logo) it.next();

            if ( !logos.contains( logo ) )
            {
                logos.add( logo );
            }

            resolveLogoPaths( logo, urlContainer );
        }

        for ( Iterator it = childList.iterator(); it.hasNext(); )
        {
            Logo logo = (Logo) it.next();

            if ( !logos.contains( logo ) )
            {
                logos.add( logo );
            }
        }

        return logos;
    }

    private String convertPath( final String relativePath, final URLContainer urlContainer )
    {
        try
        {
            PathDescriptor oldPathDescriptor = new PathDescriptor( urlContainer.getOldPath(), relativePath );
            PathDescriptor newPathDescriptor = new PathDescriptor( urlContainer.getNewPath(), "" );

            PathDescriptor relativePathDescriptor = PathUtils.convertPath( oldPathDescriptor, newPathDescriptor );

            return relativePathDescriptor.getLocation();
        }
        catch ( MalformedURLException mue )
        {
            throw new RuntimeException( "While converting Pathes:", mue );
        }
    }

    /**
     * Contains an old and a new path.
     */
    public final class URLContainer
    {

        private final String oldPath;

        private final String newPath;

        /**
         * Construct a URLContainer.
         *
         * @param oldPath the old path.
         * @param newPath the new path.
         */
        public URLContainer( final String oldPath, final String newPath )
        {
            this.oldPath = oldPath;
            this.newPath = newPath;
        }

        /**
         * Get the new path.
         *
         * @return the new path.
         */
        public String getNewPath()
        {
            return this.newPath;
        }

        /**
         * Get the old path.
         *
         * @return the old path.
         */
        public String getOldPath()
        {
            return this.oldPath;
        }
    }
}
