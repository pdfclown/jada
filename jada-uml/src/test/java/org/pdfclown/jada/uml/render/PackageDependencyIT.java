/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageDependencyIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_DEPS;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__EXCLUDED_PACKAGE_DEPENDENCIES;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import org.junit.jupiter.api.Test;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertArgs;
import org.pdfclown.jada.uml.UmlConfig;
import org.pdfclown.jada.uml.__test.BaseIT;
import org.pdfclown.jada.uml.render._1.Test1Exception;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.dependencies.PackageDependenciesTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class PackageDependencyIT extends BaseIT {
  private static final String FILENAME__PACKAGE_DEPS_PUML =
      FILENAME__PACKAGE_DEPS + FILE_EXTENSION__PLANTUML;

  // SourceName: testPackageDependenciesCustomExclusions
  @Test
  void _customExclusions() {
    runJavadoc(prepareJavadocArgs()
        .type(PackageDependencyScanner.class)
        .arg(OPTION__EXCLUDED_PACKAGE_DEPENDENCIES, "java.util*"));
    String puml = outputContent(FILENAME__PACKAGE_DEPS_PUML);

    assertThat(puml, not(containsString("java.util")));
    assertThat(puml, containsString("org.pdfclown.jada.uml.render --> "
        + "java.lang"));
    assertThat(puml, containsString("org.pdfclown.jada.uml.render --> "
        + "javax.lang.model.type"));
    assertThat(puml, containsString("org.pdfclown.jada.uml.render --> "
        + "org.pdfclown.jada.uml.render.model"));
  }

  // SourceName: testPackageDependenciesDefaultExclusions
  @Test
  void _defaultExclusions() {
    runJavadoc(prepareJavadocArgs()
        .type(PackageDependencyScanner.class));
    String puml = outputContent(FILENAME__PACKAGE_DEPS_PUML);

    assertThat(puml, containsString("org.pdfclown.jada.uml.render --> "
        + "org.pdfclown.jada.uml"));
    assertThat(puml, containsString("org.pdfclown.jada.uml.render --> "
        + "org.pdfclown.jada.uml.render.model"));
    assertThat(puml, containsString("org.pdfclown.jada.uml.render --> "
        + "org.pdfclown.jada.uml.__test"));
    assertThat(puml, not(containsString("java.lang")));
    assertThat(puml, not(containsString("javax.lang")));
    assertThat(puml, not(containsString("java.util")));
  }

  // SourceName: testPackageDependenciesIncludeExceptions
  @Test
  void _includeExceptions() throws Test1Exception /* DO NOT remove */ {
    runJavadoc(prepareJavadocArgs()
        .type(PackageDependencyIT.class));
    String puml = outputContent(FILENAME__PACKAGE_DEPS_PUML);

    assertThat(puml, containsString(getClass().getPackageName() + " --> "
        + Test1Exception.class.getPackageName()));
  }

  // SourceName: testPackageDependenciesWithoutExclusions
  @Test
  void _withoutExclusions() {
    runJavadoc(prepareJavadocArgs()
        .type(PackageDependencyScanner.class)
        .arg(OPTION__EXCLUDED_PACKAGE_DEPENDENCIES, EMPTY));
    String puml = outputContent(FILENAME__PACKAGE_DEPS_PUML);

    assertThat(puml, containsString("org.pdfclown.jada.uml.render --> "
        + "java.lang"));
    assertThat(puml, containsString("org.pdfclown.jada.uml.render --> "
        + "java.util"));
    assertThat(puml, containsString("org.pdfclown.jada.uml.render --> "
        + "javax.lang.model.type"));
    assertThat(puml, containsString("org.pdfclown.jada.uml.render --> "
        + "org.pdfclown.jada.uml.render.model"));
  }

  private JavadocAssertArgs prepareJavadocArgs() {
    return javadocArgs()
        .arg(UmlConfig.OPTION__PACKAGE_DEPENDENCIES_MAX_COUNT, -1);
  }
}
