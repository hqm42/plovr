#!/bin/bash

echo updating all dependend repositories
cd ../plovr-main
git-hg pull

cd ../closure-compiler
git svn fetch

cd ../closure-templates
git svn fetch

cd ../closure-library
git svn fetch

echo Merging plovr upstream changes
cd ../plovr
git pull origin
git merge master-hg

echo updating closure versions


cp -ra ../closure-compiler/* closure/closure-compiler
cp -ra ../closure-library/* closure/closure-library
cp -ra ../closure-templates/* closure/closure-templates

echo Apply Plovr Patch to enable visibility
patch -p 1 < patches/0001-Patching-the-closure-compiler-to-be-compatible-wit-plovr.patch

git add closure
git rm -r --cached closure/closure-compiler/build
git rm -r --cached closure/closure-templates/build



