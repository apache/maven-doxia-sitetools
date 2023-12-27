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

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlHeading1;
import org.htmlunit.html.HtmlListItem;
import org.htmlunit.html.HtmlMain;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlParagraph;
import org.htmlunit.html.HtmlSection;
import org.htmlunit.html.HtmlUnorderedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author ltheussl
 */
public class MultipleBlockVerifier extends AbstractVerifier {
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
        // Verify link
        // ----------------------------------------------------------------------

        HtmlSection section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        HtmlHeading1 h1 = (HtmlHeading1) elementIterator.next();
        assertNotNull(h1);
        assertEquals("section name", h1.asNormalizedText().trim());

        // ----------------------------------------------------------------------
        // Paragraph
        // ----------------------------------------------------------------------

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("text", p.asNormalizedText().trim());

        // ----------------------------------------------------------------------
        // Unordered list
        // ----------------------------------------------------------------------

        HtmlUnorderedList ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull(ul);

        HtmlListItem li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("list1", li.getFirstChild().asNormalizedText().trim());

        // ----------------------------------------------------------------------
        // Paragraph
        // ----------------------------------------------------------------------

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("text2", p.asNormalizedText().trim());

        // ----------------------------------------------------------------------
        // Unordered list
        // ----------------------------------------------------------------------

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull(ul);

        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("list1", li.getFirstChild().asNormalizedText().trim());

        // ----------------------------------------------------------------------
        // Paragraph
        // ----------------------------------------------------------------------

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("text3", p.asNormalizedText().trim());

        // ----------------------------------------------------------------------
        // Unordered list
        // ----------------------------------------------------------------------

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull(ul);

        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("list1", p.getFirstChild().asNormalizedText().trim());

        assertFalse(elementIterator.hasNext());
    }
}
