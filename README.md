<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

# Apache Maven Doxia Sitetools

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.apache.maven.doxia/doxia-sitetools.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.apache.maven.doxia/doxia-sitetools)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/org/apache/maven/doxia/doxia-sitetools/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/org/apache/maven/doxia/doxia-sitetools/README.md)
[![Verify](https://github.com/apache/maven-doxia-sitetools/actions/workflows/maven-verify.yml/badge.svg)](https://github.com/apache/maven-doxia-sitetools/actions/workflows/maven-verify.yml)

Doxia Sitetools turns single documents into a website. [Doxia](https://maven.apache.org/doxia/) converts each page
to HTML; Sitetools adds the menus, breadcrumbs, header and footer described in `site.xml`, and a skin that gives the
pages their look. The [Maven Site Plugin](https://maven.apache.org/plugins/maven-site-plugin/) uses it for every
`mvn site` run.

- Documentation: <https://maven.apache.org/doxia/doxia-sitetools/>
- Issues: <https://github.com/apache/maven-doxia-sitetools/issues>
- Security reports: see [SECURITY.md](SECURITY.md); never a public issue
- Conventions for contributors and agents: [AGENTS.md](AGENTS.md)
- Questions and design discussion: the [dev mailing list](https://maven.apache.org/mailing-lists.html)

## Modules

| Module | What it does |
|---|---|
| `doxia-site-model` | The `site.xml` model and how a child descriptor inherits from its parent |
| `doxia-skin-model` | The `skin.xml` model that a skin declares |
| `doxia-integration-tools` | `SiteTool`, which finds, inherits and interpolates the site descriptor of a Maven project |
| `doxia-site-renderer` | Renders documents and reports into a skin |
| `doxia-site-scm-context` | Optional: adds the last change date and author of each page from the SCM |

The artifacts run on Java 8 or later. Building them needs Maven 3.9 or later.

## Build

```shell
mvn verify
```

The integration test of `doxia-site-scm-context` runs only with the `run-its` profile:

```shell
mvn -Prun-its verify
```

## Contributing

Open a pull request against `master` from your fork. The pull request title becomes the line in the release notes
and its label picks the section, so write the title for a user of the Maven Site Plugin. Format the code before
pushing:

```shell
mvn spotless:apply
```

See [Maven development](https://maven.apache.org/guides/development/guide-maven-development.html) for the general
process, and file an [Individual Contributor License Agreement](https://www.apache.org/licenses/contributor-agreements.html)
if you plan to contribute regularly.
