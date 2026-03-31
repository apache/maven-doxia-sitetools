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
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.apache.maven.doxia.siterenderer.ContextCustomizer;
import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.velocity.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ContextCustomizer} that adds SCM attributes to the Velocity context for use in templates.
 * It looks for an SCM repository in the directory of the site being rendered, and if found, retrieves SCM info for the file being rendered and adds it to the Velocity context.
 *
 * The SCM repository is lazily retrieved and cached in the SiteRenderingContext attributes for subsequent retrievals, so that it is only looked up once per site rendering.
 *
 * The SCM info is retrieved for each document being rendered, but only if an SCM repository was found for the site.
 *
 * The following attributes are added to the Velocity context:
 * <ul>
 * <li>{@value #ATTRIBUTE_NAME_SCM_MODIFIED_DATE}: the last modification date of the file being rendered according to SCM, as a {@link java.util.Date} (if available)</li>
 * <li>{@value #ATTRIBUTE_NAME_SCM_MODIFIED_AUTHOR}: the author of the last modification of the file being rendered according to SCM, as a {@link String} (if available)</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Singleton
@Named("scmAttributes")
public class ScmAttributesContextCustomizer implements ContextCustomizer {

    private static final String ATTRIBUTE_NAME_SCM_MODIFIED_AUTHOR = "scmModifiedAuthor";

    private static final String ATTRIBUTE_NAME_SCM_MODIFIED_DATE = "scmModifiedDate";

    private static final String KEY_SCM_REPOSITORY = "org.apache.maven.doxia.scm.siterenderer.scmRepository";

    private static final Logger LOGGER = LoggerFactory.getLogger(ScmAttributesContextCustomizer.class);

    private final ScmManager scmManager;

    @Inject
    ScmAttributesContextCustomizer(ScmManager scmManager) {
        this.scmManager = scmManager;
    }

    /**
     * Lazily retrieves the SCM repository for the site being rendered, caching it in the SiteRenderingContext attributes for subsequent retrievals.
     * @param siteRenderingContext
     * @return an Optional containing the SCM repository if found, or an empty Optional if not found or if an error occurs while trying to find it.
     */
    private Optional<ScmRepository> getScmRepository(SiteRenderingContext siteRenderingContext) {
        if (siteRenderingContext.getAttributes().containsKey(KEY_SCM_REPOSITORY)) {
            return Optional.ofNullable(
                    (ScmRepository) siteRenderingContext.getAttributes().get(KEY_SCM_REPOSITORY));
        } else {
            Optional<ScmRepository> scmRepository =
                    getScmRepository(scmManager, siteRenderingContext.getRootDirectory());
            siteRenderingContext.putAttribute(KEY_SCM_REPOSITORY, scmRepository.orElse(null));
            return scmRepository;
        }
    }

    static Optional<ScmRepository> getScmRepository(ScmManager scmManager, File directory) {
        Optional<ScmRepository> scmRepository = scmManager.makeProviderScmRepository(directory);
        if (scmRepository.isPresent()) {
            LOGGER.debug("Found SCM repository for directory \"{}\"", directory);
        } else {
            LOGGER.debug("No SCM repository found for directory {}", directory);
            File parentDirectory = directory.getParentFile();
            if (parentDirectory != null) {
                return getScmRepository(scmManager, parentDirectory);
            }
        }
        return scmRepository;
    }

    static InfoItem getScmInfo(ScmManager scmManager, ScmRepository scmRepository, File file) {
        try {
            ScmFileSet fileSet = new ScmFileSet(file.getParentFile(), Collections.singletonList(file));
            InfoScmResult infos = scmManager
                    .getProviderByRepository(scmRepository)
                    .info(scmRepository.getProviderRepository(), fileSet, null);
            if (infos != null && infos.isSuccess() && !infos.getInfoItems().isEmpty()) {
                return infos.getInfoItems().get(0);
            } else {
                LOGGER.warn("Failed to get SCM info for file \"{}\": {}", file, infos);
            }
        } catch (ScmException e) {
            LOGGER.warn("Failed to get SCM info for file \"{}\"", file, e);
        }
        return null;
    }

    @Override
    public void customizeContext(
            Context context, DocumentRenderingContext docRenderingContext, SiteRenderingContext siteRenderingContext) {
        File inputFile = new File(docRenderingContext.getBasedir(), docRenderingContext.getInputPath());
        if (!inputFile.exists()) {
            LOGGER.debug("Input file \"{}\" does not exist, cannot retrieve SCM info", inputFile);
            return;
        }
        Optional<ScmRepository> scmRepository = getScmRepository(siteRenderingContext);

        final InfoItem scmInfo;
        if (scmRepository.isPresent()) {
            scmInfo = getScmInfo(scmManager, scmRepository.get(), inputFile);
        } else {
            scmInfo = null;
        }

        if (scmInfo != null) {
            if (scmInfo.getLastChangedDateTime() != null) {
                // Velocity can only deal with Date/Calendar
                Date scmModifiedDate =
                        Date.from(scmInfo.getLastChangedDateTime().toInstant());
                context.put(ATTRIBUTE_NAME_SCM_MODIFIED_DATE, scmModifiedDate);
            } else {
                LOGGER.warn(
                        "SCM info for file \"{}\" does not contain last modification date, maybe not yet committed or not existing?",
                        inputFile);
            }
            if (scmInfo.getLastChangedAuthor() != null) {
                context.put(ATTRIBUTE_NAME_SCM_MODIFIED_AUTHOR, scmInfo.getLastChangedAuthor());
            } else {
                LOGGER.warn(
                        "SCM info for file \"{}\" does not contain last author, maybe not yet committed or not existing?",
                        inputFile);
            }
        } else {
            LOGGER.debug("No SCM info available for file \"{}\"", inputFile);
        }
    }
}
