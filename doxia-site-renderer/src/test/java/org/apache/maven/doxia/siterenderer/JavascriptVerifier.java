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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.htmlunit.CollectingAlertHandler;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlHeading1;
import org.htmlunit.html.HtmlMain;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlParagraph;
import org.htmlunit.html.HtmlScript;
import org.htmlunit.html.HtmlSection;

import static org.codehaus.plexus.testing.PlexusExtension.getTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verify javascript code.
 *
 * @author ltheussl
 */
public class JavascriptVerifier extends AbstractVerifier {
    /**
     * Verifies a HtmlPage.
     *
     * @param file the file to verify.
     *
     * @throws Exception if something goes wrong.
     */
    public void verify(String file) throws Exception {
        File jsTest = getTestFile("target/output/javascript.html");
        assertNotNull(jsTest);
        assertTrue(jsTest.exists());

        // HtmlUnit
        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setCssEnabled(false);

            final List<String> collectedAlerts = new ArrayList<>(4);
            webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

            HtmlPage page = (HtmlPage) webClient.getPage(jsTest.toURI().toURL());
            assertNotNull(page);

            HtmlElement element = page.getHtmlElementById("contentBox");
            assertNotNull(element);
            HtmlMain main = (HtmlMain) element;
            assertNotNull(main);

            Iterator<HtmlElement> elementIterator =
                    main.getHtmlElementDescendants().iterator();

            // ----------------------------------------------------------------------
            //
            // ----------------------------------------------------------------------

            HtmlSection section = (HtmlSection) elementIterator.next();
            assertNotNull(section);

            HtmlAnchor anchor = (HtmlAnchor) elementIterator.next();
            assertNotNull(anchor);
            assertEquals("Test", anchor.getAttribute("id"));
            HtmlHeading1 h1 = (HtmlHeading1) elementIterator.next();
            assertNotNull(h1);
            assertEquals("Test", h1.asNormalizedText().trim());

            HtmlParagraph p = (HtmlParagraph) elementIterator.next();
            assertNotNull(p);
            assertEquals(
                    "You should see a JavaScript alert...", p.asNormalizedText().trim());

            HtmlScript script = (HtmlScript) elementIterator.next();
            assertNotNull(script);
            assertEquals("text/javascript", script.getAttribute("type"));
            assertEquals("", script.asNormalizedText().trim());
            List<String> expectedAlerts = Collections.singletonList("Hello!");
            assertEquals(expectedAlerts, collectedAlerts);
        }
    }
}
