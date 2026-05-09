/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TestExtension1.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada._testext1;

import org.pdfclown.jada.core.JadaExtConfig;
import org.pdfclown.jada.core.JadaExtension;

@SuppressWarnings({ "FieldMayBeFinal", "NullableProblems" })
public class TestExtension1 extends JadaExtension {
  public static class TestExtConfig extends JadaExtConfig {
  }

  private TestExtConfig extConfig;

  public TestExtension1() {
    extConfig = new TestExtConfig();
  }

  @Override
  public JadaExtConfig getExtConfig() {
    return extConfig;
  }

  @Override
  public String getName() {
    return "TestExt1";
  }
}
