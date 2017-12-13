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

/**
 * <p>RendererException class.</p>
 *
 * @author Emmanuel Venisse
 */
public class RendererException
    extends Exception
{

    private static final long serialVersionUID = 3141592653589793238L;

    /**
     * Construct a RendererException with a message.
     *
     * @param message a custom message.
     */
    public RendererException( String message )
    {
        super( message );
    }

    /**
     * Construct a RendererException with a message and a cause.
     *
     * @param message a custom message.
     * @param t the cause.
     */
    public RendererException( String message, Throwable t )
    {
        super( message, t );
    }
}
