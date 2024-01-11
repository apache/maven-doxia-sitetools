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

import java.util.Optional;

/**
 * Retrieves a {@link ParserConfiguration} for a particular Doxia parser id.
 * @since 2.0.0
 */
@FunctionalInterface
public interface ParserConfigurationRetriever {

    /**
     * Retrieves the parser configuration applicable for the given parser id.
     * @param parserId the id of the parser for which to retrieve the configuration
     * @return The applicable parser configuration, may be {@link Optional.empty} if no applicable configuration found
     */
    Optional<ParserConfiguration> retrieve(String parserId);
}
