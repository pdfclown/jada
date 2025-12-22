/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaScriptExtension.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import java.net.URL;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.common.util.annot.LazyNonNull;

/**
 * Jada script extension.
 *
 * @author Stefano Chizzolini
 */
public abstract class JadaScriptExtension extends JadaExtension {
  protected @LazyNonNull @Nullable JadaExtConfig extConfig;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @InitNonNull
  String language;
  @SuppressWarnings("NotNullFieldNotInitialized")
  @InitNonNull
  URL location;
  @SuppressWarnings("NotNullFieldNotInitialized")
  @InitNonNull
  String name;

  @Override
  public JadaExtConfig getExtConfig() {
    if (extConfig == null) {
      extConfig = new JadaExtConfig();
    }
    return extConfig;
  }

  /**
   * Script language.
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Script location.
   */
  public final URL getLocation() {
    return location;
  }

  @Override
  public final String getName() {
    return name;
  }
}
