[Jada](..) >

# JadaMaven

[![JDK Compatibility](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/projects/jdk/17/)
[![maven](https://img.shields.io/maven-central/v/org.pdfclown/jada-maven-plugin)](https://search.maven.org/artifact/org.pdfclown/jada-maven-plugin/0.2.1/jar)
[![javadoc](https://javadoc.io/badge2/org.pdfclown/jada-maven-plugin/javadoc.svg)](https://javadoc.io/static/org.pdfclown/jada-maven-plugin/0.2.1/index.html)

Maven plugin for Javadoc processing on top of Jada doclet.

## Usage

For usage information, run this CLI command:

```shell
./mvnw jada:help -Ddetail
```

To list available source code processors:

```shell
./mvnw jada:processSource -Djada.procs.info=PROCS
```