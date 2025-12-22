/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlMocks.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.uml.__test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.core.system.SystemConfig;
import org.pdfclown.jada.core.test.JadaMocks;
import org.pdfclown.jada.core.test.assertion.JadaLogCaptor;
import org.pdfclown.jada.uml.UmlConfig;

/**
 * JadaUML-related mocks.
 *
 * @author Stefano Chizzolini
 */
public final class UmlMocks {
  /**
   * Mocks JadaUML configuration.
   *
   * @return An object with the following features:
   *         <ul>
   *         <li>{@link UmlConfig#getConfig() getConfig()}:
   *         {@link JadaMocks#mockJadaConfig(JadaLogCaptor)}</li>
   *         <li>{@link UmlConfig#getLog() getLog()}:
   *         {@code logCaptor.}{@link JadaLogCaptor#createLog( SystemConfig ) createLog(..)}</li>
   *         </ul>
   */
  public static UmlConfig mockUmlConfig(@Nullable JadaLogCaptor logCaptor) {
    var ret = mock(UmlConfig.class);
    {
      var jadaConfig = JadaMocks.mockJadaConfig(logCaptor);
      when(ret.getConfig()).thenReturn(jadaConfig);
      when(ret.getLog()).thenCallRealMethod();
    }
    return ret;
  }

  private UmlMocks() {
  }
}
