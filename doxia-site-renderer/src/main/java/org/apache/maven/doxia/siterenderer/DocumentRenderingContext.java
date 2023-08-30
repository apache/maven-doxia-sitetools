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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.PathTool;

/**
 * The rendering context of a document.
 * If not rendered from a Doxia markup source, parserId and extension will be null.
 *
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @since 1.5 (was since 1.1 in o.a.m.d.sink.render)
 */
public class DocumentRenderingContext {
    private final File basedir;

    private final String basedirRelativePath;

    private final String inputName;

    private final String outputName;

    private final String parserId;

    private final String relativePath;

    private final String extension;

    private Map<String, String> attributes;

    private final boolean editable;

    private final String generator;

    /**
     * <p>
     * Constructor for rendering context when document is not rendered from a Doxia markup source.
     * </p>
     *
     * @param basedir the pseudo-source base directory.
     * @param document the pseudo-source document name: will be used to compute output name (same name with extension
     *            replaced with <code>.html</code>).
     * @param generator the generator (in general a reporting goal: <code>groupId:artifactId:version:goal</code>)
     * @since 1.8
     */
    public DocumentRenderingContext(File basedir, String document, String generator) {
        this(basedir, null, document, null, null, false, generator);
    }

    public DocumentRenderingContext(
            File basedir,
            String basedirRelativePath,
            String document,
            String parserId,
            String extension,
            boolean editable) {
        this(basedir, basedirRelativePath, document, parserId, extension, editable, null);
    }

    /**
     * <p>
     * Constructor for document rendering context.
     * </p>
     *
     * @param basedir the source base directory (not null, pseudo value when not a Doxia source).
     * @param basedirRelativePath the relative path from root (null if not Doxia source)
     * @param document the source document name.
     * @param parserId the Doxia module parser id associated to this document, may be null if document not rendered from
     *            a Doxia source.
     * @param extension the source document filename extension, may be null if document not rendered from
     *            a Doxia source.
     * @param editable is the document editable as source, i.e. not generated?
     * @param generator the generator (in general a reporting goal: <code>groupId:artifactId:version:goal</code>)
     * @since 1.8
     */
    public DocumentRenderingContext(
            File basedir,
            String basedirRelativePath,
            String document,
            String parserId,
            String extension,
            boolean editable,
            String generator) {
        this.basedir = basedir;
        this.parserId = parserId;
        this.extension = extension;
        this.generator = generator;
        this.attributes = new HashMap<>();

        document = document.replace('\\', '/');
        this.inputName = document;

        if (extension != null && !extension.isEmpty()) {
            this.basedirRelativePath = basedirRelativePath.replace('\\', '/');
            // document comes from a Doxia source: see DoxiaDocumentRenderer
            this.editable = editable;

            // here we know the parserId and extension, we can play with this to get output name from document:
            // - index.xml -> index.html
            // - index.xml.vm -> index.html
            // - download.apt.vm --> download.html
            if (DefaultSiteRenderer.endsWithIgnoreCase(document, ".vm")) {
                document = document.substring(0, document.length() - 3);
            }
            String fileNameWithoutExt = document.substring(0, document.length() - extension.length() - 1);
            this.outputName = fileNameWithoutExt + ".html";
        } else {
            // document does not come from a Doxia source but direct Sink API, so no file extension to strip
            this.basedirRelativePath = null;
            this.editable = false;
            this.outputName = document + ".html";
        }

        this.relativePath = PathTool.getRelativePath(basedir.getPath(), new File(basedir, inputName).getPath())
                .replace('\\', '/');
    }

    /**
     * <p>Getter for the field <code>basedir</code>.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getBasedir() {
        return basedir;
    }

    /**
     * <p>Getter for the field <code>inputName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInputName() {
        return inputName;
    }

    /**
     * Get html output name, relative to site root.
     *
     * @return html output name
     * @see PathTool#getRelativePath(String)
     */
    public String getOutputName() {
        return outputName;
    }

    /**
     * Get the parserId when document comes from a Doxia source.
     *
     * @return parser id, or <code>null</code> if not froma DOxia source.
     */
    public String getParserId() {
        return parserId;
    }

    /**
     * Get the relative path to site root.
     *
     * @return the relative path to site root
     */
    public String getRelativePath() {
        return relativePath;
    }

    /**
     * <p>setAttribute.</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    /**
     * <p>getAttribute.</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Get the source document filename extension (when a Doxia source)
     *
     * @return the source document filename extension when a Doxia source, or <code>null</code> if not a Doxia source
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Is the source document editable?
     *
     * @return <code>true</code> if comes from an editable Doxia source (not generated one).
     * @since 1.8
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Is the document rendered from a Doxia source?
     *
     * @return <code>true</code> if comes from a Doxia source.
     * @since 1.8
     */
    public boolean isDoxiaSource() {
        return extension != null && !extension.isEmpty();
    }

    /**
     * What is the generator (if any)?
     *
     * @return <code>null</code> if no known generator
     * @since 1.8
     */
    public String getGenerator() {
        return generator;
    }

    /**
     * Get the relative path of basedir (when a Doxia source)
     *
     * @return the relative path of basedir when a Doxia source, or <code>null</code> if not a Doxia source
     * @since 1.8
     */
    public String getBasedirRelativePath() {
        return basedirRelativePath;
    }

    /**
     * Get the relative path to Doxia source from build root.
     *
     * @return the relative path to Doxia source from build root, or <code>null</code> if not a Doxia source
     * @since 1.8
     */
    public String getDoxiaSourcePath() {
        return isDoxiaSource() ? (basedirRelativePath + '/' + inputName) : null;
    }

    /**
     * Get url of the Doxia source calculate from given base url.
     *
     * @param base the base url to use
     * @return the resulting url
     * @since 1.8
     */
    public String getDoxiaSourcePath(String base) {
        return PathTool.calculateLink(getDoxiaSourcePath(), base);
    }
}
