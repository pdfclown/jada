/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageDiagram.java) is part of jada-uml module in Jada project
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

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_NS_SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.LazyNonNull;
import org.pdfclown.common.util.io.IndentWriter;
import org.pdfclown.jada.uml.UmlConfig;

// SourceName: nl.talsmasoftware.umldoclet.uml.PackageDiagram
/**
 * Package diagram.
 * <p>
 * UML helper class to render a {@link Diagram} for a java package.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class PackageDiagram extends Diagram {
  private static final String SEPARATOR_DIRECTIVE = "set separator ";

  private final @Nullable String moduleName;
  private final String packageName;
  private @LazyNonNull @Nullable Path pumlFile;

  public PackageDiagram(UmlConfig config, String packageName, @Nullable String moduleName) {
    super(config);

    this.packageName = requireNonNull(packageName, "`packageName`");
    this.moduleName = moduleName;
  }

  @Override
  protected Path getPlantUmlFile() {
    if (pumlFile == null) {
      var b = new StringBuilder(getConfig().getConfig().getOutputDirectory().toString());
      if (!b.isEmpty() && b.charAt(b.length() - 1) != File.separatorChar) {
        b.append(File.separatorChar);
      }
      if (moduleName != null) {
        b.append(moduleName).append(File.separatorChar);
      }
      b.append(packageName.replace(DOT, File.separatorChar)).append(File.separatorChar);
      b.append(FILENAME__PACKAGE).append(FILE_EXTENSION__PLANTUML);
      pumlFile = Path.of(b.toString());
    }
    return pumlFile;
  }

  @Override
  protected IndentWriter writeCustomDirectives(@Nullable List<String> customDirectives,
      IndentWriter out) throws IOException {
    final var directives = new ArrayList<>(customDirectives != null ? customDirectives
        : emptyList());
    {
      directives.removeIf($ -> $.startsWith(SEPARATOR_DIRECTIVE));
      directives.add(SEPARATOR_DIRECTIVE + PUML_NS_SEPARATOR);
    }
    return super.writeCustomDirectives(directives, out);
  }
}
