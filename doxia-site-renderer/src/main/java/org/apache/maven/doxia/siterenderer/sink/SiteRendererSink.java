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
import javax.swing.text.html.HTML.Tag;

import org.apache.maven.doxia.module.xhtml.XhtmlSink;
import org.apache.maven.doxia.module.xhtml.decoration.render.RenderingContext;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.util.HtmlTools;

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

    private boolean sectionHasID;

    private final Writer writer;

    /**
     * @param renderingContext
     */
    public SiteRendererSink( RenderingContext renderingContext )
    {
        this( new StringWriter(), renderingContext );
    }

    /**
     * @param writer
     * @param renderingContext
     */
    private SiteRendererSink( StringWriter writer, RenderingContext renderingContext )
    {
        super( writer, renderingContext, null );

        this.writer = writer;
    }

    /** {@inheritDoc} */
    public void title_()
    {
        if ( getBuffer().length() > 0 )
        {
            title = getBuffer().toString();
        }

        resetBuffer();
    }

    /**
     * Do nothing.
     *
     * @see org.apache.maven.doxia.module.xhtml.XhtmlSink#title()
     */
    public void title()
    {
        // nop
    }

    public String getTitle()
    {
        return title;
    }

    /** {@inheritDoc} */
    public void author_()
    {
        if ( getBuffer().length() > 0 )
        {
            authors.add( getBuffer().toString() );
        }

        resetBuffer();
    }

    public List getAuthors()
    {
        return authors;
    }

    /** {@inheritDoc} */
    public void date_()
    {
        if ( getBuffer().length() > 0 )
        {
            date = getBuffer().toString();
        }

        resetBuffer();
    }

    public String getDate()
    {
        return date;
    }

    /**
     * Do nothing.
     *
     * @see org.apache.maven.doxia.module.xhtml.XhtmlSink#body_()
     */
    public void body_()
    {
        // nop
    }

    /**
     * Do nothing.
     *
     * @see org.apache.maven.doxia.module.xhtml.XhtmlSink#body()
     */
    public void body()
    {
        // nop
    }

    public String getBody()
    {
        return writer.toString();
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
    public void sectionTitle( int level, SinkEventAttributes attributes )
    {
        if ( level == SECTION_LEVEL_1 || level == SECTION_LEVEL_2 )
        {
            setHeadFlag( true );

            sectionHasID = ( attributes != null && attributes.isDefined ( Attribute.ID.toString() ) );
        }
        else
        {
            super.sectionTitle( level, attributes );
        }
    }

    /** {@inheritDoc} */
    public void sectionTitle_( int level )
    {
        if ( level == SECTION_LEVEL_1 || level == SECTION_LEVEL_2 )
        {
            String sectionTitle = "";

            if ( getBuffer().length() > 0 )
            {
                sectionTitle = getBuffer().toString();
            }

            resetBuffer();

            setHeadFlag( false );

            writeStartTag( level == SECTION_LEVEL_1 ? Tag.H2 : Tag.H3  );

            if ( !sectionHasID )
            {
                anchor( HtmlTools.encodeId( sectionTitle ) );
                anchor_();
            }
            else
            {
                sectionHasID = false;
            }

            text( sectionTitle );
            writeEndTag( level == SECTION_LEVEL_1 ? Tag.H2 : Tag.H3 );
        }
        else
        {
            super.sectionTitle_( level );
        }
    }

    /**
     * Sets the head flag to true so the title text is buffered until the closing tag.
     *
     * @see org.apache.maven.doxia.sink.XhtmlBaseSink#sectionTitle1()
     */
    public void sectionTitle1()
    {
        sectionTitle( SECTION_LEVEL_1, null );
    }

    /**
     * Writes out a sectionTitle1 block, including an anchor that is constructed from the
     * buffered title text via {@link org.apache.maven.doxia.util.HtmlTools#encodeId(String)}.
     *
     * @see org.apache.maven.doxia.sink.XhtmlBaseSink#sectionTitle1_()
     */
    public void sectionTitle1_()
    {
        sectionTitle_( SECTION_LEVEL_1 );
    }

    /**
     * Sets the head flag to true so the title text is buffered until the closing tag.
     *
     * @see org.apache.maven.doxia.sink.XhtmlBaseSink#sectionTitle2()
     */
    public void sectionTitle2()
    {
        sectionTitle( SECTION_LEVEL_2, null );
    }

    /**
     * Writes out a sectionTitle2 block, including an anchor that is constructed from the
     * buffered title text via {@link org.apache.maven.doxia.util.HtmlTools#encodeId(String)}.
     *
     * @see org.apache.maven.doxia.sink.XhtmlBaseSink#sectionTitle2_()
     */
    public void sectionTitle2_()
    {
        sectionTitle_( SECTION_LEVEL_2 );
    }
}
