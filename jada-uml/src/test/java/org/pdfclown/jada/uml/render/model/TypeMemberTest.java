/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TypeMemberTest.java) is part of jada-uml module in Jada project
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.uml.TypeMemberTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class TypeMemberTest extends BaseTest {
  private static class MinimalTypeMember extends TypeMember {
    private MinimalTypeMember(String name) {
      super(null, name, null);
    }
  }

  // SourceName: testMinimalTypeMemberInstance
  @Test
  void _minimalTypeMemberInstance() {
    var minimalInstance = new MinimalTypeMember("name");

    assertThat(minimalInstance.hashCode(), is(new MinimalTypeMember("name").hashCode()));
    assertThat(minimalInstance, is(equalTo(new MinimalTypeMember("name"))));
    // TODO: newline rendering should be in the writeChildren logic, not the child itself
    //        assertThat(minimalInstance, hasToString("+name"));
  }
}
