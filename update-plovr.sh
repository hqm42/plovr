#!/bin/bash

echo updating all dependend repositories
cd ../plovr-main
git-hg pull

rm ../plovr/changes.txt


cd ../closure-compiler
git svn fetch
echo "Closure Compiler" >> ../plovr/changes.txt
echo "-----------------------------------------------------------------------------" >> ../plovr/changes.txt
git log master..git-svn >> ../plovr/changes.txt
git rebase git-svn

echo "Closure Templates" >> ../plovr/changes.txt
echo "-----------------------------------------------------------------------------" >> ../plovr/changes.txt
cd ../closure-templates
git svn fetch
git log master..git-svn >> ../plovr/changes.txt
git rebase git-svn

echo "Closure Library" >> ../plovr/changes.txt
echo "-----------------------------------------------------------------------------" >> ../plovr/changes.txt
cd ../closure-library
git svn fetch
git log master..git-svn >> ../plovr/changes.txt
git rebase git-svn

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



