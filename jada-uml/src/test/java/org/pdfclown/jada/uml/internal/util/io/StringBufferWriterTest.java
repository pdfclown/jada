/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (StringBufferWriterTest.java) is part of jada-uml module in Jada project
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.rendering.writers.StringBufferingWriterTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class StringBufferWriterTest extends BaseTest {
  // SourceName: createWithNullDelegate
  @Test
  void _withNullDelegate() {
    NullPointerException expected = assertThrows(NullPointerException.class,
        () -> new StringBufferWriter(null));

    assertThat(expected.getMessage(), notNullValue());
  }

  // SourceName: testGetBuffer
  @Test
  void getBuffer() throws IOException {
    final var writer = new StringBufferWriter(new NopWriter());
    final StringBuffer buffer = writer.getBuffer();
    writer.write("The quick brown fox jumps over the lazy dog");
    writer.flush();

    assertThat(buffer, hasToString("The quick brown fox jumps over the lazy dog"));
  }

  // SourceName: testToString
  @Test
  void toString_() {
    assertThat(new StringBufferWriter(new NopWriter()),
        hasToString("StringBufferWriter [NopWriter]"));
  }
}
