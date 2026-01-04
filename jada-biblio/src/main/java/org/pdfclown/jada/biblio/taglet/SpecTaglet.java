/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (SpecTaglet.java) is part of jada-biblio module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.biblio.taglet;

import static org.pdfclown.jada.biblio.internal.Internals.TAG_PREFIX__BIBLIO;

/**
 * Normative publication taglet ({@code @biblio.spec} tag).
 * <p>
 * Represents a citation (that is, bibliographic reference) associated to a {@code <spec>} entry in
 * {@code biblio.xml} — see {@link PubTaglet} for further information.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class SpecTaglet extends PubTaglet {
  public static final String NAME = TAG_PREFIX__BIBLIO + "spec";

  @Override
  public String getName() {
    return NAME;
  }
}
