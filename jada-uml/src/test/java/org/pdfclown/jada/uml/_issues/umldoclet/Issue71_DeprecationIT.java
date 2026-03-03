/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue71_DeprecationIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet;

import static java.nio.file.Files.exists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.common.build.test.assertion.Matchers.containsPattern;
import static org.pdfclown.common.util.Objects.fqnd;
import static org.pdfclown.common.util.Objects.sqnd;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertResult;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Issue71DeprecationTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
@Deprecated
public class Issue71_DeprecationIT extends BaseIT {
  /*
   * IMPORTANT: DO NOT add `@Deprecated` annotation here (Javadoc `@deprecated` tag testing!).
   */
  /**
   * @deprecated Testing deprecation by Javadoc tag with a comment.
   */
  public static class MoreDeprecation {
    /*
     * IMPORTANT: DO NOT add `@Deprecated` annotation here (Javadoc `@deprecated` tag testing!).
     */
    /**
     * @deprecated Testing deprecation by Javadoc tag with a comment.
     */
    public MoreDeprecation() {
      this(null);
    }

    @Deprecated
    public MoreDeprecation(String ignored) {
      // Empty method to test UML generation.
    }
  }

  @Deprecated
  public static String deprecatedStaticField;

  /**
   * @deprecated Testing deprecation of static method.
   */
  @Deprecated
  public static void deprecatedStaticMethod() {
    // Empty method to test UML generation.
  }

  @Deprecated
  public String deprecatedByAnnotation;

  /*
   * IMPORTANT: DO NOT add `@Deprecated` annotation here (Javadoc `@deprecated` tag testing!).
   */
  /**
   * @deprecated Testing deprecation by Javadoc tag with a comment.
   */
  public String deprecatedByJavadocTag;

  private String classUml;
  private String packageUml;

  Issue71_DeprecationIT() {
    super(Issue71_DeprecationIT.class);

    singleRun();
  }

  @Deprecated
  public Object getDeprecatedPropertyByAnnotation() {
    return null;
  }

  /*
   * IMPORTANT: DO NOT add `@Deprecated` annotation here (Javadoc `@deprecated` tag testing!).
   */
  /**
   * @deprecated Testing deprecation by Javadoc tag with a comment.
   */
  public Object getDeprecatedPropertyByJavadocTag() {
    return null;
  }

  @Override
  protected void onSingleRunTerm(JavadocAssertResult result) {
    assert sourceType != null;
    classUml = outputContent(getEnv().outputName(filename(sourceType, FILE_EXTENSION__PLANTUML)));
    packageUml = outputContent(getEnv().outputName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));
  }

  // SourceName: testClassDeprecatedByAnnotation
  @Test
  void _classDeprecatedByAnnotation() {
    assertThat(classUml, containsPattern(
        "class (\".*\" as )?" + fqnd(sourceType) + " <<deprecated>>"));
  }

  // SourceName: testClassDeprecatedByJavadoc
  @Test
  void _classDeprecatedByJavadoc() {
    assertThat(packageUml, containsString(
        "class " + sqnd(MoreDeprecation.class) + " <<deprecated>>"));
  }

  // SourceName: testConstructorDeprecatedByAnnotation
  @Test
  void _constructorDeprecatedByAnnotation() {
    assertThat(packageUml, containsString("+--MoreDeprecation--(String)"));
  }

  // SourceName: testConstructorDeprecatedByJavadoc
  @Test
  void _constructorDeprecatedByJavadoc() {
    assertThat(packageUml, containsString("+--MoreDeprecation--()"));
  }

  // SourceName: testFieldDeprecatedByAnnotation
  @Test
  void _fieldDeprecatedByAnnotation() {
    assertThat(classUml, containsString("+--deprecatedByAnnotation--: String"));
  }

  // SourceName: testFieldDeprecatedByJavadoc
  @Test
  void _fieldDeprecatedByJavadoc() {
    assertThat(classUml, containsString("+--deprecatedByJavadocTag--: String"));
  }

  // SourceName: testIssue73InnerClassImageName
  @Test
  void _issue73_innerClassImageName() {
    Path innerClassFile = getEnv().outputPath(
        getEnv().outputName(filename(MoreDeprecation.class, FILE_EXTENSION__SVG)));

    assertThat(innerClassFile + " exists", exists(innerClassFile), is(true));
  }

  // SourceName: testMethodDeprecatedByAnnotation
  @Test
  void _methodDeprecatedByAnnotation() {
    assertThat(classUml, not(containsString("getDeprecatedByAnnotation")));
  }

  // SourceName: testMethodDeprecatedByJavadoc
  @Test
  void _methodDeprecatedByJavadoc() {
    assertThat(classUml, not(containsString("getDeprecatedByJavadocTag")));
  }

  @Test
  void _propertyDeprecatedByAnnotation() {
    assertThat(classUml, containsString("+--deprecatedPropertyByAnnotation--: Object"));
  }

  @Test
  void _propertyDeprecatedByJavadoc() {
    assertThat(classUml, containsString("+--deprecatedPropertyByJavadocTag--: Object"));
  }
}
