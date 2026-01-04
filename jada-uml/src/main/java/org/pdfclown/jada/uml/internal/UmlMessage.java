/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlMessage.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.internal;

import org.pdfclown.jada.core.system.Message;
import org.pdfclown.jada.uml.UmlExtension;

// SourceName: nl.talsmasoftware.umldoclet.logging.Message
/**
 * {@link UmlExtension} message.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public enum UmlMessage implements Message {
  CANNOT_READ_ELEMENT_LIST,
  CANNOT_READ_PACKAGE_LIST,
  ELEMENT_LIST_HTML_IGNORED,
  GENERATING_FILE,
  PACKAGE_DEPENDENCY_CYCLES,
  PACKAGE_VISITED_BUT_UNDOCUMENTED,
  SKIPPING_INVALID_PACKAGE_NAME,
  UNKNOWN_VISIBILITY;

  @Override
  public String getKey() {
    return name();
  }
}
