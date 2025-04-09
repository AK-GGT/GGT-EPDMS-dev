#!/bin/bash
set -o errexit
set -o pipefail
set -o nounset

git checkout tags/$1

mvn clean
cd Doc
mvn generate-resources pre-site 
cd ..
mvn clean package -DskipTests -DskipITs

rm -Rf target

mkdir target
mkdir target/bin
cp Registry/target/Registry.war target/bin
cp Node/target/Node.war target/bin
cp -R Doc/target target/doc
rm -Rf target/doc/tmp
cp Doc/src/RELEASE_NOTES.md target/
cp CONTRIBUTORS.txt target/
cp LICENSE.txt target/ 

mkdir target/src
mkdir target/src/Commons
mkdir target/src/Doc
mkdir target/src/Node
mkdir target/src/Registry
mkdir target/src/Registry-api

cp -R Commons/src target/src/Commons/
cp -R Commons/pom.xml target/src/Commons/
cp -R Doc/src target/src/Doc/
cp -R Doc/pom.xml target/src/Doc/
cp -R Registry/src target/src/Registry/
cp -R Registry/pom.xml target/src/Registry/
cp -R Registry-api/src target/src/Registry-api/
cp -R Registry-api/pom.xml target/src/Registry-api/
cp -R Node/src target/src/Node/
cp -R Node/pom.xml target/src/Node/
cp pom.xml target/src/

VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep '^[0-9]\+\.'`

rm -Rf soda4LCA_$VERSION.zip
rm -Rf soda4LCA_$VERSION

mv target soda4LCA_$VERSION

zip -q -9 -r soda4LCA_$VERSION{.zip,}
