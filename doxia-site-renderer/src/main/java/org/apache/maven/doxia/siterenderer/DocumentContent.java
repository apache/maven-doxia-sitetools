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

import java.util.List;

/**
 * Document content, that will be merged into a site template.
 *
 * @since 1.8
 */
public interface DocumentContent
{
    /**
     * Get the title of the document.
     * @return the document title
     */
    String getTitle();

    /**
     * Get the date of the document.
     * @return the document date
     */
    String getDate();

    /**
     * Get the authors of the document.
     * @return the document authors
     */
    List<String> getAuthors();

    /**
     * Get the html head of the document.
     * @return the document html head
     */
    String getHead();

    /**
     * Get the html body of the document.
     * @return the document body head
     */
    String getBody();

    /**
     * Get the document rendering context.
     * @return the document rendering context
     */
    RenderingContext getRenderingContext();
}
