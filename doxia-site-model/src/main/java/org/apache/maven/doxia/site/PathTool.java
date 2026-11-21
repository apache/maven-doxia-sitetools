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

import java.io.File;
import java.util.StringTokenizer;

/**
 * Path calculations used by site descriptors and by the site renderer.
 *
 * <p>This is a trimmed copy of the methods used from {@code org.codehaus.plexus.util.PathTool}
 * (Apache License 2.0), kept here so that Doxia Sitetools no longer needs a dependency on
 * plexus-utils. The behavior is intentionally identical, including the handling of Windows
 * drive letters, trailing separators and the historical {@code /site/} and {@code /absolute/}
 * link prefixes.</p>
 */
public class PathTool {

    /**
     * Determines the relative path of a filename, using the separator found in the filename.
     *
     * @param filename the filename to be parsed
     * @return the relative path, not terminated with a separator
     */
    public static String getRelativePath(String filename) {
        filename = uppercaseDrive(filename);

        if (isEmpty(filename)) {
            return "";
        }

        String separator = determineSeparator(filename);
        filename = chompLast(filename, separator);
        if (!filename.startsWith(separator)) {
            filename = separator + filename;
        }

        return determineRelativePath(filename, separator);
    }

    /**
     * Determines the relative path of a filename with respect to a base directory.
     *
     * @param basedir the base directory
     * @param filename the filename that is relative to the base directory
     * @return the relative path, or an empty string when the filename is not below the base directory
     */
    public static String getRelativePath(String basedir, String filename) {
        basedir = uppercaseDrive(basedir);
        filename = uppercaseDrive(filename);

        if (isEmpty(basedir) || isEmpty(filename) || !filename.startsWith(basedir)) {
            return "";
        }

        String separator = determineSeparator(filename);
        basedir = chompLast(basedir, separator);
        filename = chompLast(filename, separator);

        String relativeFilename = filename.substring(basedir.length());

        return determineRelativePath(relativeFilename, separator);
    }

    /**
     * Determines the directory component of a filename.
     *
     * @param filename the filename to be parsed
     * @return the directory portion, or {@code .} when there is no directory component
     */
    public static String getDirectoryComponent(String filename) {
        if (isEmpty(filename)) {
            return "";
        }

        String separator = determineSeparator(filename);
        int index = filename.lastIndexOf(separator);
        String directory = index != -1 ? filename.substring(0, index) : filename;
        if (filename.equals(directory)) {
            return ".";
        }
        return directory;
    }

    /**
     * Calculates the appropriate link given the preferred link and the relative path of the document.
     *
     * @param link the preferred link
     * @param relativePath the relative path of the current document
     * @return the link relative to the current document
     */
    public static String calculateLink(String link, String relativePath) {
        if (link == null) {
            link = "";
        }
        if (relativePath == null) {
            relativePath = "";
        }

        // Historical site and absolute link prefixes.
        if (link.startsWith("/site/")) {
            return link.substring(5);
        }
        if (link.startsWith("/absolute/")) {
            return link.substring(10);
        }

        // Allows absolute links in nav-bars etc.
        if (link.contains(":")) {
            return link;
        }

        if (equals(relativePath, ".")) {
            if (link.startsWith("/")) {
                return link.substring(1);
            }
            return link;
        }

        // Avoid a double slash in "../" + "/page.html".
        if (relativePath.endsWith("/") && link.startsWith("/")) {
            return relativePath + "." + link.substring(1);
        }

        if (relativePath.endsWith("/") || link.startsWith("/")) {
            return relativePath + link;
        }

        return relativePath + "/" + link;
    }

    /**
     * Calculates the relative path between two paths on a web site.
     *
     * @param oldPath the path of the referring page
     * @param newPath the path of the target
     * @return a relative web path from {@code oldPath}
     */
    public static String getRelativeWebPath(String oldPath, String newPath) {
        if (isEmpty(oldPath) || isEmpty(newPath)) {
            return "";
        }

        String resultPath = buildRelativePath(newPath, oldPath, '/');

        if (newPath.endsWith("/") && !resultPath.endsWith("/")) {
            return resultPath + "/";
        }

        return resultPath;
    }

