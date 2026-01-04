/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaObject.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import org.pdfclown.jada.core.system.Logger;
import org.pdfclown.jada.core.system.SystemObject;

/**
 * Object belonging to the Jada doclet framework.
 *
 * @author Stefano Chizzolini
 */
public interface JadaObject extends SystemObject {
  /**
   * Doclet configuration.
   */
  @Override
  default JadaConfig getConfig() {
    return getJada().getConfig();
  }

  /**
   * Doclet environment.
   */
  default JadaEnvironment getEnv() {
    return getJada().getEnv();
  }

  /**
   * Doclet.
   */
  Jada getJada();

  @Override
  default Logger getLog() {
    return getConfig().getLog();
  }
}
