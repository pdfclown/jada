/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaScriptContext.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import static org.pdfclown.common.util.Exceptions.runtime;

import groovy.lang.Binding;
import groovy.lang.Script;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Deque;
import java.util.Map;
import javax.tools.Diagnostic.Kind;
import org.jspecify.annotations.Nullable;

/**
 * Jada script context.
 * <p>
 * Scripts are defined as hooks in {@code scripts} {@linkplain JadaConfig#getResourceDirectories()
 * resource subdirectory}; each script instance is bound to a dedicated context accessible via
 * {@code self} variable.
 * </p>
 * <p>
 * Data can be shared with other scripts across the whole Jada execution via
 * {@link #put(String, Object)} and {@link #get(String)} methods.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class JadaScriptContext implements JadaObject {
  private final JadaScriptManager manager;
  private final String phase;
  private final Deque<Path> scriptFiles;
  private @Nullable Script superScript;

  JadaScriptContext(JadaScriptManager manager, String phase, Deque<Path> scriptFiles) {
    this.manager = manager;
    this.phase = phase;
    this.scriptFiles = scriptFiles;
  }

  /**
   * Calls the ancestor of this script, that is a script of the same phase belonging to the next
   * resource directory in the {@linkplain JadaConfig#getResourceDirectories() priority list}.
   *
   * @return Whether the ancestor script existed.
   */
  public boolean callSuper() {
    if (superScript == null) {
      if (scriptFiles.isEmpty())
        return false;

      var scriptFile = scriptFiles.pop();

      manager.getJada().getLog().print(Kind.NOTE, this, "{} phase: {} script hook running", phase,
          scriptFile);

      try {
        superScript = manager.getGroovy().parse(scriptFile.toFile());
        superScript.setBinding(new Binding(Map.of("self",
            new JadaScriptContext(manager, phase, scriptFiles))));
      } catch (IOException ex) {
        throw runtime("{} script loading FAILED", scriptFile, ex);
      }
    }
    superScript.run();
    return true;
  }

  /**
   * Retrieves the value associated to a name.
   */
  public Object get(String name) {
    return manager.getSharedData().get(name);
  }

  @Override
  public Jada getJada() {
    return manager.getJada();
  }

  /**
   * Jada execution phase.
   */
  public String getPhase() {
    return phase;
  }

  /**
   * Stores a value associated to the name.
   */
  public void put(String name, Object value) {
    manager.getSharedData().put(name, value);
  }
}
