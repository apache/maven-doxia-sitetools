package org.apache.maven.doxia.siterenderer.sink;

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

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.HTML.Attribute;

import org.apache.maven.doxia.module.xhtml.XhtmlSink;
import org.apache.maven.doxia.sink.render.RenderingContext;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.util.HtmlTools;

import org.codehaus.plexus.util.StringUtils;

/**
 * Sink for site renderering.
 *
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SiteRendererSink
    extends XhtmlSink
    implements Sink, org.codehaus.doxia.sink.Sink
{
    private String date = "";

    private String title = "";

    private List authors = new ArrayList();

    private final StringWriter headWriter;

    private StringBuffer sectionTitleBuffer;

    private boolean sectionHasID;

    private final Writer writer;

    private RenderingContext renderingContext;

    /**
     * Construct a new SiteRendererSink.
     *
     * @param renderingContext the RenderingContext.
     */
    public SiteRendererSink( RenderingContext renderingContext )
    {
        this( new StringWriter(), renderingContext );
    }

    /**
     * Construct a new SiteRendererSink.
     *
     * @param writer the writer for the sink.
     * @param renderingContext the RenderingContext.
     */
    private SiteRendererSink( StringWriter writer, RenderingContext renderingContext )
    {
        super( writer );

        this.writer = writer;
        this.headWriter = new StringWriter();
        this.renderingContext = renderingContext;
    }

    /** {@inheritDoc} */
    public void title_()
    {
        if ( getTextBuffer().length() > 0 )
        {
            title = getTextBuffer().toString();
        }

        resetTextBuffer();
    }

    /**
     * {@inheritDoc}
     *
     * Do nothing.
     * @see org.apache.maven.doxia.module.xhtml.XhtmlSink#title()
     */
    public void title()
    {
        // nop
    }

    /**
     * <p>Getter for the field <code>title</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTitle()
    {
        return title;
    }

    /** {@inheritDoc} */
    public void author_()
    {
        if ( getTextBuffer().length() > 0 )
        {
            String text = HtmlTools.escapeHTML( getTextBuffer().toString() );
            text = StringUtils.replace( text, "&amp;#", "&#" );
            authors.add( text.trim() );
        }

        resetTextBuffer();
    }

    /**
     * <p>Getter for the field <code>authors</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List getAuthors()
    {
        return authors;
    }

    /** {@inheritDoc} */
    public void date_()
    {
        if ( getTextBuffer().length() > 0 )
        {
            date = getTextBuffer().toString().trim();
        }

        resetTextBuffer();
    }

    /**
     * <p>Getter for the field <code>date</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDate()
    {
        return date;
    }

    /**
     * {@inheritDoc}
     *
     * Do nothing.
     * @see org.apache.maven.doxia.module.xhtml.XhtmlSink#body_()
     */
    public void body_()
    {
        // nop
    }

    /**
     * {@inheritDoc}
     *
     * Do nothing.
     * @see org.apache.maven.doxia.module.xhtml.XhtmlSink#body()
     */
    public void body()
    {
        // nop
    }

    /**
     * <p>getBody</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBody()
    {
        return writer.toString();
    }

    /**
     * <p>getHead</p>
     *
     * @return a {@link java.lang.String} object.
     *
     * @since 1.1.1
     */
    public String getHead()
    {
        return headWriter.toString();
    }

    /** {@inheritDoc} */
    public void head_()
    {
        setHeadFlag( false );
    }

    /** {@inheritDoc} */
    public void head()
    {
        setHeadFlag( true );
    }

    /** {@inheritDoc} */
    protected void onSectionTitle( int depth, SinkEventAttributes attributes )
    {
        this.sectionTitleBuffer = new StringBuffer();
        sectionHasID = ( attributes != null && attributes.isDefined ( Attribute.ID.toString() ) );

        super.onSectionTitle( depth, attributes );
    }

    /** {@inheritDoc} */
    protected void onSectionTitle_( int depth )
    {
        String sectionTitle = sectionTitleBuffer.toString();
        this.sectionTitleBuffer = null;

        if ( !sectionHasID && !StringUtils.isEmpty( sectionTitle ) )
        {
            anchor( HtmlTools.encodeId( sectionTitle ) );
            anchor_();
        }
        else
        {
            sectionHasID = false;
        }

        super.onSectionTitle_( depth );
    }

    /**
     * <p>Getter for the field <code>renderingContext</code>.</p>
     *
     * @return the current rendering context
     * @since 1.1
     */
    public RenderingContext getRenderingContext()
    {
        return renderingContext;
    }

    /** {@inheritDoc} */
    public void text( String text )
    {
        if ( sectionTitleBuffer != null )
        {
            // this implies we're inside a section title, collect text events for anchor generation
            sectionTitleBuffer.append( text );
        }

        super.text( text );
    }

    /** {@inheritDoc} */
    protected void write( String text )
    {
        if ( isHeadFlag() )
        {
            headWriter.write( unifyEOLs( text ) );

            return;
        }

        if ( renderingContext != null )
        {
            String relativePathToBasedir = renderingContext.getRelativePath();

            if ( relativePathToBasedir == null )
            {
                text = StringUtils.replace( text, "$relativePath", "." );
            }
            else
            {
                text = StringUtils.replace( text, "$relativePath", relativePathToBasedir );
            }
        }

        super.write( text );
    }
}
