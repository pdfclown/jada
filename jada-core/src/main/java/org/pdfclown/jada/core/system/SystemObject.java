/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SystemObject.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.system;

import static org.pdfclown.common.util.Objects.sqn;

import org.pdfclown.jada.core.util.Nameable;

/**
 * System object.
 * <p>
 * Represents a top-level object which has direct access to global application state.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public interface SystemObject extends Nameable {
  /**
   * Configuration.
   */
  SystemConfig getConfig();

  /**
   * Logger.
   */
  default Logger getLog() {
    return getConfig().getLog();
  }

  @Override
  default String getName() {
    return sqn(this);
  }
}
