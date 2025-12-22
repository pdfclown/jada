/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ExtMessage.java) is part of jada-ext module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.ext.internal;

import org.pdfclown.jada.core.system.Message;

/**
 * Supplemental Javadoc functionalities message.
 *
 * @author Stefano Chizzolini
 */
public enum ExtMessage implements Message {
  DOC_REUSE_FRAGMENT_EVENT,
  LAST_MOD_TIME_ACTION_FAILED,
  P__QUERY,
  P__SAVE;

  @Override
  public String getKey() {
    return name();
  }
}
