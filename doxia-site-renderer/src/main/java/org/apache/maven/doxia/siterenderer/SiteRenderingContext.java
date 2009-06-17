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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.doxia.site.decoration.DecorationModel;

/**
 * <p>SiteRenderingContext class.</p>
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class SiteRenderingContext
{
    private static final String DEFAULT_INPUT_ENCODING = "UTF-8";

    private static final String DEFAULT_OUTPUT_ENCODING = "UTF-8";

    private String inputEncoding = DEFAULT_INPUT_ENCODING;

    private String outputEncoding = DEFAULT_OUTPUT_ENCODING;

    private String templateName;

    private ClassLoader templateClassLoader;

    private Map templateProperties;

    private Locale locale = Locale.getDefault();

    private DecorationModel decoration;

    private String defaultWindowTitle;

    private File skinJarFile;

    private boolean usingDefaultTemplate;

    private List siteDirectories = new ArrayList();

    private Map moduleExcludes;

    private List modules = new ArrayList();

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
    public Map getTemplateProperties()
    {
        return templateProperties;
    }

    /**
     * <p>Setter for the field <code>templateProperties</code>.</p>
     *
     * @param templateProperties a {@link java.util.Map} object.
     */
    public void setTemplateProperties( Map templateProperties )
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
     * <p>addModuleDirectory.</p>
     *
     * @param file a {@link java.io.File} object.
     * @param moduleParserId a {@link java.lang.String} object.
     */
    public void addModuleDirectory( File file, String moduleParserId )
    {
        this.modules.add( new ModuleReference( moduleParserId, file ) );
    }

    /**
     * <p>Getter for the field <code>siteDirectories</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List getSiteDirectories()
    {
        return siteDirectories;
    }

    /**
     * <p>Getter for the field <code>modules</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List getModules()
    {
        return modules;
    }

    /**
     * <p>Getter for the field <code>moduleExcludes</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map getModuleExcludes()
    {
        return moduleExcludes;
    }

    /**
     * <p>Setter for the field <code>moduleExcludes</code>.</p>
     *
     * @param moduleExcludes a {@link java.util.Map} object.
     */
    public void setModuleExcludes( Map moduleExcludes )
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
}
