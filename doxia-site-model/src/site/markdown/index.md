---
title: Doxia Sitetool Site Model
author: 
  - Hervé Boutemy
date: 2011-08-18
---

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Doxia Sitetool Site Model

This is strictly the model for Doxia Sitetool Site Model, used in maven-site-plugin as `site.xml` to inject parameters into site template: see [Doxia Site Renderer](../doxia-site-renderer/) for more details on site rendering.

The following are generated from this model:

- [Java sources](./apidocs/index.html) with Reader and Writers for the Xpp3 XML parser
- A [Descriptor Reference](./site.html)
- An XSD referenced in the [Descriptor Reference](./site.html).

## Inheritance

Site model can be merged from a parent `site.xml` into a child `site.xml` using `SiteModelInheritanceAssembler` ([javadoc](./apidocs/org/apache/maven/doxia/site/inheritance/SiteModelInheritanceAssembler.html)) with its `DefaultSiteModelInheritanceAssembler` implementation ([source](https://github.com/apache/maven-doxia-sitetools/blob/master/doxia-site-model/src/main/java/org/apache/maven/doxia/site/inheritance/DefaultSiteModelInheritanceAssembler.java)).

By default, the child's links and breadcrumbs are added to the parent's. Since site descriptor 2.2.0, `combine.links="override"` and `combine.breadcrumbs="override"` on the child's `<body>` keep only the child's own entries; for breadcrumbs, the entry generated for the child project is left out too, so an empty list stays empty:

```xml
<body combine.breadcrumbs="override">
  <breadcrumbs>
    <item name="My project" href="https://example.com/"/>
  </breadcrumbs>
</body>
```

`<site combine.self="override">` drops the whole parent descriptor.

## Model versions

A descriptor declares its model version in its namespace, for example `http://maven.apache.org/SITE/2.2.0`. Since 2.2.0, reading a site or skin descriptor that declares a newer model than this release supports logs a warning naming both versions: the descriptor's unknown elements would otherwise fail with less obvious parse errors. Older model versions are read without a warning.

## Interpolation

Interpolation is not done here: see [ Doxia Integration Tools](../doxia-integration-tools/) documentation for more details.

