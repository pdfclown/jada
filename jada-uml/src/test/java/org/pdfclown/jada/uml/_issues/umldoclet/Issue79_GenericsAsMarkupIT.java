/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue79_GenericsAsMarkupIT.java) is part of jada-uml module in Jada project
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
import static org.hamcrest.Matchers.not;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertResult;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Bug79GenericsAsMarkupTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue79_GenericsAsMarkupIT extends BaseIT {
  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull String classPuml;

  Issue79_GenericsAsMarkupIT() {
    super(Issue79_GenericsAsMarkupIT.class);

    singleRun();
  }

  public <B> Optional<B> boldMarkup() {
    return Optional.empty();
  }

  public <I> Optional<I> italicMarkup() {
    return Optional.empty();
  }

  public <U> Optional<U> underlineMarkup() {
    return Optional.empty();
  }

  @Override
  protected void onSingleRunTerm(JavadocAssertResult result) {
    assert sourceType != null;
    classPuml = outputContent(getEnv().basedName(filename(sourceType, FILE_EXTENSION__PLANTUML)));
  }

  // SourceName: testNoMarkup
  @Test
  void _noMarkup() {
    assertThat(classPuml, not(containsString("Optional<U>")));
    assertThat(classPuml, not(containsString("Optional<I>")));
    assertThat(classPuml, not(containsString("Optional<B>")));

    String stripped = classPuml
        .replace('\u200B', '?') /* Makes zero-width-space 'visible' for test */;
    assertThat(stripped, containsString("Optional<?U>"));
    assertThat(stripped, containsString("Optional<?I>"));
    assertThat(stripped, containsString("Optional<?B>"));
  }
}
