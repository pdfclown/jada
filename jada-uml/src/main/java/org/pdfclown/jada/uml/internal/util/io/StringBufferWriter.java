/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (StringBufferWriter.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  Changes: Adaptation and redesign for Jada.
 */
/*
 * Copyright 2016-2026 Talsma ICT
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pdfclown.jada.uml.internal.util.io;

import static org.pdfclown.common.util.Objects.toStringWithValues;

import java.io.StringWriter;
import java.io.Writer;

// SourceName: nl.talsmasoftware.umldoclet.rendering.writers.StringBufferingWriter
/**
 * Delegates to another {@link Writer} retaining a {@link StringBuffer} of all written characters.
 * <p>
 * Manipulating the contained StringBuffer is not thread-safe.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class StringBufferWriter extends DelegateWriter {
  /**
   * New writer that delegates to the given one and also retains a {@link StringBuffer} of all
   * written characters.
   *
   * @param delegate
   *          The delegate writer to write to.
   */
  public StringBufferWriter(Writer delegate) {
    super(new StringWriter(), delegate);
  }

  /**
   * A buffer of the written characters.
   * <p>
   * Changes to this buffer do not propagate towards the delegate writer. Furthermore, write
   * operations on this writer and buffer changes are not considered thread-safe and should be
   * avoided.
   * </p>
   *
   * @return A StringBuffer of the written characters.
   */
  public StringBuffer getBuffer() {
    return ((StringWriter) delegates.get(0)).getBuffer();
  }

  /**
   * @return The name of this class plus the wrapped delegate writer.
   */
  @Override
  public String toString() {
    return toStringWithValues(this, delegates.get(1));
  }
}
