/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue266_PropertyTighteningIT.java) is part of jada-uml module in Jada project
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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.annot.Initializer;
import org.pdfclown.common.util.annot.ReadOnly;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertResult;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Issue266Test
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue266_PropertyTighteningIT extends BaseIT {
  public static class TesterUtil {
    public static @ReadOnly Set<TesterUtil> setOf(TesterUtil... testers) {
      return unmodifiableSet(new LinkedHashSet<>(asList(testers)));
    }
  }

  @SuppressWarnings("NotNullFieldNotInitialized")
  private String classPuml;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private String packagePuml;

  Issue266_PropertyTighteningIT() {
    super(Issue266_PropertyTighteningIT.class);

    singleRun();
  }

  @Initializer
  @Override
  protected void onSingleRunTerm(JavadocAssertResult result) {
    classPuml = outputContent(getEnv().outputName(filename(TesterUtil.class,
        FILE_EXTENSION__PLANTUML)));
    packagePuml = outputContent(getEnv().outputName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));
  }

  // SourceName: testBug266Rendering
  /**
   * Verifies that no 'of' property is rendered.
   */
  @Test
  void _pseudoPropertyShouldRenderAsMethod() {
    assertThat(classPuml, not(containsString(": of")));
    assertThat(packagePuml, not(containsString(": of")));
  }
}
