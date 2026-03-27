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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.codehaus.plexus.util.PathTool;

/**
 * The rendering context of a document.
 * If not rendered from a Doxia markup source, parserId and extension will be null.
 *
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @since 1.5 (was since 1.1 in o.a.m.d.sink.render)
 */
public class DocumentRenderingContext {

    /** absolute path to the source base directory (not null, pseudo value when not a Doxia source), this is parser format specific. Must start with {@link #siteRootDirectory}. */
    private final File basedir;

    /** {@link #basedir} relative path to the document's source including {@link #extension}. May be {@code null} if not rendered from a Doxia source */
    private final String inputPath;

    /** same as {@link #inputPath} but with extension replaced with {@code .html}, this is parser format specific */
    private final String outputPath;

    /** the Doxia module parser id associated to this document, may be null if document not rendered from a Doxia source. */
    private final String parserId;

    /** the source document filename extension, may be null if document not rendered from a Doxia source. */
    private final String extension;

    private Map<String, String> attributes;

    /**
     * The absolute paths of directories which may contain the original editable source.
     * If empty document is not editable.
     */
    private final Collection<File> sourceDirectories;

    /** The project's build directory, may be {@code null} rendered from a Doxia source) */
    private final File rootDirectory;

    /** The site's root directory, must be below {@link #rootDirectory}, may be {@code null} if not rendered from a Doxia source */
    private final File siteRootDirectory;

    /** optional descriptive text of the plugin which generated the output (usually Maven coordinates). Only set when document is not based on a Doxia source. */
    private final String generator;

    static File stripSuffixFromPath(File file, String suffix) {
        File relevantFile = file;
        if (suffix == null || suffix.isEmpty()) {
            return relevantFile;
        }
        File currentSuffixPart = new File(suffix);
        // compare elements from end, suffix should be a suffix of file
        do {
            if (currentSuffixPart.getName().equals(relevantFile.getName())) {
                relevantFile = relevantFile.getParentFile();
                if (relevantFile == null) {
                    throw new IllegalArgumentException("Suffix " + suffix + " has more elements than file " + file);
                }
            } else {
                throw new IllegalArgumentException("Suffix " + suffix + " is not a suffix of file " + file);
            }
        } while ((currentSuffixPart = currentSuffixPart.getParentFile()) != null);
        return relevantFile;
    }

    /**
     * <p>
     * Constructor for rendering context when document is not rendered from a Doxia markup source.
     * </p>
     *
     * @param basedir the pseudo-source base directory.
     * @param document the pseudo-source document path: will be used to compute output path (same path with extension
     *            replaced with <code>.html</code>).
     * @param generator the generator (in general a reporting goal: <code>groupId:artifactId:version:goal</code>)
     * @since 1.8
     */
    public DocumentRenderingContext(File basedir, String document, String generator) {
        this(basedir, document, null, null, null, null, Collections.emptySet(), generator);
    }

