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
