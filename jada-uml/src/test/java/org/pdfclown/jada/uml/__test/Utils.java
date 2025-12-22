/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Utils.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.uml.__test;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.writeString;
import static org.pdfclown.common.util.Objects.sqnd;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Stefano Chizzolini
 */
public final class Utils {
  public static String filename(Class<?> type, String extension) {
    return sqnd(type) + extension;
  }

  /**
   * Writes text to a file, encoded in UTF-8 charset; any non-existent parent directory is
   * automatically created.
   */
  public static void writeText(Path path, CharSequence text) throws IOException {
    createDirectories(path.getParent());
    writeString(path, text);
  }

  private Utils() {
  }
}
