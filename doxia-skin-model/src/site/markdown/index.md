---
title: Doxia Sitetools Skin Model
author: 
  - Hervé Boutemy
date: 2016-02-07
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

# Doxia Sitetools Skin Model

This is strictly the model for Doxia Sitetools Skin Model, used in skins in `META-INF/maven/skin.xml`.

The following are generated from this model:

- [Java sources](./apidocs/index.html) with Reader for the Xpp3 XML parser
- A [Descriptor Reference](./skin.html)
- An XSD referenced in the [Descriptor Reference](./skin.html).
## Doxia Sitetools Skin

A Doxia Sitetools skin must contain a Velocity template named `META-INF/maven/site.vm`: it will be called by [Site Renderer](../doxia-site-renderer/index.html) with additional variables about the rendered document as documented in the [Site Template section](../doxia-site-renderer/index.html#Site_Template), the main variable being `bodyContent`.

Maven team provides [a collection of skins](/skins/) for projects use.

Some documentation is available on [how to create a new skin](/plugins/maven-site-plugin/examples/creatingskins.html), by copying other skins to benefit from examples of breadcrumbs or menu generation from [site model](../doxia-site-model/index.html).

Since Doxia Sitetools 1\.7, a skin descriptor can be added in `META-INF/maven/skin.xml`.

