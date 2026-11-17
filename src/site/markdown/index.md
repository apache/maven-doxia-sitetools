---
title: Doxia Sitetools
author: 
  - Hervé Boutemy
---

<!--
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
-->


# Doxia Sitetools

Doxia Sitetools turns single documents into a website. [Doxia](../doxia/) converts each page from Markdown, APT or
another format into HTML; Sitetools adds what a site needs around it: the menus, breadcrumbs, header and footer
described in `site.xml`, and a skin that gives the pages their look. The
[Maven Site Plugin](/plugins/maven-site-plugin/) uses Sitetools for every `mvn site` run.

If you build a site with Maven, the user guides are the place to start:

- [Creating a site](/guides/mini/guide-site.html)
- [Configuring the site descriptor](/guides/mini/guide-site-descriptor.html), `site.xml`
- [Building a site for a multi-module project](/guides/mini/guide-multimodule-site.html)
- [Choosing a skin](/skins/) or [creating your own](/plugins/maven-site-plugin/examples/creatingskins.html)

The pages here document the components themselves, for skin authors and for developers of plugins that render
documents.

| Module | What it does |
|---|---|
| [Site Renderer](doxia-site-renderer/index.html) | Renders documents and reports into a skin. Documents the Velocity variables available to pages and skins. |
| [Site Model](doxia-site-model/index.html) | The `site.xml` model, its [reference](doxia-site-model/site.html), and how a child descriptor inherits from its parent. |
| [Skin Model](doxia-skin-model/index.html) | The `skin.xml` model that a skin declares. |
| [Integration Tools](doxia-integration-tools/index.html) | `SiteTool`, which finds, inherits and interpolates the site descriptor of a Maven project. |
| [SCM Context](doxia-site-scm-context/index.html) | Optional: adds the last change date and author of each page from Git or another SCM. |

The following diagram shows how the modules depend on each other.

<img src="images/doxia-sitetools-deps.png" usemap="#Doxia_sitetools_dependencies" alt="Doxia Sitetools Dependencies"/>
<map name="Doxia_sitetools_dependencies" id="Doxia_sitetools_dependencies">
          <area shape="rect" coords="133,63,274,97"   href="doxia-site-renderer/index.html"       alt="Doxia Site Renderer"/>
          <area shape="rect" coords="2,119,162,153"   href="doxia-integration-tools/index.html"   alt="Doxia Integration Tools"/>
          <area shape="rect" coords="32,232,199,267"  href="doxia-site-model/index.html"          alt="Doxia Site Model"/>
          <area shape="rect" coords="207,232,324,267" href="doxia-skin-model/index.html"          alt="Doxia Skin Model"/>
          <area shape="rect" coords="288,123,444,159" href="https://codehaus-plexus.github.io/plexus-velocity/"        alt="Plexus Velocity"/>
          <area shape="rect" coords="318,180,414,214" href="https://velocity.apache.org/"                               alt="Velocity"/>
          <area shape="rect" coords="134,301,274,337" href="/doxia/doxia/doxia-modules/doxia-module-xhtml5/index.html" alt="Doxia Module XHTML5"/>
          <area shape="rect" coords="114,288,252,353" href="/doxia/doxia/doxia-modules/index.html" alt="Doxia Modules"/>
        </map> 

