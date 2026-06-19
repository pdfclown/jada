/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TypeNameVisitor.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render;

import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.lcase;

import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor9;
import org.pdfclown.jada.uml.render.model.TypeName;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.TypeNameVisitor
/**
 * The UML type name implemented as {@link TypeVisitor}.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
final class TypeNameVisitor extends SimpleTypeVisitor9<TypeName, Void> {
  private static final EnumSet<TypeKind> NO_KNOWN_TYPES = EnumSet.of(
      TypeKind.VOID, TypeKind.NONE, TypeKind.NULL, TypeKind.ERROR, TypeKind.OTHER);

  static final TypeNameVisitor INSTANCE = new TypeNameVisitor();

  private static final ThreadLocal<Set<TypeMirror>> VISITED = ThreadLocal.withInitial(
      () -> Collections.newSetFromMap(new IdentityHashMap<>()));

  private TypeNameVisitor() {
  }

  @Override
  public TypeName visitArray(ArrayType arrayType, Void parameter) {
    return TypeName.Array.of(_visit(arrayType.getComponentType(), parameter));
  }

  @Override
  public TypeName visitDeclared(DeclaredType declaredType, Void parameter) {
    final Element el = declaredType.asElement();
    final String simpleName = el.getSimpleName().toString();
    final String qualifiedName = el instanceof QualifiedNameable qualifiedNameable
        ? qualifiedNameable.getQualifiedName().toString()
        : simpleName;
    final TypeName[] generics = declaredType.getTypeArguments().stream()
        .map($ -> _visit($, parameter))
        .toArray(TypeName[]::new);
    final String packageName;
    Element enclosingElement = el.getEnclosingElement();
    if (enclosingElement.getKind().isInterface() || enclosingElement.getKind().isClass()) {
      packageName = visit(enclosingElement.asType()).getPackageName();
    } else {
      int dot = qualifiedName.lastIndexOf(DOT);
      packageName = dot > 0 ? qualifiedName.substring(0, dot) : EMPTY;
    }
    return new TypeName(packageName, simpleName, qualifiedName, generics);
  }

  @Override
  public TypeName visitNoType(NoType noType, Void parameter) {
    // "void", "package", "module", "none"
    final String none = lcase(noType.getKind().name());
    return new TypeName(EMPTY, none, none);
  }

  @Override
  public TypeName visitPrimitive(PrimitiveType primitiveType, Void parameter) {
    // "byte", "char", "short", "int", "long", "float", "double", "boolean"
    final String primitive = lcase(primitiveType.getKind().name());
    return new TypeName(EMPTY, primitive, primitive);
  }

  @Override
  public TypeName visitTypeVariable(TypeVariable typeVariable, Void parameter) {
    TypeMirror upperBound = typeVariable.getUpperBound();
    if (upperBound != null && !NO_KNOWN_TYPES.contains(upperBound.getKind())) {
      // Fix for #64: Avoid redundant <T extends Object> (which is obviously true for all T's)
      TypeName upperBoundName = _visit(upperBound, parameter);
      if (!Object.class.getName().equals(upperBoundName.getQualifiedName()))
        return TypeName.Variable.extendsBound(typeVariable.toString(), upperBoundName);
    }
    TypeMirror lowerBound = typeVariable.getLowerBound();
    if (lowerBound != null && !NO_KNOWN_TYPES.contains(lowerBound.getKind()))
      return TypeName.Variable.superBound(typeVariable.toString(), _visit(lowerBound, parameter));

    return defaultAction(typeVariable, parameter);
  }

  @Override
  public TypeName visitWildcard(WildcardType wildcardType, Void parameter) {
    TypeMirror extendsBound = wildcardType.getExtendsBound();
    if (extendsBound != null)
      return TypeName.Variable.extendsBound("?", _visit(extendsBound, parameter));
    TypeMirror superBound = wildcardType.getSuperBound();
    if (superBound != null)
      return TypeName.Variable.superBound("?", _visit(superBound, parameter));

    return defaultAction(wildcardType, parameter);
  }

  @Override
  protected TypeName defaultAction(TypeMirror tp, Void parameter) {
    String qualifiedName = tp.toString();
    int lt = qualifiedName.lastIndexOf('<');
    int dot = (lt < 0 ? qualifiedName : qualifiedName.substring(0, lt)).lastIndexOf(DOT);
    String packageName = dot < 0 ? EMPTY : qualifiedName.substring(0, dot);
    return new TypeName(packageName, qualifiedName.substring(dot + 1), qualifiedName);
  }

  /**
   * Internal variant of {@link #visit(TypeMirror, Object)} for calls from inside this visitor
   * itself.
   * <p>
   * Main purpose of this method is to limit the endless recursion that would result for types such
   * as {@code <T extends Comparable<T>>}
   * </p>
   *
   * @param type
   *          The type to visit.
   * @param parameter
   *          The parameter (ignored by our visitor).
   * @return The type name
   */
  private TypeName _visit(TypeMirror type, Void parameter) {
    if (VISITED.get().add(type)) {
      try {
        return super.visit(type, parameter);
      } finally {
        VISITED.get().remove(type);
        if (VISITED.get().isEmpty()) {
          VISITED.remove();
        }
      }
    }
    return defaultAction(type, parameter);
  }
}
