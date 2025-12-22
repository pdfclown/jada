/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Namespace.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render.model;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.jada.core.util.lang.LangModels.MODULE__UNNAMED;
import static org.pdfclown.jada.uml.util.Plantumls.normalNs;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.io.IndentPrintWriter;

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

  @Override
  public boolean equals(Object o) {
    return this == o || (o != null && this.getClass() == o.getClass()
        && this.name.equals(((Namespace) o).name));
  }

  public String getModuleName() {
    return moduleName;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public <T extends IndentPrintWriter> T writeTo(T out) {
    writeNameTo(out.append("package").whitespace()).append('{').newline();
    writeChildrenTo(out.withIndent());
    out.append('}').newline();
    return out;
  }

  /**
   * Adds {@link #getName() name} to the diagram.
   *
   * @param out
   *          Output to append the package name to.
   * @return {@code out}
   */
  private <T extends IndentPrintWriter> T writeNameTo(T out) {
    out.append(normalNs(name)).whitespace();
    return out;
  }
}
