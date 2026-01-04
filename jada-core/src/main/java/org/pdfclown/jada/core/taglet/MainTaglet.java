/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (MainTaglet.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.taglet;

import com.sun.source.doctree.DocTree;
import java.nio.file.Path;
import java.util.List;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.Taglet;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.internal.ElementLinkFixer;
import org.pdfclown.jada.core.internal.JadaMessage;
import org.pdfclown.jada.core.proc.JadaFileProcess;

/**
 * Jada taglet for main processing.
 *
 * @author Stefano Chizzolini
 * @see PostTaglet
 * @see Jada
 */
public abstract class MainTaglet extends JadaTaglet {
  /**
   * Gets the representation of the faulty tag to be included in the generated output.
   */
  protected static String toFailureString(List<? extends DocTree> tags) {
    return toFailureString(tags.get(0).toString());
  }

  private boolean elementLinkFixerEnabled = false;

  /**
   * Gets the path where the tag is located, using the element as a fallback hint.
   * <p>
   * <span class="important">IMPORTANT: To be called from within {@link #toString(List, Element)}
   * only — abusive calls will cause unpredictable results.</span>
   * </p>
   *
   * @implNote On {@linkplain Taglet#toString(List, Element) rendering request}, the {@link Doclet}
   *           architecture doesn't provide the target filesystem location where the rendering of a
   *           specific tag occurs, so we are forced to work around this limitation
   *           {@linkplain JadaConfig#getCurrentOutputFile() intercepting the diagnostic log} for
   *           the current file path; if unavailable, relative paths are blindly generated based on
   *           the {@code element}, and finally {@linkplain ElementLinkFixer post-processed} to fix
   *           possible inconsistencies.
   */
  protected Path resolveOutputFile(Element element) {
    var config = getConfig();

    // Try actual path!
    var ret = config.getCurrentOutputFile();
    // If missing, fall back to calculated path!
    if (ret == null) {
      /*
       * NOTE: Calculated paths are problematic, as they are blind to the actual output file where
       * the tags associated to the element are placed (for example, a taglet in the first sentence
       * of the Javadoc comment of a package is rendered twice: once in its own page (subdirectory)
       * and once in the overview (root directory)), causing relative links to break.
       * `ElementLinkFixer` is responsible to fix such broken links on post-processing.
       */
      ret = config.getOutputPage(element);

      if (!elementLinkFixerEnabled) {
        /*
         * NOTE: Since the `Doclet` architecture isolates each taglet from all the others by
         * `ClassLoader` boundaries (see also `org.pdfclown.jada.core.Jada` for more info), all its
         * dependency `Class`es are duplicated along with their static state (that is, there is a
         * distinct `MainTaglet` along with its `elementLinkFixerEnabled` field for `SpecTaglet`,
         * for `DocTaglet`, for `RefTaglet`, ... -- yes, it's totally crazy, but this is how the
         * official `jdk.javadoc.doclet.StandardDoclet` was implemented!). Consequently, we have to
         * check directly whether `ElementLinkFixer` has already been added to the (shared)
         * configuration by another binary copy of `MainTaglet` (ouch!).
         */
        elementLinkFixerEnabled = true;

        var fileProcess = config.getOperation(JadaFileProcess.class);
        if (fileProcess.getProcessor(ElementLinkFixer.class) == null) {
          getLog().print(Kind.WARNING, element, this, JadaMessage.ELEMENT_PATHS_UNAVAILABLE,
              element, ret);

          fileProcess.addProcessor(new ElementLinkFixer(config));
        }
      }
    }
    return ret;
  }
}
