/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlCharacters.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render.model;

import org.pdfclown.common.util.Strings;
import org.pdfclown.common.util.io.IndentPrintWriter;

// SourceName: nl.talsmasoftware.umldoclet.uml.UmlCharacters
/**
 * A literal piece of UML.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class UmlCharacters extends UmlNode {
  private static class UmlLine extends UmlCharacters {
    private UmlLine(String line) {
      super(line);
    }

    @Override
    public <T extends IndentPrintWriter> T writeTo(T out) {
      super.writeTo(out).newline();
      return out;
    }
  }

  /**
   * Incomplete definition (see <a href=
   * "https://forum.plantuml.net/1672/specify-incomplete-specification-ellipsis-attributes-methods">how
   * to specify an incomplete definition of class diagram members</a>).
   */
  public static final UmlCharacters ELLIPSIS = new UmlLine(Strings.ELLIPSIS__CHICAGO);
  public static final UmlCharacters EMPTY = new UmlCharacters(Strings.EMPTY);
  public static final UmlCharacters NEWLINE = new UmlLine(Strings.EMPTY);

  private final String content;

  private UmlCharacters(String content) {
    super(null);

    this.content = content;
  }

  @Override
  public <T extends IndentPrintWriter> T writeTo(T out) {
    out.append(content);
    return out;
  }
}
