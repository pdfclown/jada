/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Event.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.system;

/**
 * Event.
 *
 * @param <S>
 *          Event source type.
 * @author Stefano Chizzolini
 */
public abstract class Event<S> {
  @SuppressWarnings("NotNullFieldNotInitialized")
  private S source;

  /**
   * <span class="warning">(For internal use only)</span>
   */
  protected Event() {
  }

  protected Event(S source) {
    this.source = source;
  }

  public S getSource() {
    return source;
  }
}
