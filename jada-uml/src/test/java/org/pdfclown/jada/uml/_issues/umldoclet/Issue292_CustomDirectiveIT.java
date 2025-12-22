/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue292_CustomDirectiveIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_DEPS;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__PLANTUML_CUSTOM_DIRECTIVE;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import org.junit.jupiter.api.Test;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertArgs;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Issue292CustomDirectiveTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue292_CustomDirectiveIT extends BaseIT {
  Issue292_CustomDirectiveIT() {
    super(Issue292_CustomDirectiveIT.class);

    singleRun();
  }

  @Override
  protected void onSingleRunInit(JavadocAssertArgs args) {
    args.arg(OPTION__PLANTUML_CUSTOM_DIRECTIVE, "skinparam handwritten true");
  }

  // SourceName: testCustomDirectiveInClassDiagram
  @Test
  void _customDirectiveInClassDiagram() {
    assert sourceType != null;
    var puml = outputContent(getEnv().basedName(filename(sourceType, FILE_EXTENSION__PLANTUML)));

    assertThat(puml, containsString("skinparam handwritten true"));
  }

  // SourceName: testCustomDirectiveInPackageDependenciesDiagram
  @Test
  void _customDirectiveInPackageDependenciesDiagram() {
    var puml = outputContent(FILENAME__PACKAGE_DEPS + FILE_EXTENSION__PLANTUML);

    assertThat(puml, containsString("skinparam handwritten true"));
  }

  // SourceName: testCustomDirectiveInPackageDiagram
  @Test
  void _customDirectiveInPackageDiagram() {
    var puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

    assertThat(puml, containsString("skinparam handwritten true"));
  }
}
