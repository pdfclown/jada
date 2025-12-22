/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ThrowWriter.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.internal.util.io;

import java.io.IOException;
import java.io.Writer;

// SourceName: nl.talsmasoftware.umldoclet.rendering.writers.ThrowingWriter
/**
 * Writer that throws exceptions when writing, flushing or closing.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
final class ThrowWriter extends Writer {
  public static ThrowWriter throwing(Throwable throwable) {
    return new ThrowWriter(throwable);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
    throw (T) t;
  }

  private final Throwable throwable;

  private ThrowWriter(Throwable throwable) {
    this.throwable = throwable;
  }

  @Override
  public void close() throws IOException {
    sneakyThrow(throwable);
    throw (IOException) throwable;
  }

  @Override
  public void flush() throws IOException {
    sneakyThrow(throwable);
    throw (IOException) throwable;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    sneakyThrow(throwable);
    throw (IOException) throwable;
  }
}
