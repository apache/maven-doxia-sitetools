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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolInfo;
import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.config.DefaultKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Builds the Velocity Tools context of the site renderer without Velocity Tools' {@code ToolManager} and
 * configuration classes, which require Commons BeanUtils and Commons Digester.
 * <p>
 * Tools are still instances of Velocity Tools classes, created lazily by {@link Toolbox}. Custom tools are read
 * from {@code META-INF/maven/site-tools.xml} in the format of Velocity Tools' {@code XmlFactoryConfiguration},
 * with these differences: all tools share one toolbox per document (as all toolboxes were already created per
 * document), {@code restrictTo} is ignored, and values of type {@code field} or a custom type are kept as strings.
 * Property precedence follows Velocity Tools: tool properties beat toolbox properties, which beat global ones.
 */
final class SiteVelocityTools {
    private static final Logger LOGGER = LoggerFactory.getLogger(SiteVelocityTools.class);

    private final Map<String, SiteToolInfo> tools = new LinkedHashMap<>();

    private final Map<String, Object> data = new LinkedHashMap<>();

    private final Map<String, Map<String, Object>> toolboxProperties = new LinkedHashMap<>();

    private final Map<String, Object> globalProperties = new LinkedHashMap<>();

    /**
     * Adds or replaces a tool. A tool with the key of an existing one keeps that tool's properties unless
     * overridden, as in Velocity Tools.
     */
    SiteVelocityTools tool(String scope, Class<?> clazz, Object... keysAndValues) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            properties.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        addTool(scope, defaultKey(clazz), clazz, properties);
        return this;
    }

    /**
     * Sets a property of every tool in the given scope, with lower precedence than the tools' own properties.
     */
    SiteVelocityTools toolboxProperty(String scope, String name, Object value) {
        toolboxProperties.computeIfAbsent(scope, s -> new LinkedHashMap<>()).put(name, value);
        return this;
    }

    /**
     * Sets a property of every tool, with lower precedence than tool and toolbox properties.
     */
    SiteVelocityTools globalProperty(String name, Object value) {
        globalProperties.put(name, value);
        return this;
    }

    /**
     * Reads every resource with the given name from the thread context class loader and this class' loader,
     * later ones overriding earlier ones.
     */
    SiteVelocityTools readAll(String resourceName) {
        Set<String> seen = new LinkedHashSet<>();
        for (ClassLoader loader : classLoaders()) {
            try {
                Enumeration<URL> resources = loader.getResources(resourceName);
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    if (seen.add(url.toString())) {
                        read(url);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot look up " + resourceName, e);
            }
        }
        return this;
    }

    /**
     * Reads one Velocity Tools XML configuration.
     */
    SiteVelocityTools read(URL url) {
        Element root;
        try (InputStream in = url.openStream()) {
            root = newDocumentBuilder().parse(in, url.toExternalForm()).getDocumentElement();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read Velocity tools configuration " + url, e);
        }
        if (!"tools".equals(root.getNodeName())) {
            throw new IllegalStateException(
                    "Root element of Velocity tools configuration " + url + " must be <tools>: " + root.getNodeName());
        }

        globalProperties.putAll(attributes(root));
        for (Element child : children(root)) {
            switch (child.getNodeName()) {
                case "property":
                    globalProperties.put(child.getAttribute("name"), value(child));
                    break;
                case "data":
                    data.put(child.getAttribute("key"), value(child));
                    break;
                case "toolbox":
                    readToolbox(child, url);
                    break;
                default:
                    LOGGER.warn("Ignoring unknown element <{}> in {}", child.getNodeName(), url);
            }
        }
        return this;
    }

    /**
     * Creates the context. Tools are instantiated on first use and cached in the context.
     */
    ToolContext createContext() {
        Map<String, ToolInfo> infos = new LinkedHashMap<>();
        for (SiteToolInfo info : tools.values()) {
            info.addProperties(toolboxProperties.getOrDefault(info.scope, Collections.emptyMap()));
            info.addProperties(globalProperties);
            infos.put(info.getKey(), info);
        }

        ToolContext context = new ToolContext();
        context.addToolbox(new Toolbox(infos));
        data.forEach(context::put);
        return context;
    }

    private void readToolbox(Element toolbox, URL url) {
        String scope = toolbox.hasAttribute("scope") ? toolbox.getAttribute("scope") : Scope.REQUEST;
        Map<String, Object> properties = toolboxProperties.computeIfAbsent(scope, s -> new LinkedHashMap<>());
        properties.putAll(attributes(toolbox));
        properties.put("scope", scope);

        for (Element child : children(toolbox)) {
            switch (child.getNodeName()) {
                case "property":
                    properties.put(child.getAttribute("name"), value(child));
                    break;
                case "tool":
                    readTool(scope, child, url);
                    break;
                default:
                    LOGGER.warn("Ignoring unknown element <toolbox><{}> in {}", child.getNodeName(), url);
            }
        }
    }

    private void readTool(String scope, Element tool, URL url) {
        Map<String, Object> properties = attributes(tool);
        properties.remove("class");
        for (Element child : children(tool)) {
            if ("property".equals(child.getNodeName())) {
                properties.put(child.getAttribute("name"), value(child));
            } else {
                LOGGER.warn("Ignoring unknown element <tool><{}> in {}", child.getNodeName(), url);
            }
        }
        if (tool.hasAttribute("restrictTo")) {
            LOGGER.debug("Ignoring restrictTo of tool {} in {}", tool.getAttribute("key"), url);
        }

        String className = tool.getAttribute("class");
        if (className.isEmpty()) {
            String key = tool.getAttribute("key");
            SiteToolInfo existing = tools.get(key);
            if (existing == null) {
                throw new IllegalStateException("Tool '" + key + "' in " + url + " has no class and no default");
            }
            properties.forEach(existing::putProperty);
            return;
        }

        Class<?> clazz = loadClass(className, url);
        String key = tool.hasAttribute("key") ? tool.getAttribute("key") : defaultKey(clazz);
        addTool(scope, key, clazz, properties);
    }

    private void addTool(String scope, String key, Class<?> clazz, Map<String, Object> properties) {
        SiteToolInfo info = new SiteToolInfo(key, clazz, scope);
        SiteToolInfo existing = tools.get(key);
        if (existing != null) {
            existing.getProperties().forEach(info::putProperty);
        }
        properties.forEach(info::putProperty);
        Object skipSetters = info.getProperties().get("skipSetters");
        if (skipSetters != null) {
            info.setSkipSetters(Boolean.parseBoolean(skipSetters.toString()));
        }
        tools.put(key, info);
    }

    /**
     * Same as Velocity Tools: {@link DefaultKey} or the class' simple name without "Tool", decapitalized.
     */
    static String defaultKey(Class<?> clazz) {
        DefaultKey defaultKey = clazz.getAnnotation(DefaultKey.class);
        if (defaultKey != null) {
            return defaultKey.value();
        }
        String name = clazz.getSimpleName();
        if (name.endsWith("Tool") && name.length() > 4) {
            name = name.substring(0, name.length() - 4);
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private static Class<?> loadClass(String className, URL url) {
        for (ClassLoader loader : classLoaders()) {
            Class<?> clazz;
            try {
                clazz = loader.loadClass(className);
            } catch (ClassNotFoundException e) {
                continue;
            }
            try {
                clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(
                        "Tool class " + className + " configured in " + url + " has no public no-arg constructor", e);
            }
            return clazz;
        }
        throw new IllegalStateException("Cannot load tool class " + className + " configured in " + url);
    }

    private static List<ClassLoader> classLoaders() {
        List<ClassLoader> loaders = new ArrayList<>(2);
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        if (context != null) {
            loaders.add(context);
        }
        ClassLoader own = SiteVelocityTools.class.getClassLoader();
        if (own != context) {
            loaders.add(own);
        }
        return loaders;
    }

    private static DocumentBuilder newDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }

    private static List<Element> children(Element parent) {
        List<Element> elements = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) nodes.item(i));
            }
        }
        return elements;
    }

    private static Map<String, Object> attributes(Element element) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        NamedNodeMap nodes = element.getAttributes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node attribute = nodes.item(i);
            attributes.put(attribute.getNodeName(), convert(attribute.getNodeValue(), "auto"));
        }
        return attributes;
    }

    /**
     * Value of a {@code <property>} or {@code <data>} element, from its {@code value} attribute or body.
     */
    private static Object value(Element element) {
        String value = element.hasAttribute("value")
                ? element.getAttribute("value")
                : element.getTextContent().trim();
        String type = element.hasAttribute("type") ? element.getAttribute("type") : "auto";
        return convert(value, type);
    }

    /**
     * The conversions of Velocity Tools' {@code Data}, except types {@code field} and custom converters.
     */
    static Object convert(String value, String type) {
        switch (type) {
            case "string":
                return value;
            case "boolean":
                return toBoolean(value);
            case "number":
                return toNumber(value);
            case "list":
            case "list.string":
            case "list.boolean":
            case "list.number":
                List<Object> list = new ArrayList<>();
                for (String item : value.split(",")) {
                    String trimmed = item.trim();
                    list.add(
                            type.equals("list.boolean")
                                    ? toBoolean(trimmed)
                                    : type.equals("list.number") ? toNumber(trimmed) : trimmed);
                }
                return list;
            case "auto":
                if (value.matches("(?i)true|false|yes|no|y|n|on|off")) {
                    return toBoolean(value);
                }
                if (value.matches("-?[0-9]+(\\.[0-9]+)?")) {
                    return toNumber(value);
                }
                return value;
            default:
                LOGGER.warn("Unsupported Velocity tools value type '{}', keeping '{}' as a string", type, value);
                return value;
        }
    }

    private static Boolean toBoolean(String value) {
        return value.matches("(?i)true|yes|y|on|1");
    }

    private static Number toNumber(String value) {
        if (value.contains(".")) {
            return Double.valueOf(value);
        }
        long number;
        try {
            number = Long.parseLong(value);
        } catch (NumberFormatException e) {
            return Double.valueOf(value);
        }
        if (number == (int) number) {
            return (int) number;
        }
        return number;
    }

    /**
     * A {@link ToolInfo} which calls setters by plain reflection instead of Commons BeanUtils' {@code PropertyUtils}.
     * Like {@code PropertyUtils.setProperty}, it does not convert values and skips properties without a matching
     * setter.
     */
    static final class SiteToolInfo extends ToolInfo {
        private static final long serialVersionUID = 1L;

        private final String scope;

        SiteToolInfo(String key, Class<?> clazz, String scope) {
            super(key, clazz);
            this.scope = scope;
        }

        @Override
        protected void setProperty(Object tool, String name, Object value) throws Exception {
            if (name == null || name.isEmpty() || value == null) {
                return;
            }
            String setter = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            for (Method method : tool.getClass().getMethods()) {
                if (method.getName().equals(setter)
                        && method.getParameterCount() == 1
                        && wrap(method.getParameterTypes()[0]).isInstance(value)) {
                    method.invoke(tool, value);
                    return;
                }
            }
        }

        private static Class<?> wrap(Class<?> type) {
            if (!type.isPrimitive()) {
                return type;
            }
            if (type == int.class) {
                return Integer.class;
            }
            if (type == boolean.class) {
                return Boolean.class;
            }
            if (type == long.class) {
                return Long.class;
            }
            if (type == double.class) {
                return Double.class;
            }
            if (type == float.class) {
                return Float.class;
            }
            if (type == short.class) {
                return Short.class;
            }
            if (type == byte.class) {
                return Byte.class;
            }
            return Character.class;
        }
    }
}
