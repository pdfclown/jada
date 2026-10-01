/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageTypeVisitor.java) is part of jada-uml module in Jada project
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
package org.pdfclown.jada.uml.render;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor9;
import org.jspecify.annotations.Nullable;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.dependencies.PackageTypeVisitor
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class PackageTypeVisitor extends SimpleTypeVisitor9<@Nullable String, @Nullable Void> {
  static final PackageTypeVisitor INSTANCE = new PackageTypeVisitor();

  @Override
  public @Nullable String visitArray(ArrayType t, @Nullable Void p) {
    return visit(t.getComponentType(), p);
  }

  @Override
  public @Nullable String visitDeclared(DeclaredType t, @Nullable Void p) {
    return PackageElementVisitor.INSTANCE.visit(t.asElement().getEnclosingElement(), p);
  }

  @Override
  public @Nullable String visitError(ErrorType t, @Nullable Void p) {
    return PackageElementVisitor.INSTANCE.visit(t.asElement().getEnclosingElement(), p);
  }

  @Override
  public @Nullable String visitExecutable(ExecutableType t, @Nullable Void p) {
    TypeMirror receiverType = t.getReceiverType();
    return receiverType != null ? visit(receiverType) : null;
  }

  @Override
  public @Nullable String visitUnknown(TypeMirror t, @Nullable Void p) {
    return null;
  }

  @Override
  public @Nullable String visitWildcard(WildcardType t, @Nullable Void p) {
    TypeMirror bound = t.getExtendsBound();
    if (bound == null) {
      bound = t.getSuperBound();
    }
    return bound != null ? visit(bound, p) : null;
  }
}
