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

<!-- MACRO{toc} -->

# Site (Decoration) Descriptor 1.x

[Site Descriptor 1.x][site-descriptor-1.x] also referred to as *Site Decoration descriptor* was the format used with Doxia Sitetools 1.x for `site.xml`. Its most recent version was 1.8.1 but all 1.x versions evolved in a backwards compatible way (i.e. elements/attributes/semantics introduced in earlier version have not changed in later 1.x versions).

# Site Descriptor 2.x

With Doxia Sitetools 2.0 a new [Site Descriptor 2.0][site-descriptor-2.x] was introduced which differs in quite some aspects from the older format and is not backwards compatible. The main differences are:

1. A new root element named `site`
2. Use of XML attributes instead of XML element content for most elements
3. Consolidation of image elements

Not all elements/attributes from Site Descriptor 1.x do still exist in 2.x.
Further details in [GH issue 263](https://github.com/apache/maven-doxia-sitetools/issues/263).

Although Site Descriptor 1.x is still supported, it will lead to a deprecation warning during build and doesn't allow to use newer features introduced with Doxia Sitetools 2.x, therefore migration to 2.x is strongly recommended.

# How to migrate 

One should migrate to the most recent 2.x version, which for Doxia Sitetools 2.1.0 is Site Descriptor 2.1.0.

1. Convert root element from `<project ...>` to `<site ...>`
2. Convert default namespace to `xmlns="http://maven.apache.org/SITE/2.1.0"`
3. Optionally convert schema location to `xsi:schemaLocation="http://maven.apache.org/SITE/2.1.0 https://maven.apache.org/xsd/site-2.1.0.xsd"`
4. Convert XML values to attributes where appropriate. For details refer to [Site Descriptor 1.x][site-descriptor-1.x] and [Site Descriptor 2.x][site-descriptor-2.x].
5. Reference a [Skin compatible with descriptor 2.0](https://maven.apache.org/skins/index.html) (e.g. [`maven-fluido-skin 2.x`][maven-fluido-skin]).

## Differences in Skins

Some elements/attributes are interpreted differently in Skins upgraded to Doxia Site Descriptor 2.0.0. For `maven-fluido-skin` 2.x this is `name` in `bannerLeft` or `bannerRight`. Instead of being exposed as `img alt` text this is now always exposed next to the image [GH issue #216](https://github.com/apache/maven-fluido-skin/issues/216).

[site-descriptor-1.x]: decoration.html
[site-descriptor-2.x]: site.html
[maven-fluido-skin]: https://maven.apache.org/skins/maven-fluido-skin/
