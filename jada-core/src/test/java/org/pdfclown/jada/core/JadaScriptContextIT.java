/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaScriptContextIT.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.util.Chars.UNDERSCORE;
import static org.pdfclown.common.util.Objects.sqn;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.jada.core.JadaConfig.OPTION__RESOURCE_DIR;

import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.util.io.ResourceNames;
import org.pdfclown.jada.core.__test.BaseIT;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertResult;

/**
 * @author Stefano Chizzolini
 */
public class JadaScriptContextIT extends BaseIT {
  JadaScriptContextIT() {
    super(JadaScriptContextIT.class);
  }

  @Test
  void _multipleResourceDirectories() {
    JavadocAssertResult result = runJavadoc(javadocArgs()
        .arg(OPTION__RESOURCE_DIR,
            getEnv().resourcePath(ResourceNames.name(S + UNDERSCORE + sqn(this), "res1")))
        .arg(OPTION__RESOURCE_DIR,
            getEnv().resourcePath(ResourceNames.name(S + UNDERSCORE + sqn(this), "res2")))
        .outputStreams(true));

    var scriptLogPattern = Pattern.compile("""
        JadaScriptContext: (\\S+) phase: \
        ".+org/pdfclown/jada/core/_JadaScriptContextIT/([^/]+)/scripts/(\\S+?)\\.groovy" script \
        hook running""");

    var expectedMatches = List.of(
        List.of("onMainProcess", "res2", "onMainProcess"),
        List.of("onMainProcess", "res1", "onMainProcess"),
        List.of("onPostProcess", "res2", "onPostProcess"));
    var m = scriptLogPattern.matcher(result.out);
    for (var it = expectedMatches.listIterator(); it.hasNext();) {
      if (!m.find())
        fail("Script log entry %s MISSING".formatted(it.nextIndex()));

      var expectedMatch = it.next();
      for (int i = 0; i < expectedMatch.size(); i++) {
        assertThat("Script log entry %s, group %s".formatted(it.previousIndex(), i + 1),
            expectedMatch.get(i), is(m.group(i + 1)));
      }
    }
    if (m.find())
      fail("Script log entry UNEXPECTED:\n  \"%s\"".formatted(m.group()));
  }
}
