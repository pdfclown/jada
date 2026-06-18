/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Parameters.java) is part of jada-uml module in Jada project
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

import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.COMMA;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Strings.ELLIPSIS;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;

import java.io.IOException;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.io.IndentWriter;
import org.pdfclown.jada.uml.UmlConfig.MethodConfig;
import org.pdfclown.jada.uml.UmlConfig.TypeMode;

// SourceName: nl.talsmasoftware.umldoclet.uml.Parameters
/**
 * Parameters.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class ParameterList extends UmlNode {
  /**
   * Parameter.
   *
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   */
  public class Parameter extends UmlNode {
    private final @Nullable String name;
    private @Nullable TypeName type;

    private Parameter(@Nullable String name, @Nullable TypeName type) {
      super(ParameterList.this);

      this.name = name;
      this.type = type;
    }

    @Override
    public IndentWriter writeTo(IndentWriter out) throws IOException {
      String sep = EMPTY;
      MethodConfig methodConfig = getConfig().getMethodConfig();
      if (name != null
          && MethodConfig.ParamNameMode.BEFORE_TYPE.equals(methodConfig.getParamNameMode())) {
        out.append(name);
        sep = S + COLON + SPACE;
      }
      if (type != null && !TypeMode.NONE.equals(methodConfig.getParamTypeMode())) {
        String typeUml = type.toUml(methodConfig.getParamTypeMode(), null);
        if (varargs && typeUml.endsWith("[]")) {
          typeUml = typeUml.substring(0, typeUml.length() - 2) + ELLIPSIS;
        }
        out.append(sep).append(typeUml);
        sep = S + SPACE;
      }
      if (name != null
          && MethodConfig.ParamNameMode.AFTER_TYPE.equals(methodConfig.getParamNameMode())) {
        out.append(sep).append(name);
      }
      return out;
    }
  }

  private boolean varargs = false;

  public ParameterList(@Nullable UmlNode parent) {
    super(parent);
  }

  public ParameterList add(String name, TypeName type) {
    addChild(new Parameter(name, type));
    return this;
  }

  @Override
  public void addChild(UmlNode child) {
    if (child instanceof Parameter) {
      super.addChild(child);
    }
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  public ParameterList varargs(boolean varargs) {
    this.varargs = varargs;
    return this;
  }

  @Override
  public IndentWriter writeChildrenTo(IndentWriter out) throws IOException {
    out.append(ROUND_BRACKET_OPEN);
    String sep = EMPTY;
    for (UmlNode param : getChildren()) {
      param.writeTo(out.append(sep));
      sep = S + COMMA + SPACE;
    }
    out.append(ROUND_BRACKET_CLOSE);
    return out;
  }

  @Override
  public IndentWriter writeTo(IndentWriter out) throws IOException {
    return writeChildrenTo(out);
  }

  void replaceParameterizedType(@Nullable TypeName from, TypeName to) {
    if (from != null) {
      getChildren().stream()
          .filter(Parameter.class::isInstance).map(Parameter.class::cast)
          .filter($ -> from.equals($.type))
          .forEach($ -> $.type = to);
    }
  }
}
