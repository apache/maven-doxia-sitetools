package org.apache.maven.doxia.site.decoration.inheritance;

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

import org.apache.maven.doxia.site.decoration.DecorationModel;

/**
 * Manage inheritance of the decoration model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public interface DecorationModelInheritanceAssembler
{
    /** Plexus lookup role. */
    String ROLE = DecorationModelInheritanceAssembler.class.getName();

    /**
     * Manage inheritance of the decoration model between a parent and child.
     *
     * @param name a name, used for breadcrumb.
     * @param child the child DecorationModel to be merged with parent.
     * @param parent the parent DecorationModel not null.
     * @param childBaseUrl the child base URL.
     * @param parentBaseUrl the parent base URL.
     */
    void assembleModelInheritance( String name, DecorationModel child, DecorationModel parent,
                                   String childBaseUrl, String parentBaseUrl );

    /**
     * Resolve relative paths for a DecorationModel given a base URL.
     *
     * @param decoration  the DecorationModel.
     * @param baseUrl the base URL.
     */
    void resolvePaths( DecorationModel decoration, String baseUrl );
}
