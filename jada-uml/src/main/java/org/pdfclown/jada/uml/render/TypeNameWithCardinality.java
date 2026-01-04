/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TypeNameWithCardinality.java) is part of jada-uml module in Jada project
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

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.uml.render.model.TypeName;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.TypeNameWithCardinality
/**
 * Simple data object containing a (possibly) derived type name with a cardinality.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
final class TypeNameWithCardinality {
  /**
   * Returns a function that applies the TypeNameVisitor, but also:
   * <ol>
   * <li>Checks if a type is an {@code Array}, {@code Iterable} or {@code Stream} to return the type
   * argument with cardinality {@code "*"}</li>
   * <li>Checks if a type is a Java 8 or Guava {@code Optional} object to return the type argument
   * with cardinality {@code "0..1"}</li>
   * <li>Otherwise, the name of the actual type is returned with cardinality {@code null}</li>
   * </ol>
   *
   * @param typeUtils
   *          The type utils to use for supertype introspection (required).
   * @return The function to return TypeName with cardinality for use in same-package references.
   */
  static Function<TypeMirror, TypeNameWithCardinality> function(final Types typeUtils) {
    requireNonNull(typeUtils, "`typeUtils`");

    return $type -> {
      if ($type instanceof ArrayType arrayType) {
        TypeName componentName = TypeNameVisitor.INSTANCE.visit(arrayType.getComponentType());
        return new TypeNameWithCardinality(componentName, "*");
      } else if ($type instanceof DeclaredType) {
        var superTypes = new ArrayDeque<>(singleton($type));
        var checkedTypes = new HashSet<String>();
        while (!superTypes.isEmpty()) {
          TypeMirror superType = superTypes.poll();
          String qName = TypeNameVisitor.INSTANCE.visit(superType).getQualifiedName();
          if (checkedTypes.add(qName)) { // Don't reiterate
            String cardinality = null;
            if ("java.util.Optional".equals(qName)
                || "com.google.common.base.Optional".equals(qName)) {
              cardinality = "0..1";
            } else if ("java.lang.Iterable".equals(qName)
                || "java.util.stream.Stream".equals(qName)) {
              cardinality = "*";
            }

            /*
             * Assumption: the 'iterable' and 'optional' types are DeclaredTypes with a single
             * TypeArgument.
             */
            Optional<TypeName> typeArgument = Optional.ofNullable(cardinality)
                .map(c -> superType instanceof DeclaredType declaredType ? declaredType : null)
                .map(DeclaredType::getTypeArguments)
                .map(args -> args.size() == 1 ? args.get(0) : null)
                .map(TypeNameVisitor.INSTANCE::visit);
            if (typeArgument.isPresent())
              return new TypeNameWithCardinality(typeArgument.get(), cardinality);

            superTypes.addAll(typeUtils.directSupertypes(superType));
          }
        }
      }
      return new TypeNameWithCardinality(TypeNameVisitor.INSTANCE.visit($type), null);
    };
  }

  final @Nullable String cardinality;
  final TypeName typeName;

  private TypeNameWithCardinality(TypeName typeName, @Nullable String cardinality) {
    this.typeName = typeName;
    this.cardinality = cardinality;
  }
}
