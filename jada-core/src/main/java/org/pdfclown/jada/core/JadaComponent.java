/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaComponent.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import javax.tools.Diagnostic.Kind;

/**
 * {@link Jada} component.
 * <p>
 * Represents an element of the Jada component model (either a {@linkplain Jada doclet} or an
 * {@linkplain JadaExtension extension}).
 * </p>
 *
 * @author Stefano Chizzolini
 */
public interface JadaComponent extends JadaObject {
  /**
   * Execution status.
   *
   * @author Stefano Chizzolini
   */
  enum Status {
    ERROR(Kind.ERROR),
    WARNING(Kind.MANDATORY_WARNING, Kind.WARNING),
    SUCCESS(Kind.OTHER);

    /**
     * Gets the status corresponding to the diagnostic kind.
     */
    public static Status of(Kind kind) {
      /*
       * NOTE: The assumption here is that `javax.tools.Diagnostic.Kind` enum order by severity will
       * be preserved by future JDK versions.
       */
      for (var value : values()) {
        if (value.level.compareTo(kind) >= 0)
          return value;
      }
      return SUCCESS;
    }

    private final Kind level;
    private final Kind threshold;

    Status(Kind threshold) {
      this(threshold, threshold);
    }

    Status(Kind threshold, Kind level) {
      this.threshold = threshold;
      this.level = level;
    }

    /**
     * Default diagnostic level.
     */
    public Kind getLevel() {
      return level;
    }

    /**
     * Minimum diagnostic level.
     */
    public Kind getThreshold() {
      return threshold;
    }
  }

  /**
   * Number of errors encountered so far by this component.
   */
  int getErrorCount();

  /**
   * Component status.
   * <p>
   * Corresponds to the most severe event encountered so far.
   * </p>
   */
  default Status getStatus() {
    /*
     * NOTE: This corresponds to the javadoc tool's algorithm used after the doclet execution to
     * detect the problem level (see `jdk.javadoc.internal.tool.Start.begin(List<String>, Iterable<?
     * extends JavaFileObject>)`).
     */
    if (getErrorCount() > 0)
      return Status.ERROR;
    else if (getWarningCount() > 0)
      return Status.WARNING;
    else
      return Status.SUCCESS;
  }

  /**
   * Number of warnings encountered so far by this component.
   */
  int getWarningCount();

  /**
   * Whether the execution of this component has been successful so far (that is, no problem which
   * prevents the javadoc tool's normal completion happened).
   */
  default boolean isSuccess() {
    var status = getStatus();
    return switch (status) {
      case ERROR -> false;
      case WARNING -> !getConfig().isWarningRejected();
      case SUCCESS -> true;
    };
  }
}
