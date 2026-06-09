/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageDependency.java) is part of jada-uml module in Jada project
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

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.pdfclown.common.util.annot.Immutable;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.dependencies.PackageDependency
/**
 * Package dependency.
 * <p>
 * Contains a 'from' package and a 'to' package. A (from) package has a dependency on a (to) package
 * if there is at least one element in the 'from' package that needs at least one element in the
 * 'to' package.
 * </p>
 * <p>
 * This class overrides {@code equals} and {@code hashCode} methods so unique package dependencies
 * can easily be included in hashed collections.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
@Immutable
public class PackageDependency {
  /**
   * The qualified name of the dependent package. This package contains at least one element that
   * has a dependency on an element in the {@link #toPackage}.
   */
  public final String fromPackage;

  /**
   * The qualified name of the depended-upon package. This package contains at least one element
   * that is needed by an element in the {@link #fromPackage}.
   */
  public final String toPackage;

  /**
   * Create a new package dependency object.
   *
   * @param fromPackage
   *          The package that has a dependency on another package.
   * @param toPackage
   *          The package that is depended upon.
   */
  public PackageDependency(String fromPackage, String toPackage) {
    this.fromPackage = requireNonNull(fromPackage, "`fromPackage`");
    this.toPackage = requireNonNull(toPackage, "`toPackage`");
  }

  /**
   * @implNote Marked as final to enforce equivalence symmetry.
   */
  @Override
  public final boolean equals(Object o) {
    return this == o || (o instanceof PackageDependency that
        && this.fromPackage.equals(that.fromPackage)
        && this.toPackage.equals(that.toPackage));
  }

  /**
   * @implNote Marked as final to enforce equivalence symmetry.
   */
  @Override
  public final int hashCode() {
    return Objects.hash(fromPackage, toPackage);
  }

  /**
   * @return Human-readable representation of this package dependency.
   */
  @Override
  public String toString() {
    return fromPackage + "->" + toPackage;
  }
}
