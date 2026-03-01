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
package org.apache.maven.doxia.scm.siterenderer;

import javax.inject.Inject;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

import org.apache.maven.doxia.siterenderer.ContextCustomizer;
import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;

import static org.codehaus.plexus.testing.PlexusExtension.getTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@PlexusTest
class ScmAttributesContextCustomizerIT {

    @Inject
    ScmManager scmManager;

    @Test
    void lastModifiedDate() throws Exception {
        File siteDirectory = getTestFile("src/test/resources/site-last-modified");
        File doxiaSource = new File(siteDirectory, "markdown/lastmodified.md.vm");
        assertTrue(doxiaSource.exists(), "Test source file does not exist: " + doxiaSource.getAbsolutePath());
        assumeTrue(isScmInfoAvailable(doxiaSource), "SCM info is not available, skipping test");

        OffsetDateTime modifiedDate = OffsetDateTime.of(2026, 3, 1, 19, 51, 28, 0, ZoneOffset.UTC);

        ContextCustomizer contextCustomizer = new ScmAttributesContextCustomizer(scmManager);
        Context context = new VelocityContext();
        DocumentRenderingContext docContext =
                new DocumentRenderingContext(doxiaSource.getParentFile(), doxiaSource.getName(), null);
        SiteRenderingContext siteContext = new SiteRenderingContext();
        siteContext.setRootDirectory(siteDirectory);
        contextCustomizer.customizeContext(context, docContext, siteContext);
        assertTrue(context.containsKey("scmModifiedDate"));
        assertEquals(Date.from(modifiedDate.toInstant()), context.get("scmModifiedDate"));
    }

    boolean isScmInfoAvailable(File file) {
        try {
            Optional<ScmRepository> repo =
                    ScmAttributesContextCustomizer.getScmRepository(scmManager, file.getParentFile());
            if (!repo.isPresent()) {
                return false;
            }
            InfoItem infoItem = ScmAttributesContextCustomizer.getScmInfo(scmManager, repo.get(), file);
            if (infoItem != null && infoItem.getLastChangedDateTime() != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
