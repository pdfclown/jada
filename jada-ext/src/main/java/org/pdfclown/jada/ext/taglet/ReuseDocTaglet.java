/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ReuseDocTaglet.java) is part of jada-ext module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.ext.taglet;

/**
 * Javadoc fragment placeholder taglet ({@code @jada.reuseDoc} tag).
 * <p>
 * References a Javadoc fragment defined elsewhere via {@link DocTaglet @jada.doc} tag.
 * </p>
 * <p>
 * Useful to reuse and automatically synchronize Javadoc fragments, similarly to standard
 * {@code @inheritDoc} tag (the former is applicable everywhere, whilst the latter is specific to
 * element inheritance).
 * </p>
 * <p>
 * For more information about usage and configuration, see {@link DocReuseTaglet}.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class ReuseDocTaglet extends DocReuseTaglet {
  public static final String NAME = "jada.reuseDoc";

  @Override
  public String getName() {
    return NAME;
  }
}
