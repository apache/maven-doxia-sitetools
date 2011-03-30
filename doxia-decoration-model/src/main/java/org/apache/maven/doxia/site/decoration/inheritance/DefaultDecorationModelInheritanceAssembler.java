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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.doxia.site.decoration.Banner;
import org.apache.maven.doxia.site.decoration.Body;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.site.decoration.LinkItem;
import org.apache.maven.doxia.site.decoration.Logo;
import org.apache.maven.doxia.site.decoration.Menu;
import org.apache.maven.doxia.site.decoration.MenuItem;
import org.apache.maven.doxia.site.decoration.PublishDate;
import org.apache.maven.doxia.site.decoration.Skin;
import org.apache.maven.doxia.site.decoration.Version;

import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Manage inheritance of the decoration model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.doxia.site.decoration.inheritance.DecorationModelInheritanceAssembler"
 */
public class DefaultDecorationModelInheritanceAssembler
    implements DecorationModelInheritanceAssembler
{
    /** {@inheritDoc} */
    public void assembleModelInheritance( String name, DecorationModel child, DecorationModel parent,
                                          String childBaseUrl, String parentBaseUrl )
    {
        // cannot inherit from null parent.
        if ( parent == null )
        {
            return;
        }

        URLContainer urlContainer = new URLContainer( parentBaseUrl, childBaseUrl );

        if ( child.getBannerLeft() == null && parent.getBannerLeft() != null )
        {
            child.setBannerLeft( (Banner) parent.getBannerLeft().clone() );
            rebaseBannerPaths( child.getBannerLeft(), urlContainer );
        }

        if ( child.getBannerRight() == null && parent.getBannerRight() != null)
        {
            child.setBannerRight( (Banner) parent.getBannerRight().clone() );
            rebaseBannerPaths( child.getBannerRight(), urlContainer );
        }

        if ( child.getPublishDate() == null && parent.getPublishDate() != null )
        {
            child.setPublishDate( (PublishDate) parent.getPublishDate().clone() );
        }

        if ( child.getVersion() == null && parent.getVersion() != null )
        {
            child.setVersion( (Version) parent.getVersion().clone() );
        }

        if ( child.getSkin() == null && parent.getSkin() != null )
        {
            child.setSkin( (Skin) parent.getSkin().clone() );
        }

        child.setPoweredBy( mergePoweredByLists( child.getPoweredBy(), parent.getPoweredBy(), urlContainer ) );

        if ( parent.getLastModified() > child.getLastModified() )
        {
            child.setLastModified( parent.getLastModified() );
        }

        assembleBodyInheritance( name, child, parent, urlContainer );

        assembleCustomInheritance( child, parent );
    }

    /** {@inheritDoc} */
    public void resolvePaths( final DecorationModel decoration, final String baseUrl )
    {
        if ( baseUrl == null )
        {
            return;
        }

        if ( decoration.getBannerLeft() != null )
        {
            relativizeBannerPaths( decoration.getBannerLeft(), baseUrl );
        }

        if ( decoration.getBannerRight() != null )
        {
            relativizeBannerPaths( decoration.getBannerRight(), baseUrl );
        }

        for ( Logo logo : decoration.getPoweredBy() )
        {
            relativizeLogoPaths( logo, baseUrl );
        }

        if ( decoration.getBody() != null )
        {
            for ( LinkItem linkItem : decoration.getBody().getLinks() )
            {
                relativizeLinkItemPaths( linkItem, baseUrl );
            }

            for ( LinkItem linkItem : decoration.getBody().getBreadcrumbs() )
            {
                relativizeLinkItemPaths( linkItem, baseUrl );
            }

            for ( Menu menu : decoration.getBody().getMenus() )
            {
                relativizeMenuPaths( menu.getItems(), baseUrl );
            }
        }
    }

    /**
     * Resolves all relative paths between the elements in a banner. The banner element might contain relative paths
     * to the oldBaseUrl, these are changed to the newBannerUrl.
     *
     * @param banner
     * @param baseUrl
     */
    private void relativizeBannerPaths( final Banner banner, final String baseUrl )
    {
        // banner has been checked to be not null, both href and src may be empty or null
        banner.setHref( relativizeLink( banner.getHref(), baseUrl ) );
        banner.setSrc( relativizeLink( banner.getSrc(), baseUrl ) );
    }

    private void rebaseBannerPaths( final Banner banner, final URLContainer urlContainer )
    {
        if ( banner.getHref() != null ) // it may be empty
        {
            banner.setHref( rebaseLink( banner.getHref(), urlContainer ) );
        }

        if ( banner.getSrc() != null )
        {
            banner.setSrc( rebaseLink( banner.getSrc(), urlContainer ) );
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
                breadcrumb.setHref( "" );
                cBody.getBreadcrumbs().add( breadcrumb );
            }
            cBody.setBreadcrumbs( mergeLinkItemLists( cBody.getBreadcrumbs(), pBody.getBreadcrumbs(), urlContainer ) );

            cBody.setMenus( mergeMenus( cBody.getMenus(), pBody.getMenus(), urlContainer ) );
        }
    }

    private List<Menu> mergeMenus( final List<Menu> childMenus, final List<Menu> parentMenus,
                                   final URLContainer urlContainer )
    {
        List<Menu> menus = new ArrayList<Menu>( childMenus.size() + parentMenus.size() );

        for ( Menu menu : childMenus )
        {
            menus.add( menu );
        }

        int topCounter = 0;
        for ( Menu menu : parentMenus )
        {
            menu = menu.clone();

            if ( "top".equals( menu.getInherit() ) )
            {
                menus.add( topCounter, menu );
                topCounter++;

                rebaseMenuPaths( menu.getItems(), urlContainer );
            }
            else if ( "bottom".equals( menu.getInherit() ) )
            {
                menus.add( menu );

                rebaseMenuPaths( menu.getItems(), urlContainer );
            }
        }

        return menus;
    }

    private void relativizeMenuPaths( final List<MenuItem> items, final String baseUrl )
    {
        for ( MenuItem item : items )
        {
            relativizeLinkItemPaths( item, baseUrl );
            relativizeMenuPaths( item.getItems(), baseUrl );
        }
    }

    private void rebaseMenuPaths( final List<MenuItem> items, final URLContainer urlContainer )
    {
        for ( MenuItem item : items )
        {
            rebaseLinkItemPaths( item, urlContainer );
            rebaseMenuPaths( item.getItems(), urlContainer );
        }
    }

    private void relativizeLinkItemPaths( final LinkItem item, final String baseUrl )
    {
        item.setHref( relativizeLink( item.getHref(), baseUrl ) );
    }

    private void rebaseLinkItemPaths( final LinkItem item, final URLContainer urlContainer )
    {
        item.setHref( rebaseLink( item.getHref(), urlContainer ) );
    }

    private void relativizeLogoPaths( final Logo logo, final String baseUrl )
    {
        logo.setImg( relativizeLink( logo.getImg(), baseUrl ) );
        relativizeLinkItemPaths( logo, baseUrl );
    }

    private void rebaseLogoPaths( final Logo logo, final URLContainer urlContainer )
    {
        logo.setImg( rebaseLink( logo.getImg(), urlContainer ) );
        rebaseLinkItemPaths( logo, urlContainer );
    }

    private List<LinkItem> mergeLinkItemLists( final List<LinkItem> childList, final List<LinkItem> parentList,
                                               final URLContainer urlContainer )
    {
        List<LinkItem> items = new ArrayList<LinkItem>( childList.size() + parentList.size() );

        for ( LinkItem item : parentList )
        {
            item = item.clone();

            rebaseLinkItemPaths( item, urlContainer );

            if ( !items.contains( item ) )
            {
                items.add( item );
            }
        }

        for ( LinkItem item : childList )
        {
            if ( !items.contains( item ) )
            {
                items.add( item );
            }
        }

        return items;
    }

    private List<Logo> mergePoweredByLists( final List<Logo> childList, final List<Logo> parentList,
                                            final URLContainer urlContainer )
    {
        List<Logo> logos = new ArrayList<Logo>( childList.size() + parentList.size() );

        for ( Logo logo : parentList )
        {
            logo = logo.clone();

            if ( !logos.contains( logo ) )
            {
                logos.add( logo );
            }

            rebaseLogoPaths( logo, urlContainer );
        }

        for ( Logo logo : childList )
        {
            if ( !logos.contains( logo ) )
            {
                logos.add( logo );
            }
        }

        return logos;
    }

    // rebase only affects relative links, a relative link wrt an old base gets translated,
    // so it points to the same location as viewed from a new base
    private String rebaseLink( final String link, final URLContainer urlContainer )
    {
        if ( link == null || urlContainer.getOldPath() == null )
        {
            return link;
        }

        final URIPathDescriptor oldPath = new URIPathDescriptor( urlContainer.getOldPath(), link );

        return oldPath.rebaseLink( urlContainer.getNewPath() ).toString();
    }

    // relativize only affects absolute links, if the link has the same scheme, host and port
    // as the base, it is made into a relative link as viewed from the base
    private String relativizeLink( final String link, final String baseUri )
    {
        if ( link == null || baseUri == null )
        {
            return link;
        }

        // this shouldn't be necessary, just to swallow mal-formed hrefs
        try
        {
            final URIPathDescriptor path = new URIPathDescriptor( baseUri, link );

            return path.relativizeLink().toString();
        }
        catch ( IllegalArgumentException e )
        {
            return link;
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
