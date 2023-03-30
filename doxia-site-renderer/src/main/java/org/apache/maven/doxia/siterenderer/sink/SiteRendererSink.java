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
package org.apache.maven.doxia.siterenderer.sink;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.doxia.markup.HtmlMarkup;
import org.apache.maven.doxia.module.xhtml5.Xhtml5Sink;
import org.apache.maven.doxia.siterenderer.DocumentContent;
import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;
import org.codehaus.plexus.util.StringUtils;

/**
 * Sink for site rendering of a document, to allow later merge document's output with a template.
 * During raw Doxia rendering, content is stored in multiple fields for later use when incorporating
 * into skin or template: title, date, authors, head, body
 *
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
@SuppressWarnings("checkstyle:methodname")
public class SiteRendererSink extends Xhtml5Sink implements DocumentContent {
    private String date;

    private String title;

    private List<String> authors = new ArrayList<String>();

    private final StringWriter headWriter;

    private final Writer writer;

    private DocumentRenderingContext docRenderingContext;

    /**
     * Construct a new SiteRendererSink for a document.
     *
     * @param docRenderingContext the document's rendering context.
     */
    public SiteRendererSink(DocumentRenderingContext docRenderingContext) {
        this(new StringWriter(), docRenderingContext);
    }

    /**
     * Construct a new SiteRendererSink for a document.
     *
     * @param writer the writer for the sink.
     * @param docRenderingContext the document's rendering context.
     */
    private SiteRendererSink(StringWriter writer, DocumentRenderingContext docRenderingContext) {
        super(writer);

        this.writer = writer;
        this.headWriter = new StringWriter();
        this.docRenderingContext = docRenderingContext;

        /* the template is expected to have used the main tag, which can be used only once */
        super.contentStack.push(HtmlMarkup.MAIN);
    }

    /** {@inheritDoc} */
    @Override
    public void title_() {
        if (getTextBuffer().length() > 0) {
            title = getTextBuffer().toString();
        }

        resetTextBuffer();
    }

    /**
     * {@inheritDoc}
     *
     * Reset text buffer, since text content before title mustn't be in title.
     * @see org.apache.maven.doxia.module.xhtml5.Xhtml5Sink#title()
     */
    @Override
    public void title() {
        resetTextBuffer();
    }

    /** {@inheritDoc} */
    @Override
    public void author() {
        resetTextBuffer();
    }

    /** {@inheritDoc} */
    @Override
    public void author_() {
        if (getTextBuffer().length() > 0) {
            String text = getTextBuffer().toString().trim();
            authors.add(text);
        }

        resetTextBuffer();
    }

    /** {@inheritDoc} */
    @Override
    public void date() {
        resetTextBuffer();
    }

    /** {@inheritDoc} */
    @Override
    public void date_() {
        if (getTextBuffer().length() > 0) {
            date = getTextBuffer().toString().trim();
        }

        resetTextBuffer();
    }

    /**
     * {@inheritDoc}
     *
     * Do nothing.
     * @see org.apache.maven.doxia.module.xhtml5.Xhtml5Sink#body_()
     */
    @Override
    public void body_() {
        // nop
    }

    /**
     * {@inheritDoc}
     *
     * Do nothing.
     * @see org.apache.maven.doxia.module.xhtml5.Xhtml5Sink#body()
     */
    @Override
    public void body() {
        // nop
    }

    /** {@inheritDoc} */
    @Override
    public void head_() {
        setHeadFlag(false);
    }

    /** {@inheritDoc} */
    @Override
    public void head() {
        setHeadFlag(true);
    }

    /** {@inheritDoc} */
    @Override
    protected void write(String text) {
        String txt = text;

        if (isHeadFlag()) {
            headWriter.write(unifyEOLs(txt));

            return;
        }

        if (docRenderingContext != null) {
            String relativePathToBasedir = docRenderingContext.getRelativePath();

            if (relativePathToBasedir == null) {
                txt = StringUtils.replace(txt, "$relativePath", ".");
            } else {
                txt = StringUtils.replace(txt, "$relativePath", relativePathToBasedir);
            }
        }

        super.write(txt);
    }

    // DocumentContent interface

    /** {@inheritDoc} */
    public String getTitle() {
        return title;
    }

    /** {@inheritDoc} */
    public List<String> getAuthors() {
        return authors;
    }

    /** {@inheritDoc} */
    public String getDate() {
        return date;
    }

    /** {@inheritDoc} */
    public String getBody() {
        String body = writer.toString();

        return body.length() > 0 ? body : null;
    }

    /** {@inheritDoc} */
    public String getHead() {
        String head = headWriter.toString();

        return head.length() > 0 ? head : null;
    }

    /** {@inheritDoc} */
    public DocumentRenderingContext getRenderingContext() {
        return docRenderingContext;
    }
}
