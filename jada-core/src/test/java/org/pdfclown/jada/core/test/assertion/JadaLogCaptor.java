/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaLogCaptor.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.test.assertion;

import static org.mockito.Mockito.mock;
import static org.pdfclown.common.util.Conditions.requireState;
import static org.pdfclown.common.util.Objects.objDo;

import com.sun.source.util.DocTreePath;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import jdk.javadoc.doclet.Reporter;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.test.assertion.LogCaptor;
import org.pdfclown.jada.core.system.Logger;
import org.pdfclown.jada.core.system.SystemConfig;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

/**
 * {@link LogCaptor} for Jada.
 */
public class JadaLogCaptor extends LogCaptor {
  protected static class CaptureAppender extends Logger {
    private final List<LoggingEvent> events = new ArrayList<>();

    private @Nullable Kind eventThreshold;

    private CaptureAppender(SystemConfig config) {
      super(config, mock(Reporter.class));
    }

    @Override
    public void print(Kind kind, DocTreePath path, @Nullable Object source, String message) {
      events.add(new Capture(level(kind), "@" + path + " - " + message, null));
    }

    @Override
    public void print(Kind kind, Element element, @Nullable Object source, String message) {
      events.add(new Capture(level(kind), "@" + element + " - " + message, null));
    }

    @Override
    public void print(Kind kind, FileObject file, int start, int pos, int end,
        @Nullable Object source, String message) {
      events.add(new Capture(level(kind), "@" + file + ":" + pos + "(" + start + "-" + end + ") - "
          + message, null));
    }

    @Override
    public void print(Kind kind, @Nullable Object source, String message) {
      events.add(new Capture(level(kind), message, null));
    }

    @Override
    protected boolean isPrintable(Kind kind) {
      return eventThreshold != null ? isEnabled() && kind.compareTo(eventThreshold) <= 0
          : super.isPrintable(kind);
    }
  }

  private static Level level(Kind nativeLevel) {
    return switch (nativeLevel) {
      case OTHER -> Level.DEBUG;
      case NOTE -> Level.INFO;
      case WARNING, MANDATORY_WARNING -> Level.WARN;
      case ERROR -> Level.ERROR;
    };
  }

  private static @Nullable Kind nativeLevel(@Nullable Level level) {
    if (level == null)
      return null;

    return switch (level) {
      case INFO -> Kind.NOTE;
      case WARN -> Kind.WARNING;
      case ERROR -> Kind.ERROR;
      case DEBUG, TRACE -> Kind.OTHER;
    };
  }

  private @Nullable CaptureAppender appender;

  public Logger createLog(SystemConfig config) {
    return appender = new CaptureAppender(config);
  }

  @Override
  public List<LoggingEvent> getEvents() {
    return getAppender().events;
  }

  @Override
  public void reset() {
    getAppender().events.clear();
  }

  @Override
  public void start() {
    /*
     * NOTE: On test class initialization, the appender may not be ready yet.
     */
    objDo(appender, $ -> $.setEnabled(true));
  }

  @Override
  public void stop() {
    getAppender().setEnabled(false);
  }

  @Override
  protected void attach() {
    // NOP
  }

  @Override
  protected boolean detach() {
    // NOP
    return false;
  }

  protected CaptureAppender getAppender() {
    return requireState(appender, "`createLog(*)` MUST be called before operating this captor");
  }

  @Override
  protected void onLevelChanged() {
    getAppender().eventThreshold = nativeLevel(getLevel());
  }
}