    /**
     *
     * @param basedir
     * @param basedirRelativePath
     * @param document
     * @param parserId
     * @param extension
     * @param editable
     * @deprecated Use {@link #DocumentRenderingContext(File, String, String, String, File, File, Collection, String)} instead.
     */
    @Deprecated
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
     * Constructor for rendering context when document is a Doxia markup source.
     * </p>
     * Same as {@link #DocumentRenderingContext(File, String, String, String, File, File, Collection, String)} with {@code generator} set to {@code null}.
     *
     * @param basedir the source base directory (not null, pseudo value when not a Doxia source).
     * @param document the source document path.
     * @param parserId the Doxia module parser id associated with this document, may be null if document not rendered from
     *            a Doxia source.
     * @param extension the source document filename extension, may be null if document not rendered from
     *            a Doxia source.
     * @param rootDirectory the absolute project's root directory (not null), usually {@code project.basedir}, must be an ancestor of {@code siteRootDirectory}
     * @param siteRootDirectory the absolute site's root directory (not null), must start with {@code rootDirectory}, often ends with {@code /src/site}
     * @param sourceDirectories the absolute paths of directories which may contain the original editable source.
     * @since 2.1
     */
    public DocumentRenderingContext(
            File basedir,
            String document,
            String parserId,
            String extension,
            File rootDirectory,
            File siteRootDirectory,
            Collection<File> sourceDirectories) {
        this(basedir, document, parserId, extension, rootDirectory, siteRootDirectory, sourceDirectories, null);
    }

    /**
     * <p>
     * Constructor for document rendering context.
     * </p>
     *
     * @param basedir the absolute source base directory (not null, pseudo value when not a Doxia source).
     * @param basedirRelativePath the relative path of {@code #basedir} from project root (null if not Doxia source)
     * @param document the source document path.
     * @param parserId the Doxia module parser id associated with this document, may be null if document not rendered from
     *            a Doxia source.
     * @param extension the source document filename extension, may be null if document not rendered from
     *            a Doxia source.
     * @param editable {@code true} if the document is editable as source, i.e. not generated, {@code false} otherwise.
     * @param generator the generator of this document (in general a reporting goal: <code>groupId:artifactId:version:goal</code>), not set when document is based on a Doxia source.
     * @since 1.8
     * @deprecated Use {@link #DocumentRenderingContext(File, String, String, String, File, File, Collection, String)} instead.
     */
    @Deprecated
    public DocumentRenderingContext(
            File basedir,
            String basedirRelativePath,
            String document,
            String parserId,
            String extension,
            boolean editable,
            String generator) {
        this(
                basedir,
                document,
                parserId,
                extension,
                stripSuffixFromPath(basedir, basedirRelativePath),
                // assume that site root is the parent of basedir (i.e. module specific source directory is directly
                // below site root)
                basedir.getParentFile(),
                editable ? Collections.singleton(basedir) : Collections.emptySet(),
                generator);
    }

    /**
     * <p>
     * Constructor for document rendering context.
     * </p>
     *
     * @param basedir the source base directory (not null, pseudo value when not a Doxia source).
     * @param document the source document path.
     * @param parserId the Doxia module parser id associated with this document, may be null if document not rendered from
     *            a Doxia source.
     * @param extension the source document filename extension, may be null if document not rendered from
     *            a Doxia source.
     * @param rootDirectory the absolute project's root directory (not null), usually {@code project.basedir}, must be an ancestor of {@code siteRootDirectory}
     * @param siteRootDirectory the absolute site's root directory (not null), must start with {@code rootDirectory}, often ends with {@code /src/site}
     * @param sourceDirectories the absolute paths of directories which may contain the original editable source.
     * @param generator the generator (in general a reporting goal: <code>groupId:artifactId:version:goal</code>)
     * @since 2.1
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public DocumentRenderingContext(
            File basedir,
            String document,
            String parserId,
            String extension,
            File rootDirectory,
            File siteRootDirectory,
            Collection<File> sourceDirectories,
            String generator) {
        this.basedir = basedir;
        this.parserId = parserId;
        this.extension = extension;
        this.generator = generator;
        this.attributes = new HashMap<>();

        document = document.replace('\\', '/');
        this.inputPath = document;

        if (rootDirectory == null) {
            if (siteRootDirectory != null) {
                throw new IllegalArgumentException("Root directory must not be null when site root directory is set");
            }
        } else {
            Objects.requireNonNull(
                    siteRootDirectory, "Site root directory must not be null when root directory is set");
            if (!siteRootDirectory.getPath().startsWith(rootDirectory.getPath())) {
                throw new IllegalArgumentException("Site root directory " + siteRootDirectory
                        + " must start with root directory " + rootDirectory);
            }
        }
        this.rootDirectory = rootDirectory;
        this.siteRootDirectory = siteRootDirectory;
        if (extension != null && !extension.isEmpty()) {
            // document comes from a Doxia source: see DoxiaDocumentRenderer
            this.sourceDirectories = sourceDirectories;

            // here we know the parserId and extension, we can play with this to get output name from document:
            // - index.xml -> index.html
            // - index.xml.vm -> index.html
            // - download.apt.vm --> download.html
            if (DefaultSiteRenderer.endsWithIgnoreCase(document, ".vm")) {
                document = document.substring(0, document.length() - 3);
            }
            String filePathWithoutExt = document.substring(0, document.length() - extension.length() - 1);
            this.outputPath = filePathWithoutExt + ".html";
        } else {
            // document does not come from a Doxia source but direct Sink API, so no file extension to strip
            this.sourceDirectories = Collections.emptySet();
            this.outputPath = document + ".html";
        }
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
     * <p>Getter for the field <code>inputPath</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInputPath() {
        return inputPath;
    }

    /**
     * @deprecated Method name does not properly reflect its purpose. Use {@link #getInputPath()} instead.
     */
    @Deprecated
    public String getInputName() {
        return getInputPath();
    }

    /**
     * Get html output path, relative to site root.
     *
     * @return html output path
     * @see PathTool#getRelativePath(String)
     */
    public String getOutputPath() {
        return outputPath;
    }

    /**
     * @deprecated Method name does not properly reflect its purpose. Use {@link #getOutputPath()} instead.
     */
    @Deprecated
    public String getOutputName() {
        return getOutputPath();
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
     * Get the relative path of the parent directory of this document to site root.
     *
     * @return the relative path to site root
     */
    public String getRelativePath() {
        return PathTool.getRelativePath(basedir.getPath(), new File(basedir, inputPath).getPath())
                .replace('\\', '/');
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
        return getDoxiaSourcePath() != null;
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
     * Get the project root relative path of basedir (when a Doxia source). For example {@code src/site/markdown}.
     *
     * @return the relative path of basedir when a Doxia source, or <code>null</code> if not a Doxia source
     * @since 1.8
     */
    public String getBasedirRelativePath() {
        if (!isDoxiaSource()) {
            return null;
        }
        return PathTool.getRelativeFilePath(rootDirectory.getPath(), basedir.getPath());
    }

    /**
     * Get the site root relative path of basedir (when a Doxia source). For example {@code markdown}.
     *
     * @return the relative path of basedir when a Doxia source, or <code>null</code> if not a Doxia source
     */
    private String getBasedirRelativePathAgainstSiteRoot() {
        if (!isDoxiaSource()) {
            return null;
        }
        return PathTool.getRelativeFilePath(siteRootDirectory.getPath(), basedir.getPath());
    }

    /**
     * Get the relative path to Doxia source from build root. The file separators in the returned path are {@code /} regardless of the platform..
     *
     * @return the relative path to Doxia source from build root, or <code>null</code> if not a Doxia source
     * @since 1.8
     */
    public String getDoxiaSourcePath() {
        if (!isDoxiaSource()) {
            return null;
        } else {
            for (File sourceDirectory : sourceDirectories) {
                File sourceFile = new File(sourceDirectory, getBasedirRelativePathAgainstSiteRoot() + '/' + inputPath);
                if (sourceFile.exists()) {
                    return PathTool.getRelativeFilePath(rootDirectory.getPath(), sourceFile.getPath())
                            .replace('\\', '/');
                }
            }
        }
        return null;
    }

    /**
     * Get absolute url of the Doxia source calculate from given base url.
     * Used from Skins to render an edit button.
     *
     * @param base the base url to use
     * @return the resulting url
     * @since 1.8
     */
    public String getDoxiaSourcePath(String base) {
        return PathTool.calculateLink(getDoxiaSourcePath(), base);
    }
}
