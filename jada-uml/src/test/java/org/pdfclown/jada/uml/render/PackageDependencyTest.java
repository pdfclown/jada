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
