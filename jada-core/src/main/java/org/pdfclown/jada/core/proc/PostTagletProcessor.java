/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PostTagletProcessor.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.proc;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.jada.core.util.lang.Javadocs.inlineTagName;
import static org.pdfclown.jada.core.util.lang.Javadocs.inlineTagPattern;
import static org.pdfclown.jada.core.util.lang.Javadocs.inlineTagValue;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.tools.Diagnostic.Kind;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.jada.core.event.PostProcessEvent;
import org.pdfclown.jada.core.internal.JadaMessage;
import org.pdfclown.jada.core.system.proc.FileProcess;
import org.pdfclown.jada.core.taglet.PostTaglet;
import org.pdfclown.jada.core.util.lang.Javadocs.JavadocFormat;

/**
 * Javadoc post-process tag rendering.
 * <p>
 * Renders {@link PostTaglet} tags.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class PostTagletProcessor extends JadaHtmlProcessor {
  private static final Pattern PATTERN__TAGS =
      inlineTagPattern(Set.of(), Set.of(JavadocFormat.HTML));

  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull Map<String, PostTaglet> taglets;

  /**
   * @return {@code 0}
   * @implNote Normal priority provides the opportunity to other higher-priority processors to add
   *           post-process tags themselves, and to lower-priority processors to further transform
   *           the resulting rendering.
   */
  @Override
  public int getPriority() {
    return 0;
  }

  @Override
  public void onPostProcess(PostProcessEvent event) {
    super.onPostProcess(event);

    taglets = getConfig().getTaglets().values().stream()
        .filter($ -> $ instanceof PostTaglet)
        .map(PostTaglet.class::cast)
        .collect(toUnmodifiableMap(PostTaglet::getName, identity()));
  }

  @Override
  protected @Nullable String processContent(String content, Path file,
      FileProcess.Context context) {
    var matcher = PATTERN__TAGS.matcher(content);
    StringBuilder b = null;
    while (matcher.find()) {
      var tagName = inlineTagName(matcher);
      var taglet = taglets.get(tagName);
      if (taglet == null) {
        getLog().print(Kind.WARNING, this, JadaMessage.POST_TAG_UNKNOWN, tagName, file);
        continue;
      }

      var tagValue = inlineTagValue(matcher);
      String render;
      try {
        render = taglet.render(tagValue, matcher.start(), matcher.end(), content, file);
        if (render != null) {
          context.changeFile();
          if (!render.isEmpty()) {
            /*
             * NOTE: Rendering results may contain tags themselves, requiring further passes to
             * completely resolve them.
             */
            String nextRender;
            while ((nextRender = processContent(render, file, context)) != null) {
              render = nextRender;
            }
          }
        }
        // Rendering postponed.
        else {
          context.postponeFile();
          continue;
        }
      } catch (Exception ex) {
        throw runtime("Page \"{}\" rendering FAILED for \"{}\"", file, matcher.group(), ex);
      }
      if (b == null) {
        b = new StringBuilder();
      }
      matcher.appendReplacement(b, render);
    }
    // No change?
    if (b == null)
      return null;

    matcher.appendTail(b);
    return b.toString();
  }
}
