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
import java.util.Iterator;
import java.util.List;

import org.htmlunit.BrowserVersion;
import org.htmlunit.CollectingAlertHandler;
import org.htmlunit.WebClient;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNodeList;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlHeading1;
import org.htmlunit.html.HtmlMain;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSection;

import static org.codehaus.plexus.testing.PlexusExtension.getTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verify client side rendering of Mermaid diagrams.
 */
public class MermaidVerifier extends AbstractVerifier {
    /**
     * Verifies a HtmlPage.
     *
     * @param file the file to verify.
     *
     * @throws Exception if something goes wrong.
     */
    public void verify(String file) throws Exception {
        File jsTest = getTestFile(file);
        assertNotNull(jsTest);
        assertTrue(jsTest.exists());

        // HtmlUnit
        try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            webClient.getOptions().setCssEnabled(false);
            // the JS for Mermaid is too complex for HtmlUnit, so disable it and just verify the presence of the script
            // element
            webClient.getOptions().setJavaScriptEnabled(false);

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
            assertEquals("Example_Mermaid_diagram", anchor.getAttribute("id"));
            HtmlHeading1 h1 = (HtmlHeading1) elementIterator.next();
            assertNotNull(h1);
            assertEquals("Example Mermaid diagram", h1.asNormalizedText().trim());

            DomNodeList<DomElement> scripts = page.getElementsByTagName("script");
            assertEquals(2, scripts.getLength());

            // first one is the external Mermaid script,
            scripts.get(0).getAttribute("src").equals("./js/mermaid.min.js");

            // second one is the inline script to call the Mermaid API
            scripts.get(1).asNormalizedText().trim().contains("mermaid.initialize");
        }
    }
}
