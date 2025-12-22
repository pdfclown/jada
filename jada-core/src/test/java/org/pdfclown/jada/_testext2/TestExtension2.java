/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TestExtension2.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada._testext2;

import org.jspecify.annotations.NonNull;
import org.pdfclown.jada.core.JadaExtConfig;
import org.pdfclown.jada.core.JadaExtension;

@SuppressWarnings("FieldMayBeFinal")
public class TestExtension2 extends JadaExtension {
  public static class TestExtConfig extends JadaExtConfig {
  }

  public static final String NAME = "TestExt2";

  private TestExtConfig extConfig;

  public TestExtension2() {
    extConfig = new TestExtConfig();
  }

  @Override
  public @NonNull JadaExtConfig getExtConfig() {
    return extConfig;
  }

  @Override
  public @NonNull String getName() {
    return NAME;
  }
}
