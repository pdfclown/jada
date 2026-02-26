/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Strings.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.internal.temp.util;

/**
 * String utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Strings {
  /**
   * Joins the elements of an iterable into a single string, prepending a prefix to each of them.
   */
  public static String joinWithPrefix(Iterable<?> obj, String prefix) {
    var b = new StringBuilder();
    for (var e : obj) {
      b.append(prefix).append(e);
    }
    return b.toString();
  }

  private Strings() {
  }
}
