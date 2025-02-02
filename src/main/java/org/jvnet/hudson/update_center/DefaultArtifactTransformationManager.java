package org.jvnet.hudson.update_center;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.transform.ArtifactTransformation;
import org.apache.maven.artifact.transform.ArtifactTransformationManager;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import java.util.Collections;
import java.util.List;

/**
 * Patched to avoid the problem in the initialize method.
 */
public class DefaultArtifactTransformationManager
    implements ArtifactTransformationManager, Initializable
{
    private List<ArtifactTransformation> artifactTransformations;

	public void initialize() throws InitializationException {
		// TODO this is a hack until plexus can fix the ordering of the arrays
        Collections.sort(artifactTransformations, (t1, t2) -> {
        	return t1.getClass().getName().indexOf("Snapshot");
        });
	}

    public void transformForResolve( Artifact artifact, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
    	for ( ArtifactTransformation transform : artifactTransformations) { 
        	transform.transformForResolve( artifact, remoteRepositories, localRepository );
        }
    }

    public void transformForInstall( Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException
    {
    	for ( ArtifactTransformation transform : artifactTransformations) { 
        	transform.transformForInstall( artifact, localRepository );
        }
    }

    public void transformForDeployment( Artifact artifact, ArtifactRepository remoteRepository,
                                        ArtifactRepository localRepository )
        throws ArtifactDeploymentException
    {
        for ( ArtifactTransformation transform : artifactTransformations) { 
        	transform.transformForDeployment( artifact, remoteRepository, localRepository);
        }
    }

    public List<ArtifactTransformation> getArtifactTransformations() {
        return artifactTransformations;
    }
}
