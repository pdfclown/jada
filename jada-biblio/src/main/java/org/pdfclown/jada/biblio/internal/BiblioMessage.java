/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BiblioMessage.java) is part of jada-biblio module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.biblio.internal;

import org.pdfclown.jada.biblio.BiblioExtension;
import org.pdfclown.jada.core.system.Message;

/**
 * {@link BiblioExtension} message.
 *
 * @author Stefano Chizzolini
 */
public enum BiblioMessage implements Message {
  BIBLIO_DATA_ELEMENT_UNKNOWN,
  BIBLIO_ENTRY_DUPLICATE,
  BIBLIO_ENTRY_ID_INVALID,
  BIBLIO_ENTRY_NOT_FOUND,
  BIBLIO_ENTRY_OVERRIDDEN,
  BIBLIO_ENTRY_TAG_INVALID,
  BIBLIO_LOCATION_UNEXPECTED,
  BIBLIO_LOAD_FAILED,
  BIBLIO_LOADED,
  BIBLIO_NOT_FOUND,
  BIBLIO_REF_INVALID,
  BIBLIO_STATIC_ENTRY_MISSING,
  SOURCE_FILES_SCAN_FAILED;

  @Override
  public String getKey() {
    return name();
  }
}
