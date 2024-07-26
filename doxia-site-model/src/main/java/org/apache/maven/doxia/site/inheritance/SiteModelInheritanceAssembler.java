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
package org.apache.maven.doxia.site.inheritance;

import org.apache.maven.doxia.site.SiteModel;

/**
 * Manage inheritance of the site model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public interface SiteModelInheritanceAssembler {
    /**
     * Manage inheritance of the site model between a parent and child.
     *
     * Any relative links in the parent model will be re-based to work from the merged child
     * model, otherwise no content from either the parent or child model should be modified.
     *
     * @param name a name, used for breadcrumb.
     *      If the parent model contains breadcrumbs and the child doesn't,
     *      a child breadcrumb will be added to the merged model with this name. Not null.
     * @param child the child SiteModel to be merged with parent.
     *      Not null. If parent == null, the child is unchanged, otherwise
     *      child will contain the merged model upon exit.
     * @param parent the parent SiteModel. Unchanged upon exit.
     *      May be null in which case the child is not changed.
     * @param childBaseUrl the child base URL.
     *      May be null, in which case relative links inherited from the parent
     *      will not be resolved in the merged child.
     * @param parentBaseUrl the parent base URL.
     *      May be null, in which case relative links inherited from the parent
     *      will not be resolved in the merged child.
     */
    void assembleModelInheritance(
            String name, SiteModel child, SiteModel parent, String childBaseUrl, String parentBaseUrl);

    /**
     * Resolve relative paths for a SiteModel given a base URL.
     *
     * Note that 'resolve' here means 'relativize' in the sense of
     * {@link java.net.URI#relativize(java.net.URI)}, ie if any link in the site model
     * has a base URL that is equal to the given baseUrl, it is replaced by a relative link
     * with respect to that base.
     *
     * @param siteModel the SiteModel.
     *      Not null.
     * @param baseUrl the base URL.
     *      May be null in which case the site model is unchanged.
     */
    void resolvePaths(SiteModel siteModel, String baseUrl);
}
