[Jada](../README.md) >

# JadaUML

*Jada doclet extension for embedding UML diagrams into Javadoc*

[![JDK Compatibility](https://img.shields.io/badge/Java-17-blue)](https://openjdk.org/projects/jdk/17/)
[![maven](https://img.shields.io/maven-central/v/org.pdfclown/jada-uml)](https://search.maven.org/artifact/org.pdfclown/jada-uml/0.4.1/jar)
[![javadoc](https://javadoc.io/badge2/org.pdfclown/jada-uml/javadoc.svg)](https://javadoc.io/doc/org.pdfclown/jada-uml/0.4.1/index.html)

This project started as a proof of concept for the Jada doclet extension model, porting the excellent [UMLDoclet](https://github.com/talsma-ict/umldoclet) to Jada. Because of its effectiveness, it is now a stable module of the Jada project.

## Introduction

The purpose of **Jada doclet** is to *spare third-party Javadoc extensions the heavy lifting required by the standard Java Doclet API*, through common high-level functionalities ready to use in a convenient and enjoyable manner (for example, source code pre-processing, Javadoc post-processing, advanced taglets integration, off-the-shelf option building (CLI arguments handling, CLI help descriptors, transparent option overriding, ...), doclet lifecycle events, advanced logger and message management, ...).

**UMLDoclet porting** implied the streamlining of doclet-related functionalites (doclet, options, logger, message management, post-processor), replacing them with the corresponding Jada model equivalents. Even though the port initially took just 3 hours of code adaptation to run successfully as a Jada extension, subsequently the original codebase has been heavily refactored to suit the Jada project's design style — that's just at implementer's discretion: if you are a doclet developer who wants to move to Jada, it's up to you how to organize your code once your extension has been plugged into the extension mechanism!

## Changes

Compared to its upstream project (UMLDoclet), JadaUML differs in the following aspects:

  - **CLI options**:
    - [NEW]
        - `--uml-empty-diagrams-render`: replaces `--uml-render-empty-diagrams`.
        - `--uml-image-dir`: replaces `--uml-image-directory`.
        - `--uml-package-deps-check-cyclic`: replaces `--fail-on-cyclic-package-dependencies`, changing its logic — see "cyclic package dependencies" here below for further information.
        - `--uml-package-deps-exclude`: replaces `--uml-excluded-package-dependencies`, extending exclusion filters (now wildcard-based (default: `"java.*,javax.*"`) instead of prefix-based (original default: `"java,javax"`)) for additional flexibility. Moreover, this argument can be prefixed by a modifier (`'+'` or `'-'`) which, respectively, appends or removes the new filters to/from the existing ones instead of replacing them, for incremental definitions (no modifier means collection overriding).
        - `--uml-package-deps-max-count`: limits the number of packages in the dependencies diagram (default: 10) in order to keep it tidy (the upstream project, which had no limit, ended up with cluttered diagrams as soon as the documented projects became a bit complex; this regressive behavior has been [successively addressed](https://github.com/talsma-ict/umldoclet/pull/738) via coarse boolean logic); the packages are ordered by dependency count before being filtered, ensuring the ones most referenced across the project stick out. This diagram can also be suppressed by simply setting this option to zero.
        - `--uml-properties-flatten`: replaces `--uml-java-bean-properties-as-fields` inverting its logic — see "properties" here below for further information.
        - `--uml-server-timeout`: replaces `--uml-timeout`.
        - `--uml-server-url`: replaces `--plantuml-server-url`.
        - `--uml-static-fields-max-count`: limits the number of fields in class diagrams (default: 10) in order to keep them tidy (long sequences of static fields make diagrams annoyingly noisy). En passant, the support to associative "uses" references of static fields in package diagrams has been removed (they cluttered the diagrams without significant benefit).
        - `--uml-type-refs-exclude`: replaces `--uml-excluded-type-references`, extending its syntax with a modifier prefix (`'+'` or `'-'`) which, respectively, appends or removes the new references to/from the existing ones instead of replacing them, for incremental definitions (no modifier means collection overriding).
    - [REMOVE]
        - `--create-puml-files`: use Jada's `--debug` option instead.
        - `--delegate-doclet`: see Jada's base doclet mechanism.
        - `--fail-on-cyclic-package-dependencies`: replaced by `--uml-package-deps-check-cyclic`.
        - `--plantuml-server-url`: replaced by `--uml-server-url`.
        - `--uml-excluded-package-dependencies`: replaced by `--uml-package-deps-exclude`.
        - `--uml-excluded-type-references`: replaced by `--uml-type-refs-exclude`.
        - `--uml-image-directory`: replaced by `--uml-image-dir`.
        - `--uml-java-bean-properties-as-fields`: replaced by `--uml-properties-flatten`.
        - `--uml-render-empty-diagrams`: replaced by `--uml-empty-diagrams-render`.
        - `--uml-timeout`: replaced by `--uml-server-timeout`.
  - [CHANGE] **cyclic package dependencies** detection is skipped by default (whilst UMLDoclet logs a warning by default, escalating to error level if requested by users via `--fail-on-cyclic-package-dependencies` CLI option) — despite this functionality may be deemed useful by someone, it is not Javadoc's purpose to apply static analysis to code (dedicated tools like [SpotBugs](https://spotbugs.readthedocs.io/en/stable/detectors.html#findcirculardependencies) are more appropriate for such evaluations).<br>
Nonetheless, if users desire such extra bit of diagnostic aid, `--uml-package-deps-check-cyclic` CLI option will cause a warning to be logged instead.
  - [CHANGE] **properties** are represented, by default, as fields, even if *only* the getter is present, or it is abstract, or it is not public (whilst UMLDoclet by default represents JavaBean properties as plain methods, and as fields only if explicitly requested by users via `--uml-java-bean-properties-as-fields` CLI option, and only if *both* getter and setter are present, concrete and public), because a property is a property is a property, and Javadoc already describes the API details — no need of redundancies, the UML diagrams should provide a neat, uncluttered glance to the model.<br>
Nonetheless, if users are keen on verbosity, `--uml-properties-flatten` CLI option will cause properties to be represented as plain methods instead.
  - [NEW] **HTML tags for UML diagrams** injected in post-processed Javadoc files are                            associated to "`uml`" CSS class for custom styling (UMLDoclet lacks this feature).
  - [NEW] **SVG diagrams** are dynamically embedded to overcome the limitations of `<object>` elements, providing built-in zooming and panning for complex package diagrams.
  - internally:
    - **types and members naming** has been harmonized to maximize consistency and minimize ambiguities.
    - **types** have been reorganized to adhere to Jada design style.
    - **tests** have been reorganized and split between unit tests (`*Test`) and integration tests (`*IT`), leveraging the [pdfclown-common-build](https://github.com/pdfclown/pdfclown-common) testing harness to simplify Javadoc environment configuration and output directories layout.

## Source code

In the source code, `SourceName:`-tagged comments provide mappings to the corresponding upstream
elements in case of renaming.

### Fork reconciliation

<table border="1">
<tr>
<td><b>Local package</b></td>
<td><b>Upstream package</b></td>
<td><b>Upstream commit*</b></td>
</tr>
<tr><td><code>org.pdfclown.jada.uml</code></td><td><code><a href="https://github.com/talsma-ict/umldoclet/tree/6374a7cadfad1a3c4d6fc61e75cdc6caaed33d1d/src/main/java/nl/talsmasoftware/umldoclet">nl.talsmasoftware.umldoclet</a></code></td><td><a href="https://github.com/talsma-ict/umldoclet/commit/6374a7cadfad1a3c4d6fc61e75cdc6caaed33d1d">6374a7c</a> (2026-06-18_13:09+0200)</td>
</tr>
</table>
[*] Latest commit reconciled

## Building

### Prerequisites

In order to build this project, the following additional tools are required:

- a [Docker environment supported by Testcontainers](https://www.testcontainers.org/supported_docker_environment/) (for local installation, Docker Desktop seems the most convenient) — for integration tests via [Testcontainers](https://testcontainers.com/)

## Credits

- [Sjoerd Talsma](https://github.com/sjoerdtalsma): this extension wouldn't have been possible without his [UMLDoclet](https://github.com/talsma-ict/umldoclet) project
