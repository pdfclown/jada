/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaScriptManager.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import static org.pdfclown.common.util.Objects.loaderOf;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__GROOVY;

import groovy.lang.GroovyShell;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Jada script manager.
 *
 * @author Stefano Chizzolini
 */
class JadaScriptManager {
  private final GroovyShell groovy;
  private final Jada jada;
  private final Map<String, Object> sharedData = new HashMap<>();

  JadaScriptManager(Jada jada) {
    this.jada = jada;

    groovy = new GroovyShell(loaderOf(jada));
  }

  public GroovyShell getGroovy() {
    return groovy;
  }

  public Jada getJada() {
    return jada;
  }

  public Map<String, Object> getSharedData() {
    return sharedData;
  }

  public void run(String phase) {
    var scriptFiles = jada.getConfig().getResources("scripts/" + phase + FILE_EXTENSION__GROOVY)
        .collect(Collectors.toCollection(ArrayDeque::new));
    if (scriptFiles.isEmpty())
      return;

    var context = new JadaScriptContext(this, phase, scriptFiles);
    context.callSuper();
  }
}
