/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue25_ImageDirIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.pdfclown.common.util.Objects.fqnd;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__IMAGE_DIR;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertArgs;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertResult;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Issue25ImagedirTest
/**
 * Tests <a href="https://github.com/talsma-ict/umldoclet/issues/25">enhancement 25</a>: Send images
 * to a single directory.
 * <p>
 * The maven job is configured so that it creates a directory called <code>test-content</code> in
 * the target where images should be located in a single <code>images</code> directory.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue25_ImageDirIT extends BaseIT {
  private static final String IMAGE_DIR_NAME = "images";

  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull Path imageDir;

  Issue25_ImageDirIT() {
    super(Issue25_ImageDirIT.class);

    singleRun();
  }

  @Override
  protected void onSingleRunInit(JavadocAssertArgs args) {
    args.arg(OPTION__IMAGE_DIR, IMAGE_DIR_NAME);
  }

  @Override
  protected void onSingleRunTerm(JavadocAssertResult result) {
    imageDir = result.outputDir.resolve(IMAGE_DIR_NAME);
  }

  // SourceName: testEnhancement25ImagePresence
  @Test
  void _enhancement25ImagePresence() {
    var imageFile = imageDir.resolve(fqnd(sourceType) + FILE_EXTENSION__SVG);

    assertThat("Image " + imageFile + " exists", exists(imageFile), is(true));
    assertThat("Image is directory", isDirectory(imageFile), is(false));
    assertThat("Image is file", isRegularFile(imageFile), is(true));
  }

  // SourceName: testImagesDirectoryPresence
  @Test
  void _imagesDirectoryPresence() {
    assertThat("Images dir exists", exists(imageDir), is(true));
    assertThat("Images dir is directory", isDirectory(imageDir), is(true));
  }
}
