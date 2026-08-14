/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (NopWriter.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.internal.util.io;

import java.io.Writer;
import org.jspecify.annotations.Nullable;

// SourceName: nl.talsmasoftware.umldoclet.rendering.writers.NoopWriter
/**
 * Simple writer that does nothing, for testing purposes only.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
@SuppressWarnings("ConditionCoveredByFurtherCondition")
final class NopWriter extends Writer {
  @Override
  public void close() {
    // NOP
  }

  @Override
  public boolean equals(@Nullable Object o) {
    return this == o || o instanceof NopWriter;
  }

  @Override
  public void flush() {
    // NOP
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public void write(char[] cbuf, int off, int len) {
    // NOP
  }
}
