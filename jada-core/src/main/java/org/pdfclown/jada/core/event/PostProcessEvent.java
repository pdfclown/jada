/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PostProcessEvent.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.event;

import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.system.Event;

/**
 * Event notifying that Javadoc post-processing is about to start, as {@linkplain MainProcessEvent
 * main processing} has just completed.
 *
 * @author Stefano Chizzolini
 */
public class PostProcessEvent extends Event<Jada> {
  public PostProcessEvent(Jada source) {
    super(source);
  }

  /**
   * <span class="warning">(For internal use only)</span>
   */
  protected PostProcessEvent() {
  }
}
