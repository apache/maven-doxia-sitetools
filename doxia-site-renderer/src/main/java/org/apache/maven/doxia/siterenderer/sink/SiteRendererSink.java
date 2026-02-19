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
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet;
import org.apache.maven.doxia.sink.impl.SinkUtils;
import org.apache.maven.doxia.site.MermaidConfiguration;
import org.apache.maven.doxia.siterenderer.DefaultSiteRenderer;
import org.apache.maven.doxia.siterenderer.DocumentContent;
import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;

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

    private List<String> authors = new ArrayList<>();

    private final StringWriter headWriter;

    /** Buffer inside verbatim elements to potentially remove enclosed code elements for Mermaid diagrams */
    private StringBuilder verbatimBuffer;

    private final Writer writer;

    private final MermaidConfiguration mermaidConfig;

    private DocumentRenderingContext docRenderingContext;

    private boolean containsMermaidDiagram = false;

    private boolean insideMermaidCodeElement = false;
    /**
     * Construct a new SiteRendererSink for a document.
     *
     * @param docRenderingContext the document's rendering context.
     */
    public SiteRendererSink(DocumentRenderingContext docRenderingContext) {
        this(docRenderingContext, null);
    }

    public SiteRendererSink(DocumentRenderingContext docRenderingContext, MermaidConfiguration mermaid) {
        this(new StringWriter(), docRenderingContext, mermaid);
    }

    private SiteRendererSink(
            StringWriter writer, DocumentRenderingContext docRenderingContext, MermaidConfiguration mermaid) {
        super(writer);

        this.writer = writer;
        this.headWriter = new StringWriter();
        this.docRenderingContext = docRenderingContext;
        this.mermaidConfig = mermaid;

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
     * Reset text buffer, since text content before title must not be in title.
     * @see org.apache.maven.doxia.module.xhtml5.Xhtml5Sink#title()
     */
    @Override
    public void title(SinkEventAttributes attributes) {
        resetTextBuffer();
    }

    /** {@inheritDoc} */
    @Override
    public void author(SinkEventAttributes attributes) {
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
    public void date(SinkEventAttributes attributes) {
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

    @Override
    public void verbatim(SinkEventAttributes attributes) {
        if (mermaidConfig != null && normalizeClassAttributesForMermaid(attributes)) {
            containsMermaidDiagram = true;
            // remove the decoration code for Mermaid diagrams (otherwise Skins may add line numbers to the code
            // element, which breaks Mermaid rendering)
            SinkEventAttributes filteredAttributes = (SinkEventAttributes)
                    SinkUtils.filterAttributes(attributes, new String[] {SinkEventAttributes.DECORATION});
            super.verbatim(filteredAttributes);
        } else {
            // write subsequent verbatim content to a buffer, to be able to detect Mermaid diagrams in it and remove
            // code element if needed
            verbatimBuffer = new StringBuilder();
            super.verbatim(attributes);
        }
    }

    @Override
    public void verbatim_() {
        flushVerbatimBuffer(false);
        super.verbatim_();
    }

    @Override
    public void inline(SinkEventAttributes attributes) {
        if (attributes.containsAttributes(SinkEventAttributeSet.Semantics.CODE)
                && mermaidConfig != null
                && normalizeClassAttributesForMermaid(attributes)) {
            containsMermaidDiagram = true;
            // writes to buffer
            super.inline(attributes);
            // remove code element from inline stack to prevent closing it
            inlineStack.pop();
            insideMermaidCodeElement = true;
            // remove the code element from the verbatim buffer, to prevent Skins from adding line numbers to it, which
            // breaks Mermaid rendering
            flushVerbatimBuffer(true);
        } else {
            flushVerbatimBuffer(false);
            super.inline(attributes);
        }
    }

    @Override
    public void inline_() {
        if (insideMermaidCodeElement) {
            // this is the end of the code tag (which has been removed), so no need to close it
            insideMermaidCodeElement = false;
        } else {
            super.inline_();
        }
    }

    private void flushVerbatimBuffer(boolean stripCodeElement) {
        if (verbatimBuffer != null) {
            String buffer = verbatimBuffer.toString();
            if (stripCodeElement) {
                // remove code element and instead add attributes to the parent element <pre> to prevent the skin from
                // adding code highlighting/line numbers, which breaks Mermaid rendering
                buffer = buffer.replaceFirst("<pre><code([^>]*)>", "<pre$1>");
            }
            verbatimBuffer = null;
            write(buffer);
        }
    }

    /**
     * Normalize class attributes for Mermaid diagrams, to allow using either "mermaid" or "language-mermaid" as class.
     *
     * @param attributes the attributes to check and normalize.
     * @return {@code true} if the attributes indicate a Mermaid diagram, {@code false} otherwise.
     */
    boolean normalizeClassAttributesForMermaid(SinkEventAttributes attributes) {
        String lang = attributes != null ? (String) attributes.getAttribute(SinkEventAttributes.CLASS) : null;
        if ("language-mermaid"
                .equals(lang)) { // "language-" prefix is used by some markdown parsers, e.g. flexmark-java
            attributes.addAttribute(SinkEventAttributes.CLASS, "mermaid");
            return true;
        } else if ("mermaid".equals(lang)) {
            return true;
        }
        return false;
    }

    /**
     * Include the Mermaid rendering script (either internal or external) and call it on diagrams afterwards.
     * @see <a href="https://mermaid.ai/open-source/intro/getting-started.html#_4-calling-the-mermaid-javascript-api">Calling the Mermaid JavaScript API</a>
     */
    private void writeMermaidScript() {
        if (mermaidConfig.getExternalJs() != null) {
            write(mermaidConfig.getExternalJs().asScriptTag());
        } else {
            write("\n<script src=\"");
            write(docRenderingContext.getRelativePath());

            if (mermaidConfig.isUseTiny()) {
                // use integrated tiny version of mermaid, which is smaller and faster to load, but has some limitations
                // (e.g. no sequence diagrams)
                write("/js/mermaid-" + DefaultSiteRenderer.MERMAID_VERSION + ".tiny.min.js");
            } else {
                // use integrated full version of mermaid, which is larger and slower to load, but has all features
                write("/js/mermaid-" + DefaultSiteRenderer.MERMAID_VERSION + ".min.js");
            }
            write("\"></script>\n");
        }
        write("\n<script>\n");
        if (mermaidConfig.getConfig() != null) {
            write("mermaid.initialize(" + mermaidConfig.getConfig() + ");\n");
        } else {
            // By default, mermaid.run will be called when the document is ready, rendering all elements with
            // class="mermaid".
            write("mermaid.initialize({startOnLoad:true, securityLevel: 'loose'});\n");
        }
        write("</script>\n");
    }

    /**
     * {@inheritDoc}
     *
     * Do nothing.
     * @see org.apache.maven.doxia.module.xhtml5.Xhtml5Sink#body_()
     */
    @Override
    public void body_() {
        if (containsMermaidDiagram && mermaidConfig != null) {
            writeMermaidScript();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Do nothing.
     * @see org.apache.maven.doxia.module.xhtml5.Xhtml5Sink#body()
     */
    @Override
    public void body(SinkEventAttributes attributes) {
        // nop
    }

    /** {@inheritDoc} */
    @Override
    public void head_() {
        setHeadFlag(false);
    }

    /** {@inheritDoc} */
    @Override
    public void head(SinkEventAttributes attributes) {
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
                txt = txt == null || txt.isEmpty() ? txt : txt.replace("$relativePath", ".");
            } else {
                txt = txt == null || txt.isEmpty() || relativePathToBasedir == null
                        ? txt
                        : txt.replace("$relativePath", relativePathToBasedir);
            }
        }

        if (verbatimBuffer != null) {
            verbatimBuffer.append(unifyEOLs(txt));
            return;
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
