/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TypeName.java) is part of jada-uml module in Jada project
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
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Objects.textLiteral;

import java.io.IOException;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.uml.UmlConfig.TypeMode;

// SourceName: nl.talsmasoftware.umldoclet.uml.TypeName
/**
 * Class representing a type name.
 * <p>
 * This is less simple than it sounds: A type basically has a 'qualified name' and a 'simple name'
 * (these may be equal).
 * </p>
 * <p>
 * Also, if the type is a generic type, the actual type parameters can be seen as 'part' of the
 * name: The names of {@code List<String>} and {@code List<Integer>} are different, while in Java
 * the actual types are equal (due to erasure of the generic type).
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class TypeName {
  /**
   * Array representation.
   *
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   */
  public static class Array extends TypeName {
    public static Array of(TypeName componentType) {
      return new Array(requireNonNull(componentType, "`componentType`"));
    }

    private Array(TypeName componentType) {
      super(componentType.packageName, componentType.simpleName, componentType.qualifiedName,
          componentType.generics);
    }

    @Override
    protected String toUml(@Nullable TypeMode mode, @Nullable Namespace namespace) {
      return super.toUml(mode, namespace) + "[]";
    }
  }

  /**
   * Variable representation.
   *
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   */
  public static class Variable extends TypeName {
    /**
     * @param bound
     *          Upper bound.
     */
    public static Variable extendsBound(String variable, TypeName bound) {
      return new Variable(variable, requireNonNull(bound, "`bound`"), true);
    }

    /**
     * @param bound
     *          Lower bound.
     */
    public static Variable superBound(String variable, TypeName bound) {
      return new Variable(variable, requireNonNull(bound, "`bound`"), false);
    }

    private final String variable;
    // SourceName: isExtends
    private final boolean extends_;

    private Variable(String variable, TypeName bound, boolean extends_) {
      super(requireNonNull(bound, "`bound`").packageName, bound.simpleName, bound.qualifiedName,
          bound.generics);

      this.variable = variable;
      this.extends_ = extends_;
    }

    @Override
    public boolean equals(Object o) {
      return super.equals(o) && this.extends_ == ((Variable) o).extends_;
    }

    @Override
    protected String toUml(@Nullable TypeMode mode, @Nullable Namespace namespace) {
      return "%s %s %s".formatted(variable, extends_ ? "extends" : "super",
          super.toUml(mode, namespace));
    }
  }

  private static boolean isMarkupTag(String value) {
    return value.equalsIgnoreCase("<u")
        || value.equalsIgnoreCase("<b")
        || value.equalsIgnoreCase("<i");
  }

  private static boolean isQualified(@Nullable TypeMode mode) {
    return mode != null && mode.name().startsWith("QUALIFIED");
  }

  private final TypeName[] generics;
  // SourceName: packagename
  private final String packageName;
  // SourceName: qualified
  private final String qualifiedName;
  // SourceName: simple
  private final String simpleName;

  /**
   */
  public TypeName(String packageName, String simpleName, String qualifiedName,
      TypeName... generics) {
    this.packageName = packageName;
    this.simpleName = simpleName;
    this.qualifiedName = requireNonNull(qualifiedName, "`qualifiedName`");
    this.generics = requireNonNull(generics, "`generics`").clone();
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o != null && this.getClass() == o.getClass()
        && this.qualifiedName.equals(((TypeName) o).qualifiedName));
  }

  public TypeName[] getGenerics() {
    return generics.clone();
  }

  public String getPackageName() {
    return packageName;
  }

  /**
   */
  public String getQualified(@Nullable String separator) {
    int plen = packageName.length();
    return qualifiedName.length() > plen && plen > 0 && separator != null && !separator.isEmpty()
        ? packageName + separator + qualifiedName.substring(plen + 1)
        : qualifiedName;
  }

  public String getQualifiedName() {
    return qualifiedName;
  }

  public String getSimpleName() {
    return simpleName;
  }

  @Override
  public int hashCode() {
    return qualifiedName.hashCode();
  }

  @Override
  public String toString() {
    return toUml(TypeMode.SIMPLE, null);
  }

  protected String toUml(@Nullable TypeMode mode, @Nullable Namespace namespace) {
    var output = new StringBuilder();
    if (mode == null) {
      mode = TypeMode.SIMPLE;
    }
    if (!TypeMode.NONE.equals(mode)) {
      try {
        if (namespace != null && this.qualifiedName.startsWith(namespace.getName() + ".")) {
          output.append(this.qualifiedName.substring(namespace.getName().length() + 1));
        } else if (isQualified(mode)) {
          output.append(this.qualifiedName);
        } else {
          output.append(this.simpleName);
        }
        writeGenericsTo(output, TypeMode.QUALIFIED_GENERICS.equals(mode) ? mode : TypeMode.SIMPLE);
      } catch (IOException ex) {
        throw runtime("Type name {} writing FAILED", textLiteral(qualifiedName), ex);
      }
    }
    return output.toString();
  }

  private <A extends Appendable> A writeGenericsTo(A output, TypeMode mode)
      throws IOException {
    if (generics.length > 0) {
      StringBuilder buffer = new StringBuilder();
      String sep = "<";
      for (TypeName generic : generics) {
        buffer.append(sep).append(generic.toUml(mode, null));
        sep = ", ";
      }
      String res = buffer.toString();
      if (isMarkupTag(res)) {
        res = "<\u200B" + res.substring(1) /*
                                            * Inserts zero-width-space character between '<' and
                                            * markup character
                                            */;
      }
      output.append(res).append(">");
    }
    return output;
  }
}
