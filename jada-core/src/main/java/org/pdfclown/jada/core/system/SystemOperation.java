/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SystemOperation.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.system;

/**
 * System operation.
 *
 * @param <R>
 *          Result type.
 * @author Stefano Chizzolini
 */
public interface SystemOperation<R> extends SystemObject {
  /**
   * Initializes this operation.
   */
  void init(SystemConfig config);

  /**
   * Executes this operation.
   */
  R run();

  /**
   * Terminates this operation.
   */
  void term();
}
