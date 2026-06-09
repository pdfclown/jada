Jada > [Documentation](README.md) >

# Building

This document describes how to set up your development environment to build and test the Jada project.

## Prerequisites

- [Prerequisites](https://github.com/pdfclown/pdfclown-common/blob/v0.8.0/docs/common/building.md#prerequisites) < pdfClown.org

## Setup

1. [Setup](https://github.com/pdfclown/pdfclown-common/blob/v0.8.0/docs/common/building.md#setup) < pdfClown.org

2. that's all! :tada: Now you are ready to build (see next section) — happy development!

## Building

See [Building](https://github.com/pdfclown/pdfclown-common/blob/v0.8.0/docs/common/building.md#building-1) < pdfClown.org

## Debugging

Debug configurations for Intellij IDEA and Eclipse IDE are available under "[conf](../conf)" directory. Jada can be debugged against any project, provided these steps are followed (here Apache Maven is assumed as build system):

1. **create the Javadoc configuration** ("options", "packages", "argfile" files), running the following CLI command in the folder of the target project:

   ```bash
   mvn javadoc:javadoc -Ddebug
   ```

    > [!TIP]
    > It's recommended, whenever possible, to use project-specific `./mvnw` (Maven Wrapper) command instead of global `mvn`.


2. **execute the debug launcher** ("Jada DEBUG") in your IDE, selecting it among the Debug Configurations. In the interactive console, enter the folder of the target project when prompted for `javadocTargetDir` argument.
