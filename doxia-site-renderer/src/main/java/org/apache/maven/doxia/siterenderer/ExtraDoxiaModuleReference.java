package org.apache.maven.doxia.siterenderer;

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

import java.io.File;

/**
 * Holds an extra Doxia source module reference in the list of added modules to the site rendering context.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
class ExtraDoxiaModuleReference
{
    private final String parserId;

    private final File basedir;

    ExtraDoxiaModuleReference( String parserId, File basedir )
    {
        this.parserId = parserId;
        this.basedir = basedir;
    }

    /**
     * <p>Getter for the field <code>parserId</code>.</p>
     *
     * @return Doxia parser id associated to this source module.
     */
    String getParserId()
    {
        return parserId;
    }

    /**
     * <p>Getter for the field <code>basedir</code>.</p>
     *
     * @return The base directory for module's source files.
     */
    File getBasedir()
    {
        return basedir;
    }
}
