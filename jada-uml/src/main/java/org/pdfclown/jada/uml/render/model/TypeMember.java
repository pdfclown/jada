/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TypeMember.java) is part of jada-uml module in Jada project
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

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.pdfclown.common.util.Exceptions.wrongArg;

import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.io.IndentPrintWriter;
import org.pdfclown.jada.uml.UmlConfig.Visibility;

// SourceName: nl.talsmasoftware.umldoclet.uml.TypeMember
/**
 * Model object for a Field or Method in a UML class.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public abstract class TypeMember extends UmlNode {
  // SourceName: isDeprecated
  private boolean deprecated;
  private final String name;
  // SourceName: isStatic
  private boolean static_;
  private @Nullable TypeName type;
  private @Nullable Visibility visibility;

  protected TypeMember(@Nullable Type containingType, String name, @Nullable TypeName type) {
    super(containingType);

    name = requireNonNull(name, "`name`").trim();
    if (name.isEmpty())
      throw wrongArg("name", name, "INVALID (empty) for {}",
          containingType != null ? containingType.getName() : type != null ? type : "?");

    this.name = name;
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    else if (o == null || this.getClass() != o.getClass())
      return false;

    var that = (TypeMember) o;
    return Objects.equals(this.getParent(), that.getParent())
        && this.name.equals(that.name);
  }

  public String getName() {
    return name;
  }

  public @Nullable TypeName getType() {
    return type;
  }

  public Visibility getVisibility() {
    return requireNonNullElse(visibility, Visibility.PUBLIC);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getParent(), name);
  }

  public boolean isDeprecated() {
    return deprecated;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  public boolean isStatic() {
    return static_;
  }

  public void setDeprecated(boolean value) {
    deprecated = value;
  }

  public void setStatic(boolean value) {
    static_ = value;
  }

  public void setVisibility(Visibility value) {
    visibility = value;
  }

  @Override
  public <T extends IndentPrintWriter> T writeTo(T out) {
    if (static_) {
      out.append("{static}").whitespace();
    }
    out.append(umlVisibility());
    if (deprecated) {
      out.append("--").append(name).append("--");
    } else {
      out.append(name);
    }
    writeParametersTo(out);
    writeTypeTo(out);
    out.newline();
    return out;
  }

  protected <T extends IndentPrintWriter> T writeParametersTo(T out) {
    return out;
  }

  protected <T extends IndentPrintWriter> T writeTypeTo(T out) {
    if (type != null) {
      out.append(": ").append(type.toString());
    }
    return out;
  }

  void replaceParameterizedType(@Nullable TypeName from, TypeName to) {
    if (from != null && from.equals(this.type)) {
      this.type = to;
    }
  }

  private String umlVisibility() {
    return switch (getVisibility()) {
      case PRIVATE -> "-";
      case PROTECTED -> "#";
      case PACKAGE_PRIVATE -> "~";
      default /* assume PUBLIC */ -> "+";
    };
  }
}
