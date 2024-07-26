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

import java.util.Iterator;

import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlBold;
import org.htmlunit.html.HtmlCode;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlHeading1;
import org.htmlunit.html.HtmlHeading2;
import org.htmlunit.html.HtmlItalic;
import org.htmlunit.html.HtmlMain;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlParagraph;
import org.htmlunit.html.HtmlSection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies apt transformations.
 *
 * @author ltheussl
 */
public class AptVerifier extends AbstractVerifier {
    /** {@inheritDoc} */
    public void verify(String file) throws Exception {
        HtmlPage page = htmlPage(file);
        assertNotNull(page);

        HtmlElement element = page.getHtmlElementById("contentBox");
        assertNotNull(element);
        HtmlMain main = (HtmlMain) element;
        assertNotNull(main);

        Iterator<HtmlElement> elementIterator = main.getHtmlElementDescendants().iterator();

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        HtmlSection section = (HtmlSection) elementIterator.next();

        HtmlAnchor anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("Links", anchor.getAttribute("id"));
        HtmlHeading1 h1 = (HtmlHeading1) elementIterator.next();
        assertNotNull(h1);
        assertEquals("Links", h1.asNormalizedText().trim());

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);

        // Expected log: [APT Parser] Ambiguous link: 'cdc.html'. If this is a local link, prepend "./"!
        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertEquals("Anchor", a.getAttribute("id"));
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("cdc.html", a.getAttribute("id"));

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("#Anchor", a.getAttribute("href"));
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("#Anchor", a.getAttribute("href"));

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("Anchor_with_space", a.getAttribute("id"));
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("#Anchor_with_space", a.getAttribute("href"));

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("http://maven.apache.org/", a.getAttribute("href"));
        assertEquals("externalLink", a.getAttribute("class"));
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("http://maven.apache.org/", a.getAttribute("href"));
        assertEquals("externalLink", a.getAttribute("class"));

        // Expected log: [APT Parser] Ambiguous link: 'cdc.html'. If this is a local link, prepend "./"!
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("./cdc.html", a.getAttribute("href"));
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("#cdc.html", a.getAttribute("href"));

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("/index.html", a.getAttribute("href"));

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("Section_formatting.3A_italic_bold_mono", anchor.getAttribute("id"));
        h1 = (HtmlHeading1) elementIterator.next();
        assertNotNull(h1);
        // Note: htmlunit strips the white space, actual result is ok
        assertEquals(
                "Section formatting: italic bold mono", h1.asNormalizedText().trim());

        HtmlItalic italic = (HtmlItalic) elementIterator.next();
        assertEquals("i", italic.getTagName());
        assertEquals("italic", italic.asNormalizedText().trim());

        HtmlBold bold = (HtmlBold) elementIterator.next();
        assertEquals("b", bold.getTagName());
        assertEquals("bold", bold.asNormalizedText().trim());

        HtmlCode code = (HtmlCode) elementIterator.next();
        assertEquals("code", code.getTagName());
        assertEquals("mono", code.asNormalizedText().trim());

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("SubSection_formatting.3A_italic_bold_mono", anchor.getAttribute("id"));
        HtmlHeading2 h2 = (HtmlHeading2) elementIterator.next();
        assertNotNull(h2);
        // Note: htmlunit strips the white space, actual result is ok
        assertEquals(
                "SubSection formatting: italic bold mono", h2.asNormalizedText().trim());

        italic = (HtmlItalic) elementIterator.next();
        assertEquals("i", italic.getTagName());
        assertEquals("italic", italic.asNormalizedText().trim());

        bold = (HtmlBold) elementIterator.next();
        assertEquals("b", bold.getTagName());
        assertEquals("bold", bold.asNormalizedText().trim());

        code = (HtmlCode) elementIterator.next();
        assertEquals("code", code.getTagName());
        assertEquals("mono", code.asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);

        italic = (HtmlItalic) elementIterator.next();
        assertEquals("i", italic.getTagName());
        assertEquals("italic", italic.asNormalizedText().trim());

        bold = (HtmlBold) elementIterator.next();
        assertEquals("b", bold.getTagName());
        assertEquals("bold", bold.asNormalizedText().trim());

        code = (HtmlCode) elementIterator.next();
        assertEquals("code", code.getTagName());
        assertEquals("mono", code.asNormalizedText().trim());

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        h1 = (HtmlHeading1) elementIterator.next();
        assertNotNull(h1);
        assertEquals(
                "No Default Anchor in Section Title with Explicit Anchor",
                h1.asNormalizedText().trim());
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("No_Default_Anchor_in_Section_Title_with_Explicit_Anchor", a.getAttribute("id"));

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);
    }
}
