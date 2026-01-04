/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Link.java) is part of jada-uml module in Jada project
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

import static java.nio.file.Files.isRegularFile;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML;
import static org.pdfclown.common.util.io.Files.relativize;
import static org.pdfclown.common.util.net.Uris.SCHEME__FILE;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__MODULE_SUMMARY;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_SUMMARY;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.io.IndentPrintWriter;

// SourceName: nl.talsmasoftware.umldoclet.uml.Link
/**
 * Class for rendering links in the generated UML
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Link extends UmlNode {
  // SourceName: LINK_FROM
  private static final ThreadLocal<@Nullable Path> BASE_DIRECTORY = new ThreadLocal<>();

  /**
   */
  public static Link forPackage(Namespace namespace) {
    final String destinationDirectory =
        namespace.getConfig().getConfig().getOutputDirectory().toString();
    final String moduleName = namespace.getModuleName();
    final String packageName = namespace.getName();
    Optional<URI> target = Stream.of(FILENAME__PACKAGE_SUMMARY, FILENAME__MODULE_SUMMARY)
        .map(name -> relativeHtmlFile(destinationDirectory, moduleName, packageName, name))
        .filter(Optional::isPresent).map(Optional::get)
        .findFirst()
        .or(() -> namespace.getConfig().resolveExternalLinkToType(packageName,
            FILENAME__PACKAGE_SUMMARY));
    return new Link(namespace, target.orElse(null));
  }

  /**
   */
  public static Link forType(Type type) {
    final String destinationDirectory = type.getConfig().getConfig().getOutputDirectory()
        .toString();
    final String packageName = type.getPackageName();
    final String packageLocalName = type.getPackageLocalName();
    Optional<URI> target = relativeHtmlFile(destinationDirectory, type.getModuleName(),
        packageName, packageLocalName)
            .or(() -> type.getConfig().resolveExternalLinkToType(packageName, packageLocalName));
    return new Link(type, target.orElse(null));
  }

  // SourceName: linkFrom
  /**
   * Sets the base directory to resolve relative links.
   * <p>
   * This setting is configured on a per-thread basis.
   * </p>
   *
   * @return whether the base path was modified or not
   */
  public static boolean updateBaseDirectory(@Nullable Path value) {
    if (Objects.equals(value, BASE_DIRECTORY.get()))
      return false;

    if (value == null) {
      BASE_DIRECTORY.remove();
    } else {
      BASE_DIRECTORY.set(value);
    }
    return true;
  }

  /**
   * @implNote Contrary to upstream implementation, {@code moduleName} cannot be {@code null} (empty
   *           corresponds to unnamed module).
   */
  private static Optional<URI> relativeHtmlFile(String destinationDirectory, String moduleName,
      String packageName, String nameInPackage) {
    final String packagePath = packageName.replace(DOT, File.separatorChar);
    final String htmlFileName = nameInPackage + FILE_EXTENSION__HTML;

    Path file;
    if (isRegularFile(file = Path.of(destinationDirectory, packagePath, htmlFileName)))
      return Optional.of(file.toUri());
    else if (!moduleName.isEmpty() && isRegularFile(file = Path.of(destinationDirectory, moduleName,
        packagePath, htmlFileName)))
      return Optional.of(file.toUri());

    return Optional.empty();
  }

  private final @Nullable URI target;

  private Link(UmlNode parent, @Nullable URI target) {
    super(parent);

    this.target = target;
  }

  @Override
  public <T extends IndentPrintWriter> T writeTo(T out) {
    if (target != null) {
      out.append("[[").append(relativeTarget().orElseGet(target::toASCIIString)).append("]]");
    }
    return out;
  }

  // SourceName: linkFromDir
  private Optional<Path> baseDirectory() {
    return Optional.ofNullable(BASE_DIRECTORY.get())
        .or(() -> Optional.of(getConfig().getConfig().getOutputDirectory()))
        .filter(Files::isDirectory);
  }

  private Optional<String> relativeTarget() {
    return Optional.ofNullable(target)
        .filter($ -> SCHEME__FILE.equals($.getScheme())).map(Path::of)
        .flatMap($targetFile -> baseDirectory().map($ -> relativize($, $targetFile).toString()));
  }
}
