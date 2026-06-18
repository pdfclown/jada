/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DiagramCollector.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.proc;

import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNullElse;
import static org.pdfclown.common.util.function.Functions.toOrNull;
import static org.pdfclown.common.util.io.Files.isExtension;
import static org.pdfclown.common.util.io.Files.normal;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_DEPS;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.UnmodifiableView;
import org.pdfclown.common.util.io.Files;
import org.pdfclown.jada.uml.UmlConfig;
import org.pdfclown.jada.uml.UmlConfig.ImageConfig;

// SourceName: nl.talsmasoftware.umldoclet.html.DiagramCollector
/**
 * Collects all generated diagram files from the output directory.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
final class DiagramCollector extends SimpleFileVisitor<Path> {
  // SourceName: basedir
  private final Path baseDir;
  private final ImageConfig.Format imageFormat;
  // SourceName: imagesDirectory
  private final @Nullable Path imagesDir;
  // SourceName: collected
  private final ThreadLocal<Collection<DiagramFile>> resultsLocal =
      ThreadLocal.withInitial(ArrayList::new);

  /**
   * @implNote Contrary to the original implementation, {@link UmlConfig.ImageConfig#getFormats()}
   *           cannot be empty, ever.
   */
  DiagramCollector(UmlConfig config) {
    this.baseDir = config.getConfig().getOutputDirectory();
    this.imageFormat = config.getImageConfig().getFormats().stream().findFirst().orElseThrow();
    this.imagesDir = toOrNull(config.getImageConfig().getSubDirectory(),
        $ -> config.getConfig().getOutputDirectory().resolve($));
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (attrs.isRegularFile() && isExtension(file.toString(), imageFormat.fileExtension)) {
      resultsLocal.get().add(createDiagram(file));
    }
    return super.visitFile(file, attrs);
  }

  /**
   * Collects all generated diagram files by walking the path.
   */
  @UnmodifiableView
  Collection<DiagramFile> collectDiagrams() throws IOException {
    try {
      walkFileTree(requireNonNullElse(imagesDir, baseDir), this);
      return unmodifiableCollection(resultsLocal.get());
    } finally {
      resultsLocal.remove();
    }
  }

  // SourceName: createDiagramInstance
  private DiagramFile createDiagram(Path file) {
    return switch (Files.basename(file = normal(file))) {
      case FILENAME__PACKAGE -> new PackageDiagramFile(baseDir, file, imageFormat,
          imagesDir != null);
      case FILENAME__PACKAGE_DEPS -> new PackageDependenciesFile(baseDir, file, imageFormat);
      default -> new ClassDiagramFile(baseDir, file, imageFormat, imagesDir != null);
    };
  }
}
