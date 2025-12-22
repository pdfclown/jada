/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RenderTaglet.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.taglet;

import static org.pdfclown.common.util.Objects.nonNull;
import static org.pdfclown.jada.core.internal.Internals.TAG_PREFIX__JADA;
import static org.pdfclown.jada.core.internal.JadaMessage.P__RENDERER;
import static org.pdfclown.jada.core.render.JadaRenderer.renderer;

import com.sun.source.doctree.DocTree;
import java.nio.file.Path;
import java.util.List;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.internal.JadaMessage;
import org.pdfclown.jada.core.render.JadaRenderer;

/**
 * Render taglet ({@code @jada.render} tag).
 * <p>
 * Invokes the specialized renderer on post-processing.
 * </p>
 *
 * @author Stefano Chizzolini
 * @see Jada
 */
public class RenderTaglet extends PostTaglet {
  public static final String NAME = TAG_PREFIX__JADA + "render";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public @Nullable String render(String tagValue, int tagStart, int tagEnd, String fileContent,
      Path filePath) {
    /*
     * TODO: `tagValue` should be an array of values, whose first one is the renderer's name.
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    var rendererName = tagValue;
    return nonNull(renderer(rendererName)).render(filePath, getJada());
  }

  @Override
  public String toString(List<? extends DocTree> tags, Element element) {
    /*
     * TODO: `tagValue` should be an array of values, whose first one is the renderer's name.
     */
    var rendererName = toValue(tags, element);
    JadaRenderer renderer = renderer(rendererName);
    if (renderer == null) {
      getLog().print(Kind.ERROR, this, JadaMessage.OBJECT_MISSING, P__RENDERER, rendererName);

      return toFailureString("@%s %s".formatted(getName(), rendererName));
    }

    try {
      renderer.validate(rendererName, element, getJada());
    } catch (RuntimeException ex) {
      var tagString = "@%s %s".formatted(getName(), rendererName);
      getLog().print(Kind.ERROR, this, JadaMessage.TAG_INVALID, tagString, element, ex);

      return toFailureString(tagString);
    }

    return super.toString(tags, element);
  }
}
