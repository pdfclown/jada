/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Messages.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.util;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.pdfclown.common.util.Chars.LF;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.jada.core.internal.temp.util.Strings.joinWithPrefix;

/**
 * Message-related utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Messages {
  /**
   * Gets the message representation of an iterable as a list.
   *
   * @param indentLevel
   *          Zero-based indentation level.
   */
  public static String list(Iterable<?> o, int indentLevel) {
    return joinWithPrefix(o, S + LF + repeat(SPACE, indentLevel * 2));
  }

  private Messages() {
  }
}
