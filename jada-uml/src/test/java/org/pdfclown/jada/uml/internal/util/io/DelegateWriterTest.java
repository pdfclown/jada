/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DelegateWriterTest.java) is part of jada-uml module in Jada project
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.rendering.writers.DelegatingWriterTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class DelegateWriterTest extends BaseTest {
  // SourceName: testCloseCombinedSuccessAndException
  @Test
  void _closeCombinedSuccessAndException() throws IOException {
    ThrowWriter throwingWriter = ThrowWriter.throwing(
        new IllegalStateException("Illegal state!"));
    var mockWriter = mock(Writer.class);
    var delegatingWriter = new DelegateWriter(throwingWriter, mockWriter);

    final RuntimeException expected = assertThrows(RuntimeException.class, delegatingWriter::close);
    assertThat(expected.getMessage(), is("Illegal state!"));
    verify(mockWriter).close();
  }

  // SourceName: testCloseMultipleExceptions
  @Test
  void _closeMultipleExceptions() {
    var expectedException1 = new IOException("IO error!");
    var expectedException2 = new IllegalStateException("Illegal state!");
    var delegatingWriter = new DelegateWriter(ThrowWriter.throwing(expectedException1),
        ThrowWriter.throwing(expectedException2));

    final IOException ioe = assertThrows(IOException.class, delegatingWriter::close);
    Throwable[] suppressed = ioe.getSuppressed();
    assertThat(suppressed, is(notNullValue()));
    assertThat(suppressed.length, is(2));
    assertThat(suppressed[0], is(sameInstance(expectedException1)));
    assertThat(suppressed[1], is(sameInstance(expectedException2)));
  }

  // SourceName: testCloseSingleIOException
  @Test
  void _closeSingleIOException() {
    final var ioException = new IOException("IO error!");
    var delegatingWriter = new DelegateWriter(ThrowWriter.throwing(ioException));

    assertThat(
        assertThrows(IOException.class, delegatingWriter::close),
        is(sameInstance(ioException)));
  }

  // SourceName: testCloseSingleRuntimeException
  @Test
  void _closeSingleRuntimeException() {
    final var runtimeException = new IllegalStateException("Illegal state!");
    var delegatingWriter = new DelegateWriter(ThrowWriter.throwing(runtimeException));

    assertThat(
        assertThrows(RuntimeException.class, delegatingWriter::close),
        is(sameInstance(runtimeException)));
  }

  // SourceName: testEmptyDelegatingWriter
  @Test
  void _emptyDelegatingWriter() throws IOException {
    var delegatingWriter = new DelegateWriter();
    delegatingWriter.write("The quick brown fox jumps over the lazy dog");
    delegatingWriter.flush();
    delegatingWriter.close();

    assertThat(delegatingWriter, hasToString("DelegateWriter[]"));
  }

  // SourceName: testFlushCombinedSuccessAndException
  @Test
  void _flushCombinedSuccessAndException() throws IOException {
    ThrowWriter throwingWriter = ThrowWriter.throwing(new IllegalStateException("Illegal state!"));
    var mockWriter = mock(Writer.class);
    var delegatingWriter = new DelegateWriter(throwingWriter, mockWriter);

    final RuntimeException expected = assertThrows(RuntimeException.class, delegatingWriter::flush);
    assertThat(expected.getMessage(), is("Illegal state!"));
    verify(mockWriter).flush();
  }

  // SourceName: testFlushMultipleExceptions
  @Test
  void _flushMultipleExceptions() {
    var expectedException1 = new IOException("IO error!");
    var expectedException2 = new IllegalStateException("Illegal state!");
    var delegatingWriter = new DelegateWriter(ThrowWriter.throwing(expectedException1),
        ThrowWriter.throwing(expectedException2));

    final IOException ioe = assertThrows(IOException.class, delegatingWriter::flush);
    Throwable[] suppressed = ioe.getSuppressed();
    assertThat(suppressed, is(notNullValue()));
    assertThat(suppressed.length, is(2));
    assertThat(suppressed[0], is(sameInstance(expectedException1)));
    assertThat(suppressed[1], is(sameInstance(expectedException2)));
  }

  // SourceName: testFlushSingleIOException
  @Test
  void _flushSingleIOException() {
    final var ioException = new IOException("IO error!");
    var delegatingWriter = new DelegateWriter(ThrowWriter.throwing(ioException));

    assertThat(
        assertThrows(IOException.class, delegatingWriter::flush),
        is(sameInstance(ioException)));
  }

  // SourceName: testFlushSingleRuntimeException
  @Test
  void _flushSingleRuntimeException() {
    final var runtimeException = new IllegalStateException("Illegal state!");
    var delegatingWriter = new DelegateWriter(ThrowWriter.throwing(runtimeException));

    assertThat(
        assertThrows(RuntimeException.class, delegatingWriter::flush),
        is(sameInstance(runtimeException)));
  }

  // SourceName: testSingleDelegation
  @Test
  void _singleDelegation() throws IOException {
    var stringDelegate = new StringWriter();
    var delegatingWriter = new DelegateWriter(stringDelegate);
    delegatingWriter.write("The quick brown fox jumps over the lazy dog");
    delegatingWriter.flush();
    delegatingWriter.close();

    assertThat(stringDelegate, hasToString("The quick brown fox jumps over the lazy dog"));
  }

  // SourceName: testWriteCombinedSuccessAndException
  @Test
  void _writeCombinedSuccessAndException() {
    ThrowWriter throwingWriter = ThrowWriter.throwing(new IllegalStateException("Illegal state!"));
    StringWriter stringWriter = new StringWriter();
    var delegatingWriter = new DelegateWriter(throwingWriter, stringWriter);

    final RuntimeException expected = assertThrows(RuntimeException.class,
        () -> delegatingWriter.write("The quick brown fox jumps over the lazy dog"));
    assertThat(expected.getMessage(), is("Illegal state!"));
    assertThat(stringWriter, hasToString("The quick brown fox jumps over the lazy dog"));
  }

  // SourceName: testWriteMultipleExceptions
  @Test
  void _writeMultipleExceptions() {
    IOException expectedException1 = new IOException("IO error!");
    RuntimeException expectedException2 = new IllegalStateException("Illegal state!");
    var delegatingWriter = new DelegateWriter(ThrowWriter.throwing(expectedException1),
        ThrowWriter.throwing(expectedException2));

    IOException ioe = assertThrows(IOException.class,
        () -> delegatingWriter.write("The quick brown fox jumps over the lazy dog"));
    Throwable[] suppressed = ioe.getSuppressed();
    assertThat(suppressed, is(notNullValue()));
    assertThat(suppressed.length, is(2));
    assertThat(suppressed[0], is(sameInstance(expectedException1)));
    assertThat(suppressed[1], is(sameInstance(expectedException2)));
  }

  // SourceName: testWriteSingleIOException
  @Test
  void _writeSingleIOException() {
    final IOException ioException = new IOException("IO error!");
    var delegatingWriter = new DelegateWriter(ThrowWriter.throwing(ioException));

    assertThat(
        assertThrows(IOException.class,
            () -> delegatingWriter.write("The quick brown fox jumps over the lazy dog")),
        is(sameInstance(ioException)));
  }

  // SourceName: testWriteSingleRuntimeException
  @Test
  void _writeSingleRuntimeException() {
    final RuntimeException runtimeException = new IllegalStateException("Illegal state!");
    var delegatingWriter = new DelegateWriter(ThrowWriter.throwing(runtimeException));

    assertThat(
        assertThrows(RuntimeException.class,
            () -> delegatingWriter.write("The quick brown fox jumps over the lazy dog")),
        is(sameInstance(runtimeException)));
  }
}
