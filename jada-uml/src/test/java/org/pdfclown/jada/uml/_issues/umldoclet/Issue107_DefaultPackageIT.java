/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue107_DefaultPackageIT.java) is part of jada-uml module in Jada project
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
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__JAVA;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;
import static org.pdfclown.jada.uml.util.Plantumls.puml;

import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertArgs;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Bug107DefaultPackageTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue107_DefaultPackageIT extends BaseIT {
  private static final String FOO_TYPE_SIMPLE_NAME = "Foo";

  Issue107_DefaultPackageIT() {
    singleRun();
  }

  @Override
  protected void onSingleRunInit(JavadocAssertArgs args) {
    args.arg(getEnv().dir(ProjectDirId.TEST_TYPE_SOURCE)
        .resolve(FOO_TYPE_SIMPLE_NAME + FILE_EXTENSION__JAVA));
  }

  // SourceName: testDefaultPackageDocumentation
  @Test
  void _main() {
    String puml = outputContent(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML);

    assertThat(puml, containsString("package unnamed"));
    assertThat(puml, containsString(puml()
        .join("class").join(FOO_TYPE_SIMPLE_NAME).toString()));
  }
}
