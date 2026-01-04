/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IncludeTaglet.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.taglet;

import static org.pdfclown.jada.core.internal.Internals.TAG_PREFIX__JADA;

import com.sun.source.doctree.DocTree;
import java.util.List;
import javax.lang.model.element.Element;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.internal.IncludeDocFilter;

/**
 * Content fragment inclusion taglet ({@code @jada.include} tag).
 * <p>
 * The tag is replaced by the content fragment at <code>"{@link
 * JadaConfig#OPTION__RESOURCE_DIR %JADA-DIR%}/$value"</code> — for example:
 * </p>
 * <pre class="lang-java"><code>
 * {&#64;jada.include overview.include.html}</code></pre>
 * <p>
 * resolves to <code>"{@link
 * JadaConfig#OPTION__RESOURCE_DIR %JADA-DIR%}/overview.include.html"</code>.
 * </p>
 *
 * @author Stefano Chizzolini
 * @see Jada
 */
public class IncludeTaglet extends MainTaglet {
  public static final String NAME = TAG_PREFIX__JADA + "include";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void init(DocletEnvironment environment, Doclet doclet) {
    super.init(environment, doclet);

    // Register pre-process filter!
    getEnv().getDocTrees().getTransformer().addFilter(new IncludeDocFilter(getJada()));
  }

  /**
   * @implNote This method is expected to NEVER be called, as its tag should have already been
   *           replaced by the corresponding inclusion content on {@linkplain IncludeDocFilter
   *           Javadoc pre-processing}.
   */
  @Override
  public String toString(List<? extends DocTree> tags, Element element) {
    return toFailureString(tags);
  }
}
