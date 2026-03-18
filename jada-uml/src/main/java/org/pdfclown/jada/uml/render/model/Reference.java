/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Reference.java) is part of jada-uml module in Jada project
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

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.pdfclown.common.util.Exceptions.wrongArg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.io.IndentWriter;

// SourceName: nl.talsmasoftware.umldoclet.uml.Reference
/**
 * Reference between two types.
 * <p>
 * The following reference types are currently supported:
 * </p>
 * <ul>
 * <li>The 'extends' reference: {@code "--|>"}</li>
 * <li>The 'implements' reference: {@code "..|>"}</li>
 * <li>The 'inner class' reference: {@code "+--"}</li>
 * </ul>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Reference extends UmlNode {
  /**
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   */
  public static final class Side {
    /**
     * The index of the searched character or the length of the string if not found.
     *
     * @param value
     *          The string to search in
     * @param ch
     *          The character to search for
     * @return The index of the character in the string or the length of the string if not found.
     */
    private static int indexOrLengthOf(String value, char ch) {
      int idx = value.indexOf(ch);
      return idx >= 0 ? idx : value.length();
    }

    private final String cardinality;
    private final boolean nameFirst;
    private final String qualifiedName;

    private Side(String qualifiedName, @Nullable String cardinality, boolean nameFirst) {
      qualifiedName = requireNonNull(qualifiedName, "`qualifiedName`")
          .substring(0, indexOrLengthOf(qualifiedName, '<')).trim();
      if (qualifiedName.isEmpty())
        throw wrongArg("qualifiedName", qualifiedName, "INVALID (empty)");

      this.qualifiedName = qualifiedName;
      this.cardinality = trimToEmpty(cardinality);
      this.nameFirst = nameFirst;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      else if (o == null || this.getClass() != o.getClass())
        return false;

      var that = (Side) o;
      return this.qualifiedName.equals(that.qualifiedName)
          && this.cardinality.equals(that.cardinality);
    }

    public String getCardinality() {
      return cardinality;
    }

    public String getQualifiedName() {
      return qualifiedName;
    }

    @Override
    public int hashCode() {
      return Objects.hash(qualifiedName, cardinality);
    }

    @Override
    public String toString() {
      return toString(null);
    }

    private boolean matches(@Nullable TypeName typeName) {
      return typeName != null && this.qualifiedName.equals(typeName.getQualifiedName());
    }

    private String toString(@Nullable Namespace namespace) {
      String name = qualifiedName;
      if (namespace != null && name.startsWith(namespace.getName() + ".")) {
        name = name.substring(namespace.getName().length() + 1);
        if (name.indexOf('.') > 0) {
          name = qualifiedName;
        }
      }
      return cardinality.isEmpty() ? name
          : nameFirst ? name + " \"" + cardinality + '"'
          : '"' + cardinality + "\" " + name;
    }
  }

  public static Side from(String qualifiedName, @Nullable String cardinality) {
    return new Side(qualifiedName, cardinality, true);
  }

  public static Side to(String qualifiedName, @Nullable String cardinality) {
    return new Side(qualifiedName, cardinality, false);
  }

  private static char reverseChar(char ch) {
    return ch == '<' ? '>' : ch == '>' ? '<'
        : ch == '{' ? '}' : ch == '}' ? '{'
        : ch;
  }

  public final Side from;
  public final Collection<String> notes;
  public final Side to;
  public final String type;

  public Reference(Side from, String type, Side to, @Nullable String @Nullable... notes) {
    this(from, type, to, notes != null ? asList(notes) : emptySet());
  }

  private Reference(Side from, String type, Side to, Collection<@Nullable String> notes) {
    super(null);

    requireNonNull(from, "`from`");
    type = requireNonNull(type, "`type`").trim();
    if (type.isEmpty())
      throw wrongArg("type", type, "INVALID (empty)");

    requireNonNull(to, "`to`");
    requireNonNull(notes, "`notes`");

    this.from = from;
    this.type = type;
    this.to = to;
    this.notes = notes.stream()
        .filter(Objects::nonNull)
        .map(String::trim).filter($ -> !$.isEmpty())
        .collect(toCollection(LinkedHashSet::new));
  }

  /**
   */
  public Reference addNote(@Nullable String note) {
    final String trimmed = StringUtils.trimToEmpty(note);
    if (trimmed.isEmpty() || notes.contains(trimmed))
      return this;

    final var newNotes = new ArrayList<String>(notes.size() + 1);
    {
      newNotes.addAll(notes);
      newNotes.add(trimmed);
    }
    return new Reference(from, type, to, newNotes);
  }

  /**
   * @return The canonical type that can be used for equality matching.
   */
  public Reference canonical() {
    return type.startsWith("<-") || type.startsWith("<..")
        || type.endsWith("-|>") || type.endsWith("..|>")
        || type.endsWith("-*") || type.endsWith("-o")
        || type.endsWith("-+")
            ? inverse()
            : this;
  }

  /**
   * Returns whether this reference contains the requested type.
   *
   * @param typeName
   *          The name of a type to check.
   * @return Whether either {@code from} or {@code to} matches {@code typeName}.
   */
  public boolean contains(TypeName typeName) {
    return from.matches(typeName) || to.matches(typeName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    else if (o == null || this.getClass() != o.getClass())
      return false;

    final Reference thisC = this.canonical();
    final Reference thatC = ((Reference) o).canonical();
    return thisC.from.equals(thatC.from)
        && thisC.type.equals(thatC.type)
        && thisC.to.equals(thatC.to);
  }

  @Override
  public int hashCode() {
    final Reference ref = canonical();
    return Objects.hash(ref.from, ref.type, ref.to);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  public boolean isSelfReference() {
    return from.qualifiedName.equals(to.qualifiedName);
  }

  @Override
  public IndentWriter writeTo(IndentWriter out) throws IOException {
    // Namespace aware compensation
    final Namespace namespace = findParent(Namespace.class).orElse(null);

    out.append(from.toString(namespace)).space()
        .append(type).space()
        .append(to.toString(namespace));
    if (!notes.isEmpty()) {
      out.append(": ").append(String.join("\\n", notes));
    }
    out.nl();
    return out;
  }

  private Reference inverse() {
    return new Reference(from(to.qualifiedName, to.cardinality),
        reverseType(),
        to(from.qualifiedName, from.cardinality),
        this.notes);
  }

  private String reverseType() {
    char[] chars = type.toCharArray();
    char swap;
    for (int i = 0, j = chars.length - 1; i < j; i++) {
      swap = chars[i];
      chars[i] = reverseChar(chars[j]);
      chars[j--] = reverseChar(swap);
    }
    return String.valueOf(chars);
  }
}
