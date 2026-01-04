/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue70_ImageFormatIT.java) is part of jada-uml module in Jada project
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

import static java.nio.file.Files.isRegularFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__PDF;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__PNG;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__XML;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__IMAGE_FORMAT;
import static org.pdfclown.jada.uml.__test.Utils.filename;

import java.nio.file.Path;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertArgs;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Issue70ConfigurableFormatTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue70_ImageFormatIT extends BaseIT {
  Issue70_ImageFormatIT() {
    super(Issue70_ImageFormatIT.class);
  }

  // SourceName: testImageFormatDefaults
  @Test
  void _defaultFormat() {
    runJavadoc();

    assertImageExists(FILE_EXTENSION__SVG, is(true));
    assertImageExists(FILE_EXTENSION__PNG, is(false));
    assertImageExists(FILE_EXTENSION__PDF, is(false));
    assertImageExists(FILE_EXTENSION__XML, is(false));
  }

  // SourceName: testImageFormatPng
  @Test
  void _png() {
    runJavadoc("png");

    assertImageExists(FILE_EXTENSION__SVG, is(false));
    assertImageExists(FILE_EXTENSION__PNG, is(true));
  }

  // SourceName: testImageFormatSvg
  @Test
  void _svg() {
    runJavadoc("svg");

    assertImageExists(FILE_EXTENSION__SVG, is(true));
    assertImageExists(FILE_EXTENSION__PNG, is(false));
  }

  // SourceName: testImageFormatSvgPngEps
  @Test
  void _svg_png() {
    runJavadoc("svg,png");

    assertImageExists(FILE_EXTENSION__SVG, is(true));
    assertImageExists(FILE_EXTENSION__PNG, is(true));
  }

  // SourceName: testImageFormatUnrecognized
  @Test
  void _unrecognizedFormat() {
    runJavadoc("pdf", javadocArgs().exitcode(4));
  }

  private void assertImageExists(String imageExtension, Matcher<Boolean> matcher) {
    assert sourceType != null;
    Path imageFile = getEnv().outputPath(getEnv().basedName(filename(sourceType, imageExtension)));

    assertThat(imageFile + " exists and is a file", isRegularFile(imageFile), matcher);
  }

  private void runJavadoc(String imageFormatNames) {
    runJavadoc(imageFormatNames, javadocArgs());
  }

  private void runJavadoc(String imageFormatNames, JavadocAssertArgs args) {
    runJavadoc(args.arg(OPTION__IMAGE_FORMAT, imageFormatNames));
  }
}
