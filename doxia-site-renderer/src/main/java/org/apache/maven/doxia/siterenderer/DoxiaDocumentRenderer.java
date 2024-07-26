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
 * Renderer for a document that has a source file to be parsed by Doxia.
 * Details about the source file are in {@link DocumentRenderingContext}, which is expected to have
 * a non-null parserId and extension.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class DoxiaDocumentRenderer implements DocumentRenderer {
    private DocumentRenderingContext docRenderingContext;

    /**
     * Constructor.
     *
     * @param docRenderingContext the document's rendering context to use.
     */
    public DoxiaDocumentRenderer(DocumentRenderingContext docRenderingContext) {
        this.docRenderingContext = docRenderingContext;
    }

    /** {@inheritDoc} */
    public void renderDocument(Writer writer, SiteRenderer siteRenderer, SiteRenderingContext siteRenderingContext)
            throws IOException, RendererException {
        siteRenderer.renderDocument(writer, docRenderingContext, siteRenderingContext);
    }

    /** {@inheritDoc} */
    public String getOutputName() {
        return docRenderingContext.getOutputName();
    }

    /** {@inheritDoc} */
    public DocumentRenderingContext getRenderingContext() {
        return docRenderingContext;
    }

    /** {@inheritDoc} */
    public boolean isOverwrite() {
        return false;
    }

    public boolean isExternalReport() {
        return false;
    }
}
