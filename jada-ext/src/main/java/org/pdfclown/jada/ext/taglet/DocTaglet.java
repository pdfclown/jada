/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DocTaglet.java) is part of jada-ext module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.ext.taglet;

/**
 * Javadoc fragment definition taglet ({@code @jada.doc} tag).
 * <p>
 * Defines a Javadoc fragment for reuse via {@link ReuseDocTaglet @jada.reuseDoc} tag.
 * </p>
 * <p>
 * For more information about usage and configuration, see {@link DocReuseTaglet}.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class DocTaglet extends DocReuseTaglet {
  public static final String NAME = "jada.doc";

  @Override
  public String getName() {
    return NAME;
  }
}
