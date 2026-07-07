/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue72_DefaultConstructorIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.common.util.Objects.sqnd;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.annot.Initializer;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertResult;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Issue72DefaultConstructorTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue72_DefaultConstructorIT extends BaseIT {
  @SuppressWarnings("NotNullFieldNotInitialized")
  private String classPuml;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private String packagePuml;

  Issue72_DefaultConstructorIT() {
    super(Issue72_DefaultConstructorIT.class);

    singleRun();
  }

  @Initializer
  @Override
  protected void onSingleRunTerm(JavadocAssertResult result) {
    assert sourceType != null;
    classPuml = outputContent(getEnv().outputName(filename(sourceType, FILE_EXTENSION__PLANTUML)));
    packagePuml = outputContent(getEnv().outputName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));
  }

  // SourceName: testDefaultConstructorShouldBeHidden
  @Test
  void _defaultConstructorShouldBeHidden() {
    var defaultConstructorChunk = "+" + sqnd(sourceType) + "()";

    assertThat(classPuml, not(containsString(defaultConstructorChunk)));
    assertThat(packagePuml, not(containsString(defaultConstructorChunk)));
  }
}
