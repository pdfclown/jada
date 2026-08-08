/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ElementLinkFixer.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.internal;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.proc.JadaHtmlProcessor;
import org.pdfclown.jada.core.system.proc.FileProcess;

/*
 * TODO: this class is only a partial solution (for example, it doesn't fix broken links from
 * resolved @inheritDoc tag blocks); anyway, if log interception (see
 * `JadaConfig.LogWriterInterceptor`) proves reliable for retrieving the current output context,
 * this class could eventually become unnecessary.
 */
/**
 * Relative link fixer for program elements.
 * <p>
 * This class post-processes root-level documentation files to fix element-derived relative paths
 * generated during taglet rendering (see
 * {@link org.pdfclown.jada.core.taglet.MainTaglet#resolveOutputFile(Element)
 * MainTaglet.resolveOutputFile(Element)}), as they are outside their element-specific paths — the
 * taglet may have called {@link JadaConfig#getOutputPage(Element)} to get, say,
 * {@code "com/example/myapp/module1/package-summary.html"}, which corresponds to
 * {@code "../../../../index.html"} as relative link to Overview page, but if that tag is rendered
 * in the "Package summary" inside {@code index.html}, that link must be fixed as
 * {@code "index.html"}).
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class ElementLinkFixer extends JadaHtmlProcessor {
  private static final Pattern PATTERN__WRONG_PATH = Pattern.compile("(?:\\.\\./)+");

  @SuppressWarnings("NotNullFieldNotInitialized")
  private Path rootPath;

  public ElementLinkFixer(JadaConfig config) {
    rootPath = config.getOutputDirectory();
  }

  /**
   * <span class="warning">(For internal use only)</span>
   */
  @SuppressWarnings("NullAway")
  protected ElementLinkFixer() {
  }

  @Override
  public int getPriority() {
    return 0;
  }

  /**
   * @implNote Only root-level files require to be processed for fixing.
   */
  @Override
  public boolean isProcessable(Path file, FileProcess.Context context) {
    return super.isProcessable(file, context)
        && requireNonNull(file.getParent(), "file.parent").equals(rootPath);
  }

  /**
   * @implNote This method strips off any relative link segment from root-level files.
   */
  @Override
  protected @Nullable String processContent(String content, Path file,
      FileProcess.Context context) {
    Matcher matcher = PATTERN__WRONG_PATH.matcher(content);
    StringBuilder out = null;
    while (matcher.find()) {
      if (out == null) {
        out = new StringBuilder();
      }
      matcher.appendReplacement(out, EMPTY);
    }
    // No change?
    if (out == null)
      return null;

    context.changeFile();
    matcher.appendTail(out);
    return out.toString();
  }
}
