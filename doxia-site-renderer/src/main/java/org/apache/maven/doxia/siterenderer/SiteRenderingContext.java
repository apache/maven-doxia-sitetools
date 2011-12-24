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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.WriterFactory;

/**
 * Context for a site rendering.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class SiteRenderingContext
{
    private String inputEncoding = ReaderFactory.UTF_8;

    private String outputEncoding = WriterFactory.UTF_8;

    private String templateName;

    private ClassLoader templateClassLoader;

    private Map<String, ?> templateProperties;

    private Locale locale = Locale.getDefault();

    private List<Locale> siteLocales = new ArrayList<Locale>();

    private DecorationModel decoration;

    private String defaultWindowTitle;

    private File skinJarFile;

    private boolean usingDefaultTemplate;

    private List<File> siteDirectories = new ArrayList<File>();

    private Map<String, String> moduleExcludes;

    private List<ModuleReference> modules = new ArrayList<ModuleReference>();

    private boolean validate;

    private Date publishDate;

    /**
     * If input documents should be validated before parsing.
     * By default no validation is performed.
     *
     * @return true if validation is switched on.
     * @since 1.1.3
     */
    public boolean isValidate()
    {
        return validate;
    }

    /**
     * Switch on/off validation.
     *
     * @param validate true to switch on validation.
     * @since 1.1.3
     */
    public void setValidate( boolean validate )
    {
        this.validate = validate;
    }

    /**
     * <p>Getter for the field <code>templateName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTemplateName()
    {
        return templateName;
    }

    /**
     * <p>Getter for the field <code>templateClassLoader</code>.</p>
     *
     * @return a {@link java.lang.ClassLoader} object.
     */
    public ClassLoader getTemplateClassLoader()
    {
        return templateClassLoader;
    }

    /**
     * <p>Setter for the field <code>templateClassLoader</code>.</p>
     *
     * @param templateClassLoader a {@link java.lang.ClassLoader} object.
     */
    public void setTemplateClassLoader( ClassLoader templateClassLoader )
    {
        this.templateClassLoader = templateClassLoader;
    }

    /**
     * <p>Getter for the field <code>templateProperties</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, ?> getTemplateProperties()
    {
        return templateProperties;
    }

    /**
     * <p>Setter for the field <code>templateProperties</code>.</p>
     *
     * @param templateProperties a {@link java.util.Map} object.
     */
    public void setTemplateProperties( Map<String, ?> templateProperties )
    {
        this.templateProperties = Collections.unmodifiableMap( templateProperties );
    }

    /**
     * <p>Getter for the field <code>locale</code>.</p>
     *
     * @return a {@link java.util.Locale} object.
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * <p>Setter for the field <code>locale</code>.</p>
     *
     * @param locale a {@link java.util.Locale} object.
     */
    public void setLocale( Locale locale )
    {
        this.locale = locale;
    }

   /**
     * <p>Getter for the field <code>siteLocales</code> -
     * a list of locales available for this site context.</p>
     *
     * @return a {@link java.util.List} object with {@link java.util.Locale} objects.
     */
    public List<Locale> getSiteLocales()
    {
        return siteLocales;
    }

   /**
    * <p>Adds passed locales to the list of site locales.</p>
    *
    * @param locales List of {@link java.util.Locale} objects to add to the site locales list.
    */
    public void addSiteLocales( List<Locale> locales )
    {
        siteLocales.addAll( locales );
    }

    /**
     * <p>Getter for the field <code>decoration</code>.</p>
     *
     * @return a {@link org.apache.maven.doxia.site.decoration.DecorationModel} object.
     */
    public DecorationModel getDecoration()
    {
        return decoration;
    }

    /**
     * <p>Setter for the field <code>decoration</code>.</p>
     *
     * @param decoration a {@link org.apache.maven.doxia.site.decoration.DecorationModel} object.
     */
    public void setDecoration( DecorationModel decoration )
    {
        this.decoration = decoration;
    }

    /**
     * <p>Setter for the field <code>defaultWindowTitle</code>.</p>
     *
     * @param defaultWindowTitle a {@link java.lang.String} object.
     */
    public void setDefaultWindowTitle( String defaultWindowTitle )
    {
        this.defaultWindowTitle = defaultWindowTitle;
    }

    /**
     * <p>Getter for the field <code>defaultWindowTitle</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultWindowTitle()
    {
        return defaultWindowTitle;
    }

    /**
     * <p>Getter for the field <code>skinJarFile</code>.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getSkinJarFile()
    {
        return skinJarFile;
    }

    /**
     * <p>Setter for the field <code>skinJarFile</code>.</p>
     *
     * @param skinJarFile a {@link java.io.File} object.
     */
    public void setSkinJarFile( File skinJarFile )
    {
        this.skinJarFile = skinJarFile;
    }

    /**
     * <p>Setter for the field <code>templateName</code>.</p>
     *
     * @param templateName a {@link java.lang.String} object.
     */
    public void setTemplateName( String templateName )
    {
        this.templateName = templateName;
    }

    /**
     * <p>Setter for the field <code>usingDefaultTemplate</code>.</p>
     *
     * @param usingDefaultTemplate a boolean.
     */
    public void setUsingDefaultTemplate( boolean usingDefaultTemplate )
    {
        this.usingDefaultTemplate = usingDefaultTemplate;
    }

    /**
     * <p>isUsingDefaultTemplate.</p>
     *
     * @return a boolean.
     */
    public boolean isUsingDefaultTemplate()
    {
        return usingDefaultTemplate;
    }

    /**
     * <p>addSiteDirectory.</p>
     *
     * @param file a {@link java.io.File} object.
     */
    public void addSiteDirectory( File file )
    {
        this.siteDirectories.add( file );
    }

    /**
     * Add a module source directory.
     *
     * @param moduleBasedir The base directory for module's source files.
     * @param moduleParserId a {@link java.lang.String} object.
     */
    public void addModuleDirectory( File moduleBasedir, String moduleParserId )
    {
        this.modules.add( new ModuleReference( moduleParserId, moduleBasedir ) );
    }

    /**
     * <p>Getter for the field <code>siteDirectories</code>.</p>
     *
     * @return List of site directories files.
     */
    public List<File> getSiteDirectories()
    {
        return siteDirectories;
    }

    /**
     * <p>Getter for the field <code>modules</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<ModuleReference> getModules()
    {
        return modules;
    }

    /**
     * <p>Getter for the field <code>moduleExcludes</code>.</p>
     *
     * @return a map defining exclude patterns (comma separated) by parser id.
     */
    public Map<String, String> getModuleExcludes()
    {
        return moduleExcludes;
    }

    /**
     * <p>Setter for the field <code>moduleExcludes</code>.</p>
     *
     * @param moduleExcludes a {@link java.util.Map} object.
     */
    public void setModuleExcludes( Map<String, String> moduleExcludes )
    {
        this.moduleExcludes = moduleExcludes;
    }

    /**
     * <p>Getter for the field <code>inputEncoding</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInputEncoding()
    {
        return inputEncoding;
    }

    /**
     * <p>Setter for the field <code>inputEncoding</code>.</p>
     *
     * @param inputEncoding a {@link java.lang.String} object.
     */
    public void setInputEncoding( String inputEncoding )
    {
        this.inputEncoding = inputEncoding;
    }

    /**
     * <p>Getter for the field <code>outputEncoding</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOutputEncoding()
    {
        return outputEncoding;
    }

    /**
     * <p>Setter for the field <code>outputEncoding</code>.</p>
     *
     * @param outputEncoding a {@link java.lang.String} object.
     */
    public void setOutputEncoding( String outputEncoding )
    {
        this.outputEncoding = outputEncoding;
    }

    /**
     * <p>If you want to specify a specific publish date instead of the current date.</p>
     * 
     * @return the publish date, can be {@code null}
     */
    public Date getPublishDate()
    {
        return publishDate;
    }

    /**
     * <p>Specify a specific publish date instead of the current date.</p>
     * 
     * @param publishDate the publish date
     */
    public void setPublishDate( Date publishDate )
    {
        this.publishDate = publishDate;
    }
    
}
