/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PluginConfig.java) is part of jada-maven-plugin module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.maven;

import static org.pdfclown.common.util.Exceptions.unsupported;

import com.sun.source.util.DocTreePath;
import java.util.Locale;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import org.apache.maven.plugin.logging.Log;
import org.pdfclown.jada.core.system.SystemConfig;

/**
 * Jada configuration adapter for Maven.
 *
 * @author Stefano Chizzolini
 */
class PluginConfig extends SystemConfig {
  /**
   * Javadoc logger adapter for Maven.
   *
   * @author Stefano Chizzolini
   */
  private static class Reporter implements jdk.javadoc.doclet.Reporter {
    private final Log base;

    Reporter(Log base) {
      this.base = base;
    }

    @Override
    public void print(Kind kind, DocTreePath path, String message)
        throws UnsupportedOperationException {
      throw unsupported();
    }

    @Override
    public void print(Kind kind, Element element, String message)
        throws UnsupportedOperationException {
      throw unsupported();
    }

    @Override
    public void print(Kind kind, String message) {
      switch (kind) {
        case MANDATORY_WARNING:
        case WARNING:
          base.warn(message);
          break;
        case ERROR:
          base.error(message);
          break;
        case NOTE:
          base.info(message);
          break;
        default:
          base.debug(message);
      }
    }
  }

  PluginConfig(Locale locale, Log log) {
    super(locale, new Reporter(log));

    setQuiet(!log.isInfoEnabled());
    setVerbose(log.isDebugEnabled());
  }
}
