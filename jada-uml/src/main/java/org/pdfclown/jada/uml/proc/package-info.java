/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (package-info.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */

/**
 * HTML post-processing for UML-diagrams embedding into Javadoc output.
 * <p>
 * The {@linkplain org.pdfclown.jada.uml.proc.PageProcessor post-processor} injects the references
 * (relative paths) of the diagrams into their respective HTML files.
 * </p>
 * <p>
 * UML diagrams are injected as {@code <object>} tags in case of SVG format, which makes their links
 * clickable from the document; all other formats are injected as normal {@code <img>} tags.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 * @see org.pdfclown.jada.uml.proc.PageProcessor
 */
@NullMarked
package org.pdfclown.jada.uml.proc;

import org.jspecify.annotations.NullMarked;
