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
import org.htmlunit.html.HtmlCode;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlHeading1;
import org.htmlunit.html.HtmlHeading2;
import org.htmlunit.html.HtmlHeading3;
import org.htmlunit.html.HtmlMain;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlParagraph;
import org.htmlunit.html.HtmlPreformattedText;
import org.htmlunit.html.HtmlSection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verify the <code>site/xdoc/entityTest.xml</code>
 *
 * @author ltheussl
 */
public class EntitiesVerifier extends AbstractVerifier {
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
        assertEquals(
                "section_name_with_entities.3A_.27.26.27_.27.CE.91.27_.27.C2.A0.27_.27.3F.3F.27",
                anchor.getAttribute("id"));
        HtmlHeading1 h1 = (HtmlHeading1) elementIterator.next();
        assertNotNull(h1);
        assertEquals(h1.asNormalizedText().trim(), "section name with entities: '&' '\u0391' ' ' '\uD835\uDFED'");

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("Entities", anchor.getAttribute("id"));
        HtmlHeading2 h2 = (HtmlHeading2) elementIterator.next();
        assertNotNull(h2);
        assertEquals("Entities", h2.asNormalizedText().trim());

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals(
                "Generic_Entities.3A_.27.26.27_.27.3C.27_.27.3E.27_.27.22.27_.27.27.27", anchor.getAttribute("id"));
        HtmlHeading3 h3 = (HtmlHeading3) elementIterator.next();
        assertNotNull(h3);
        assertEquals(
                "Generic Entities: '&' '<' '>' '\"' '''", h3.asNormalizedText().trim());

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("'&' '<' '>' '\"' '''", p.asNormalizedText().trim());

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals(
                "Local_Entities.3A_.27.CE.91.27_.27.CE.92.27_.27.CE.93.27_.27.3F.3F.27", anchor.getAttribute("id"));
        h3 = (HtmlHeading3) elementIterator.next();
        assertNotNull(h3);
        assertEquals(
                "Local Entities: '\u0391' '\u0392' '\u0393' '\uD835\uDFED'",
                h3.asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals(
                "'\u0391' '\u0392' '\u0393' '\uD835\uDFED\uD835\uDFED' '\u0159\u0159' '\u0159'",
                p.asNormalizedText().trim());

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("DTD_Entities.3A_.27.27_.27.C2.A1.27_.27.C2.A2.27", anchor.getAttribute("id"));
        h3 = (HtmlHeading3) elementIterator.next();
        assertNotNull(h3);
        assertEquals(
                "DTD Entities: ' ' '\u00A1' '\u00A2'", h3.asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("' ' '\u00A1' '\u00A2'", p.asNormalizedText().trim());

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        anchor = (HtmlAnchor) elementIterator.next();
        assertNotNull(anchor);
        assertEquals("CDATA", anchor.getAttribute("id"));
        h2 = (HtmlHeading2) elementIterator.next();
        assertNotNull(h2);
        assertEquals("CDATA", h2.asNormalizedText().trim());

        HtmlPreformattedText pre = (HtmlPreformattedText) elementIterator.next();
        assertNotNull(pre);
        HtmlCode code = (HtmlCode) elementIterator.next();
        assertNotNull(code);
        assertEquals(
                "<project xmlns:ant=\"jelly:ant\">", code.asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("'&nbsp;' '&iexcl;'", p.asNormalizedText().trim());

        assertFalse(elementIterator.hasNext());
    }
}
