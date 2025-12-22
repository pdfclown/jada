/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FileProcessor.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.system.proc;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.jada.core.internal.JadaMessage.P__OVERVIEW;

import java.nio.file.Path;
import javax.tools.Diagnostic.Kind;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.jada.core.internal.JadaMessage;
import org.pdfclown.jada.core.system.SystemConfig;
import org.pdfclown.jada.core.system.SystemObject;

/**
 * File processor.
 *
 * @param <T>
 *          Deserialized file content type.
 * @author Stefano Chizzolini
 */
public abstract class FileProcessor<T> implements SystemObject {
  protected FileSerializer<T> serializer;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull SystemConfig config;

  @SuppressWarnings("null")
  protected FileProcessor(FileSerializer<T> serializer) {
    this.serializer = serializer;
  }

  /**
   * Creates a message reporting the current status of this processor.
   * <p>
   * Typically invoked by {@link FileProcess} in case of failure, to provide details about
   * anomalies, pending processing, ...
   * </p>
   */
  public String createStatusMessage() {
    return EMPTY;
  }

  /**
   * Ends a processing session.
   *
   * @param success
   *          Whether the session completed normally, without failures.
   */
  public void end(boolean success) {
  }

  /**
   * Configuration.
   */
  @Override
  public SystemConfig getConfig() {
    return config;
  }

  /**
   * Processing priority.
   * <p>
   * Ensures the processing order of the processors over an execution, independently of their
   * {@linkplain FileProcess#addProcessor(FileProcessor) registration} order.
   * </p>
   *
   * @return Zero, for normal priority; less than zero, for higher-than-normal priority; more than
   *         zero, for lower-than-normal priority.
   * @implSpec Typically, processors adding new contents should define a higher-than-normal priority
   *           (say, {@code -10}); conversely, processors altering existing contents should define a
   *           lower-than-normal priority (say, {@code 10}). This way, additions have more chances
   *           of being placed in a predictable manner and can be transformed along with the
   *           existing contents by the successive processors.
   */
  public abstract int getPriority();

  /**
   * Initializes this processor.
   */
  public void init(SystemConfig config) {
    this.config = requireNonNull(config, "`config`");
  }

  /**
   * Gets whether the file can be processed by this processor.
   */
  public abstract boolean isProcessable(Path file, FileProcess.Context context);

  /**
   * Loads the content at the file.
   *
   * @return File content.
   */
  public T load(Path file) {
    try {
      return serializer.deserialize(file);
    } catch (RuntimeException ex) {
      throw runtime("Deserialization of {} FAILED", file, ex);
    }
  }

  /**
   * Processes the file content.
   *
   * @return Processed content.
   */
  public final T process(T content, Path file, FileProcess.Context context) {
    var ret = processContent(content, file, context);
    return ret != null ? ret : content;
  }

  /**
   * Saves the file content.
   */
  public void save(T content, Path file) {
    try {
      serializer.serialize(content, file);
    } catch (RuntimeException ex) {
      throw runtime("Serialization of {} FAILED", file, ex);
    }

    getLog().print(Kind.NOTE, this, JadaMessage.FILE_PROCESSOR_ITEM_COMPLETE, file, P__OVERVIEW);
  }

  public void term() {
  }

  /**
   * Processes the file content.
   *
   * @return Processed content ({@code null} is treated as no change occurred to {@code content}).
   * @implSpec Implementers MUST update the status ({@code context}) according to the processing
   *           advancement of this method.
   */
  protected abstract @Nullable T processContent(T content, Path file,
      FileProcess.Context context);
}
