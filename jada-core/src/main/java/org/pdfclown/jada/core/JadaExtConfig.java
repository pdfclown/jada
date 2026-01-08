/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaExtConfig.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Derived;

/**
 * {@linkplain JadaExtension Jada extension} configuration.
 *
 * @author Stefano Chizzolini
 */
public class JadaExtConfig implements JadaObject {
  @SuppressWarnings("NotNullFieldNotInitialized")
  private JadaExtension extension;

  @Derived
  private transient @Nullable List<Path> resourceDirectories;

  /**
   * <span class="warning">(For internal use only)</span>
   */
  protected JadaExtConfig() {
  }

  protected JadaExtConfig(JadaExtension extension) {
    this.extension = extension;
  }

  /**
   * Extension this configuration belongs to.
   */
  public JadaExtension getExtension() {
    return extension;
  }

  @Override
  public Jada getJada() {
    return extension.getJada();
  }

  /**
   * Gets the resource associated to this extension for the name.
   * <p>
   * In case multiple instances are associated to the name, the first one is returned.
   * </p>
   *
   * @param name
   *          Resource name (that is, relative path under {@linkplain #getResourceDirectories()
   *          resource directories}).
   * @see #getResources(String)
   */
  public Optional<Path> getResource(String name) {
    return getResources(name).findFirst();
  }

  /**
   * Resource directories associated to this extension.
   * <p>
   * These directories correspond to
   * <code>"ext/{@link JadaExtension#getName() %EXTENSION_NAME%}"</code> under the respective
   * {@linkplain JadaConfig#getResourceDirectories() Jada resource directory}.
   * </p>
   */
  public List<Path> getResourceDirectories() {
    if (resourceDirectories == null) {
      resourceDirectories = getJada().getConfig().getResourceDirectories().stream()
          .map($ -> $.resolve("ext" + File.separator + extension.getName()))
          .filter(Files::isDirectory)
          .collect(Collectors.toUnmodifiableList());
    }
    return resourceDirectories;
  }

  /**
   * Gets the resources associated to this extension for the name.
   *
   * @param name
   *          Resource name (that is, relative path under {@linkplain #getResourceDirectories()
   *          resource directories}).
   * @see #getResource(String)
   */
  public Stream<Path> getResources(String name) {
    return getResourceDirectories().stream()
        .map($ -> $.resolve(name))
        .filter(Files::exists);
  }
}
