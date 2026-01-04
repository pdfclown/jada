/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Internals.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.internal;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;
import org.pdfclown.common.util.annot.UnmodifiableView;
import org.pdfclown.common.util.reflect.Reflects;
import org.pdfclown.jada.core.JadaScriptExtension;

/**
 * Internal utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Internals {
  public static final String TAG_PREFIX__JADA = "jada.";

  private static final Map<String, String> componentNames = new HashMap<>();

  /**
   * Jada component names by package name.
   */
  public static @UnmodifiableView Map<String, String> getComponentNames() {
    return unmodifiableMap(componentNames);
  }

  /**
   * Registers a component for name resolution.
   */
  public static void registerComponentName(Object component) {
    componentNames.put(component instanceof JadaScriptExtension
        ? component.getClass().getSimpleName()
        : component.getClass().getPackageName(), Reflects.get(component, "getName"));
  }

  private Internals() {
  }
}
