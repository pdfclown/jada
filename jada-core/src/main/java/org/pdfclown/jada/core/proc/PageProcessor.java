/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PageProcessor.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.proc;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.basicLiteral;
import static org.pdfclown.common.util.Objects.fqn;
import static org.pdfclown.common.util.Strings.isInteger;
import static org.pdfclown.common.util.Strings.stripEmptyLines;
import static org.pdfclown.common.util.io.Files.baseName;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.tools.Diagnostic.Kind;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.core.event.PostProcessEvent;
import org.pdfclown.jada.core.system.proc.FileProcess.Context;
import org.pdfclown.jada.core.util.html.Jsoups;

/**
 * Processes the generated documents to add custom contents and to fix content flaws off-the-shelf
 * formatters are blind to.
 *
 * @author Stefano Chizzolini
 */
public class PageProcessor extends JadaHtmlProcessor {
  /**
   * Page tweak.
   * <p>
   * Represents a specialized page content modifier embedded in {@link PageProcessor}, providing a
   * lightweight alternative to full-fledged
   * {@linkplain org.pdfclown.jada.core.system.proc.FileProcessor file processors}.
   * </p>
   *
   * @author Stefano Chizzolini
   */
  @FunctionalInterface
  public interface PageTweak {
    /**
     * Processes a page content.
     * <p>
     * In case of content modification, {@code changedRef} MUST be
     * {@linkplain MutableBoolean#setTrue() set to true}
     * </p>
     *
     * @param doc
     *          Page content.
     * @param file
     *          Page location.
     * @param changedRef
     *          Modification flag, used to require the update of {@code file} at the end of the
     *          processing.
     */
    void processContent(Document doc, Path file, MutableBoolean changedRef);
  }

  private final Map<String, PageTweak> tweaks = new LinkedHashMap<>();

  /**
   * Adds a tweak to this processor.
   *
   * @param tweak
   *          Page tweak.
   * @param name
   *          Unique tweak name (if {@code null}, the fully-qualified class name of {@code tweak} is
   *          used instead).
   * @throws IllegalArgumentException
   *           if {@code name} is already registered.
   */
  public PageProcessor addTweak(PageTweak tweak, @Nullable String name) {
    requireNonNull(tweak, "`tweak`");
    if (name == null) {
      name = fqn(tweak);
    }
    if (tweaks.containsKey(name))
      throw wrongArg("name", name, "ALREADY PRESENT");

    tweaks.put(name, tweak);
    return this;
  }

  /**
   * @return {@code -100}
   */
  @Override
  public int getPriority() {
    return -100;
  }

  /**
   * Registered tweaks.
   */
  public Map<String, PageTweak> getTweaks() {
    return tweaks;
  }

  @Override
  public void onPostProcess(PostProcessEvent event) {
    super.onPostProcess(event);

    /*
     * Normalizes whitespace in code blocks ({@code <pre><code>. . .</code></pre>}).
     *
     * This is the static server-side equivalent of Prism - Normalize Whitespace (see
     * <https://prismjs.com/plugins/normalize-whitespace>), ensuring contents are published in their
     * normal form rather than patched on the fly.
     */
    addTweak(($doc, $file, $changedRef) -> $doc.select("pre > code").forEach($ -> {
      String oldHtml = $.html();
      // Normalize pre/code content!
      String newHtml = stripEmptyLines(oldHtml).stripIndent();
      if (!newHtml.equals(oldHtml)) {
        $.html(newHtml);
        $changedRef.setTrue();
      }
    }), "normalizePreCodeContent");

    /*
     * Inserts custom page contents.
     */
    if (getConfig().getPageContents().values().stream()
        .anyMatch($ -> !$.isEmpty())) {
      addTweak(($doc, $file, $changedRef) -> {
        String relativeRoot = $file.getParent().relativize(getConfig().getOutputDirectory())
            .toString();
        getConfig().getPageContents().entrySet().stream()
            .filter($ -> !$.getValue().isEmpty())
            .forEachOrdered($ -> {
              Element targetNode = $doc.selectFirst($.getKey());
              if (targetNode != null) {
                // Append the custom content!
                for (var content : $.getValue()) {
                  targetNode.append(resolveContent(content, relativeRoot));
                }
                $changedRef.setTrue();
              } else {
                getLog().print(Kind.WARNING, this,
                    "Cannot inject custom page content in \"{0}\" because of missing tag <{1}>",
                    $file, $.getKey());
              }
            });
      }, "insertPageContent");
    }

    /*
     * Cleans up spurious URLs generated prepending `{@docRoot}`.
     *
     * NOTE: Despite `{@docRoot}` is typically prepended to URLs, it doesn't render its trailing
     * slash, forcing users to place a fixed slash to concatenate the local path (e.g.,
     * "src=\"{@docRoot}/my/local/path\""); as a result, in the files at Javadoc root directory
     * `{@docRoot}` renders as an empty string, causing the fixed trailing slash to become the
     * leading one (which makes the URL point to the website root; e.g., "src=\"/my/local/path\"")),
     * thus disrupting its resolution.
     *
     * This tweak removes the offending leading slash.
     */
    addTweak(($doc, $file, $changedRef) -> {
      if (!$file.getParent().equals(getConfig().getOutputDirectory()))
        return;

      var oldHtml = $doc.html();
      var newHtml = oldHtml.replace("href=\"/", "href=\"").replace("src=\"/", "src=\"");
      if (!newHtml.equals(oldHtml)) {
        $doc.html(newHtml);
        $changedRef.setTrue();
      }
    }, "cleanupDocRootAtRootDir");

    /*
     * Adds the literal representation to character constant values.
     */
    addTweak(($doc, $file, $changedRef) -> {
      if (!baseName($file).equals("constant-values"))
        return;

      $doc.select("td[class=colFirst]").stream()
          .filter($ -> $.text().contains("static final char"))
          .forEach($ -> {
            //noinspection DataFlowIssue : non-nullable
            for (var valueElement : $.parent().lastElementChild().children()) {
              if (isInteger(valueElement.text())) {
                int charCode = Integer.parseInt(valueElement.text());
                valueElement.text("%s (%s)".formatted(basicLiteral((char) charCode), charCode));
                $changedRef.setTrue();
                break;
              }
            }
          });
    }, "enrichCharConstantFieldValues");
  }

  @Override
  protected @Nullable String processContent(String content, Path file, Context context) {
    Document doc;
    try {
      doc = Jsoups.parse(content);
    } catch (IOException ex) {
      throw runtime(ex);
    }

    var changedRef = new MutableBoolean();
    tweaks.values().forEach($ -> $.processContent(doc, file, changedRef));
    if (!changedRef.get())
      return null;

    context.changeFile();
    return doc.outerHtml();
  }

  private String resolveContent(String content, String relativeRoot) {
    return content.replace("{@docRoot}", relativeRoot);
  }
}
