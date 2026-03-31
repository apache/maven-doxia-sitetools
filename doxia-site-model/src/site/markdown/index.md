---
title: Doxia Sitetool Site Model
author: 
  - Hervé Boutemy
date: 2011-08-18
---

<!-- Licensed to the Apache Software Foundation (ASF) under one-->
<!-- or more contributor license agreements.  See the NOTICE file-->
<!-- distributed with this work for additional information-->
<!-- regarding copyright ownership.  The ASF licenses this file-->
<!-- to you under the Apache License, Version 2.0 (the-->
<!-- "License"); you may not use this file except in compliance-->
<!-- with the License.  You may obtain a copy of the License at-->
<!---->
<!--   http://www.apache.org/licenses/LICENSE-2.0-->
<!---->
<!-- Unless required by applicable law or agreed to in writing,-->
<!-- software distributed under the License is distributed on an-->
<!-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY-->
<!-- KIND, either express or implied.  See the License for the-->
<!-- specific language governing permissions and limitations-->
<!-- under the License.-->

# Doxia Sitetool Site Model

This is strictly the model for Doxia Sitetool Site Model, used in maven-site-plugin as `site.xml` to inject parameters into site template: see [Doxia Site Renderer](../doxia-site-renderer/) for more details on site rendering.

The following are generated from this model:

- [Java sources](./apidocs/index.html) with Reader and Writers for the Xpp3 XML parser
- A [Descriptor Reference](./site.html)
- An XSD referenced in the [Descriptor Reference](./site.html).

## Inheritance

Site model can be merged from a parent `site.xml` into a child `site.xml` using `SiteModelInheritanceAssembler` \([javadoc](./apidocs/org/apache/maven/doxia/site/inheritance/SiteModelInheritanceAssembler.html)\) with its `DefaultSiteModelInheritanceAssembler` implementation \([source](./xref/org/apache/maven/doxia/site/inheritance/DefaultSiteModelInheritanceAssembler.html)\).

## Interpolation

Interpolation is not done here: see [ Doxia Integration Tools](../doxia-integration-tools/) documentation for more details.

