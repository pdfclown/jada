/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue64_ExtendsObjectIT.java) is part of jada-uml module in Jada project
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

import static java.util.Collections.emptyIterator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.common.util.Objects.fqnd;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__ENCLOSES;
import static org.pdfclown.jada.uml.util.Plantumls.puml;

import java.util.AbstractSet;
import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.annot.Initializer;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertResult;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Issue64ExtendsObjectTest
/**
 * Test that any generic {@code EmptySet<T>} doesn't get rendered in UML as
 * {@code EmptySet<T extends Object>}.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue64_ExtendsObjectIT extends BaseIT {
  public static class EmptySet<T> extends AbstractSet<T> {
    @Override
    public Iterator<T> iterator() {
      return emptyIterator();
    }

    @Override
    public int size() {
      return 0;
    }
  }

  @SuppressWarnings("NotNullFieldNotInitialized")
  private String emptySetPuml;

  Issue64_ExtendsObjectIT() {
    super(Issue64_ExtendsObjectIT.class.getPackageName());

    singleRun();
  }

  @Initializer
  @Override
  protected void onSingleRunTerm(JavadocAssertResult result) {
    emptySetPuml = outputContent(getEnv().outputName(filename(EmptySet.class,
        FILE_EXTENSION__PLANTUML)));
  }

  // SourceName: testIssue64_TextendsObject
  @Test
  void _issue64_textEndsObject() {
    var simpleName = EmptySet.class.getSimpleName();

    assertThat(emptySetPuml, not(containsString(simpleName + "<T extends Object>")));
    assertThat(emptySetPuml, containsString(simpleName + "<T>"));
  }

  // SourceName: testIssue82_ContainingClassReference
  @Test
  void _issue82_containingClassReference() {
    assertThat(emptySetPuml, containsString(puml()
        .join(fqnd(Issue64_ExtendsObjectIT.class))
        .join(PUML_REF__ENCLOSES)
        .join(fqnd(EmptySet.class)).toString()));
  }
}
