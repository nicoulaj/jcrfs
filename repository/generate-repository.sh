#!/bin/sh

REPO_ROOT=$PWD

git clone git://github.com/dtrott/fuse4j.git /tmp/fuse4j
mvn -f /tmp/fuse4j/maven/pom.xml package source:jar javadoc:jar deploy -DaltDeploymentRepository=project-local::default::file://$REPO_ROOT
