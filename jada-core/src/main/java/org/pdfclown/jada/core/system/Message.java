/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Message.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.system;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.jspecify.annotations.Nullable;

/**
 * Message resource.
 *
 * @author Stefano Chizzolini
 * @implSpec Implementations are expected to be backed by corresponding {@linkplain ResourceBundle
 *           resource bundle} files ({@link #getBundleName() %BUNDLE_NAME%}{@code .properties}),
 *           where each entry is assigned its {@linkplain MessageFormat parameterized} message
 *           string, like this:<pre><code>
 *             COMPONENTS=\
 *             Selected components:\n\
 *             {0}</code></pre>
 */
public interface Message {
  /**
   * Name of the resource bundle containing this message.
   *
   * @implSpec Should correspond to a fully-qualified class name.
   * @implNote The default implementation corresponds to the fully-qualified class name of the
   *           implementing class.
   */
  default String getBundleName() {
    return getClass().getName();
  }

  /**
   * Key to the {@linkplain #getBundleName() bundled} resource.
   */
  String getKey();

  /**
   * Resolves this message.
   */
  default String toString(SystemConfig config, @Nullable Object... args) {
    return config.getMessageManager().getText(this, args);
  }
}
