#!/bin/bash
#
# Use this script to update the specified Closure Tool. Usage:
#
# ./update-repository.sh closure-library

HG=${HG:-hg}
HGROOT=${HGROOT:-$(${HG} root)}

# Make sure that exactly one argument is specified.
EXPECTED_ARGS=1
if [ $# -ne $EXPECTED_ARGS ]; then
  echo "Must specify one of: closure-library, closure-compiler, closure-templates"
  exit 1
fi

# Make sure that the argument correctly identifies a repository.
REPOSITORY=$1
if [ ! -d "${HGROOT}/tools/imports/${REPOSITORY}" ]; then
  echo "No repository for ${REPOSITORY}"
  exit 1
fi

cd ${HGROOT}
tools/imports/closure-library/update.sh
hg commit -m "Pull latest changes from Closure Library SVN repository." \
    tools/imports/closure-library/shamap

# REV is something like 1648:af131e4e3231
REV=`hg branches | grep closure-library | awk '{print $2}'

# REV2 will be the part after the colon: af131e4e3231
REV2=`echo $REV | awk -F ":" '{print $2}'`

hg merge -r $REV2
hg commit -m "merge from closure-library branch"
