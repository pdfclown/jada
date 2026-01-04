/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue152_InnerClassVisibilityIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet.issue148;

import static java.nio.file.Files.exists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertArgs;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.features.Issue152InnerClassIncludeVisibilityTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue152_InnerClassVisibilityIT extends BaseIT {
  Issue152_InnerClassVisibilityIT() {
    super(Issue152_InnerClassVisibilityIT.class.getPackageName());

    singleRun();
  }

  @Override
  protected void onSingleRunInit(@NonNull JavadocAssertArgs args) {
    args.arg("--show-types", "public");
  }

  // SourceName: testPublicInnerClassVisibility
  @Test
  void _main() {
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat("Package uml", puml, allOf(
          containsString("PublicClass.PublicInnerClass"),
          not(containsString("PublicClass.ProtectedInnerClass")),
          not(containsString("PublicClass.PackageProtectedInnerClass")),
          not(containsString("PublicClass.PrivateInnerClass"))));
    }
    {
      String puml = outputContent(getEnv().basedName(filename(PublicClass.class,
          FILE_EXTENSION__PLANTUML)));

      assertThat("Class uml", puml, allOf(
          containsString("PublicClass.PublicInnerClass"),
          not(containsString("PublicClass.ProtectedInnerClass")),
          not(containsString("PublicClass.PackageProtectedInnerClass")),
          not(containsString("PublicClass.PrivateInnerClass"))));

      Stream.of(
          FILE_EXTENSION__HTML,
          FILE_EXTENSION__PLANTUML,
          FILE_EXTENSION__SVG).forEach($ -> {
            assertThat("public innerclass " + $,
                exists(getEnv().outputPath(getEnv().basedName(
                    filename(PublicClass.PublicInnerClass.class, $)))),
                is(true));
            assertThat("protected innerclass " + $,
                exists(getEnv().outputPath(getEnv().basedName(
                    filename(PublicClass.ProtectedInnerClass.class, $)))),
                is(false));
            assertThat("package innerclass " + $,
                exists(getEnv().outputPath(getEnv().basedName(
                    filename(PublicClass.PackageProtectedInnerClass.class, $)))),
                is(false));
            assertThat("private innerclass " + $,
                exists(getEnv().outputPath(getEnv().basedName(
                    "PublicClass.PrivateInnerClass" + $))),
                is(false));
          });
    }
  }
}
