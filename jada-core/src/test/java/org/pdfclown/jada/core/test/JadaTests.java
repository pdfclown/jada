/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaTests.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.system.MessageManager;
import org.pdfclown.jada.core.system.SystemConfig;
import org.pdfclown.jada.core.test.assertion.JadaLogCaptor;

/**
 * Common utilities for Jada unit testing.
 *
 * @author Stefano Chizzolini
 */
public final class JadaTests {
  /**
   * Mocks Jada configuration.
   *
   * @return An object with the following features:
   *         <ul>
   *         <li>{@link #mockSystemConfig(Class, JadaLogCaptor) mockSystemConfig} features</li>
   *         <li>{@link JadaConfig#getJada() getJada()}: {@link Jada} mock</li>
   *         </ul>
   */
  public static JadaConfig mockJadaConfig(@Nullable JadaLogCaptor logCaptor) {
    var ret = mockSystemConfig(JadaConfig.class, logCaptor);
    {
      var jada = mock(Jada.class);
      {
        when(jada.getConfig()).thenReturn(ret);
        var log = ret.getLog();
        when(jada.getLog()).thenReturn(log);
      }
      when(ret.getJada()).thenReturn(jada);
    }
    return ret;
  }

  /**
   * Mocks Jada system configuration.
   *
   * @return An object with the following features:
   *         <ul>
   *         <li>{@link SystemConfig#getLocale() getLocale()}: {@link Locale#US}</li>
   *         <li>{@link SystemConfig#getLog() getLog()}:
   *         {@code logCaptor.}{@link JadaLogCaptor#createLog(SystemConfig) createLog(..)}</li>
   *         <li>{@link SystemConfig#getMessageManager() getMessageManager()}:
   *         {@link MessageManager}</li>
   *         <li>{@link SystemConfig#getOutputCharset() getOutputCharset()}:
   *         {@link StandardCharsets#UTF_8}</li>
   *         </ul>
   */
  public static <T extends SystemConfig> T mockSystemConfig(Class<T> type,
      @Nullable JadaLogCaptor logCaptor) {
    T ret = mock(type);
    {
      //noinspection resource
      var log = (logCaptor != null ? logCaptor : new JadaLogCaptor()).createLog(ret);

      when(ret.getInputCharset()).thenReturn(UTF_8);
      when(ret.getOutputCharset()).thenReturn(UTF_8);
      when(ret.getLocale()).thenReturn(Locale.ROOT);
      when(ret.getMessageManager()).thenReturn(new MessageManager(ret));
      when(ret.getLog()).thenReturn(log);
    }
    return ret;
  }

  /**
   * Mocks Jada system configuration.
   *
   * @return An object with the following features:
   *         <ul>
   *         <li>{@link SystemConfig#getLocale() getLocale()}: {@link Locale#US}</li>
   *         <li>{@link SystemConfig#getLog() getLog()}:
   *         {@code logCaptor.}{@link JadaLogCaptor#createLog(SystemConfig) createLog(..)}</li>
   *         <li>{@link SystemConfig#getMessageManager() getMessageManager()}:
   *         {@link MessageManager}</li>
   *         <li>{@link SystemConfig#getOutputCharset() getOutputCharset()}:
   *         {@link StandardCharsets#UTF_8}</li>
   *         </ul>
   */
  public static SystemConfig mockSystemConfig(@Nullable JadaLogCaptor logCaptor) {
    return mockSystemConfig(SystemConfig.class, logCaptor);
  }

  private JadaTests() {
  }
}
