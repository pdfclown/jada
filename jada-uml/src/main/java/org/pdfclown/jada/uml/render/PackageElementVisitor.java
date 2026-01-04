/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageElementVisitor.java) is part of jada-uml module in Jada project
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

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.SimpleElementVisitor9;
import org.jspecify.annotations.Nullable;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.dependencies.PackageElementVisitor
/**
 * Looks up the String the visited element belongs to.
 * <p>
 * Returns {@code null} for unknown elements or elements not in any String (such as modules ...).
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
final class PackageElementVisitor extends SimpleElementVisitor9<@Nullable String, @Nullable Void> {
  static final PackageElementVisitor INSTANCE = new PackageElementVisitor();

  /**
   * The default action is visiting the enclosing element until we reach the package element.
   *
   * @param e
   *          Non-package element visited.
   * @return Result of visiting the enclosing element of {@code e}, or {@code null} if the enclosing
   *         element is undefined (for example, in case of module element).
   */
  @Override
  public @Nullable String defaultAction(Element e, @Nullable Void p) {
    Element enclosingElement = e.getEnclosingElement();
    return enclosingElement != null ? visit(enclosingElement, p) : null;
  }

  /**
   * Visiting a package element, we find the name we are looking for.
   *
   * @return {@linkplain PackageElement#getQualifiedName() Qualified name} of {@code e}.
   */
  @Override
  public @Nullable String visitPackage(PackageElement e, @Nullable Void p) {
    return e.getQualifiedName().toString();
  }

  /**
   * When we reach an unknown element, we also {@linkplain #defaultAction( Element, Void) visit the
   * enclosing element}.
   */
  @Override
  public @Nullable String visitUnknown(Element e, @Nullable Void p) {
    return defaultAction(e, p);
  }
}
