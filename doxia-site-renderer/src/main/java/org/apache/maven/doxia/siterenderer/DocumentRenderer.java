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
package org.apache.maven.doxia.siterenderer;

import java.io.IOException;
import java.io.Writer;

/**
 * Renders a page in a site, whatever the source is: a Doxia source file, a report or anything else.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @see DocumentRenderingContext document rendering context
 */
public interface DocumentRenderer {
    /**
     * Render a document in a site.
     *
     * @param writer the Writer for the document output.
     * @param siteRenderer the site renderer to merge document content to.
     * @param siteRenderingContext the site rendering context.
     * @throws RendererException if it bombs.
     * @throws IOException if it bombs.
     */
    void renderDocument(Writer writer, SiteRenderer siteRenderer, SiteRenderingContext siteRenderingContext)
            throws IOException, RendererException;

    /**
     * The output path of the document.
     * <p>
     * Note: This method won't be {@code default} anymore when {@link #getOutputName()} is removed.
     * You are advised to implement it as soon as possible.
     *
     * @since 2.0.0
     * @return the name of the output document.
     */
    default String getOutputPath() {
        return getOutputName();
    }

    /**
     * @deprecated Method name does not properly reflect its purpose. Implement and use
     * {@link #getOutputPath()} instead.
     */
    @Deprecated
    String getOutputName();

    /**
     * Return the rendering context of the document.
     *
     * @return DocumentRenderingContext.
     */
    DocumentRenderingContext getRenderingContext();

    /**
     * Whether to always overwrite the document, or only do so when it is changed.
     *
     * @return whether to overwrite
     */
    boolean isOverwrite();

    /**
     * Whether this document is an external report, independent from the site templating.
     *
     * @return {@code true} if report is external, otherwise {@code false}
     * @since 1.7
     */
    boolean isExternalReport();
}