    /**
     * Calculates the relative path between two paths on the file system.
     *
     * @param oldPath the path of the referring file
     * @param newPath the path of the target
     * @return a relative file path from {@code oldPath}, or {@code null} when the paths are on different drives
     */
    public static String getRelativeFilePath(String oldPath, String newPath) {
        if (isEmpty(oldPath) || isEmpty(newPath)) {
            return "";
        }

        String fromPath = new File(oldPath).getPath();
        String toPath = new File(newPath).getPath();

        // strip any leading slashes if it is a Windows path
        if (toPath.matches("^\\[a-zA-Z]:")) {
            toPath = toPath.substring(1);
        }
        if (fromPath.matches("^\\[a-zA-Z]:")) {
            fromPath = fromPath.substring(1);
        }

        // lowercase Windows drive letters
        if (fromPath.startsWith(":", 1)) {
            fromPath = Character.toLowerCase(fromPath.charAt(0)) + fromPath.substring(1);
        }
        if (toPath.startsWith(":", 1)) {
            toPath = Character.toLowerCase(toPath.charAt(0)) + toPath.substring(1);
        }

        boolean fromHasDrive = fromPath.startsWith(":", 1);
        boolean toHasDrive = toPath.startsWith(":", 1);

        if ((toHasDrive && fromHasDrive) && !toPath.substring(0, 1).equals(fromPath.substring(0, 1))) {
            return null;
        }
        if (toHasDrive != fromHasDrive) {
            return null;
        }

        String resultPath = buildRelativePath(toPath, fromPath, File.separatorChar);

        if (newPath.endsWith(File.separator) && !resultPath.endsWith(File.separator)) {
            return resultPath + File.separator;
        }

        return resultPath;
    }

    /**
     * For each separator within the filename (except a leading one), appends a {@code ../}.
     */
    private static String determineRelativePath(String filename, String separator) {
        if (filename.isEmpty()) {
            return "";
        }

        int slashCount = countMatches(filename, separator.charAt(0)) - 1;
        if (slashCount <= 0) {
            return ".";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < slashCount; i++) {
            sb.append("../");
        }

        return chop(sb.toString());
    }

    /**
     * Returns the separator which occurs more often in the filename.
     */
    private static String determineSeparator(String filename) {
        int forwardCount = countMatches(filename, '/');
        int backwardCount = countMatches(filename, '\\');

        return forwardCount >= backwardCount ? "/" : "\\";
    }

    /**
     * Cygwin prefers lowercase drive letters, but other parts of Maven use uppercase.
     */
    private static String uppercaseDrive(String path) {
        if (path == null) {
            return null;
        }
        if (path.length() >= 2 && path.charAt(1) == ':') {
            path = Character.toUpperCase(path.charAt(0)) + path.substring(1);
        }
        return path;
    }

    private static String buildRelativePath(String toPath, String fromPath, char separatorChar) {
        String separator = String.valueOf(separatorChar);

        StringTokenizer toTokeniser = new StringTokenizer(toPath, separator);
        StringTokenizer fromTokeniser = new StringTokenizer(fromPath, separator);

        int count = 0;

        // Walk along the to path looking for divergence from the from path.
        while (toTokeniser.hasMoreTokens() && fromTokeniser.hasMoreTokens()) {
            if (separatorChar == '\\') {
                if (!fromTokeniser.nextToken().equalsIgnoreCase(toTokeniser.nextToken())) {
                    break;
                }
            } else if (!fromTokeniser.nextToken().equals(toTokeniser.nextToken())) {
                break;
            }
            count++;
        }

        toTokeniser = new StringTokenizer(toPath, separator);
        fromTokeniser = new StringTokenizer(fromPath, separator);

        while (count-- > 0) {
            fromTokeniser.nextToken();
            toTokeniser.nextToken();
        }

        StringBuilder relativePath = new StringBuilder();

        // Add back references for the rest of the from location.
        while (fromTokeniser.hasMoreTokens()) {
            fromTokeniser.nextToken();
            relativePath.append("..");
            if (fromTokeniser.hasMoreTokens()) {
                relativePath.append(separatorChar);
            }
        }

        if (relativePath.length() != 0 && toTokeniser.hasMoreTokens()) {
            relativePath.append(separatorChar);
        }

        // Add forward fills for whatever is left of the new path.
        while (toTokeniser.hasMoreTokens()) {
            relativePath.append(toTokeniser.nextToken());
            if (toTokeniser.hasMoreTokens()) {
                relativePath.append(separatorChar);
            }
        }

        return relativePath.toString();
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private static boolean equals(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private static String chompLast(String value, String suffix) {
        return value.endsWith(suffix) ? value.substring(0, value.length() - suffix.length()) : value;
    }

    private static String chop(String value) {
        return value.substring(0, value.length() - 1);
    }

    private static int countMatches(String value, char character) {
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == character) {
                count++;
            }
        }
        return count;
    }
}
