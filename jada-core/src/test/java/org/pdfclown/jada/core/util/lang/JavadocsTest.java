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

import static java.util.Arrays.asList;
import static java.util.List.of;
import static java.util.stream.Collectors.joining;
import static org.pdfclown.common.build.test.assertion.Assertions.Argument.qnamed;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.simple;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.util.Aggregations.entry;
import static org.pdfclown.common.util.Chars.COMMA;
import static org.pdfclown.common.util.Chars.LF;
import static org.pdfclown.common.util.Objects.literal;
import static org.pdfclown.common.util.Strings.S;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.common.build.test.assertion.Assertions.ExpectedGeneration;
import org.pdfclown.jada.core.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class JavadocsTest extends BaseTest {
  static Stream<Arguments> normal() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // [1] content[0]: "  something\n  *  and even   \nmore (5 * 4 =. . ."
            "something and even more (5 * 4 = 20) !",
            // [2] content[1]: "something\n  * * and even   \nmore (5 * 4 = . . ."
            "something * and even more (5 * 4 = 20) !",
            // [3] content[2]: "///  something\n  ///  \t  * and even   \n\t. . ."
            "something * and even more (5 * 4 = 20) !",
            // [4] content[3]: "   *     <p>Some text followed by</p><pre>\n. . ."
            S
                + "<p>Some text followed by</p><pre>\n"
                + "  public class MyClass {\n"
                + "    private int myMethod() {\n"
                + "      return 0;\n"
                + "    }\n"
                + "  }</pre> <p>And other content on several lines.</p>"),
        // content
        asList(
            qnamed("comment block, with leading star and significant star",
                S
                    + "  something\n"
                    + "  *  and even   \n"
                    + "more (5 * 4 = 20)\n"
                    + "\t\t*    !"),
            qnamed("comment block, with extra leading (thus significant) star",
                S
                    + "something\n"
                    + "  * * and even   \n"
                    + "more (5 * 4 = 20)\n"
                    + "\t\t!"),
            qnamed("single-line comment symbols",
                S
                    + "///  something\n"
                    + "  ///  \t  * and even   \n"
                    + "\t///\t more (5 * 4 = 20)\n"
                    + "\t\t///!"),
            qnamed("preformatted blocks",
                S
                    + "   *     <p>Some text followed by</p><pre>\n"
                    + "   * public class MyClass {\n"
                    + "   *   private int myMethod() {\n"
                    + "      return 0;\n"
                    + "   *   }\n"
                    + "   * }</pre>\n"
                    + "   * <p>And other content\n"
                    + " on several\n"
                    + "\t    *         lines.</p>")));
  }

  @ParameterizedTest
  @MethodSource
  void inlineTagPattern(Expected<List<String>> expected, Set<String> tagNames, String content) {
    assertParameterizedOf(
        () -> {
          var pattern = Javadocs.inlineTagPattern(tagNames);
          var matcher = pattern.matcher(content);
          var ret = new ArrayList<String>();
          while (matcher.find()) {
            ret.add(Javadocs.inlineTagName(matcher));
            ret.add(Javadocs.inlineTagValue(matcher));
          }
          return ret;
        },
        expected,
        () -> new ExpectedGeneration<List<String>>(tagNames, content)
            .setExpectedSourceCodeGenerator($ -> "asList(\n%s)".formatted(
                Stream.iterate(0, $$ -> $$ < $.size(), $$ -> $$ + 2)
                    .map($$ -> literal($.get($$)) + COMMA + literal($.get($$ + 1)))
                    .collect(joining(S + COMMA + LF)))));
  }

  static Stream<Arguments> inlineTagPattern() {
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

    return argumentsStream(
        simple(),
        // expected
        asList(
            // [1] tagNames[0]: "[]"; content[0]: "public class MyClass {\n  /**\n   * A commen. . ."
            asList(
                "myTag", "WHAT?",
                "link", "AnotherClass#anotherMethod(int)",
                "linkplain", "YetAnotherClass#yetAnotherMethod(char[]) yet another method",
                "myTag", "",
                "code", "classic Javadoc value",
                "myTag", "WHAT Markdown?",
                "link", "AnotherClass#anotherMD(int)",
                "linkplain", "YetAnotherClass#yetAnotherMD(char[]) yet another method (Markdown)",
                "code", "Markdown Javadoc value"),
            // [2] tagNames[1]: "[myTag]"; content[1]: "public class MyClass {\n  /**\n   * A commen. . ."
            asList(
                "myTag", "WHAT?",
                "myTag", "",
                "myTag", "WHAT Markdown?"),
            // [3] tagNames[2]: "[myTag, code]"; content[2]: "public class MyClass {\n  /**\n   * A commen. . ."
            asList(
                "myTag", "WHAT?",
                "myTag", "",
                "code", "classic Javadoc value",
                "myTag", "WHAT Markdown?",
                "code", "Markdown Javadoc value"),
            // [4] tagNames[3]: "[link, code]"; content[3]: "public class MyClass {\n  /**\n   * A commen. . ."
            asList(
                "link", "AnotherClass#anotherMethod(int)",
                "linkplain", "YetAnotherClass#yetAnotherMethod(char[]) yet another method",
                "code", "classic Javadoc value",
                "link", "AnotherClass#anotherMD(int)",
                "linkplain", "YetAnotherClass#yetAnotherMD(char[]) yet another method (Markdown)",
                "code", "Markdown Javadoc value")),
        // tagNames, content
        List.of(Set.of(), content),
        List.of(Set.of("myTag"), content),
        List.of(Set.of("myTag", "code"), content),
        List.of(Set.of("link", "code"), content));
  }

  @ParameterizedTest
  @MethodSource
  void normal(Expected<String> expected, String content) {
    assertParameterizedOf(
        () -> Javadocs.normal(content),
        expected,
        () -> new ExpectedGeneration<>(of(
            entry("content", content))));
  }
}
