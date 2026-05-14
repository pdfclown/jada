/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JavadocsTest.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.util.lang;

import static org.pdfclown.common.build.test.assertion.Verifiers.VERIFIER__COMBINATION;
import static org.pdfclown.common.util.Strings.S;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.core.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
@SuppressWarnings("Convert2MethodRef")
class JavadocsTest extends BaseTest {
  @Test
  void inlineTagPattern() {
    final String content = """
        public class MyClass {
          /**
           * A comment to a method {@myTag WHAT?}, {@link
           * AnotherClass#anotherMethod(int)}, and also
           * {@linkplain YetAnotherClass#yetAnotherMethod(char[]) yet
           * another method}, with {@myTag}
           * {@code classic                              Javadoc
           * value}.
           */
          private int myMethod() {
            return 0;
          }

          ///
          /// A comment to a method {@myTag WHAT Markdown?}, [AnotherClass#anotherMD(int)],
          /// and also [yet another
          /// method (Markdown)][YetAnotherClass#yetAnotherMD(char\\[\\])], \
          /// with `Markdown       Javadoc
          /// value`.
          ///\s
          ///
          private int myMethod2() {
            return 0;
          }
        }
        """;

    VERIFIER__COMBINATION.verify(
        (Set<String> tagNames) -> {
          var pattern = Javadocs.inlineTagPattern(tagNames);
          var matcher = pattern.matcher(content);
          var ret = new ArrayList<String>();
          while (matcher.find()) {
            ret.add(Javadocs.inlineTagName(matcher));
            ret.add(Javadocs.inlineTagValue(matcher));
          }
          return ret;
        },
        List.of("tagNames"),
        // tagNames
        List.of(
            Set.of(),
            Set.of("myTag"),
            Set.of("myTag", "code"),
            Set.of("link", "code")));
  }

  @Test
  void normal() {
    VERIFIER__COMBINATION.verify(
        (content) -> Javadocs.normal(content),
        List.of("content"),
        List.of(
            // comment block, with leading star and significant star
            S
                + "  something\n"
                + "  *  and even   \n"
                + "more (5 * 4 = 20)\n"
                + "\t\t*    !",
            // comment block, with extra leading (thus significant) star
            S
                + "something\n"
                + "  * * and even   \n"
                + "more (5 * 4 = 20)\n"
                + "\t\t!",
            // single-line comment symbols
            S
                + "///  something\n"
                + "  ///  \t  * and even   \n"
                + "\t///\t more (5 * 4 = 20)\n"
                + "\t\t///!",
            // preformatted blocks
            S
                + "   *     <p>Some text followed by</p><pre>\n"
                + "   * public class MyClass {\n"
                + "   *   private int myMethod() {\n"
                + "      return 0;\n"
                + "   *   }\n"
                + "   * }</pre>\n"
                + "   * <p>And other content\n"
                + " on several\n"
                + "\t    *         lines.</p>"));
  }
}
