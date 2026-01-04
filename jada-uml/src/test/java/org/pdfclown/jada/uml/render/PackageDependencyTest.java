/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageDependencyTest.java) is part of jada-uml module in Jada project
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.dependencies.PackageDependencyTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class PackageDependencyTest extends BaseTest {
  // SourceName: testDependencyWithoutFromPackage
  @Test
  void _withoutFromPackage() {
    NullPointerException expected = assertThrows(NullPointerException.class,
        () -> new PackageDependency(null, "b"));

    assertThat(expected.getMessage(), notNullValue());
  }

  // SourceName: testDependencyWithoutToPackage
  @Test
  void _withoutToPackage() {
    NullPointerException expected = assertThrows(NullPointerException.class,
        () -> new PackageDependency("a", null));

    assertThat(expected.getMessage(), notNullValue());
  }

  // SourceName: testEquals
  @Test
  void equals() {
    assertThat(new PackageDependency("a", "b"), is(equalTo(new PackageDependency("a", "b"))));
    assertThat(new PackageDependency("a", "b"), not(equalTo(new PackageDependency("a", "a"))));
    assertThat(new PackageDependency("a", "b"), not(equalTo(new PackageDependency("b", "b"))));
  }

  // SourceName: testHashCode
  @Test
  void hashCode_() {
    assertThat(new PackageDependency("a", "b").hashCode(),
        is(new PackageDependency("a", "b").hashCode()));
  }

  // SourceName: testToString
  @Test
  void toString_() {
    assertThat(new PackageDependency("a", "b"), hasToString("a->b"));
  }
}
