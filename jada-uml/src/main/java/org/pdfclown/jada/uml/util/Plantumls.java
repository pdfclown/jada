/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Plantumls.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.uml.util;

import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Objects.sqn;
import static org.pdfclown.common.util.Strings.NULL;
import static org.pdfclown.common.util.function.Functions.toElse;

import org.jspecify.annotations.Nullable;

/**
 * PlantUML utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Plantumls {
  /**
   * PlantUML source fragment builder.
   *
   * @author Stefano Chizzolini
   */
  public static class Builder {
    private final StringBuilder base = new StringBuilder();

    private Builder() {
    }

    /**
     * Appends the string to this fragment.
     */
    public Builder concat(String s) {
      base.append(s);
      return this;
    }

    /**
     * Appends the string to this fragment, joining with a space character.
     */
    public Builder join(String s) {
      if (base.length() > 0) {
        base.append(SPACE);
      }
      return concat(s);
    }

    @Override
    public String toString() {
      return base.toString();
    }
  }

  /**
   * Empty-namespace representation.
   * <p>
   * NOTE: Empty namespace (that is, default package) is illegal in PlantUML
   * {@biblio.ref UML-DOCLET-BUGS 107}, so its representation is aliased as {@code "unnamed"}.
   * </p>
   */
  public static final String PUML_NS__EMPTY = "unnamed";

  public static final String PUML_NS_SEPARATOR = "::";

  public static final String PUML_REF__ASSOCIATED_BY = "<--";
  public static final String PUML_REF__ASSOCIATES = "-->";
  public static final String PUML_REF__ENCLOSED_BY = "--+";
  public static final String PUML_REF__ENCLOSES = "+--";
  public static final String PUML_REF__EXTENDED_BY = "<|--";
  public static final String PUML_REF__EXTENDS = "--|>";
  public static final String PUML_REF__IMPLEMENTED_BY = "<|..";
  public static final String PUML_REF__IMPLEMENTS = "..|>";

  /**
   * Normalizes the namespace.
   * <p>
   * Empty {@code name} (that is, default package) is replaced by {@link #PUML_NS__EMPTY}.
   * </p>
   */
  public static String normalNs(String name) {
    return name.isEmpty() ? PUML_NS__EMPTY : name;
  }

  /**
   * Creates a PlantUML source fragment builder.
   */
  public static Builder puml() {
    return new Builder();
  }

  /**
   * Gets the namespace-aware fully-qualified name of the type.
   * <p>
   * For example, if {@code type} is {@code io.mydomain.myproject.MyOuterClass$MyInnerClass}, it
   * returns {@code "io.mydomain.myproject::MyOuterClass::MyInnerClass"}.
   * </p>
   */
  public static String pumlNsFqn(@Nullable Class<?> type) {
    return toElse(type, $ -> $.getPackageName() + PUML_NS_SEPARATOR + pumlNsSqn($), NULL);
  }

  /**
   * Gets the namespace-aware qualified simple name of the type.
   * <p>
   * For example, if {@code type} is {@code io.mydomain.myproject.MyOuterClass$MyInnerClass}, it
   * returns {@code "MyOuterClass::MyInnerClass"}.
   * </p>
   */
  public static String pumlNsSqn(@Nullable Class<?> type) {
    return sqn(type).replace("$", PUML_NS_SEPARATOR);
  }

  private Plantumls() {
  }
}
