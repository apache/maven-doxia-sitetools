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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.doxia.site.SiteModel;
import org.apache.maven.doxia.site.skin.SkinModel;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.WriterFactory;

/**
 * Context for a site rendering.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class SiteRenderingContext {
    private String inputEncoding = ReaderFactory.FILE_ENCODING;

    private String outputEncoding = WriterFactory.UTF_8;

    private String templateName;

    private ClassLoader templateClassLoader;

    private Map<String, ?> templateProperties;

    private Locale locale = Locale.ROOT;

    private List<Locale> siteLocales = new ArrayList<>();

    private SiteModel siteModel;

    private String defaultTitle;

    private Artifact skin;

    private SkinModel skinModel;

    private File rootDirectory;

    private List<File> siteDirectories = new ArrayList<>();

    private Map<String, String> moduleExcludes;

    private boolean validate;

    private Date publishDate;

    private File processedContentOutput;

    private ParserConfigurator parserConfigurator;

    /**
     * If input documents should be validated before parsing.
     * By default no validation is performed.
     *
     * @return true if validation is switched on.
     * @since 1.1.3
     */
    public boolean isValidate() {
        return validate;
    }

    /**
     * Switch on/off validation.
     *
     * @param validate true to switch on validation.
     * @since 1.1.3
     */
    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    /**
     * <p>Getter for the field <code>templateName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * <p>Getter for the field <code>templateClassLoader</code>.</p>
     *
     * @return a {@link java.lang.ClassLoader} object.
     */
    public ClassLoader getTemplateClassLoader() {
        return templateClassLoader;
    }

    /**
     * <p>Setter for the field <code>templateClassLoader</code>.</p>
     *
     * @param templateClassLoader a {@link java.lang.ClassLoader} object.
     */
    public void setTemplateClassLoader(ClassLoader templateClassLoader) {
        this.templateClassLoader = templateClassLoader;
    }

    /**
     * <p>Getter for the field <code>templateProperties</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, ?> getTemplateProperties() {
        return templateProperties;
    }

    /**
     * <p>Setter for the field <code>templateProperties</code>.</p>
     *
     * @param templateProperties a {@link java.util.Map} object.
     */
    public void setTemplateProperties(Map<String, ?> templateProperties) {
        this.templateProperties = Collections.unmodifiableMap(templateProperties);
    }

    /**
     * <p>Getter for the field <code>locale</code>.</p>
     *
     * @return a {@link java.util.Locale} object.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * <p>Setter for the field <code>locale</code>.</p>
     *
     * @param locale a {@link java.util.Locale} object.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * <p>Getter for the field <code>siteLocales</code> -
     * a list of locales available for this site context.</p>
     *
     * @return a {@link java.util.List} object with {@link java.util.Locale} objects.
     */
    public List<Locale> getSiteLocales() {
        return siteLocales;
    }

    /**
     * <p>Adds passed locales to the list of site locales.</p>
     *
     * @param locales List of {@link java.util.Locale} objects to add to the site locales list.
     */
    public void addSiteLocales(List<Locale> locales) {
        siteLocales.addAll(locales);
    }

    /**
     * <p>Getter for the field <code>siteModel</code>.</p>
     *
     * @return a {@link org.apache.maven.doxia.site.SiteModel} object.
     */
    public SiteModel getSiteModel() {
        return siteModel;
    }

    /**
     * <p>Setter for the field <code>siteModel</code>.</p>
     *
     * @param siteModel a {@link org.apache.maven.doxia.site.SiteModel} object.
     */
    public void setSiteModel(SiteModel siteModel) {
        this.siteModel = siteModel;
    }

    /**
     * <p>Setter for the field <code>defaultTitle</code>.</p>
     *
     * @param defaultTitle a {@link java.lang.String} object.
     */
    public void setDefaultTitle(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    /**
     * <p>Getter for the field <code>defaultTitle</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultTitle() {
        return defaultTitle;
    }

    /**
     * <p>Getter for the field <code>skin</code>.</p>
     *
     * @return a {@link Artifact} object.
     */
    public Artifact getSkin() {
        return skin;
    }

    /**
     * <p>Setter for the field <code>skinJarFile</code>.</p>
     *
     * @param skin an {@link Artifact} object.
     */
    public void setSkin(Artifact skin) {
        this.skin = skin;
    }

    /**
     * <p>Getter for the field <code>skinModel</code>.</p>
     *
     * @return a {@link SkinModel} object.
     */
    public SkinModel getSkinModel() {
        return skinModel;
    }

    /**
     * <p>Setter for the field <code>skinModel</code>.</p>
     *
     * @param skinModel a {@link SkinModel} object.
     */
    public void setSkinModel(SkinModel skinModel) {
        this.skinModel = skinModel;
    }

    /**
     * <p>Setter for the field <code>templateName</code>.</p>
     *
     * @param templateName a {@link java.lang.String} object.
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Add a site directory, expected to have a Doxia Site layout, ie one directory per Doxia parser module containing
     * files with parser extension. Typical values are <code>src/site</code> or <code>target/generated-site</code>.
     *
     * @param siteDirectory a {@link java.io.File} object.
     */
    public void addSiteDirectory(File siteDirectory) {
        this.siteDirectories.add(siteDirectory);
    }

    /**
     * <p>Getter for the field <code>siteDirectories</code>.</p>
     *
     * @return List of site directories files.
     */
    public List<File> getSiteDirectories() {
        return siteDirectories;
    }

    /**
     * <p>Getter for the field <code>moduleExcludes</code>.</p>
     *
     * @return a map defining exclude patterns (comma separated) by parser id.
     */
    public Map<String, String> getModuleExcludes() {
        return moduleExcludes;
    }

    /**
     * <p>Setter for the field <code>moduleExcludes</code>.</p>
     *
     * @param moduleExcludes a {@link java.util.Map} object.
     */
    public void setModuleExcludes(Map<String, String> moduleExcludes) {
        this.moduleExcludes = moduleExcludes;
    }

    /**
     * <p>Getter for the field <code>inputEncoding</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInputEncoding() {
        return inputEncoding;
    }

    /**
     * <p>Setter for the field <code>inputEncoding</code>.</p>
     *
     * @param inputEncoding a {@link java.lang.String} object.
     */
    public void setInputEncoding(String inputEncoding) {
        this.inputEncoding = inputEncoding;
    }

    /**
     * <p>Getter for the field <code>outputEncoding</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOutputEncoding() {
        return outputEncoding;
    }

    /**
     * <p>Setter for the field <code>outputEncoding</code>.</p>
     *
     * @param outputEncoding a {@link java.lang.String} object.
     */
    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    /**
     * <p>If you want to specify a specific publish date instead of the current date.</p>
     *
     * @return the publish date, can be {@code null}
     */
    public Date getPublishDate() {
        return publishDate;
    }

    /**
     * <p>Specify a specific publish date instead of the current date.</p>
     *
     * @param publishDate the publish date
     */
    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    /**
     * Directory where to save content after Velocity processing (<code>*.vm</code>), but before parsing it with Doxia.
     *
     * @return not null if the documents are to be saved
     * @since 1.7
     */
    public File getProcessedContentOutput() {
        return processedContentOutput;
    }

    /**
     * Where to (eventually) save content after Velocity processing (<code>*.vm</code>), but before parsing it with
     * Doxia?
     *
     * @param processedContentOutput not null if the documents are to be saved
     * @since 1.7
     */
    public void setProcessedContentOutput(File processedContentOutput) {
        this.processedContentOutput = processedContentOutput;
    }

    /**
     * Root directory, to calculate relative path to every site directories.
     * Corresponds to the <code>pom.xml</code> directory for Maven build.
     *
     * @return the root directory
     * @since 1.8
     */
    public File getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Set the root directory.
     *
     * @param rootDirectory the root directory
     * @since 1.8
     */
    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    /**
     * Return the configurator for {@link Parser}s.
     * @return the parser configurator (may be {@code null} in which case the default configuration is applied)
     * @since 4.0
     */
    public ParserConfigurator getParserConfigurator() {
        return parserConfigurator;
    }

    /**
     * Set the configurator to use for {@link Parser}s.
     * @param parserConfigurator the configurator
     * @since 4.0
     */
    public void setParserConfigurator(ParserConfigurator parserConfigurator) {
        this.parserConfigurator = parserConfigurator;
    }
}
