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

import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;

/**
 * <p>Renderer interface.</p>
 *
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public interface Renderer
{
    /**
     * Plexus lookup role.
     */
    String ROLE = Renderer.class.getName();

    /**
     * Render a collection of documents.
     *
     * @param documents the documents to render.
     * @param siteRenderingContext the SiteRenderingContext to use.
     * @param outputDirectory the output directory to write results.
     * @throws org.apache.maven.doxia.siterenderer.RendererException if it bombs.
     * @throws java.io.IOException if it bombs.
     */
    void render( Collection<DocumentRenderer> documents, SiteRenderingContext siteRenderingContext,
                 File outputDirectory )
        throws RendererException, IOException;

    /**
     * Generate a document.
     *
     * @param writer the Writer to use.
     * @param sink the Sink to receive the events.
     * @param siteRenderingContext the SiteRenderingContext to use.
     * @throws org.apache.maven.doxia.siterenderer.RendererException if it bombs.
     */
    void generateDocument( Writer writer, SiteRendererSink sink, SiteRenderingContext siteRenderingContext )
        throws RendererException;

    /**
     * Return a SiteRenderingContext.
     *
     * @param skinFile
     * @param attributes
     * @param decoration
     * @param defaultWindowTitle
     * @param locale
     * @return a SiteRenderingContext.
     * @throws java.io.IOException if it bombs.
     */
    SiteRenderingContext createContextForSkin( File skinFile, Map<String, ?> attributes, DecorationModel decoration,
                                               String defaultWindowTitle, Locale locale )
        throws IOException;

    /**
     * Return a SiteRenderingContext.
     *
     * @param templateFile
     * @param skinFile
     * @param attributes
     * @param decoration
     * @param defaultWindowTitle
     * @param locale
     * @return a SiteRenderingContext.
     * @throws java.net.MalformedURLException if it bombs.
     */
    SiteRenderingContext createContextForTemplate( File templateFile, File skinFile, Map<String, ?> attributes,
                                                   DecorationModel decoration, String defaultWindowTitle,
                                                   Locale locale )
        throws MalformedURLException;

    /**
     * Copy resource files.
     *
     * @param siteRenderingContext
     * @param resourcesDirectory
     * @param outputDirectory
     * @throws java.io.IOException if it bombs.
     */
    void copyResources( SiteRenderingContext siteRenderingContext, File resourcesDirectory, File outputDirectory )
        throws IOException;

    /**
     * Return the document files in a Map.
     *
     * @param siteRenderingContext
     * @return the document files in a Map.
     * @throws java.io.IOException if it bombs.
     * @throws org.apache.maven.doxia.siterenderer.RendererException if it bombs.
     */
    Map<String, DocumentRenderer> locateDocumentFiles( SiteRenderingContext siteRenderingContext )
        throws IOException, RendererException;

    /**
     * Render a document.
     *
     * @param writer the writer to render the document to.
     * @param renderingContext the document's rendering context
     * @param siteContext the site's rendering context
     * @throws RendererException if it bombs.
     * @throws FileNotFoundException if it bombs.
     * @throws UnsupportedEncodingException if it bombs.
     */
    void renderDocument( Writer writer, RenderingContext renderingContext, SiteRenderingContext siteContext )
        throws RendererException, FileNotFoundException, UnsupportedEncodingException;
}
