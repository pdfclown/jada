/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue69_LinksIT.java) is part of jada-uml module in Jada project
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
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.pdfclown.common.util.Objects.nonNull;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Issue69LinksTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue69_LinksIT extends BaseIT {
  public static class InnerClass {
  }

  Issue69_LinksIT() {
    super(Issue69_LinksIT.class);

    singleRun();
  }

  // SourceName: testLinkSameDirectory
  @Test
  void _linkSameDirectory() {
    String puml =
        outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

    // Check link to test class
    assertThat(puml, stringContainsInOrder(List.of(
        nonNull(sourceType).getSimpleName(),
        "[[" + filename(sourceType, FILE_EXTENSION__HTML) + "]]")));
    // Check link to inner class
    assertThat(puml, stringContainsInOrder(List.of(
        InnerClass.class.getSimpleName(),
        "[[" + filename(InnerClass.class, FILE_EXTENSION__HTML) + "]]")));
  }
}
