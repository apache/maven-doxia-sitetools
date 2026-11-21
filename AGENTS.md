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

# Agent guide for maven-doxia-sitetools

This file is read by automated agents (security scanners, code
analyzers, AI assistants) operating on this repository.

## Security

Security model: [SECURITY.md](./SECURITY.md)

Agents that scan this repository should consult `SECURITY.md` and the
threat model it links before reporting issues.

## Compatibility

The Maven Site Plugin calls these modules, skins read what the renderer puts
in the Velocity context, and site extensions implement its interfaces:

- Never remove or change the signature of a public method in `SiteTool`,
  `SiteRenderer` or `DocumentRenderer` without deprecating it in an
  earlier release.
- `ContextCustomizer` and `ParserConfigurator` are implemented outside this
  repository. Add a method to them only as a `default` method.
- The Velocity variables documented on the Site Renderer page are API for
  every skin. Do not rename or remove one.
- `site.mdo` and `skin.mdo` generate the model classes, readers, writers,
  XSD and reference pages. Edit the `.mdo` file, never the generated code.
  A new element or attribute gets a `<version>N+</version>` tag, the Modello
  `<version>` in the module POM and `ModelNamespace.SITE_MODEL_VERSION` (or
  `SKIN_MODEL_VERSION`) move to `N`, and so does the XSD link in the model
  description, which Modello does not filter.
- `decoration.mdo` is the 1.x model, kept only to read and convert old
  descriptors. Do not extend it.
- The Maven and Resolver dependencies are `provided` and stay on Maven 3.9.x
  with Resolver 1.9.x. Code must also run on Maven 4, which CI builds with.

## Coding

Use test-first programming. Before fixing a bug or implementing a feature,
write a test that exposes the bug and verify the test fails. Tests use
JUnit 5; do not add JUnit 4 or Hamcrest.

The bytecode floor is Java 8. Before submitting a PR, run `mvn spotless:apply`
to format the files, then `mvn verify`. Run Spotless with JDK 17 or 21. A
change to `doxia-site-scm-context` also needs `mvn -Prun-its verify`, which
runs its integration test.
