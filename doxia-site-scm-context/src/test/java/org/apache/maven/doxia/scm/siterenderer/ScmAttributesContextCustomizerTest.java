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

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.apache.maven.doxia.siterenderer.ContextCustomizer;
import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ScmAttributesContextCustomizerTest {

    @Mock
    ScmManager scmManager;

    @Mock
    ScmRepository scmRepository;

    @Mock
    ScmProvider scmProvider;

    @Mock
    ScmProviderRepository scmProviderRepository;

    private ContextCustomizer contextCustomizer;

    private Context context;

    private DocumentRenderingContext docContext;

    private SiteRenderingContext siteContext;

    private File siteDirectory;

    @BeforeEach
    void setup() throws IOException {
        siteDirectory = new File("src/test/resources/site-last-modified").getCanonicalFile();
        contextCustomizer = new ScmAttributesContextCustomizer(scmManager);
        context = new VelocityContext();
        docContext = new DocumentRenderingContext(siteDirectory, "markdown/lastmodified.md.vm", null);
        siteContext = new SiteRenderingContext();
        siteContext.setRootDirectory(siteDirectory);
    }

    @Test
    void lastModifiedDate() throws Exception {
        Mockito.when(scmManager.makeProviderScmRepository(siteDirectory)).thenReturn(Optional.of(scmRepository));
        Mockito.when(scmManager.getProviderByRepository(scmRepository)).thenReturn(scmProvider);
        Mockito.when(scmRepository.getProviderRepository()).thenReturn(scmProviderRepository);

        InfoItem infoItem = new InfoItem();
        OffsetDateTime modifiedDate = OffsetDateTime.of(2024, 6, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        infoItem.setLastChangedDateTime(modifiedDate);

        // equals not properly for any of the info(...) parameters, so use Mockito.any() for all parameters
        Mockito.when(scmProvider.info(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new InfoScmResult("", Collections.singletonList(infoItem)));

        contextCustomizer.customizeContext(context, docContext, siteContext);
        assertTrue(context.containsKey("scmModifiedDate"));
        assertEquals(Date.from(modifiedDate.toInstant()), context.get("scmModifiedDate"));
    }

    @Test
    void lastModifiedDateOutsideRepo() throws Exception {
        Mockito.when(scmManager.makeProviderScmRepository(siteDirectory)).thenReturn(Optional.empty());
        contextCustomizer.customizeContext(context, docContext, siteContext);
        assertFalse(context.containsKey("scmModifiedDate"));
    }

    @Test
    void lastModifiedDateUnknown() throws Exception {
        Mockito.when(scmManager.makeProviderScmRepository(siteDirectory)).thenReturn(Optional.of(scmRepository));
        Mockito.when(scmManager.getProviderByRepository(scmRepository)).thenReturn(scmProvider);
        Mockito.when(scmRepository.getProviderRepository()).thenReturn(scmProviderRepository);

        InfoItem infoItem = new InfoItem();

        // equals not properly for any of the info(...) parameters, so use Mockito.any() for all parameters
        Mockito.when(scmProvider.info(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new InfoScmResult("", Collections.singletonList(infoItem)));
        contextCustomizer.customizeContext(context, docContext, siteContext);
        assertFalse(context.containsKey("scmModifiedDate"));
    }

    @Test
    void lastModifiedDateNonExisting() throws Exception {
        docContext = new DocumentRenderingContext(siteDirectory, "markdown/non-existing.md", null);
        Mockito.verifyNoInteractions(scmManager);

        contextCustomizer.customizeContext(context, docContext, siteContext);
        assertFalse(context.containsKey("scmModifiedDate"));
    }
}
