/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaRenderer.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.render;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.pdfclown.jada.core.util.Objects.realSubTypes;

import java.nio.file.Path;
import java.util.Map;
import javax.lang.model.element.Element;
import org.apache.commons.lang3.function.Failable;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.util.Nameable;

/**
 * Specialized taglet renderer.
 *
 * @author Stefano Chizzolini
 */
public abstract class JadaRenderer implements Nameable {
  private static final Map<String, JadaRenderer> RENDERERS = realSubTypes(JadaRenderer.class)
      .map(Failable.asFunction($ -> $.getConstructor().newInstance()))
      .collect(toUnmodifiableMap(JadaRenderer::getName, identity()));

  public static @Nullable JadaRenderer renderer(String name) {
    return RENDERERS.get(name);
  }

  private final String name;

  protected JadaRenderer(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  /*
   * TODO: add param values from tag (for example: {@myTag rendererName paramValue1 paramValue2}).
   */
  /**
   * Renders a taglet at the given file.
   *
   * @return {@code null} if rendering is postponed.
   */
  public abstract String render(Path path, Jada jada);

  /*
   * TODO: replace `tagValue` with param values from tag (for example: {@myTag rendererName
   * paramValue1 paramValue2}).
   */
  /**
   * Checks whether the context is valid for this renderer.
   * <p>
   * Ensures inappropriate tags don't get rendered.
   * </p>
   *
   * @throws RuntimeException
   *           if the context is invalid.
   */
  public abstract void validate(String tagValue, Element element, Jada jada);
}
