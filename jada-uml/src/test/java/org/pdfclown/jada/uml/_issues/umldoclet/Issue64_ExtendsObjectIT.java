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
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.common.util.Objects.fqnd;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__ENCLOSES;
import static org.pdfclown.jada.uml.util.Plantumls.puml;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.annot.InitNonNull;
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
      return Collections.emptyIterator();
    }

    @Override
    public int size() {
      return 0;
    }
  }

  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull String emptySetPuml;

  Issue64_ExtendsObjectIT() {
    super(Issue64_ExtendsObjectIT.class.getPackageName());

    singleRun();
  }

  @Override
  protected void onSingleRunTerm(JavadocAssertResult result) {
    emptySetPuml = outputContent(getEnv().basedName(filename(EmptySet.class,
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
