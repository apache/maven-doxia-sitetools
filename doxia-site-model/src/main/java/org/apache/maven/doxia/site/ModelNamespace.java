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
package org.apache.maven.doxia.site;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks the namespace version of a site or skin descriptor against the model version this Doxia Sitetools reads.
 * The generated readers do not check it, so a descriptor written for a newer model fails with errors about unknown
 * elements instead of saying that the model is too new.
 *
 * @since 2.2.0
 */
public final class ModelNamespace {
    /** The newest site descriptor model this version reads, as in {@code http://maven.apache.org/SITE/2.2.0}. */
    public static final String SITE_MODEL_VERSION = "2.2.0";

    /** The newest skin descriptor model this version reads, as in {@code http://maven.apache.org/SKIN/2.1.0}. */
    public static final String SKIN_MODEL_VERSION = "2.1.0";

    private static final Pattern NAMESPACE_VERSION =
            Pattern.compile("xmlns\\s*=\\s*[\"']http://maven\\.apache\\.org/(SITE|SKIN)/([0-9][0-9.]*)[\"']");

    private ModelNamespace() {}

    /**
     * Returns the model version of the descriptor's {@code SITE} or {@code SKIN} namespace, if it declares one.
     *
     * @param descriptorContent the descriptor XML
     * @param model {@code "SITE"} or {@code "SKIN"}
     * @return the version, for example {@code "2.1.0"}, or {@code null} without such a namespace
     */
    public static String declaredVersion(String descriptorContent, String model) {
        Matcher matcher = NAMESPACE_VERSION.matcher(descriptorContent);
        while (matcher.find()) {
            if (matcher.group(1).equals(model)) {
                return matcher.group(2);
            }
        }
        return null;
    }

    /**
     * Returns whether the descriptor declares a namespace for a newer model than {@code supportedVersion}.
     *
     * @param descriptorContent the descriptor XML
     * @param model {@code "SITE"} or {@code "SKIN"}
     * @param supportedVersion the newest model version that can be read
     * @return {@code true} if the declared namespace version is newer than the supported one
     */
    public static boolean isNewerThan(String descriptorContent, String model, String supportedVersion) {
        String declared = declaredVersion(descriptorContent, model);
        return declared != null && compare(declared, supportedVersion) > 0;
    }

    private static int compare(String a, String b) {
        String[] left = a.split("\\.");
        String[] right = b.split("\\.");
        for (int i = 0; i < Math.max(left.length, right.length); i++) {
            int l = i < left.length && !left[i].isEmpty() ? Integer.parseInt(left[i]) : 0;
            int r = i < right.length && !right[i].isEmpty() ? Integer.parseInt(right[i]) : 0;
            if (l != r) {
                return Integer.compare(l, r);
            }
        }
        return 0;
    }
}
