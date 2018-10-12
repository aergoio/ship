#!/bin/bash
# resolve links - $0 may be a softlink
if [ -z "$PROJECT_HOME" ];then
  PRG="$0"
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG=`dirname "$PRG"`/"$link"
    fi
  done
  
  cd $(dirname $PRG)
  export PROJECT_HOME=`pwd`
  cd -&>/dev/null
fi

################################################################################
# Definition
VERSION=$(grep ' version ' $PROJECT_HOME/build.gradle | cut -d"'" -f2)

export BUILD_WORKSPACE=$PROJECT_HOME/build

if [ -z "$VERSION" ]; then
  echo "The version not detected. Check build script or environment."
  exit 1
fi

function print-usage() {
  echo "build.sh [command]"
  echo "Commands: "
  echo "  clean       delete and reset intermediate obj in build"
  echo "  npm         run npm for ship web ui"
  echo "  gradle      compile source to executable using gradle"
  echo "  test        test built executable"
  echo "  docs        generate documents"
  echo "  assemble    assemble distributions"
}

function clean-workspace() {
  rm -rf $BUILD_WORKSPACE
  $PROJECT_HOME/gradlew clean
}

function install-deps() {
  WORKSPACE="$BUILD_WORKSPACE/deps"
  rm -rf $WORKSPACE
  mkdir -p $WORKSPACE
  cd $WORKSPACE
  git clone --recurse-submodule https://github.com/aergoio/heraj.git
  cd heraj
  ./gradlew install
}

function execute-npm() {
  cd $PROJECT_HOME/web
  npm install
  npm run build
  PUBLIC_DIR="$PROJECT_HOME/core/src/main/resources/public"
  rm -rf $PUBLIC_DIR
  mkdir -p $PUBLIC_DIR
  cp -r $PROJECT_HOME/web/dist/* $PUBLIC_DIR
  cd -
}
function execute-gradle() {
  $PROJECT_HOME/gradlew clean build test alljacoco
}
function execute-test() {
  $PROJECT_HOME/gradlew test jacocoTestReport coveralls
}
function execute-documentation() {
  gem install bundler
  bundle install --gemfile $PROJECT_HOME/assembly/doc/gh-pages/Gemfile
  git clone $(git remote get-url origin) $BUILD_WORKSPACE/ship-doc -b gh-pages
  git -C $BUILD_WORKSPACE/ship-doc config user.email $(git config user.email)
  git -C $BUILD_WORKSPACE/ship-doc config user.name $(git config user.name)
  
  jekyll build -s $PROJECT_HOME/assembly/doc/gh-pages -d $BUILD_WORKSPACE/ship-doc
  $PROJECT_HOME/gradlew build
  $PROJECT_HOME/gradlew alljavadoc alljacoco
  rm -rf $BUILD_WORKSPACE/ship-doc/javadoc $BUILD_WORKSPACE/ship-doc/coverage
  mv $BUILD_WORKSPACE/docs/javadoc $BUILD_WORKSPACE/ship-doc/javadoc
  mv $BUILD_WORKSPACE/reports/jacoco/alljacoco/html $BUILD_WORKSPACE/ship-doc/coverage
}

function execute-assemble() {
  rm -rf $PROJECT_HOME/assembly/build/distributions
  $PROJECT_HOME/gradlew --stop assemble
  $PROJECT_HOME/gradlew --stop assemble && \
    (cd $PROJECT_HOME/assembly/build/distributions && tar -xvf *.tar && cd -)
}


if [ 0 == $# ]; then
  clean-workspace
  execute-gradle
else
  while (( $# )); do
    case $1 in
      "clean")
        clean-workspace
        ;;
      "deps")
        install-deps
        ;;
      "npm")
        execute-npm
        ;;
      "gradle")
        execute-gradle
        ;;
      "test")
        execute-test
        ;;
      "docs")
        execute-documentation
        ;;
      "assemble")
        execute-assemble
        ;;
      *)
        print-usage
        ;;
    esac
    [[ $? != 0 ]] && exit 1
    shift
  done
fi

