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

import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;

/**
 * A sink for one subpage of a multipage report, remembering where the subpage is meant to be written to.
 * Instances are handed out by {@link MultiPageSinkFactory}, which is also the only thing that creates them.
 *
 * @see MultiPageSinkFactory
 * @since 2.2.0
 */
public class MultiPageSubSink extends SiteRendererSink {
    private final File outputDirectory;

    private final String outputName;

    MultiPageSubSink(File outputDirectory, String outputName, DocumentRenderingContext docRenderingContext) {
        super(docRenderingContext);
        this.outputDirectory = outputDirectory;
        this.outputName = outputName;
    }

    /**
     * Get the file name the subpage is to be written to, relative to {@link #getOutputDirectory()}.
     *
     * @return the file name, as the report passed it to {@link MultiPageSinkFactory#createSink(File, String)}
     */
    public String getOutputName() {
        return outputName;
    }

    /**
     * Get the directory the subpage is to be written to. It need not exist yet.
     *
     * @return the directory, as the report passed it to {@link MultiPageSinkFactory#createSink(File, String)}
     */
    public File getOutputDirectory() {
        return outputDirectory;
    }
}
