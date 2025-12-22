/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaExtension.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import static org.pdfclown.common.util.Exceptions.unexpected;

import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic.Kind;
import org.greenrobot.eventbus.Subscribe;
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.jada.core.event.MainProcessEvent;
import org.pdfclown.jada.core.event.PostProcessEvent;

/**
 * {@linkplain Jada Jada doclet} extension.
 *
 * @author Stefano Chizzolini
 */
public abstract class JadaExtension implements JadaComponent {
  private int errorCount;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull Jada jada;
  private int warningCount;

  protected JadaExtension() {
  }

  @Override
  public int getErrorCount() {
    return errorCount;
  }

  /**
   * Configuration of this doclet extension.
   */
  public abstract JadaExtConfig getExtConfig();

  /**
   * Main doclet.
   */
  @Override
  public final Jada getJada() {
    return jada;
  }

  /**
   * Latest version of the Java Programming Language supported by this component.
   *
   * @implNote By default, this component is assumed as unlimitedly compatible with the Java
   *           Programming Language.
   */
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public int getWarningCount() {
    return warningCount;
  }

  /**
   * Initializes this doclet extension.
   *
   * @param options
   *          Option builder, to specify supported CLI options.
   * @param jada
   *          Main doclet, to access Javadoc generation context.
   * @implSpec Implementers must call the overridden method.
   */
  public void init(JadaOptions options, Jada jada) {
    this.jada = jada;
  }

  @Subscribe
  public void onMainProcess(MainProcessEvent event) {
  }

  @Subscribe
  public void onPostProcess(PostProcessEvent event) {
  }

  /**
   * Terminates this doclet extension, when the doclet session is complete.
   *
   * @implSpec Implementers must call the overridden method.
   */
  public void term() {
  }

  /**
   * Notifies a problem encountered by this extension.
   */
  void onProblem(Kind kind) {
    var problemStatus = Status.of(kind);
    switch (problemStatus) {
      case ERROR:
        errorCount++;
        jada.extErrorCount++;
        break;
      case WARNING:
        warningCount++;
        jada.extWarningCount++;
        break;
      default:
        throw unexpected(kind);
    }
  }
}
