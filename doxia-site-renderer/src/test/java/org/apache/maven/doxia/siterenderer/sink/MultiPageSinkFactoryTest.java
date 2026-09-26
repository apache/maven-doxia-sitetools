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
package org.apache.maven.doxia.siterenderer.sink;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiPageSinkFactoryTest {
    private static final File BASEDIR = new File("target/test-basedir");

    private static final File REPORT_OUTPUT_DIRECTORY = new File(BASEDIR, "site");

    private MultiPageSinkFactory newSinkFactory() {
        DocumentRenderingContext mainContext = new DocumentRenderingContext(BASEDIR, "main-page", "generator");
        return new MultiPageSinkFactory(REPORT_OUTPUT_DIRECTORY, mainContext);
    }

    @Test
    void noSubpagesUntilTheReportAsksForOne() {
        assertTrue(newSinkFactory().getSinks().isEmpty());
    }

    @Test
    void subpageKeepsWhereItIsToBeWritten() {
        MultiPageSinkFactory sinkFactory = newSinkFactory();

        sinkFactory.createSink(REPORT_OUTPUT_DIRECTORY, "subpage.html");

        assertEquals(1, sinkFactory.getSinks().size());
        MultiPageSubSink subSink = sinkFactory.getSinks().get(0);
        assertEquals(REPORT_OUTPUT_DIRECTORY, subSink.getOutputDirectory());
        assertEquals("subpage.html", subSink.getOutputName());
    }

    @Test
    void subpageDocumentIsRelativeToTheReportOutputDirectory() {
        MultiPageSinkFactory sinkFactory = newSinkFactory();

        sinkFactory.createSink(new File(REPORT_OUTPUT_DIRECTORY, "nested"), "subpage.html");

        assertEquals(
                "nested/subpage.html",
                sinkFactory.getSinks().get(0).getRenderingContext().getOutputPath());
    }

    @Test
    void subpageWithoutAnExtensionIsTakenAsIs() {
        MultiPageSinkFactory sinkFactory = newSinkFactory();

        sinkFactory.createSink(REPORT_OUTPUT_DIRECTORY, "subpage");

        assertEquals(
                "subpage.html",
                sinkFactory.getSinks().get(0).getRenderingContext().getOutputPath());
    }

    @Test
    void onlyTheFileBasedFactoryMethodIsSupported() {
        MultiPageSinkFactory sinkFactory = newSinkFactory();

        assertThrows(
                UnsupportedOperationException.class,
                () -> sinkFactory.createSink(REPORT_OUTPUT_DIRECTORY, "subpage.html", "UTF-8"));
        assertThrows(UnsupportedOperationException.class, () -> sinkFactory.createSink(new ByteArrayOutputStream()));
        assertThrows(
                UnsupportedOperationException.class,
                () -> sinkFactory.createSink(new ByteArrayOutputStream(), "UTF-8"));
        assertTrue(sinkFactory.getSinks().isEmpty());
    }
}
