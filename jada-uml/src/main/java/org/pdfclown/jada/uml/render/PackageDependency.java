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

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.jspecify.annotations.Nullable;
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
  public final boolean equals(@Nullable Object o) {
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
