/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SystemConfig.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.system;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.io.Files.normal;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic.Kind;
import jdk.javadoc.doclet.Reporter;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.XtEnum;

/**
 * System configuration.
 * <p>
 * Represents a top-level configuration which contains global application state.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public abstract class SystemConfig implements SystemObject {
  /**
   * Message level.
   *
   * @author Stefano Chizzolini
   */
  public enum LogLevel implements XtEnum<Kind> {
    DEBUG(Kind.OTHER),
    INFO(Kind.NOTE),
    WARN(Kind.WARNING),
    ERROR(Kind.ERROR);

    private final Kind code;

    LogLevel(Kind code) {
      this.code = code;
    }

    @Override
    public Kind getCode() {
      return code;
    }
  }

  protected static final LogLevel LOG_LEVEL__NORMAL = LogLevel.INFO;
  protected static final LogLevel LOG_LEVEL__QUIET = LogLevel.WARN;
  protected static final LogLevel LOG_LEVEL__VERBOSE = LogLevel.DEBUG;

  private @Nullable Path buildDirectory;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private List<Path> inputDirectories;
  private @Nullable String inputEncoding;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private Locale locale;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private Logger log;
  private LogLevel logLevel = LOG_LEVEL__NORMAL;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private MessageManager messageManager;
  private @Nullable Path outputDirectory;
  private @Nullable String outputEncoding;

  /**
   * <span class="warning">(For internal use only)</span>
   */
  protected SystemConfig() {
  }

  @SuppressWarnings("this-escape")
  protected SystemConfig(Locale locale, Reporter reporter) {
    this.locale = locale;

    log = creareLogger(reporter);
    messageManager = new MessageManager(this);
    inputDirectories = new ArrayList<>();
  }

  /**
   * Target directory.
   * <p>
   * Useful for temporary files, such as caches.
   * </p>
   * <p>
   * By default, corresponds to the parent of {@link #getOutputDirectory() outputDirectory}.
   * </p>
   */
  public Path getBuildDirectory() {
    return requireNonNull(buildDirectory);
  }

  @Override
  public SystemConfig getConfig() {
    return this;
  }

  /**
   * Source charset.
   *
   * @implSpec Corresponds to {@link #getInputEncoding() inputEncoding} if specified, otherwise
   *           {@linkplain StandardCharsets#UTF_8 UTF-8}.
   */
  public Charset getInputCharset() {
    return inputEncoding != null ? Charset.forName(inputEncoding) : UTF_8;
  }

  /**
   * Source directories.
   */
  public List<Path> getInputDirectories() {
    return inputDirectories;
  }

  /**
   * Source encoding.
   * <p>
   * Used also for output if {@link #getOutputEncoding() outputEncoding} is undefined.
   * </p>
   */
  public @Nullable String getInputEncoding() {
    return inputEncoding;
  }

  public Locale getLocale() {
    return locale;
  }

  @Override
  public Logger getLog() {
    return log;
  }

  /**
   * Displayed messages' threshold.
   * <p>
   * Default: {@link #LOG_LEVEL__NORMAL}
   * </p>
   */
  public LogLevel getLogLevel() {
    return logLevel;
  }

  public MessageManager getMessageManager() {
    return messageManager;
  }

  /**
   * Documentation charset.
   * <p>
   * Used for textual formats such as HTML.
   * </p>
   *
   * @implSpec Corresponds to {@link #getOutputEncoding() outputEncoding} if specified, otherwise
   *           {@link #getInputCharset() inputCharset}.
   */
  public Charset getOutputCharset() {
    return outputEncoding != null ? Charset.forName(outputEncoding) : getInputCharset();
  }

  /**
   * Destination directory, in which transformed input is saved.
   * <p>
   * Default: current directory.
   * </p>
   */
  public Path getOutputDirectory() {
    return requireNonNull(outputDirectory);
  }

  /**
   * Documentation encoding.
   * <p>
   * Used for textual formats such as HTML.
   * </p>
   */
  public @Nullable String getOutputEncoding() {
    return outputEncoding;
  }

  /**
   * Whether only errors and warnings are displayed.
   */
  public boolean isQuiet() {
    return logLevel.compareTo(LOG_LEVEL__QUIET) >= 0;
  }

  /**
   * Whether all messages, including debug, are displayed.
   */
  public boolean isVerbose() {
    return logLevel.compareTo(LOG_LEVEL__VERBOSE) <= 0;
  }

  /**
   * Sets {@link #getBuildDirectory() buildDirectory}.
   */
  public SystemConfig setBuildDirectory(Path value) {
    buildDirectory = value;
    return this;
  }

  /**
   * Sets {@link #getInputEncoding() inputEncoding}.
   */
  public SystemConfig setInputEncoding(@Nullable String value) {
    inputEncoding = value;
    return this;
  }

  /**
   * Sets {@link #getLogLevel() logLevel}.
   */
  public SystemConfig setLogLevel(LogLevel value) {
    logLevel = value;
    return this;
  }

  /**
   * Sets {@link #getOutputDirectory() outputDirectory}.
   */
  public SystemConfig setOutputDirectory(Path value) {
    outputDirectory = normal(value);
    if (!Files.isDirectory(outputDirectory))
      throw wrongArg(null, value, "\"{}\" NOT FOUND", outputDirectory);

    if (buildDirectory == null) {
      buildDirectory = outputDirectory.getParent();
    }
    return this;
  }

  /**
   * Sets {@link #getOutputEncoding() outputEncoding}.
   */
  public SystemConfig setOutputEncoding(@Nullable String value) {
    outputEncoding = value;
    return this;
  }

  /**
   * Sets {@link #isQuiet() quiet}.
   */
  public SystemConfig setQuiet(boolean value) {
    if (value) {
      setLogLevel(LOG_LEVEL__QUIET);
    } else if (isQuiet()) {
      setLogLevel(LOG_LEVEL__NORMAL);
    }
    return this;
  }

  /**
   * Sets {@link #isVerbose() verbose}.
   */
  public SystemConfig setVerbose(boolean value) {
    if (value) {
      setLogLevel(LOG_LEVEL__VERBOSE);
    } else if (isVerbose()) {
      setLogLevel(LOG_LEVEL__NORMAL);
    }
    return this;
  }

  protected Logger creareLogger(Reporter reporter) {
    return new Logger(this, reporter);
  }
}
