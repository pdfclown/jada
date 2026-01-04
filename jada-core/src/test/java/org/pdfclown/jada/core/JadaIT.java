/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaIT.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import static org.pdfclown.common.util.Objects.fqn;
import static org.pdfclown.jada.core.JadaConfig.OPTION__DOCLET_EXTENSIONS;
import static org.pdfclown.jada.core.JadaConfig.OPTION__VERBOSE;
import static org.pdfclown.jada.core.test.assertion.Assertions.assertJavadoc;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada._testext1.TestExtension1;
import org.pdfclown.jada._testext2.TestExtension2;
import org.pdfclown.jada.core.__test.BaseIT;
import org.pdfclown.jada.core.system.SystemObject;

/**
 * @author Stefano Chizzolini
 */
class JadaIT extends BaseIT {
  @Test
  void _extension() {
    assertJavadoc(javadocArgs()
        /*
         * NOTE: Checking extension selectors both as FQN and component name.
         */
        .arg(OPTION__DOCLET_EXTENSIONS, List.of(fqn(TestExtension1.class), TestExtension2.NAME))
        .arg(OPTION__VERBOSE)
        .type(SystemObject.class));
  }

  @Test
  void _help() {
    assertJavadoc(javadocArgs()
        .arg(JadaConfig.OPTION__HELP));
  }

  @Test
  void _help_extra() {
    assertJavadoc(javadocArgs()
        .arg(JadaConfig.OPTION__HELP_EXTRA));
  }
}
