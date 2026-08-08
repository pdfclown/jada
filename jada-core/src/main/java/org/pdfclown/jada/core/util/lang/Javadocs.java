/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Javadocs.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.util.lang;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;
import static org.pdfclown.common.util.Chars.BACKTICK;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Objects.found;
import static org.pdfclown.common.util.Objects.textLiteral;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.Strings.STR_LENGTH;
import static org.pdfclown.common.util.Strings.indexOfElse;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import jdk.javadoc.doclet.Taglet;

/**
 * Javadoc utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Javadocs {
  /**
   * Javadoc format.
   *
   * @author Stefano Chizzolini
   */
  public enum JavadocFormat {
    /**
     * HTML-based Javadoc.
     */
    HTML,
    /**
     * Markdown-based Javadoc.
     */
    MARKDOWN
  }

  public static final String TAG_NAME__CODE = "code";
  public static final String TAG_NAME__LINK = "link";
  public static final String TAG_NAME__LINKPLAIN = "linkplain";
  public static final String TAG_NAME__VALUE = "value";

  public static final String FILENAME__ELEMENT_LIST = "element-list";
  public static final String FILENAME__MODULE_SUMMARY = "module-summary";
  public static final String FILENAME__OVERVIEW_SUMMARY = "overview-summary";
  public static final String FILENAME__PACKAGE_DEPS = "package-dependencies";
  public static final String FILENAME__PACKAGE_LIST = "package-list";
  public static final String FILENAME__PACKAGE_SUMMARY = "package-summary";

  /**
   * Pattern capturing group name for {@linkplain Taglet#getName() Javadoc inline tag name}.
   *
   * @see #inlineTagPattern(Set)
   */
  public static final String PATTERN_GROUP__INLINE_TAG__NAME = "name";
  /**
   * Pattern capturing group name for {@linkplain Taglet Javadoc inline tag} value.
   *
   * @see #inlineTagPattern(Set)
   */
  public static final String PATTERN_GROUP__INLINE_TAG__VALUE = "value";

  /**
   * Pattern capturing group name for Markdown code tag value.
   *
   * @see #inlineTagPattern(Set)
   */
  private static final String PATTERN_GROUP__INLINE_TAG__MARKDOWN_CODE_VALUE = "mdCodeValue";
  /**
   * Pattern capturing group name for Markdown link, part 0 (either element reference for
   * <code>@link</code> equivalent, or alternative text for <code>@linkplain</code> equivalent).
   *
   * @see #inlineTagPattern(Set)
   */
  private static final String PATTERN_GROUP__INLINE_TAG__MARKDOWN_LINK_PART0 = "mdLinkPart0";
  /**
   * Pattern capturing group name for Markdown link, part 1 (element reference for
   * <code>@linkplain</code> equivalent).
   *
   * @see #inlineTagPattern(Set)
   */
  private static final String PATTERN_GROUP__INLINE_TAG__MARKDOWN_LINK_PART1 = "mdLinkPart1";

  private static final Pattern PATTERN__LEADING_WHITESPACE =
      Pattern.compile("(?m)^\\s*(?:\\*|///)");

  /**
   * Expression impossible to match on purpose.
   */
  private static final String REGEX__UNMATCHABLE = "a\\A";

  /**
   * Creates a file object representation of the content.
   */
  public static FileObject fileObject(URI uri, JavaFileObject.Kind kind, String content) {
    return new SimpleJavaFileObject(uri, kind) {
      @Override
      public CharSequence getCharContent(boolean ignoreEncoding) {
        return content;
      }
    };
  }

  /**
   * Creates the representation of a Javadoc inline tag.
   *
   * @param name
   *          Tag name.
   * @param value
   *          Tag value.
   */
  public static String inlineTag(String name, String value) {
    return "{@%s %s}".formatted(name, value);
  }

  /**
   * Gets the tag name captured by the {@linkplain #inlineTagPattern(Set) inline tag pattern}.
   * <p>
   * Supports both classic HTML- and Markdown-based Javadoc.
   * </p>
   *
   * @param matcher
   *          {@linkplain #inlineTagPattern(Set) Inline tag pattern}-generated matcher. Its current
   *          match is used to extract the inline tag name corresponding to the
   *          {@value #PATTERN_GROUP__INLINE_TAG__NAME} capturing group (in case of classic HTML
   *          Javadoc), or to infer it from tag-specific capturing groups (in case of Markdown
   *          Javadoc).
   * @throws IllegalArgumentException
   *           if {@code matcher} was not generated by an inline tag pattern.
   * @throws IllegalStateException
   *           if {@code matcher} has no current match.
   */
  public static String inlineTagName(Matcher matcher) {
    String ret;
    ret = matcher.group(PATTERN_GROUP__INLINE_TAG__NAME);
    // Markdown tag?
    if (ret == null) {
      // @code-equivalent tag?
      if (matcher.group(PATTERN_GROUP__INLINE_TAG__MARKDOWN_CODE_VALUE) != null) {
        ret = TAG_NAME__CODE;
      }
      // @linkplain-equivalent tag?
      else if (matcher.group(PATTERN_GROUP__INLINE_TAG__MARKDOWN_LINK_PART1) != null) {
        ret = TAG_NAME__LINKPLAIN;
      }
      // @link-equivalent tag?
      else if (matcher.group(PATTERN_GROUP__INLINE_TAG__MARKDOWN_LINK_PART0) != null) {
        ret = TAG_NAME__LINK;
      } else
        throw unexpected(null, null, "inline tag pattern UNKNOWN");
    }
    return ret;
  }

  /**
   * {@jada.reuseDoc} Creates a pattern for {@linkplain Taglet Javadoc inline tag} matching.
   * <p>
   * Supports both classic HTML- and Markdown-based Javadoc.
   * </p>
   * <p>
   * Along with single-line tags, tags spread across multiple adjacent comment lines are supported
   * too — for example:
   * </p>
   * <pre class="lang-java"><code>
   * &#47;**
   *  * Description of module xyz (with a citation to {&#64;biblio.spec CSS/PAGE-3:2018
   *  * 4.8}).
   *  *&#47;</code></pre>
   * <p>
   * or
   * </p>
   * <pre class="lang-java"><code>
   * /// Description of module xyz (with a citation to {&#64;biblio.spec CSS/PAGE-3:2018
   * /// 4.8}).</code></pre>
   * <p>
   * The recommended way to access pattern matches is through {@link #inlineTagName(Matcher)} and
   * {@link #inlineTagValue(Matcher)}.
   * </p>
   *
   * @param tagNames
   *          {@linkplain Taglet#getName() Javadoc inline tag names} to match (if empty, matches any
   *          tag). As <code>@link</code> and <code>@linkplain</code> tags are strictly related,
   *          specifying one of them will cause the other to be matched as well.
   *          {@jada.reuseDoc END}
   */
  public static Pattern inlineTagPattern(Set<String> tagNames) {
    return inlineTagPattern(tagNames, Set.of());
  }

  /**
   * {@jada.doc} Creates a pattern for {@linkplain Taglet Javadoc inline tag} matching.
   * <p>
   * Supports both classic HTML- and Markdown-based Javadoc.
   * </p>
   * <p>
   * Along with single-line tags, tags spread across multiple adjacent comment lines are supported
   * too — for example:
   * </p>
   * <pre class="lang-java"><code>
   * &#47;**
   *  * Description of module xyz (with a citation to {&#64;biblio.spec CSS/PAGE-3:2018
   *  * 4.8}).
   *  *&#47;</code></pre>
   * <p>
   * or
   * </p>
   * <pre class="lang-java"><code>
   * /// Description of module xyz (with a citation to {&#64;biblio.spec CSS/PAGE-3:2018
   * /// 4.8}).</code></pre>
   * <p>
   * The recommended way to access pattern matches is through {@link #inlineTagName(Matcher)} and
   * {@link #inlineTagValue(Matcher)}.
   * </p>
   *
   * @param tagNames
   *          {@linkplain Taglet#getName() Javadoc inline tag names} to match (if empty, matches any
   *          tag). As <code>@link</code> and <code>@linkplain</code> tags are strictly related,
   *          specifying one of them will cause the other to be matched as well. {@jada.doc END}
   * @param formats
   *          Target Javadoc formats.
   */
  public static Pattern inlineTagPattern(Set<String> tagNames, Set<JavadocFormat> formats) {
    var b = new StringBuilder();

    // Markdown-equivalent tags.
    if (formats.isEmpty() || formats.contains(JavadocFormat.MARKDOWN)) {
      /*
       * NOTE: In order to work properly, named capturing groups MUST exist even if unused
       * (otherwise their absence causes `IllegalArgumentException` when matches are inspected via
       * `Matcher.group(..)`). Therefore, `REGEX__UNMATCHABLE` is used to define a dummy expression
       * whose named capturing group is disabled.
       */

      // @code-equivalent tags.
      b
          .append("(?:" + BACKTICK + "(?<" + PATTERN_GROUP__INLINE_TAG__MARKDOWN_CODE_VALUE + ">")
          .append(tagNames.isEmpty() || tagNames.contains(TAG_NAME__CODE)
              ? "[^" + BACKTICK + "]+"
              : REGEX__UNMATCHABLE)
          .append(")" + BACKTICK + ")|");

      // @link- and @linkplain-equivalent tags.
      if (tagNames.contains(TAG_NAME__LINK) || tagNames.contains(TAG_NAME__LINKPLAIN)) {
        /*
         * NOTE: @link-equivalent and @linkplain-equivalent tags are strictly related, so they are
         * integrated in case of partial omission.
         */
        if (!tagNames.contains(TAG_NAME__LINK)) {
          tagNames = new HashSet<>(tagNames);
          tagNames.add(TAG_NAME__LINK);
        } else if (!tagNames.contains(TAG_NAME__LINKPLAIN)) {
          tagNames = new HashSet<>(tagNames);
          tagNames.add(TAG_NAME__LINKPLAIN);
        }
      }
      b
          .append("(?:\\[(?<" + PATTERN_GROUP__INLINE_TAG__MARKDOWN_LINK_PART0 + ">")
          .append(tagNames.isEmpty() || tagNames.contains(TAG_NAME__LINK)
              ? "(?:[^\\\\\\[\\]]|\\\\[\\[\\]])+"
              : REGEX__UNMATCHABLE)
          .append(")](?:\\[(?<" + PATTERN_GROUP__INLINE_TAG__MARKDOWN_LINK_PART1 + ">")
          .append(tagNames.isEmpty() || tagNames.contains(TAG_NAME__LINKPLAIN)
              ? "(?:[^\\\\\\[\\]]|\\\\[\\[\\]])+"
              : REGEX__UNMATCHABLE)
          .append(")])?)|");
    }

    // Regular Javadoc inline tags.
    b
        .append("(?:\\{@(?<" + PATTERN_GROUP__INLINE_TAG__NAME + ">")
        .append(tagNames.isEmpty()
            ? "[\\S&&[^}]]+"
            : tagNames.stream()
                .map(Pattern::quote)
                .collect(joining("\\b|", EMPTY, "\\b")))
        .append(")\\s*(?<" + PATTERN_GROUP__INLINE_TAG__VALUE + ">.*?)})");

    return Pattern.compile(b.toString(), Pattern.DOTALL /*
                                                         * Supports tags split across multiple
                                                         * adjacent comment lines
                                                         */);
  }

  /**
   * Gets the {@linkplain #normal(String) normalized} tag value captured by the
   * {@linkplain #inlineTagPattern(Set) inline tag pattern}.
   * <p>
   * Supports both classic HTML- and Markdown-based Javadoc.
   * </p>
   *
   * @param matcher
   *          {@linkplain #inlineTagPattern(Set) Inline tag pattern}-generated matcher. Its current
   *          match is used to extract the inline tag value corresponding to the
   *          {@value #PATTERN_GROUP__INLINE_TAG__VALUE} capturing group (in case of classic HTML
   *          Javadoc), or to infer it from tag-specific capturing groups (in case of Markdown
   *          Javadoc).
   * @throws IllegalArgumentException
   *           if {@code matcher} was not generated by an inline tag pattern.
   * @throws IllegalStateException
   *           if {@code matcher} has no current match.
   */
  public static String inlineTagValue(Matcher matcher) {
    String ret;
    ret = matcher.group(PATTERN_GROUP__INLINE_TAG__VALUE);
    // Markdown tag?
    if (ret == null) {
      // @code-equivalent tag?
      if (matcher.group(PATTERN_GROUP__INLINE_TAG__MARKDOWN_CODE_VALUE) != null) {
        ret = matcher.group(PATTERN_GROUP__INLINE_TAG__MARKDOWN_CODE_VALUE);
      } else {
        // @linkplain-equivalent tag?
        if (matcher.group(PATTERN_GROUP__INLINE_TAG__MARKDOWN_LINK_PART1) != null) {
          ret = matcher.group(PATTERN_GROUP__INLINE_TAG__MARKDOWN_LINK_PART1) + SPACE
              + matcher.group(PATTERN_GROUP__INLINE_TAG__MARKDOWN_LINK_PART0);
        }
        // @link-equivalent tag?
        else if (matcher.group(PATTERN_GROUP__INLINE_TAG__MARKDOWN_LINK_PART0) != null) {
          ret = matcher.group(PATTERN_GROUP__INLINE_TAG__MARKDOWN_LINK_PART0);
        } else
          throw unexpected(null, null, "inline tag pattern UNKNOWN");

        /*
         * Unescape square brackets for element reference of link and linkplain tags!
         *
         * For example: `String#copyValueOf(char\[\])` to `String#copyValueOf(char[])`
         */
        ret = ret.replace("\\[", "[").replace("\\]", "]");
      }
    }
    return normal(ret);
  }

  /**
   * Normalizes Javadoc content removing comment formatting.
   * <p>
   * Strips surrounding whitespace and converts internal whitespace to single space characters.
   * Comment symbols (first {@code "*"} (classic Javadoc) or {@code "///"}
   * (<a href="https://openjdk.org/jeps/467">Markdown-based Javadoc</a>) at the beginning of new
   * lines) are treated as whitespace too. Conversely, whitespace within preformatted blocks
   * ({@code <pre>} tags) is preserved.
   * </p>
   */
  public static String normal(String content) {
    /*
     * Leading whitespace removal.
     *
     * NOTE: Leading whitespace is replaced with single space to emulate the behavior of Javadoc
     * tool.
     */
    content = PATTERN__LEADING_WHITESPACE.matcher(content).replaceAll(S + SPACE);

    /*
     * Inner (and trailing) whitespace normalization.
     *
     * NOTE: Whitespace within preformatted blocks is preserved, whilst outside is normalized.
     */
    StringBuilder b = null;
    int oldPos = 0;
    while (true) {
      int preBegin = content.indexOf("<pre", oldPos);
      if (!found(preBegin)) {
        break;
      }

      int preContentBegin = content.indexOf('>', preBegin);
      if (!found(preContentBegin++))
        throw runtime("<pre> tag MALFORMED in {}", textLiteral(content.substring(preBegin)));

      int preContentEnd = indexOfElse(content, "</pre>", preContentBegin, STR_LENGTH);
      if (b == null) {
        b = new StringBuilder();
      }
      b
          .append(normalizeSpace(content.substring(oldPos, preContentBegin)))
          .append(content, preContentBegin, preContentEnd);

      oldPos = preContentEnd;
    }
    return b != null
        ? b.append(normalizeSpace(content.substring(oldPos))).toString()
        : normalizeSpace(content);
  }

  private Javadocs() {
  }
}
