/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue79_GenericsAsMarkupIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  Changes: Adaptation and redesign for Jada.
 */
/*
 * Copyright 2016-2026 Talsma ICT
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pdfclown.jada.uml._issues.umldoclet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.common.util.Objects.nonNull;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.annot.Initializer;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertResult;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Bug79GenericsAsMarkupTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue79_GenericsAsMarkupIT extends BaseIT {
  @SuppressWarnings("NotNullFieldNotInitialized")
  private String classPuml;

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

  @Initializer
  @Override
  protected void onSingleRunTerm(JavadocAssertResult result) {
    classPuml = outputContent(getEnv().outputName(filename(nonNull(sourceType),
        FILE_EXTENSION__PLANTUML)));
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
