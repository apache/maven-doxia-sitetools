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
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.PathTool;
import org.codehaus.plexus.util.StringUtils;

/**
 * The rendering context of a document.
 *
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @since 1.5 (was since 1.1 in o.a.m.d.sink.render)
 */
public class RenderingContext
{
    private final File basedir;

    private final String inputName;

    private final String outputName;

    private final String parserId;

    private final String relativePath;

    private final String extension;

    private Map<String, String> attributes;

    /**
     * <p>
     * Constructor for RenderingContext when document is not rendered from a Doxia source.
     * </p>
     *
     * @param basedir the pseudo-source base directory.
     * @param document the pseudo-source document name: will be used to compute output name (same name with extension
     *            replaced with <code>.html</code>).
     */
    public RenderingContext( File basedir, String document )
    {
        this( basedir, document, null, null );
    }

    /**
     * <p>
     * Constructor for RenderingContext.
     * </p>
     *
     * @param basedir the source base directory.
     * @param document the source document name.
     * @param parserId the Doxia module parser id associated to this document, may be null if document not rendered from
     *            a Doxia source.
     * @param extension the source document filename extension.
     */
    public RenderingContext( File basedir, String document, String parserId, String extension )
    {
        this.basedir = basedir;
        this.extension = extension;
        this.inputName = document;
        this.parserId = parserId;
        this.attributes = new HashMap<String, String>();

        if ( StringUtils.isNotEmpty( extension ) )
        {
            // here we now the parserId we can play with this
            // index.xml -> index.html
            // index.xml.vm -> index.html
            // download.apt.vm --> download.html
            if ( DefaultSiteRenderer.endsWithIgnoreCase( document, ".vm" ) )
            {
                document = document.substring( 0, document.length() - 3 );
            }
            String fileNameWithoutExt = document.substring( 0, document.length() - extension.length() - 1 );
            this.outputName = fileNameWithoutExt + ".html";
        }
        else
        {
            this.outputName = document.substring( 0, document.lastIndexOf( '.' ) ).replace( '\\', '/' ) + ".html";
        }
        this.relativePath = PathTool.getRelativePath( basedir.getPath(), new File( basedir, inputName ).getPath() );
    }

    /**
     * <p>Getter for the field <code>basedir</code>.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getBasedir()
    {
        return basedir;
    }

    /**
     * <p>Getter for the field <code>inputName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInputName()
    {
        return inputName;
    }

    /**
     * <p>Getter for the field <code>outputName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOutputName()
    {
        return outputName;
    }

    /**
     * <p>Getter for the field <code>parserId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParserId()
    {
        return parserId;
    }

    /**
     * <p>Getter for the field <code>relativePath</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRelativePath()
    {
        return relativePath;
    }

    /**
     * <p>setAttribute.</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setAttribute( String key, String value )
    {
        attributes.put( key, value );
    }

    /**
     * <p>getAttribute.</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getAttribute( String key )
    {
        return attributes.get( key );
    }

    /**
     * <p>Getter for the field <code>extension</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getExtension()
    {
        return extension;
    }
}
