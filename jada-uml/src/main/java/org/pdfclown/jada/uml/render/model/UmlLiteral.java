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
  Changes: Adaptation and redesign for Jada.
 */
/*
 * Copyright 2016-2026 Talsma ICT
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pdfclown.jada.uml.render.model;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import org.pdfclown.common.util.Strings;
import org.pdfclown.common.util.io.IndentWriter;

// SourceName: nl.talsmasoftware.umldoclet.uml.UmlCharacters
/**
 * A literal piece of UML.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class UmlLiteral extends UmlNode {
  private static class UmlLine extends UmlLiteral {
    private UmlLine(String line) {
      super(line);
    }

    @Override
    public IndentWriter writeTo(IndentWriter out) throws IOException {
      super.writeTo(out).nl();
      return out;
    }
  }

  /**
   * Incomplete definition (see <a href=
   * "https://forum.plantuml.net/1672/specify-incomplete-specification-ellipsis-attributes-methods">how
   * to specify an incomplete definition of class diagram members</a>).
   */
  public static final UmlLiteral ELLIPSIS = new UmlLine(Strings.ELLIPSIS__CHICAGO);
  public static final UmlLiteral EMPTY = new UmlLiteral(Strings.EMPTY);
  public static final UmlLiteral NEWLINE = new UmlLine(Strings.EMPTY);

  private final String content;

  private UmlLiteral(String content) {
    super(null);

    this.content = requireNonNull(content, "`content`");
  }

  @Override
  public boolean isEmpty() {
    return content.isBlank();
  }

  @Override
  public IndentWriter writeTo(IndentWriter out) throws IOException {
    out.append(content);
    return out;
  }
}
