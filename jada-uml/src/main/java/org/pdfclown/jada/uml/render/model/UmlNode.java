/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlNode.java) is part of jada-uml module in Jada project
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

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.unmodifiableList;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.wrongState;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.function.Failable;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.UnmodifiableView;
import org.pdfclown.common.util.io.IndentWriter;
import org.pdfclown.jada.uml.UmlConfig;

// SourceName: nl.talsmasoftware.umldoclet.uml.UMLNode
/**
 * Part of a UML diagram that can render itself to the diagram by {@linkplain #writeTo(IndentWriter)
 * writing to} an indenting writer. It serves as a reusable base-class for all specific UML nodes.
 * <p>
 * UML nodes are capable of rendering themselves to {@link IndentWriter}.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public abstract class UmlNode {
  private @Nullable UmlNode parent;
  private final List<UmlNode> children = new ArrayList<>();

  protected UmlNode(@Nullable UmlNode parent) {
    this.parent = parent;
  }

  public void addChild(UmlNode child) {
    children.add(child);
    child.setParent(this);
  }

  public @UnmodifiableView List<UmlNode> getChildren() {
    return unmodifiableList(children);
  }

  /**
   * Returns all children that are an instance of a particular type.
   *
   * @param type
   *          The type of {@code UMLNode} to return (required, non-null).
   * @param <T>
   *          The type of children to obtain.
   * @return The filtered list of children of this uml node.
   */
  public <T extends UmlNode> @UnmodifiableView List<T> getChildren(Class<T> type) {
    return getChildren().stream()
        .filter(type::isInstance)
        .map(type::cast)
        .collect(Collectors.toUnmodifiableList());
  }

  public @Nullable UmlNode getParent() {
    return parent;
  }

  /**
   * Whether this UML node is empty.
   */
  public boolean isEmpty() {
    return getChildren().stream().allMatch(UmlNode::isEmpty);
  }

  public boolean removeChildren(Predicate<? super UmlNode> condition) {
    return children.removeIf(condition);
  }

  public void setParent(@Nullable UmlNode value) {
    parent = value;
  }

  /**
   * Renders the entire content of this renderer and returns it as a String value.
   *
   * @return The rendered content of this renderer.
   */
  @Override
  public String toString() {
    try {
      return writeTo(IndentWriter.of(new StringWriter(), null)).toString();
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }

  protected <U extends UmlNode> Optional<U> findParent(Class<U> nodeType) {
    final Set<UmlNode> traversed = newSetFromMap(new IdentityHashMap<>());
    for (UmlNode parent = getParent();
        parent != null && traversed.add(parent);
        parent = parent.getParent()) {
      if (nodeType.isInstance(parent))
        return Optional.of(nodeType.cast(parent));
    }
    return Optional.empty();
  }

  protected UmlConfig getConfig() {
    return findParent(Diagram.class)
        .map(Diagram::getConfig)
        .orElseThrow(() -> wrongState("Configuration MISSING"));
  }

  /**
   * Helper method to write all children to the output.
   *
   * @param out
   *          The output to write the children to.
   * @return A reference to the output for method chaining purposes.
   */
  protected IndentWriter writeChildrenTo(IndentWriter out) throws IOException {
    getChildren().forEach(Failable.asConsumer($ -> $.writeTo(out)));
    return out;
  }

  /**
   * Renders this object to the indenting {@code out}.
   *
   * @param out
   *          The output to render this object to.
   * @return A reference to the output for method chaining purposes.
   */
  protected abstract IndentWriter writeTo(IndentWriter out) throws IOException;
}
