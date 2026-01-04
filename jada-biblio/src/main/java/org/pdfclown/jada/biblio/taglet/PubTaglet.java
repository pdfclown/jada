/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PubTaglet.java) is part of jada-biblio module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.biblio.taglet;

import static org.apache.commons.lang3.StringUtils.join;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Strings.EMPTY;

import java.nio.file.Path;
import javax.lang.model.element.Element;
import org.pdfclown.jada.biblio.BiblioConfig.BiblioEntry;
import org.pdfclown.jada.biblio.BiblioExtension;

/**
 * Publication taglet.
 * <p>
 * Represents a citation (that is, bibliographic reference) associated to a publication entry in
 * {@code biblio.xml} — see {@link BiblioExtension} for further information.
 * </p>
 * <p>
 * Contrary to {@linkplain RefTaglet generic references}, a publication corresponds to a <i>static,
 * discrete resource</i> (such as a book or an article) that can be conveniently consulted off-line.
 * </p>
 * <p>
 * Format {@biblio.spec W3C-EBNF}:
 * </p>
 * <ul>
 * <li>source:<pre class="lang-ebnf"><code>
 * BiblioTag ::= '{&#64;' BiblioTagName ' ' BiblioRef '}'         /* Inline bibliographic tag *&#47;
 * BiblioTagletName ::= ( 'biblio.doc'
 *                      | 'biblio.spec' )                     /* Bibliographic taglet name *&#47;
 * BiblioRef ::= FullId ( ' ' Section ( ';' Section )* )?     /* Bibliographic reference *&#47;
 * FullId ::= Id ( '/' Part )? ( ':' Version )?               /* Fully-qualified bibliographic entry alias *&#47;
 * Id ::= Segment                                             /* Simple bibliographic entry alias *&#47;
 * Part ::= Segment                                           /* Bibliographic entry part alias *&#47;
 * Version ::= Segment                                        /* Bibliographic entry version *&#47;
 * Segment ::= [A-Z0-9.-~]+                                   /* Bibliographic entry segment *&#47;
 * Section ::= Subsection ( ':' Subsection )*                 /* Bibliographic entry section *&#47;
 * Subsection ::= [^\s\}]+                                    /* Bibliographic entry subsection *&#47;</code></pre></li>
 * <li>output:<pre class="lang-ebnf"><code>
 * PubTag ::= '[' FullId ' ' ( '§ ' Section | '§§ ' Section ( ', ' Section )+ ) ']'</code></pre></li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
public abstract class PubTaglet extends BiblioTaglet {
  @Override
  protected String renderBody(BiblioRef ref, BiblioEntry entry, Element element, Path path) {
    return "<a href=\"%s#%s\">%s</a>%s".formatted(
        path.getParent().relativize(extension.getExtConfig().getBiblioOutputFile()),
        ref.getFullId(), ref.getFullId(), ref.getSections().length > 0
            ? SPACE + (ref.getSections().length > 1 ? "§§" : "§") + SPACE
                + join(ref.getSections(), ", ")
            : EMPTY);
  }

  @Override
  protected String renderTitle(BiblioRef ref, BiblioEntry entry) {
    return entry.getLabel();
  }
}
