/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue276_AnnotationSyntaxErrorIT.java) is part of jada-uml module in Jada project
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG;
import static org.pdfclown.jada.uml.__test.Utils.filename;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Bug276AnnotationSyntaxErrorTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue276_AnnotationSyntaxErrorIT extends BaseIT {
  @Target(ElementType.TYPE)
  public @interface Generated {
    String comments();

    String date();

    String[] value();
  }

  Issue276_AnnotationSyntaxErrorIT() {
    super(Issue276_AnnotationSyntaxErrorIT.class);

    singleRun();
  }

  // SourceName: testAnnotationDiagramHasNoSyntaxError
  @Test
  void _annotationDiagramHasNoSyntaxError() {
    String svg = outputContent(getEnv().basedName(filename(Generated.class, FILE_EXTENSION__SVG)));

    assertThat(svg, not(containsString("Syntax Error")));
  }
}
