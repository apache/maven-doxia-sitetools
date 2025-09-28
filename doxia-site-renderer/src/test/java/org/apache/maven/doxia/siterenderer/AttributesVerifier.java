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
import org.htmlunit.html.HtmlImage;
import org.htmlunit.html.HtmlItalic;
import org.htmlunit.html.HtmlMain;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlParagraph;
import org.htmlunit.html.HtmlPreformattedText;
import org.htmlunit.html.HtmlS;
import org.htmlunit.html.HtmlSection;
import org.htmlunit.html.HtmlSubscript;
import org.htmlunit.html.HtmlSuperscript;
import org.htmlunit.html.HtmlTable;
import org.htmlunit.html.HtmlTableHeaderCell;
import org.htmlunit.html.HtmlUnderlined;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 *
 *
 * @author ltheussl
 */
@SuppressWarnings("MethodLength")
public class AttributesVerifier extends AbstractVerifier {
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
        assertEquals("section", anchor.getAttribute("id"));
        HtmlHeading1 h1 = (HtmlHeading1) elementIterator.next();
        assertNotNull(h1);
        assertEquals("section", h1.asNormalizedText().trim());

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);

        assertEquals("ID", p.getAttribute("id"));
        assertEquals("CLASS", p.getAttribute("class"));
        assertEquals("TITLE", p.getAttribute("title"));
        assertEquals("STYLE", p.getAttribute("style"));
        assertEquals("LANG", p.getAttribute("lang"));

        HtmlImage img = (HtmlImage) elementIterator.next();
        assertNotNull(img);

        assertEquals("project.png", img.getAttribute("src"));
        assertEquals("150", img.getAttribute("width"));
        assertEquals("93", img.getAttribute("height"));
        assertEquals("border: 1px solid silver", img.getAttribute("style"));
        assertEquals("Project", img.getAttribute("alt"));

        // test object identity to distinguish the case ATTRIBUTE_VALUE_EMPTY
        assertSame(HtmlElement.ATTRIBUTE_NOT_DEFINED, img.getAttribute("dummy"));

        HtmlTable table = (HtmlTable) elementIterator.next();
        assertEquals("none", table.getAttribute("class"));

        element = elementIterator.next();
        // this is a htmlunit bug
        assertEquals("tbody", element.getTagName());

        elementIterator.next(); // tr
        elementIterator.next(); // tableheadercell

        HtmlTableHeaderCell th = (HtmlTableHeaderCell) elementIterator.next();
        assertEquals("text-align: center; width: 50%;", th.getAttribute("style"));
        assertEquals("2", th.getAttribute("colspan"));

        elementIterator.next(); // tr

        th = (HtmlTableHeaderCell) elementIterator.next();
        assertEquals("2", th.getAttribute("rowspan"));
        assertEquals("vertical-align: middle;", th.getAttribute("style"));

        elementIterator.next(); // td
        elementIterator.next(); // td
        elementIterator.next(); // tr
        elementIterator.next(); // td
        elementIterator.next(); // td

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);

        HtmlUnderlined u = (HtmlUnderlined) elementIterator.next();
        assertEquals("u", u.getTagName());
        HtmlS s = (HtmlS) elementIterator.next();
        assertEquals("s", s.getTagName());
        HtmlSubscript sub = (HtmlSubscript) elementIterator.next();
        assertEquals("sub", sub.getTagName());
        HtmlSuperscript sup = (HtmlSuperscript) elementIterator.next();
        assertEquals("sup", sup.getTagName());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);

        HtmlBold b = (HtmlBold) elementIterator.next();
        assertEquals("b", b.getTagName());
        HtmlItalic i = (HtmlItalic) elementIterator.next();
        assertEquals("i", i.getTagName());
        i = (HtmlItalic) elementIterator.next();
        assertEquals("i", i.getTagName());
        b = (HtmlBold) elementIterator.next();
        assertEquals("b", b.getTagName());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("color: red; margin-left: 20px", p.getAttribute("style"));

        HtmlAnchor a = (HtmlAnchor) elementIterator.next();
        assertEquals("Anchor", a.getAttribute("id"));

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("#Anchor", a.getAttribute("href"));
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("#Anchor", a.getAttribute("href"));
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("http://maven.apache.org/", a.getAttribute("href"));
        assertEquals("externalLink", a.getAttribute("class"));
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("./cdc.html", a.getAttribute("href"));
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("cdc.html", a.getAttribute("href"));
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("cdc.pdf", a.getAttribute("href"));
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("./cdc.txt", a.getAttribute("href"));
        a = (HtmlAnchor) elementIterator.next();
        assertEquals("/index.html", a.getAttribute("href"));

        HtmlPreformattedText pre = (HtmlPreformattedText) elementIterator.next();
        assertNotNull(pre);
        assertEquals("pretty", pre.getAttribute("class"));
        HtmlCode code = (HtmlCode) elementIterator.next();
        assertNotNull(code);

        pre = (HtmlPreformattedText) elementIterator.next();
        assertNotNull(pre);
        assertEquals("pretty", pre.getAttribute("id"));
        code = (HtmlCode) elementIterator.next();
        assertNotNull(code);

        section = (HtmlSection) elementIterator.next();
        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("Section_without_id", anchor.getAttribute("id"));
        h1 = (HtmlHeading1) elementIterator.next();
        assertEquals("Section without id", h1.asNormalizedText().trim());

        section = (HtmlSection) elementIterator.next();
        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("Subsection_without_id", anchor.getAttribute("id"));
        HtmlHeading2 h2 = (HtmlHeading2) elementIterator.next();
        assertEquals("Subsection without id", h2.asNormalizedText().trim());

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("section-id", a.getAttribute("id"));
        section = (HtmlSection) elementIterator.next();
        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("Section_with_id", anchor.getAttribute("id"));
        h1 = (HtmlHeading1) elementIterator.next();
        assertEquals("Section with id", h1.asNormalizedText().trim());

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("subsection-id", a.getAttribute("id"));
        elementIterator.next(); // section
        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("Subsection_with_id", anchor.getAttribute("id"));
        h2 = (HtmlHeading2) elementIterator.next();
        assertEquals("Subsection with id", h2.asNormalizedText().trim());

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("foo", a.getAttribute("id"));
        section = (HtmlSection) elementIterator.next();
        assertEquals("bar", section.getAttribute("class"));
        assertEquals("foo", section.getAttribute("id"));
        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("Section_name", anchor.getAttribute("id"));
        h1 = (HtmlHeading1) elementIterator.next();
        assertEquals("Section name", h1.asNormalizedText().trim());
        assertEquals("", h1.getAttribute("class"));

        a = (HtmlAnchor) elementIterator.next();
        assertEquals("subfoo", a.getAttribute("id"));
        section = (HtmlSection) elementIterator.next();
        assertEquals("subbar", section.getAttribute("class"));
        assertEquals("subfoo", section.getAttribute("id"));
        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("Subsection_name", anchor.getAttribute("id"));
        h2 = (HtmlHeading2) elementIterator.next();
        assertEquals("Subsection name", h2.asNormalizedText().trim());
        assertEquals("", h2.getAttribute("class"));

        assertFalse(elementIterator.hasNext());
    }
}
