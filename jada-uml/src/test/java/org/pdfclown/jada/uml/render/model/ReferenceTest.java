/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ReferenceTest.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__ASSOCIATED_BY;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__ASSOCIATES;

import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.uml.ReferenceTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class ReferenceTest extends BaseTest {
  // SourceName: testCanonical
  @Test
  void canonical() {
    var ref1 = new Reference(Reference.from("type1", null), PUML_REF__ASSOCIATES,
        Reference.to("type2", "*"));
    var ref2 = new Reference(Reference.from("type2", "*"), PUML_REF__ASSOCIATED_BY,
        Reference.to("type1", null));

    assertThat(ref1, is(equalTo(ref2)));
    assertThat(ref2, is(equalTo(ref1)));
    assertThat(ref1, hasToString(containsString("type1 --> \"*\" type2")));
    assertThat(ref2, hasToString(containsString("type2 \"*\" <-- type1")));
    assertThat(ref2.canonical(), hasToString(equalTo(ref1.toString())));
  }

  // SourceName: testSelfReference
  @Test
  void isSelfReference() {
    var ref = new Reference(Reference.from(getClass().getName(), null), PUML_REF__ASSOCIATES,
        Reference.to(getClass().getName(), null));

    assertThat(ref.isSelfReference(), is(true));
  }
}
