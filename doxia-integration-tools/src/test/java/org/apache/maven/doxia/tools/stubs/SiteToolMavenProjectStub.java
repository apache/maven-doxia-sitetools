package org.apache.maven.doxia.tools.stubs;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Build;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Site;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public class SiteToolMavenProjectStub
    extends MavenProjectStub
{
    private Build build;

    private File basedir;

    private DistributionManagement distributionManagement;

    private Properties properties;

    public SiteToolMavenProjectStub( String projectName )
    {
        basedir = new File( super.getBasedir() + "/src/test/resources/unit/" + projectName );

        Model model = null;

        try
        {
            model = new MavenXpp3Reader().read( new FileReader( new File( getBasedir(), "pom.xml" ) ) );
            setModel( model );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }

        setGroupId( model.getGroupId() );
        setArtifactId( model.getArtifactId() );
        setVersion( model.getVersion() );
        setName( model.getName() );
        setUrl( model.getUrl() );
        setPackaging( model.getPackaging() );
        setProperties( model.getProperties() );

        build = new Build();
        build.setFinalName( model.getArtifactId() );
        build.setDirectory( super.getBasedir() + "/target/test/unit/" + projectName + "/target" );
        build.setSourceDirectory( getBasedir() + "/src/main/java" );
        build.setOutputDirectory( build.getDirectory() + "/classes" );
        build.setTestSourceDirectory( getBasedir() + "/src/test/java" );
        build.setTestOutputDirectory( build.getDirectory() + "/test-classes" );

        List<String> compileSourceRoots = new ArrayList<String>();
        compileSourceRoots.add( getBasedir() + "/src/main/java" );
        setCompileSourceRoots( compileSourceRoots );

        List<String> testCompileSourceRoots = new ArrayList<String>();
        testCompileSourceRoots.add( getBasedir() + "/src/test/java" );
        setTestCompileSourceRoots( testCompileSourceRoots );
    }

    /** {@inheritDoc} */
    public Build getBuild()
    {
        return build;
    }

    /** {@inheritDoc} */
    public void setBuild( Build build )
    {
        this.build = build;
    }

    /** {@inheritDoc} */
    public File getBasedir()
    {
        return basedir;
    }

    /** {@inheritDoc} */
    public void setBasedir( File basedir )
    {
        this.basedir = basedir;
    }

    /** {@inheritDoc} */
    public List<ArtifactRepository> getRemoteArtifactRepositories()
    {
        ArtifactRepository repository = new DefaultArtifactRepository( "central", "https://repo1.maven.org/maven2",
                                                                       new DefaultRepositoryLayout() );

        return Collections.singletonList( repository );
    }

    /** {@inheritDoc} */
    public Properties getProperties()
    {
        return properties;
    }

    /** {@inheritDoc} */
    public void setProperties( Properties properties )
    {
        this.properties = properties;
    }

    public void setDistgributionManagementSiteUrl( String url )
    {
        Site site = new Site();
        site.setUrl( url );
        distributionManagement = new DistributionManagement();
        distributionManagement.setSite( site );
    }

    /** {@inheritDoc} */
    public DistributionManagement getDistributionManagement()
    {
        return distributionManagement;
    }
}
