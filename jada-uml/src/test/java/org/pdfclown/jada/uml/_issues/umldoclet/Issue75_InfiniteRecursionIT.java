/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue75_InfiniteRecursionIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.util.Comparator;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Bug75StackOverflowTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue75_InfiniteRecursionIT extends BaseIT {
  public interface Comparable<T> {
    <U extends Comparable<? super U>> Comparator<T> thenComparing(
        Function<? super T, ? extends U> keyExtractor);
  }

  Issue75_InfiniteRecursionIT() {
    super(Issue75_InfiniteRecursionIT.class);

    singleRun();
  }

  // SourceName: testInifiniteRecursionIsBounded
  @Test
  void _main() {
    var comparableSimpleName = Comparable.class.getSimpleName();
    assert sourceType != null;
    outputContent(getEnv().basedName(filename(sourceType, FILE_EXTENSION__PLANTUML)));
    String packagePuml = outputContent(getEnv().basedName(FILENAME__PACKAGE
        + FILE_EXTENSION__PLANTUML));

    assertThat(packagePuml, not(containsString(
        "? extends " + comparableSimpleName + "<? super " + comparableSimpleName
            + "<? super " + comparableSimpleName)));
  }
}
