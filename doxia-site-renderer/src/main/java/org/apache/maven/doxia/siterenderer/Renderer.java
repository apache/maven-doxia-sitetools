package org.apache.maven.doxia.siterenderer;

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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;

/**
 * <p>Site Renderer interface: render a collection of documents into a site, ie decored with a site template
 * (eventually packaged as skin).</p>
 *
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public interface Renderer // TODO rename to SiteRenderer
{
    /**
     * Plexus lookup role.
     */
    String ROLE = Renderer.class.getName();

    /**
     * Render a collection of documents into a site.
     *
     * @param documents the documents to render.
     * @param siteRenderingContext the SiteRenderingContext to use.
     * @param outputDirectory the output directory to write results.
     * @throws RendererException if it bombs.
     * @throws IOException if it bombs.
     */
    void render( Collection<DocumentRenderer> documents, SiteRenderingContext siteRenderingContext,
                 File outputDirectory )
        throws RendererException, IOException;

    /**
     * Generate a document output from a Doxia SiteRenderer Sink, i.e. merge the document content into
     * the site template.
     *
     * @param writer the Writer to use.
     * @param sink the Site Renderer Sink that received the Doxia events during document content rendering.
     * @param siteRenderingContext the SiteRenderingContext to use.
     * @throws RendererException if it bombs.
     * @deprecated since 1.8, use mergeDocumentIntoSite
     */
    void generateDocument( Writer writer, SiteRendererSink sink, SiteRenderingContext siteRenderingContext )
        throws RendererException;

    /**
     * Generate a document output integrated in a site from a document content,
     * i.e. merge the document content into the site template.
     *
     * @param writer the Writer to use.
     * @param content the document content to be merged.
     * @param siteRenderingContext the SiteRenderingContext to use.
     * @throws RendererException if it bombs.
     * @since 1.8
     */
    void mergeDocumentIntoSite( Writer writer, DocumentContent content, SiteRenderingContext siteRenderingContext )
        throws RendererException;

    /**
     * Create a Site Rendering Context for a site using a skin.
     *
     * @param skin a skin
     * @param attributes attributes to use
     * @param decoration a decoration model
     * @param defaultWindowTitle default window title
     * @param locale locale to use
     * @return a SiteRenderingContext.
     * @throws RendererException if it bombs.
     * @throws java.io.IOException if it bombs.
     * @since 1.7.3 was previously with skin as File instead of Artifact
     */
    SiteRenderingContext createContextForSkin( Artifact skin, Map<String, ?> attributes, DecorationModel decoration,
                                               String defaultWindowTitle, Locale locale )
        throws RendererException, IOException;

    /**
     * Create a Site Rendering Context for a site using a local template.
     *
     * @param templateFile template file
     * @param attributes attributes to use
     * @param decoration a decoration model
     * @param defaultWindowTitle default window title
     * @param locale locale to use
     * @return a SiteRenderingContext.
     * @throws MalformedURLException if it bombs.
     * @since 1.7, had an additional skinFile parameter before
     * @deprecated Deprecated without replacement, use skins only.
     * @see #createContextForSkin(Artifact, Map, DecorationModel, String, Locale)
     */
    @Deprecated
    SiteRenderingContext createContextForTemplate( File templateFile, Map<String, ?> attributes,
                                                   DecorationModel decoration, String defaultWindowTitle,
                                                   Locale locale )
        throws MalformedURLException;

    /**
     * Copy resource files.
     *
     * @param siteRenderingContext the SiteRenderingContext to use
     * @param resourcesDirectory resources directory as file
     * @param outputDirectory output directory as file
     * @throws IOException if it bombs.
     * @deprecated since 1.7, use copyResources without resourcesDirectory parameter
     */
    void copyResources( SiteRenderingContext siteRenderingContext, File resourcesDirectory, File outputDirectory )
        throws IOException;

    /**
     * Copy resource files from skin, template, and site resources.
     *
     * @param siteRenderingContext the SiteRenderingContext to use.
     * @param outputDirectory output directory as file
     * @throws IOException if it bombs.
     * @since 1.7
     */
    void copyResources( SiteRenderingContext siteRenderingContext, File outputDirectory )
        throws IOException;

    /**
     * Locate Doxia document source files in the site source context.
     *
     * @param siteRenderingContext the SiteRenderingContext to use
     * @return the Doxia document renderers in a Map keyed by output file name.
     * @throws IOException if it bombs.
     * @throws RendererException if it bombs.
     * @deprecated since 1.8, use locateDocumentFiles with editable parameter
     */
    Map<String, DocumentRenderer> locateDocumentFiles( SiteRenderingContext siteRenderingContext )
        throws IOException, RendererException;

    /**
     * Locate Doxia document source files in the site source context.
     *
     * @param siteRenderingContext the SiteRenderingContext to use
     * @param editable Doxia document renderer as editable? (should not set editable if generated Doxia source)
     * @return the Doxia document renderers in a Map keyed by output file name.
     * @throws IOException if it bombs.
     * @throws RendererException if it bombs.
     * @since 1.8
     */
    Map<String, DocumentRenderer> locateDocumentFiles( SiteRenderingContext siteRenderingContext, boolean editable )
        throws IOException, RendererException;

    /**
     * Render a document written in a Doxia markup language. This method is an internal method, used by
     * {@link DoxiaDocumentRenderer}.
     *
     * @param writer the writer to render the document to.
     * @param docRenderingContext the document's rendering context, which is expected to have a non-null parser id.
     * @param siteContext the site's rendering context
     * @throws RendererException if it bombs.
     * @throws FileNotFoundException if it bombs.
     * @throws UnsupportedEncodingException if it bombs.
     */
    void renderDocument( Writer writer, RenderingContext docRenderingContext, SiteRenderingContext siteContext )
        throws RendererException, FileNotFoundException, UnsupportedEncodingException;
}
