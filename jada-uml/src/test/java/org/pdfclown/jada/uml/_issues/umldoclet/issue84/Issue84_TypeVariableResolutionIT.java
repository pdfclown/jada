/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue84_TypeVariableResolutionIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet.issue84;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.Issue84TypeVariableResolutionTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue84_TypeVariableResolutionIT extends BaseIT {
  Issue84_TypeVariableResolutionIT() {
    super(TestObject.class);

    singleRun();
  }

  // SourceName: testTypeMemberImplementsComparableTypeMember
  @Test
  void _typeMemberImplementsComparableTypeMember() {
    assert sourceType != null;
    String puml = outputContent(getEnv().basedName(filename(sourceType, FILE_EXTENSION__PLANTUML)));

    assertThat(puml,
        stringContainsInOrder(List.of(
            "java.lang.Comparable<TestObject>",
            "{abstract} +compareTo(TestObject): int")));
  }
}
