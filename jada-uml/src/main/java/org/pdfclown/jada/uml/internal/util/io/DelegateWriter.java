/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DelegateWriter.java) is part of jada-uml module in Jada project
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

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Objects.toStringWithValues;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.pdfclown.common.util.annot.ReadOnly;

// SourceName: nl.talsmasoftware.umldoclet.rendering.writers.DelegatingWriter
/**
 * Base implementation that delegates writing to one or more delegate writers.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class DelegateWriter extends Writer {
  /**
   * The list of {@linkplain Writer delegate writers} to write to.
   */
  protected final @ReadOnly List<Writer> delegates;

  /**
   * New writer that writes to all the delegates when written to.
   */
  public DelegateWriter(Writer... delegates) {
    final var writers = new ArrayList<Writer>(requireNonNull(delegates, "`delegates`").length);
    for (Writer delegate : delegates) {
      writers.add(requireNonNull(delegate, "`delegate`"));
    }
    this.delegates = unmodifiableList(writers);
  }

  /**
   * Delegates the close operation to all delegates and merges any occurred exceptions into a single
   * {@link IOException}.
   *
   * @throws IOException
   *           if at least one of the delegate writers threw an exception while closing. <em>Please
   *           note:</em> Attempts are made to close all delegates.
   */
  @Override
  public void close() throws IOException {
    final var exceptions = new ArrayList<Exception>(delegates.size());
    for (Writer delegate : delegates) {
      try {
        delegate.close();
      } catch (IOException | RuntimeException ex) {
        exceptions.add(ex);
      }
    }
    if (!exceptions.isEmpty())
      throw mergeExceptions("closing", exceptions);
  }

  /**
   * Delegates the flush operation to all delegates and merges any occurred exceptions into a single
   * {@link IOException}.
   *
   * @throws IOException
   *           if at least one of the delegate writers threw an exception while flushing.
   */
  @Override
  public void flush() throws IOException {
    final var exceptions = new ArrayList<Exception>(delegates.size());
    for (Writer delegate : delegates) {
      try {
        delegate.flush();
      } catch (IOException | RuntimeException ex) {
        exceptions.add(ex);
      }
    }
    if (!exceptions.isEmpty())
      throw mergeExceptions("flushing", exceptions);
  }

  @Override
  public String toString() {
    return toStringWithValues(this, delegates);
  }

  /**
   * Delegates the write operation to all delegates and merges any occurred exceptions into a single
   * {@link IOException}.
   *
   * @param cbuf
   *          The buffer containing the characters to be written.
   * @param off
   *          The offset index to write from.
   * @param len
   *          The number of characters to write.
   * @throws IOException
   *           if at least one of the delegate writers threw an exception while writing. NOTE: It is
   *           very well possible that other delegates were successfully written.
   */
  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    final var exceptions = new ArrayList<Exception>(delegates.size());
    for (Writer delegate : delegates) {
      try {
        delegate.write(cbuf, off, len);
      } catch (IOException | RuntimeException ex) {
        exceptions.add(ex);
      }
    }
    if (!exceptions.isEmpty())
      throw mergeExceptions("writing", exceptions);
  }

  /**
   * Creates a single {@link IOException} merging potentially multiple cause exceptions into it.
   * Having this as a separate method helps to avoid unnecessary wrapping for the 'single exception'
   * case.
   * <p>
   * Only in case a non-{@code IO} checked exception or multiple exceptions occurred, this method
   * will create a new IOException with message {@code "Error [ACTIONVERB] delegate writer!"} or
   * {@code "Error [ACTIONVERB] N delegate writers!"} whatever may be the case.
   * </p>
   *
   * @param actionVerb
   *          A verb describing the action, for example {@code "writing"}, {@code "flushing"} or
   *          {@code "closing"}.
   * @param exceptions
   *          The exceptions to merge into one IOException.
   * @return The merged IOException.
   */
  private IOException mergeExceptions(String actionVerb, Collection<Exception> exceptions) {
    if (exceptions.size() == 1) {
      Exception exception = exceptions.iterator().next();
      if (exception instanceof RuntimeException runtimeException)
        throw runtimeException;
      else if (exception instanceof IOException ioException)
        return ioException;
    }

    var ret = new IOException("Error %s %s delegate writers!".formatted(actionVerb,
        exceptions.size()));
    exceptions.forEach(ret::addSuppressed);
    return ret;
  }
}
