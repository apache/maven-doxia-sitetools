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
 * Renderer for a document that has a source file to be parsed by Doxia.
 * Details about the source file are in {@link RenderingContext}, which is expected to have
 * a non-null parserId and extension.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class DoxiaDocumentRenderer
    implements DocumentRenderer
{
    private RenderingContext renderingContext;

    /**
     * Constructor.
     *
     * @param renderingContext the document's RenderingContext to use.
     */
    public DoxiaDocumentRenderer( RenderingContext renderingContext )
    {
        this.renderingContext = renderingContext;
    }

    /** {@inheritDoc} */
    public void renderDocument( Writer writer, Renderer siteRenderer, SiteRenderingContext siteRenderingContext )
        throws RendererException, FileNotFoundException, UnsupportedEncodingException
    {
        siteRenderer.renderDocument( writer, renderingContext, siteRenderingContext );
    }

    /** {@inheritDoc} */
    public String getOutputName()
    {
        return renderingContext.getOutputName();
    }

    /** {@inheritDoc} */
    public RenderingContext getRenderingContext()
    {
        return renderingContext;
    }

    /** {@inheritDoc} */
    public boolean isOverwrite()
    {
        return false;
    }

    public boolean isExternalReport()
    {
        return false;
    }

}
