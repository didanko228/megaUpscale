#!/bin/bash
# read property from gradle.properties
read_property() {
    grep "^$1" gradle.properties | cut -d'=' -f2 | tr -d '[:space:]'
}

NAME=$(read_property name)
VERSION=$(read_property version)
MAIN_CLASS=com.didanko228.megaUpscale.Main
MAIN_JAR=$NAME-$VERSION-all.jar

OS=$1

echo "Building: $NAME v$VERSION $OS"
rm -f build/libs/* -v
./gradlew shadowJar

case "$OS" in
  windows)
    jpackage \
      --name $NAME \
      --input build/libs \
      --main-jar $MAIN_JAR \
      --main-class $MAIN_CLASS \
      --app-version $VERSION \
      --type exe \
      --dest build/dest/windows \
      --win-dir-chooser \
      --win-per-user-install \
      --win-shortcut-prompt \
      --win-menu \
      --win-upgrade-uuid019e061e-1ba3-7141-a767-a9e704d38124
    ;;
  linux)
    jpackage \
      --name $NAME \
      --input build/libs \
      --main-jar $MAIN_JAR \
      --main-class $MAIN_CLASS \
      --app-version $VERSION \
      --type deb \
      --dest build/dest/linux \
      --linux-shortcut
    ;;
  mac)
    jpackage \
      --name $NAME \
      --input build/libs \
      --main-jar $MAIN_JAR \
      --main-class $MAIN_CLASS \
      --app-version $VERSION \
      --type dmg \
      --dest build/dest/mac
    ;;
  *)
    echo "Usage: $0 {windows|linux|mac}"
    exit 1
    ;;
esac