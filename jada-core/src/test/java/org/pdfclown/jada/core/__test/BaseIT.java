/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BaseIT.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.__test;

import static org.pdfclown.jada.core.JadaConfig.OPTION__DEBUG;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.jada.core.test.JadaIT;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertArgs;

/**
 * Module-specific integration test.
 *
 * @author Stefano Chizzolini
 */
public abstract class BaseIT extends JadaIT {
  protected BaseIT() {
  }

  protected BaseIT(@Nullable Class<?> sourceType) {
    super(sourceType);
  }

  protected BaseIT(List<String> sourcePackageNames) {
    super(sourcePackageNames);
  }

  protected BaseIT(@Nullable ProjectDirId sourceDirId, List<String> sourcePackageNames) {
    super(sourceDirId, sourcePackageNames);
  }

  protected BaseIT(@Nullable ProjectDirId sourceDirId, String sourcePackageName) {
    super(sourceDirId, sourcePackageName);
  }

  protected BaseIT(String sourcePackageName) {
    super(sourcePackageName);
  }

  @Override
  protected JavadocAssertArgs javadocArgs() {
    return super.javadocArgs()
        .arg(OPTION__DEBUG);
  }
}
