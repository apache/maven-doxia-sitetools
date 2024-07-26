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
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlHeading1;
import org.htmlunit.html.HtmlHeading2;
import org.htmlunit.html.HtmlListItem;
import org.htmlunit.html.HtmlMain;
import org.htmlunit.html.HtmlOrderedList;
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
public class NestedItemsVerifier extends AbstractVerifier {
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

        HtmlAnchor anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("List_Section", anchor.getAttribute("id"));
        HtmlHeading1 h1 = (HtmlHeading1) elementIterator.next();
        assertNotNull(h1);
        assertEquals("List Section", h1.asNormalizedText().trim());

        // ----------------------------------------------------------------------
        // Unordered lists
        // ----------------------------------------------------------------------

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("Unordered_lists", anchor.getAttribute("id"));
        HtmlHeading2 h2 = (HtmlHeading2) elementIterator.next();
        assertNotNull(h2);
        assertEquals("Unordered lists", h2.asNormalizedText().trim());

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals(
                "Below is an unordered list, followed by six paragraphs.",
                p.asNormalizedText().trim());

        HtmlUnorderedList ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull(ul);

        HtmlListItem li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 1.", li.getFirstChild().asNormalizedText().trim());

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull(ul);

        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Item 11.", p.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Item 12.", p.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 13.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 14.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 2.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 3.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 4.", li.getFirstChild().asNormalizedText().trim());

        ul = (HtmlUnorderedList) elementIterator.next();
        assertNotNull(ul);

        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 41.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 42.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 43.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 44.", li.getFirstChild().asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 1 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 2 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 3 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 4 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 5 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 6 below list.", p.asNormalizedText().trim());

        // ----------------------------------------------------------------------
        // Ordered lists
        // ----------------------------------------------------------------------

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("Ordered_lists", anchor.getAttribute("id"));
        h2 = (HtmlHeading2) elementIterator.next();
        assertNotNull(h2);
        assertEquals("Ordered lists", h2.asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals(
                "Below is an ordered list, followed by six paragraphs.",
                p.asNormalizedText().trim());

        HtmlOrderedList ol = (HtmlOrderedList) elementIterator.next();
        assertNotNull(ol);

        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 1.", li.getFirstChild().asNormalizedText().trim());

        ol = (HtmlOrderedList) elementIterator.next();
        assertNotNull(ol);

        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 11.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 12.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 13.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 14.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 2.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 3.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 4.", li.getFirstChild().asNormalizedText().trim());

        ol = (HtmlOrderedList) elementIterator.next();
        assertNotNull(ol);

        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 41.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 42.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 43.", li.getFirstChild().asNormalizedText().trim());
        li = (HtmlListItem) elementIterator.next();
        assertNotNull(li);
        assertEquals("Item 44.", li.getFirstChild().asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 1 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 2 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 3 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 4 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 5 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 6 below list.", p.asNormalizedText().trim());

        // ----------------------------------------------------------------------
        // Definition lists
        // ----------------------------------------------------------------------

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("Definition_lists", anchor.getAttribute("id"));
        h2 = (HtmlHeading2) elementIterator.next();
        assertNotNull(h2);
        assertEquals("Definition lists", h2.asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals(
                "Below is a definition list, followed by six paragraphs.",
                p.asNormalizedText().trim());

        HtmlDefinitionList dl = (HtmlDefinitionList) elementIterator.next();
        assertNotNull(dl);

        HtmlDefinitionTerm dt = (HtmlDefinitionTerm) elementIterator.next();
        assertNotNull(dt);
        assertEquals("Term 1.", dt.getFirstChild().asNormalizedText().trim());
        HtmlDefinitionDescription dd = (HtmlDefinitionDescription) elementIterator.next();
        assertNotNull(dd);
        assertEquals("Description 1.", dd.getFirstChild().asNormalizedText().trim());

        dt = (HtmlDefinitionTerm) elementIterator.next();
        assertNotNull(dt);
        assertEquals("Term 2.", dt.getFirstChild().asNormalizedText().trim());
        dd = (HtmlDefinitionDescription) elementIterator.next();
        assertNotNull(dd);
        assertEquals("Description 2.", dd.getFirstChild().asNormalizedText().trim());

        dl = (HtmlDefinitionList) elementIterator.next();
        assertNotNull(dl);
        dt = (HtmlDefinitionTerm) elementIterator.next();
        assertNotNull(dt);
        assertEquals("Term 21.", dt.getFirstChild().asNormalizedText().trim());
        dd = (HtmlDefinitionDescription) elementIterator.next();
        assertNotNull(dd);
        assertEquals("Description 21.", dd.getFirstChild().asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 1 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 2 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 3 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 4 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 5 below list.", p.asNormalizedText().trim());
        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("Paragraph 6 below list.", p.asNormalizedText().trim());

        assertFalse(elementIterator.hasNext());
    }
}
