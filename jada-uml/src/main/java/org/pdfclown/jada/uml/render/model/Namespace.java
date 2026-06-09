/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Namespace.java) is part of jada-uml module in Jada project
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
import static org.pdfclown.jada.core.util.lang.LangModels.MODULE__UNNAMED;
import static org.pdfclown.jada.uml.util.Plantumls.normalNs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.io.IndentWriter;

// SourceName: nl.talsmasoftware.umldoclet.uml.Namespace
/**
 * UML namespace.
 * <p>
 * Corresponds to a Java package.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Namespace extends UmlNode {
  /**
   * Module names by package.
   * <p>
   * Caches module names for lookup.
   * </p>
   */
  private static final Map<String, String> packageModuleNames = new HashMap<>();

  private final String moduleName;
  private final String name;

  /**
   * @param moduleName
   *          (empty, for unnamed module; {@code null}, for automatic discovery).
   */
  public Namespace(@Nullable UmlNode parent, String name, @Nullable String moduleName) {
    super(parent);

    if (moduleName != null) {
      moduleName = moduleName.trim();
    } else {
      moduleName = packageModuleNames.computeIfAbsent(name,
          $k -> ModuleLayer.boot().modules().stream()
              .filter($ -> $.getPackages().contains($k))
              .findFirst().map(Module::getName).orElse(MODULE__UNNAMED));
    }

    this.name = requireNonNull(name, "`name`").trim();
    this.moduleName = requireNonNull(moduleName, "`moduleName`");
  }

  public boolean contains(@Nullable TypeName typeName) {
    return typeName != null && typeName.getQualifiedName().startsWith(name + ".");
  }

  /**
   * @implNote Marked as final to enforce equivalence symmetry.
   */
  @Override
  public final boolean equals(Object o) {
    return this == o || (o instanceof Namespace that
        && this.name.equals(that.name));
  }

  public String getModuleName() {
    return moduleName;
  }

  /**
   * Namespace name.
   *
   * @return Empty, if default package.
   */
  public String getName() {
    return name;
  }

  /**
   * @implNote Marked as final to enforce equivalence symmetry.
   */
  @Override
  public final int hashCode() {
    return name.hashCode();
  }

  @Override
  public IndentWriter writeTo(IndentWriter out) throws IOException {
    writeNameTo(out.append("package").space()).append('{').nl();
    writeChildrenTo(out.withIndent());
    out.append('}').nl();
    return out;
  }

  /**
   * Adds {@link #getName() name} to the diagram.
   *
   * @param out
   *          Output to append the package name to.
   * @return {@code out}
   */
  private IndentWriter writeNameTo(IndentWriter out) throws IOException {
    out.append(normalNs(name)).space();
    return out;
  }
}
