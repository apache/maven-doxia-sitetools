---
title: Usage
author: 
  - Dennis Lundberg
  - Vincent Siveton
date: 2008-04-27
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

# Using the SiteTool in a Mojo

`SiteTool` is a JSR-330 component. Inject it into your Mojo's constructor, then use it to read the site descriptor
of the current project with inheritance and interpolation applied, the same way the Maven Site Plugin does:

```java
import javax.inject.Inject;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.maven.doxia.site.SiteModel;
import org.apache.maven.doxia.tools.SiteTool;
import org.apache.maven.doxia.tools.SiteToolException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "your-own")
public class YourOwnMojo extends AbstractMojo {

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.basedir}/src/site", required = true)
    private File siteDirectory;

    @Parameter(property = "locales")
    private String locales;

    private final SiteTool siteTool;

    @Inject
    public YourOwnMojo(SiteTool siteTool) {
        this.siteTool = siteTool;
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            for (Locale locale : siteTool.getSiteLocales(locales)) {
                SiteModel siteModel = siteTool.getSiteModel(
                        siteDirectory,
                        locale,
                        session.getRequest(),
                        project,
                        session.getProjects(),
                        session.getRepositorySession(),
                        project.getRemoteProjectRepositories());
                getLog().info("Site " + siteModel.getName() + " for locale " + locale);
            }
        } catch (SiteToolException e) {
            throw new MojoExecutionException("Cannot read the site descriptor", e);
        }
    }
}
```

`getSiteLocales(null)` returns only `Locale.ROOT`, the default site locale. `getRelativePath(to, from)` computes a
relative link between two locations and treats `from` as a directory.

## References

- [SiteTool API](./apidocs/org/apache/maven/doxia/tools/SiteTool.html)
- [How the site descriptor is interpolated](./index.html#site.xml_Model_Interpolation)
