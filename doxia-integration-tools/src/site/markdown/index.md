---
title: Introduction
author: 
  - Dennis Lundberg
  - Hervé Boutemy
date: 2016-03-27
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

# Maven Doxia Integration Tools

This shared component has some utilities that are useful when integrating Doxia in Maven, mainly for site generation and report creation.

The main entry point is the [SiteTool](./apidocs/org/apache/maven/doxia/tools/SiteTool.html) Plexus component.

## Usage

Instructions on how to use the integration of Doxia in Maven can be found [here](./usage.html).

## `site.xml` Model Interpolation

Interpolation of [`site.xml` model](../doxia-site-model/site.html) injects Maven project&apos;s information, replacing `${...}` with calculated values like it happens in [Maven model interpolation](/ref/current/maven-model-builder/#Model_Interpolation).

Interpolation can be **late** or **early**:

- with **late** interpolation, replacement happens **after** inheritance. This is the classical behaviour in Maven pom,
- with **early** interpolation, replacement happens **before** inheritance: this was the default behaviour for `project.*` values until Doxia Sitetools 1\.7 \(used in [ Maven Site Plugin 3\.5](/plugins/maven-site-plugin/history.html)\), when these early and late interpolation definitions didn&apos;t exist. Since Doxia Sitetools 1\.7\.1 \(used in [ Maven Site Plugin 3\.5\.1](/plugins/maven-site-plugin/history.html)\), early interpolation happens for `this.*` values. Early interpolation doesn&apos;t support user and system properties.

Values are evaluated in sequence from different syntaxes:

|late value|early value|evaluation result|common examples|
|:---|:---|:---|:---|
|`project.*`|`this.*`|POM content \(see [POM reference](/ref/current/maven-model/maven.html)\)|`${project.version}` <br />`${this.url}`|
|`*`|`this.*`|model properties, such as project properties set in the pom|`${any.key}` <br />`${this.any.key}`|
|`env.*` <br />`*`||environment variables|`${env.PATH}`|

