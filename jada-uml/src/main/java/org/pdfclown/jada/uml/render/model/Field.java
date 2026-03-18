/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Field.java) is part of jada-uml module in Jada project
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

import static org.pdfclown.jada.uml.render.model.Type.Classification.ENUM;

import java.io.IOException;
import org.pdfclown.common.util.io.IndentWriter;

// SourceName: nl.talsmasoftware.umldoclet.uml.Field
/**
 * Model object for a Field in a UML class.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Field extends TypeMember {
  public Field(Type containingType, String name, TypeName type) {
    super(containingType, name, type);
  }

  @Override
  public IndentWriter writeTo(IndentWriter out) throws IOException {
    if (!getConfig().getFieldConfig().includes(getVisibility()))
      return out;

    return super.writeTo(out);
  }

  @Override
  protected IndentWriter writeTypeTo(IndentWriter out) throws IOException {
    return isEnumType() ? out : super.writeTypeTo(out);
  }

  private boolean isEnumType() {
    return isStatic()
        && getParent() instanceof Type parentType
        && ENUM.equals(parentType.getClassfication())
        && parentType.getName().equals(getType());
  }
}
