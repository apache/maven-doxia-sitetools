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

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;
import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;
import org.codehaus.plexus.util.PathTool;

/**
 * The sink factory to hand to a report that generates more than one page, so that each page it asks for is
 * rendered into the site the same way the report's main page is.
 * <p>
 * Every {@link #createSink(File, String)} call derives a {@link DocumentRenderingContext} from the main one and
 * records the resulting {@link MultiPageSubSink}. Once the report has run, the caller merges the main sink into
 * the site and then does the same for each sink in {@link #getSinks()}, writing it to its own
 * {@link MultiPageSubSink#getOutputDirectory() output directory} and
 * {@link MultiPageSubSink#getOutputName() output name}.
 * <p>
 * The document name of a subpage is its path relative to the report output directory, with the filename extension
 * removed, since the site renderer appends <code>.html</code> itself. A name carrying no extension is taken as-is.
 *
 * @see MultiPageSubSink
 * @since 2.2.0
 */
public class MultiPageSinkFactory implements SinkFactory {
    private static final String UNSUPPORTED_MESSAGE =
            "Only createSink(File, String) is supported by MultiPageSinkFactory.";

    /**
     * The directory the report writes its pages to, which subpage paths are relative to
     */
    private final File reportOutputDirectory;

    /**
     * The main DocumentRenderingContext, which is the base for the DocumentRenderingContext of subpages
     */
    private final DocumentRenderingContext docRenderingContext;

    /**
     * List of sinks (subpages) associated to this report
     */
    private final List<MultiPageSubSink> sinks = new ArrayList<>();

    /**
     * @param reportOutputDirectory the directory the report writes its pages to, in general
     *            <code>MavenReport.getReportOutputDirectory()</code>
     * @param docRenderingContext the rendering context of the report's main page
     */
    public MultiPageSinkFactory(File reportOutputDirectory, DocumentRenderingContext docRenderingContext) {
        this.reportOutputDirectory = reportOutputDirectory;
        this.docRenderingContext = docRenderingContext;
    }

    @Override
    public Sink createSink(File outputDirectory, String outputName) {
        // Create a new document rendering context, similar to the main one, but with a different output name
        String document = PathTool.getRelativeFilePath(
                reportOutputDirectory.getPath(), new File(outputDirectory, outputName).getPath());
        // Remove the .html suffix since we know that we are in Site Renderer context
        int extensionStart = document.lastIndexOf('.');
        if (extensionStart >= 0) {
            document = document.substring(0, extensionStart);
        }

        DocumentRenderingContext subSinkContext = new DocumentRenderingContext(
                docRenderingContext.getBasedir(), document, docRenderingContext.getGenerator());

        // Create a sink for this subpage, based on this new document rendering context
        MultiPageSubSink sink = new MultiPageSubSink(outputDirectory, outputName, subSinkContext);

        // Add it to the list of sinks associated to this report
        sinks.add(sink);

        return sink;
    }

    @Override
    public Sink createSink(File outputDir, String outputName, String encoding) {
        throw new UnsupportedOperationException(
                UNSUPPORTED_MESSAGE + " The encoding is always determined by the site rendering context.");
    }

    @Override
    public Sink createSink(OutputStream out) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE + " OutputStream based sinks are not supported.");
    }

    @Override
    public Sink createSink(OutputStream out, String encoding) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE + " OutputStream based sinks are not supported.");
    }

    /**
     * Get the subpage sinks handed out so far, in the order the report asked for them.
     *
     * @return the sinks, empty if the report turned out to produce a single page only
     */
    public List<MultiPageSubSink> getSinks() {
        return Collections.unmodifiableList(sinks);
    }
}
