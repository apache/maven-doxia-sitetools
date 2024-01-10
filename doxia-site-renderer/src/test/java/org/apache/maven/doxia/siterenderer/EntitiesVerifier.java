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

import org.htmlunit.html.HtmlDivision;
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

        HtmlHeading1 h1 = (HtmlHeading1) elementIterator.next();
        assertNotNull(h1);
        assertEquals(h1.asNormalizedText().trim(), "section name with entities: '&' '\u0391' ' ' '\uD835\uDFED'");

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        HtmlHeading3 h3 = (HtmlHeading3) elementIterator.next();
        assertNotNull(h3);
        assertEquals("Entities", h3.asNormalizedText().trim());

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        HtmlHeading2 h2 = (HtmlHeading2) elementIterator.next();
        assertNotNull(h2);
        assertEquals(
                "Generic Entities: '&' '<' '>' '\"' '''", h2.asNormalizedText().trim());

        HtmlParagraph p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("'&' '<' '>' '\"' '''", p.asNormalizedText().trim());

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        h2 = (HtmlHeading2) elementIterator.next();
        assertNotNull(h2);
        assertEquals(
                "Local Entities: '\u0391' '\u0392' '\u0393' '\uD835\uDFED'",
                h2.asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals(
                "'\u0391' '\u0392' '\u0393' '\uD835\uDFED\uD835\uDFED' '\u0159\u0159' '\u0159'",
                p.asNormalizedText().trim());

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        h2 = (HtmlHeading2) elementIterator.next();
        assertNotNull(h2);
        assertEquals(
                "DTD Entities: ' ' '\u00A1' '\u00A2'", h2.asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("' ' '\u00A1' '\u00A2'", p.asNormalizedText().trim());

        section = (HtmlSection) elementIterator.next();
        assertNotNull(section);

        h3 = (HtmlHeading3) elementIterator.next();
        assertNotNull(h3);
        assertEquals("CDATA", h3.asNormalizedText().trim());

        HtmlDivision div = (HtmlDivision) elementIterator.next();
        assertNotNull(div);

        HtmlPreformattedText pre = (HtmlPreformattedText) elementIterator.next();
        assertNotNull(pre);
        assertEquals("<project xmlns:ant=\"jelly:ant\">", pre.asNormalizedText().trim());

        p = (HtmlParagraph) elementIterator.next();
        assertNotNull(p);
        assertEquals("'&nbsp;' '&iexcl;'", p.asNormalizedText().trim());

        assertFalse(elementIterator.hasNext());
    }
}
