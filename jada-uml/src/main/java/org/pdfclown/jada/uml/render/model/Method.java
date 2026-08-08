/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Method.java) is part of jada-uml module in Jada project
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
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Objects.nonNull;
import static org.pdfclown.common.util.Strings.S;

import java.io.IOException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.io.IndentWriter;
import org.pdfclown.jada.uml.UmlConfig.TypeMode;

// SourceName: nl.talsmasoftware.umldoclet.uml.Method
/**
 * UML representation of a method.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Method extends TypeMember {
  // SourceName: isAbstract
  private boolean abstract_;

  /**
   * Create a new method in the containing type with a specific name and return type.
   *
   * @param containingType
   *          The containing type the member is part of.
   * @param name
   *          The name of the method.
   * @param returnType
   *          The name of the return type.
   */
  public Method(Type containingType, String name, @Nullable TypeName returnType) {
    super(containingType, name, returnType);
  }

  /**
   * Add a parameter to this method.
   *
   * @param name
   *          The name of the parameter.
   * @param type
   *          The type of the parameter.
   */
  public void addParameter(String name, TypeName type) {
    getParameters().add(name, type);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    return super.equals(o) && nonNull((Method) o).getParameters().equals(this.getParameters());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getParameters());
  }

  /**
   * Whether this method is abstract.
   */
  public boolean isAbstract() {
    return abstract_;
  }

  public void setAbstract(boolean value) {
    abstract_ = value;
  }

  @Override
  public IndentWriter writeTo(IndentWriter out) throws IOException {
    if (!getConfig().getMethodConfig().includes(getVisibility()))
      return out;

    if (abstract_) {
      out.append("{abstract}").space();
    }
    return super.writeTo(out);
  }

  @Override
  protected IndentWriter writeParametersTo(IndentWriter out) throws IOException {
    return getParameters().writeTo(out);
  }

  @Override
  protected IndentWriter writeTypeTo(IndentWriter out) throws IOException {
    TypeMode returnTypeDisplay = getConfig().getMethodConfig().getReturnTypeMode();
    if (getType() != null && !TypeMode.NONE.equals(returnTypeDisplay)) {
      out.append(S + COLON + SPACE).append(getType().toUml(returnTypeDisplay, null));
    }
    return out;
  }

  @Override
  void replaceParameterizedType(@Nullable TypeName from, TypeName to) {
    super.replaceParameterizedType(from, to);
    getParameters().replaceParameterizedType(from, to);
  }

  // SourceName: getOrCreateParameters
  private ParameterList getParameters() {
    return getChildren().stream()
        .filter(ParameterList.class::isInstance).map(ParameterList.class::cast)
        .findFirst()
        .orElseGet(() -> {
          var ret = new ParameterList(this);
          addChild(ret);
          return ret;
        });
  }
}
