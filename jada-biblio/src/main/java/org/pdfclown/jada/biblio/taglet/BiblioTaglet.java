/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BiblioTaglet.java) is part of jada-biblio module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.biblio.taglet;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Strings.EMPTY;

import com.sun.source.doctree.DocTree;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Initializer;
import org.pdfclown.jada.biblio.BiblioConfig.BiblioEntry;
import org.pdfclown.jada.biblio.BiblioExtension;
import org.pdfclown.jada.core.taglet.MainTaglet;
import org.pdfclown.jada.core.util.lang.LangModels;

/**
 * Bibliographic taglet.
 * <p>
 * Represents a citation (that is, bibliographic reference) associated to a bibliographic entry in
 * {@code biblio.xml} — see {@link BiblioExtension} for further information.
 * </p>
 * <p>
 * Format {@biblio.spec W3C-EBNF}:
 * </p>
 * <ul>
 * <li>source:<pre class="lang-bnf"><code>
 * BiblioTag ::= '{&#64;' BiblioTagName ' ' BiblioRef '}'         /* Inline bibliographic tag *&#47;
 * BiblioTagName ::= ( 'biblio.doc'
 *                   | 'biblio.ref'
 *                   | 'biblio.spec' )                        /* Bibliographic tag name *&#47;
 * BiblioRef ::= FullId ( ' ' Section ( ';' Section )* )?     /* Bibliographic reference *&#47;
 * FullId ::= Id ( '/' Part )? ( ':' Version )?               /* Fully-qualified bibliographic entry alias *&#47;
 * Id ::= Segment                                             /* Simple bibliographic entry alias *&#47;
 * Part ::= Segment                                           /* Bibliographic entry part alias *&#47;
 * Version ::= Segment                                        /* Bibliographic entry version *&#47;
 * Segment ::= [A-Z0-9.-~]+                                   /* Bibliographic entry segment *&#47;
 * Section ::= Subsection ( ':' Subsection )*                 /* Bibliographic entry section *&#47;
 * Subsection ::= [^\s\}]+                                    /* Bibliographic entry subsection *&#47;</code></pre></li>
 * <li>output:
 * <p>
 * (see subclasses)
 * </p>
 * </li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
public abstract class BiblioTaglet extends MainTaglet {
  /**
   * Bibliographic reference.
   * <p>
   * Format {@biblio.spec W3C-EBNF}:
   * </p>
   * <pre class="lang-bnf"><code>
   * BiblioRef ::= FullId ( ' ' Section ( ';' Section )* )?     /* Bibliographic reference *&#47;
   * FullId ::= Id ( '/' Part )? ( ':' Version )?               /* Fully-qualified bibliographic entry alias *&#47;
   * Id ::= Segment                                             /* Simple bibliographic entry alias *&#47;
   * Part ::= Segment                                           /* Bibliographic entry part alias *&#47;
   * Version ::= Segment                                        /* Bibliographic entry version *&#47;
   * Segment ::= [A-Z0-9-\.]+                                   /* Bibliographic entry segment *&#47;
   * Section ::= Subsection ( ':' Subsection )*                 /* Bibliographic entry section *&#47;
   * Subsection ::= [^\s\}]+                                    /* Bibliographic entry subsection *&#47;</code></pre>
   *
   * @author Stefano Chizzolini
   */
  public static class BiblioRef {
    public static final String SEPARATOR__PART = "/";
    public static final String SEPARATOR__SECTION = ";";
    public static final String SEPARATOR__VERSION = ":";

    public static final String PATTERN_GROUP__REF_FULL_ID = "refFullId";
    public static final String PATTERN_GROUP__REF_ID = "refId";
    public static final String PATTERN_GROUP__REF_PART = "refPart";
    public static final String PATTERN_GROUP__REF_SECTIONS = "refSections";
    public static final String PATTERN_GROUP__REF_VERSION = "refVersion";

    public static final String REGEX__SEGMENT = "[A-Z0-9-.~]+";

    public static final Pattern PATTERN = Pattern.compile("""
        (?<%s>\
        (?<%s>%s)\
        (%s(?<%s>%s))?\
        (%s(?<%s>%s))?)\
        (\\s+(?<%s>[^\\s}]++))?""".formatted(
        PATTERN_GROUP__REF_FULL_ID,
        PATTERN_GROUP__REF_ID, REGEX__SEGMENT,
        SEPARATOR__PART, PATTERN_GROUP__REF_PART, REGEX__SEGMENT,
        SEPARATOR__VERSION, PATTERN_GROUP__REF_VERSION, REGEX__SEGMENT,
        PATTERN_GROUP__REF_SECTIONS));

    static BiblioRef of(String value) {
      var m = PATTERN.matcher(value);
      if (!m.matches())
        throw wrongArg("value", value, "Bibliographic reference INVALID");

      return new BiblioRef(m.group(PATTERN_GROUP__REF_ID), m.group(PATTERN_GROUP__REF_PART),
          m.group(PATTERN_GROUP__REF_VERSION), requireNonNullElse(split(
              m.group(PATTERN_GROUP__REF_SECTIONS), SEPARATOR__SECTION), EMPTY_STRING_ARRAY));
    }

    private final String fullId;
    private final String id;
    private final @Nullable String part;
    private final String[] sections;
    private final @Nullable String version;

    private BiblioRef(String id, @Nullable String part, @Nullable String version,
        String[] sections) {
      this.id = requireNonNull(id, "`id`");
      this.part = part;
      this.version = version;
      this.sections = requireNonNull(sections, "`sections`");

      {
        var b = new StringBuilder(id);
        if (part != null) {
          b.append(SEPARATOR__PART).append(part);
        }
        if (version != null) {
          b.append(SEPARATOR__VERSION).append(version);
        }
        this.fullId = b.toString();
      }
    }

    /**
     * Fully-qualified publication alias.
     */
    public String getFullId() {
      return fullId;
    }

    /**
     * Simple publication alias.
     */
    public String getId() {
      return id;
    }

    public @Nullable String getPart() {
      return part;
    }

    public String[] getSections() {
      return sections;
    }

    public @Nullable String getVersion() {
      return version;
    }

    @Override
    public String toString() {
      return getFullId()
          + (sections.length > 0 ? SPACE + join(sections, SEPARATOR__SECTION) : EMPTY);
    }
  }

  @SuppressWarnings("NotNullFieldNotInitialized")
  protected BiblioExtension extension;

  @Initializer
  @Override
  public void init(DocletEnvironment env, Doclet doclet) {
    super.init(env, doclet);

    extension = getConfig().getExtension(BiblioExtension.class);
  }

  @Override
  public String toString(List<? extends DocTree> tags, Element element) {
    String body;
    String title;
    {
      String tagletValue = LangModels.text(tags.get(0));
      BiblioRef biblioRef = BiblioRef.of(tagletValue);
      BiblioEntry biblioEntry = extension.getExtConfig().getBiblioEntry(biblioRef.getFullId());
      Path file = resolveOutputFile(element);
      /*
       * NOTE: No need to log the missing entry (already done by `BiblioExtension.scanSource()`).
       */
      if (biblioEntry != null) {
        body = renderBody(biblioRef, biblioEntry, element, file);
        title = renderTitle(biblioRef, biblioEntry);
      } else {
        body = tagletValue;
        title = EMPTY;
      }
    }
    return !isEmpty(body)
        ? "<cite class=\"%s\" title=\"%s\">[%s]</cite>".formatted(getName(), title, body)
        : body;
  }

  /**
   * Renders this taglet's body.
   *
   * @param path
   *          Taglet location.
   */
  protected abstract String renderBody(BiblioRef ref, BiblioEntry entry, Element element,
      Path path);

  /**
   * Renders this taglet's title.
   */
  protected abstract String renderTitle(BiblioRef ref, BiblioEntry entry);
}
