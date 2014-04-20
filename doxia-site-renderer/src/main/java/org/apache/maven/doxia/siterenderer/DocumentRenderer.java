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

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Renders a page.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public interface DocumentRenderer
{
    /**
     * Render a document.
     *
     * @param writer the Writer.
     * @param renderer the Renderer.
     * @param siteRenderingContext the SiteRenderingContext.
     * @throws org.apache.maven.doxia.siterenderer.RendererException if it bombs.
     * @throws java.io.FileNotFoundException if it bombs.
     * @throws java.io.UnsupportedEncodingException if it bombs.
     */
    void renderDocument( Writer writer, Renderer renderer, SiteRenderingContext siteRenderingContext )
        throws RendererException, FileNotFoundException, UnsupportedEncodingException;

    /**
     * The name of the output document.
     *
     * @return the name of the output document.
     */
    String getOutputName();

    /**
     * Return the RenderingContext of the document.
     *
     * @return RenderingContext.
     */
    RenderingContext getRenderingContext();

    /**
     * Whether to always overwrite the document, or only do so when it is changed.
     *
     * @return whether to overwrite
     */
    boolean isOverwrite();
}
