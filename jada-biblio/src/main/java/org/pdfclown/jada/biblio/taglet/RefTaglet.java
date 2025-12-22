/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RefTaglet.java) is part of jada-biblio module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.biblio.taglet;

import static org.apache.commons.lang3.StringUtils.join;
import static org.pdfclown.jada.biblio.internal.Internals.TAG_PREFIX__BIBLIO;

import java.nio.file.Path;
import javax.lang.model.element.Element;
import org.pdfclown.common.util.ParamMessage;
import org.pdfclown.jada.biblio.BiblioConfig.BiblioEntry;
import org.pdfclown.jada.biblio.BiblioExtension;

/**
 * Generic reference taglet ({@code @biblio.ref} tag).
 * <p>
 * Represents a citation (that is, bibliographic reference) associated to a {@code <ref>} entry in
 * {@code biblio.xml} — see {@link BiblioExtension} for further information.
 * </p>
 * <p>
 * Contrary to {@linkplain PubTaglet publications}, a generic reference links a <i>dynamic,
 * parametric resource</i> (such as an entry in an issue-tracking system) that is typically
 * consulted on-line.
 * </p>
 * <p>
 * Format {@biblio.spec W3C-EBNF}:
 * </p>
 * <ul>
 * <li>source:<pre class="lang-ebnf"><code>
 * BiblioTag ::= '{&#64;' BiblioTagName ' ' BiblioRef '}'         /* Inline bibliographic tag *&#47;
 * BiblioTagletName ::= 'biblio.ref'                          /* Bibliographic taglet name *&#47;
 * BiblioRef ::= FullId ( ' ' Section ( ';' Section )* )?     /* Bibliographic reference *&#47;
 * FullId ::= Id ( '/' Part )? ( ':' Version )?               /* Fully-qualified bibliographic entry alias *&#47;
 * Id ::= Segment                                             /* Simple bibliographic entry alias *&#47;
 * Part ::= Segment                                           /* Bibliographic entry part alias *&#47;
 * Version ::= Segment                                        /* Bibliographic entry version *&#47;
 * Segment ::= [A-Z0-9.-~]+                                   /* Bibliographic entry segment *&#47;
 * Section ::= Subsection ( ':' Subsection )*                 /* Bibliographic entry section *&#47;
 * Subsection ::= [^\s\}]+                                    /* Bibliographic entry subsection *&#47;</code></pre></li>
 * <li>output:<pre class="lang-ebnf"><code>
 * RefTag ::= '[' FullId ' ' ( Section | Section ( ';' Section )+ ) ']'</code></pre></li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
public class RefTaglet extends BiblioTaglet {
  public static final String NAME = TAG_PREFIX__BIBLIO + "ref";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected String renderBody(BiblioRef ref, BiblioEntry entry, Element element, Path path) {
    return "<a href=\"%s\">%s %s</a>".formatted(
        ParamMessage.format(entry.getElement().getAttribute("url"), (Object[]) ref.getSections()),
        ref.getFullId(), join(ref.getSections(), BiblioRef.SEPARATOR__SECTION));
  }

  @Override
  protected String renderTitle(BiblioRef ref, BiblioEntry entry) {
    return entry.getLabel() + " #" + ref.getSections()[0];
  }
}
