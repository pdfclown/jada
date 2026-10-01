/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageDependencyCycleTest.java) is part of jada-uml module in Jada project
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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.dependencies.PackageDependencyCycleTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class PackageDependencyCycleTest extends BaseTest {
  // SourceName: testDependencyCycleToString
  @Test
  void _dependencyCycleToString() {
    assertThat(new PackageDependencyCycle(
        new PackageDependency("a", "b"),
        new PackageDependency("b", "c"),
        new PackageDependency("c", "a")),
        hasToString("a > b > c > a"));
  }

  // SourceName: testEmptyDependencyCycle
  @Test
  void _emptyDependencyCycle() {
    IllegalArgumentException expected = assertThrows(IllegalArgumentException.class,
        PackageDependencyCycle::new);

    assertThat(expected.getMessage(), notNullValue());
  }

  // SourceName: testIncompleteCycle
  @Test
  void _incompleteCycle() {
    IllegalArgumentException expected = assertThrows(IllegalArgumentException.class,
        () -> new PackageDependencyCycle(
            new PackageDependency("a", "b"),
            new PackageDependency("b", "c")));

    assertThat(expected.getMessage(), notNullValue());
  }

  // SourceName: testMultipeCycleDetection
  @Test
  void _multipeCycleDetection() {
    var ab = new PackageDependency("a", "b");
    var bc = new PackageDependency("b", "c");
    var bd = new PackageDependency("b", "d");
    var cd = new PackageDependency("c", "d");
    var de = new PackageDependency("d", "e");
    var ba = new PackageDependency("b", "a");
    var ca = new PackageDependency("c", "a");
    var ea = new PackageDependency("e", "a");
    var alldeps = List.of(ab, bc, bd, cd, de, ba, ca, ea);
    Collection<PackageDependencyCycle> cycles = PackageDependencyCycle.detectCycles(alldeps);

    assertThat(cycles, hasItem(new PackageDependencyCycle(ab, ba)));
    assertThat(cycles, hasItem(new PackageDependencyCycle(ab, bc, ca)));
    assertThat(cycles, hasItem(new PackageDependencyCycle(ab, bc, cd, de, ea)));
    assertThat(cycles, hasItem(new PackageDependencyCycle(ab, bd, de, ea)));
  }

  // SourceName: testSimpleCycleDetection
  @Test
  void _simpleCycleDetection() {
    var ab = new PackageDependency("a", "b");
    var ba = new PackageDependency("b", "a");
    var alldeps = List.of(ab, ba);
    Collection<PackageDependencyCycle> cycles = PackageDependencyCycle.detectCycles(alldeps);

    assertThat(cycles, hasItem(new PackageDependencyCycle(ab, ba)));
  }
}
