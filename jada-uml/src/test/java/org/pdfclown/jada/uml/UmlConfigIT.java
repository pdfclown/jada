/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlConfigIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.uml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.common.build.test.assertion.Matchers.containsPattern;
import static org.pdfclown.jada.core.JadaConfig.OPTION__HELP;
import static org.pdfclown.jada.core.system.MessageManager.MISSING_KEY_PLACEHOLDER_PREFIX;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertResult;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.DocletConfigTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class UmlConfigIT extends BaseIT {
  public static class StaticFields {
    public static final int VALUE1 = 1;
    public static final int VALUE2 = 2;
    public static final int VALUE3 = 3;
    public static final int VALUE4 = 4;
    public static final int VALUE5 = 5;
    public static final int VALUE6 = 6;
    public static final int VALUE7 = 7;
    public static final int VALUE8 = 8;
    public static final int VALUE9 = 9;
    public static final int VALUE10 = 10;
    public static final int VALUE11 = 11;
    public static final int VALUE12 = 12;
    public static final int VALUE13 = 13;
    public static final int VALUE14 = 14;
    public static final int VALUE15 = 15;
    public static final int VALUE16 = 16;
  }

  UmlConfigIT() {
    super(UmlConfigIT.class);
  }

  @Test
  void _option__help() {
    JavadocAssertResult result = runJavadoc(javadocArgs()
        .arg(OPTION__HELP)
        .outputStreams(true));

    // SourceName: testOptionDocExcludedPackageDependencies
    {
      assertThat(result.out, containsString(
          "--uml-package-deps-exclude [+-]?<package-glob>(,<package-glob>)*"));
      assertThat(result.out, containsString(
          "DEFAULT: 'java.*,javax.*'"));
    }

    // SourceName: testForUndocumentedMissingKeys
    /*
     * Tests whether there were any undocumented options added to the doclet.
     *
     * NOTE: In case of failure (missing keys), add documentation for the new option(s) in
     * `UmlExtension` resource bundle ("UmlExtension.properties").
     */
    {
      assertThat(result.out, not(containsString(MISSING_KEY_PLACEHOLDER_PREFIX)));
    }
  }

  @Test
  void _option__staticFieldsMaxCount_custom() {
    runJavadoc(javadocArgs()
        .type(StaticFields.class)
        .arg(UmlConfig.OPTION__STATIC_FIELDS_MAX_COUNT, 5));
    String puml = outputContent(getEnv().basedName(filename(StaticFields.class,
        FILE_EXTENSION__PLANTUML)));

    assertThat(puml, containsPattern("\\{static\\} \\+VALUE13: int\\s+. . .\\s+\\}",
        Pattern.MULTILINE));
  }

  @Test
  void _option__staticFieldsMaxCount_default() {
    runJavadoc(javadocArgs()
        .type(StaticFields.class));
    String puml = outputContent(getEnv().basedName(filename(StaticFields.class,
        FILE_EXTENSION__PLANTUML)));

    assertThat(puml, containsPattern("\\{static\\} \\+VALUE3: int\\s+. . .\\s+\\}",
        Pattern.MULTILINE));
  }
}
