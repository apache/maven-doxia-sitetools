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
import org.htmlunit.html.HtmlDefinitionDescription;
import org.htmlunit.html.HtmlDefinitionList;
import org.htmlunit.html.HtmlDefinitionTerm;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlHeading1;
import org.htmlunit.html.HtmlListItem;
import org.htmlunit.html.HtmlMain;
import org.htmlunit.html.HtmlOrderedList;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlParagraph;
import org.htmlunit.html.HtmlPreformattedText;
import org.htmlunit.html.HtmlSection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author ltheussl
 */
public class FaqVerifier extends AbstractVerifier {
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

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertEquals("top", a.getAttribute("id"));

        HtmlHeading1 h1 = (HtmlHeading1) elementIterator.next();
        assertEquals("Often Asked Questions", h1.asNormalizedText().trim());

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        element = elementIterator.next();
        assertEquals("b", element.getTagName());
        assertEquals("Contributing", element.asNormalizedText().trim());

        HtmlOrderedList ol = (HtmlOrderedList) elementIterator.next();
        assertEquals(
                "One stupid question & a silly answer?",
                ol.getFirstElementChild().asNormalizedText().trim());

        HtmlListItem li = (HtmlListItem) elementIterator.next();
        assertEquals(
                "One stupid question & a silly answer?",
                li.getFirstElementChild().asNormalizedText().trim());

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("#stupid-question", a.getAttribute("href"));

        element = elementIterator.next();
        assertEquals("b", element.getTagName());
        assertEquals("stupid", element.asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        element = elementIterator.next();
        assertEquals("b", element.getTagName());
        assertEquals("Using Maven", element.asNormalizedText().trim());

        ol = (HtmlOrderedList) elementIterator.next();
        assertEquals(
                "How do I disable a report on my site?",
                ol.getFirstElementChild().asNormalizedText().trim());

        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals(
                "How do I disable a report on my site?",
                li.getFirstElementChild().asNormalizedText().trim());

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("#disable-reports", a.getAttribute("href"));

        section = (HtmlSection) elementIterator.next();

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("contributing", a.getAttribute("id"));

        h1 = (HtmlHeading1) elementIterator.next();
        assertEquals("Contributing", h1.asNormalizedText().trim());

        HtmlDefinitionList dl = (HtmlDefinitionList) elementIterator.next();

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("stupid-question", a.getAttribute("id"));

        HtmlDefinitionTerm dt = (HtmlDefinitionTerm) elementIterator.next();
        assertEquals(
                "One stupid question & a silly answer?", dt.asNormalizedText().trim());

        element = elementIterator.next();
        assertEquals("b", element.getTagName());
        assertEquals("stupid", element.asNormalizedText().trim());

        HtmlDefinitionDescription dd = (HtmlDefinitionDescription) elementIterator.next();

        p = (HtmlParagraph) elementIterator.next();

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("#using", a.getAttribute("href"));
        assertEquals("local link", a.asNormalizedText().trim());

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("./cdc.html", a.getAttribute("href"));
        assertEquals("source document", a.asNormalizedText().trim());

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("http://maven.apache.org/?l=a&m=b", a.getAttribute("href"));
        assertEquals("external link", a.asNormalizedText().trim());

        element = elementIterator.next();
        assertEquals("i", element.getTagName());
        assertEquals("italic", element.asNormalizedText().trim());

        element = elementIterator.next();
        assertEquals("b", element.getTagName());
        assertEquals("non-US-ASCII characters: àéèç", element.asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("#top", a.getAttribute("href"));
        assertEquals("[top]", a.asNormalizedText().trim());

        section = (HtmlSection) elementIterator.next();

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("using", a.getAttribute("id"));

        h1 = (HtmlHeading1) elementIterator.next();
        assertEquals("Using Maven", h1.asNormalizedText().trim());

        dl = (HtmlDefinitionList) elementIterator.next();

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("disable-reports", a.getAttribute("id"));

        dt = (HtmlDefinitionTerm) elementIterator.next();
        assertEquals(
                "How do I disable a report on my site?",
                dt.getFirstChild().asNormalizedText().trim());

        dd = (HtmlDefinitionDescription) elementIterator.next();

        p = (HtmlParagraph) elementIterator.next();

        element = elementIterator.next();
        assertEquals("code", element.getTagName());
        assertEquals("<source></source>", element.asNormalizedText().trim());

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertEquals("verbatim source", div.getAttribute("class"));

        HtmlPreformattedText pre = (HtmlPreformattedText) elementIterator.next();
        assertEquals("<source>1.5</source>", pre.asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("#top", a.getAttribute("href"));
        assertEquals("[top]", a.asNormalizedText().trim());

        assertFalse(elementIterator.hasNext());
    }
}
