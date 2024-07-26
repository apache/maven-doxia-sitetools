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
package org.apache.maven.doxia.tools;

/**
 * An exception occurring during the execution of this tool.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public class SiteToolException extends Exception {
    /** serialVersionUID */
    static final long serialVersionUID = 2331441332996055959L;

    /**
     * Construct a new <code>SiteToolException</code> exception wrapping an underlying <code>Exception</code>
     * and providing a <code>message</code>.
     *
     * @param message could be null
     * @param cause could be null
     */
    public SiteToolException(String message, Exception cause) {
        super(message, cause);
    }

    /**
     * Construct a new <code>SiteToolException</code> exception wrapping an underlying <code>Throwable</code>
     * and providing a <code>message</code>.
     *
     * @param message could be null
     * @param cause could be null
     */
    public SiteToolException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a new <code>SiteToolException</code> exception providing a <code>message</code>.
     *
     * @param message could be null
     */
    public SiteToolException(String message) {
        super(message);
    }
}
