/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ClassDiagram.java) is part of jada-uml module in Jada project
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

import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.LazyNonNull;
import org.pdfclown.common.util.io.IndentWriter;
import org.pdfclown.jada.uml.UmlConfig;

// SourceName: nl.talsmasoftware.umldoclet.uml.ClassDiagram
/**
 * UML diagram for a single class.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class ClassDiagram extends Diagram {
  private @LazyNonNull @Nullable Path pumlFile;

  /**
   */
  @SuppressWarnings("this-escape")
  public ClassDiagram(UmlConfig config, Type type) {
    super(config);

    addChild(type);
  }

  @Override
  public void addChild(UmlNode child) {
    super.addChild(child);

    if (child instanceof Type type) {
      type.setIncludePackageName(true);
    }
  }

  /**
   * Type defined in this diagram.
   */
  public Type getType() {
    return getChildren().stream()
        .filter(Type.class::isInstance).map(Type.class::cast)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No Type defined in Class diagram"));
  }

  /**
   * @implNote Contrary to upstream implementation, empty module name is collapsed (that is, NOT
   *           appended to file path).
   */
  @Override
  protected Path getPlantUmlFile() {
    if (pumlFile == null) {
      final Type type = getType();
      var b = new StringBuilder(getConfig().getConfig().getOutputDirectory().toString());
      if (!b.isEmpty() && b.charAt(b.length() - 1) != File.separatorChar) {
        b.append(File.separatorChar);
      }

      if (!type.getModuleName().isEmpty()) {
        b.append(type.getModuleName()).append(File.separatorChar);
      }

      b.append(type.getPackageName().replace(DOT, File.separatorChar)).append(File.separatorChar);
      if (type.getName().getQualifiedName().startsWith(type.getPackageName() + DOT)) {
        b.append(type.getName().getQualifiedName().substring(type.getPackageName().length() + 1));
      } else {
        b.append(type.getName().getSimpleName());
      }
      pumlFile = Path.of(b.append(FILE_EXTENSION__PLANTUML).toString());
    }
    return pumlFile;
  }

  @Override
  protected IndentWriter writeChildrenTo(IndentWriter out) throws IOException {
    out.append("set namespaceSeparator none").nl()
        .append("hide empty fields").nl()
        .append("hide empty methods").nl()
        .nl();
    return super.writeChildrenTo(out);
  }
}
