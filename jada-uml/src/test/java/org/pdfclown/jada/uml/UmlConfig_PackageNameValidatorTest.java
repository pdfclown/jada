/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlConfig_PackageNameValidatorTest.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.pdfclown.common.util.Strings.EMPTY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pdfclown.jada.uml.UmlConfig.ExternalLink.PackageNameValidator;
import org.pdfclown.jada.uml.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.PackagenameValidatorTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class UmlConfig_PackageNameValidatorTest extends BaseTest {
  PackageNameValidator subject;

  // SourceName: must_not_start_or_end_with_dot
  @ParameterizedTest
  @ValueSource(strings = { ".", ".prefixed", "suffixed." })
  void _mustNotStartOrEndWithDot(String candidatePackageName) {
    assertThat(subject.test(candidatePackageName), is(false));
  }

  // SourceName: package_may_not_contain_spaces_or_dashes
  @ParameterizedTest
  @ValueSource(strings = { "my package", "my-package" })
  void _packageMayNotContainSpacesOrDashes(String candidatePackageName) {
    assertThat(subject.test(candidatePackageName), is(false));
  }

  // SourceName: unnamed_package_must_be_valid
  @Test
  void _unnamedPackageMustBeValid() {
    assertThat(subject.test(EMPTY), is(true));
  }

  // SourceName: valid_package_names_must_pass_validation
  @ParameterizedTest
  @ValueSource(strings = { "org.pdfclown.jada.uml", "org.pdfclown", "no_dot" })
  void _validPackages(String candidatePackageName) {
    assertThat(subject.test(candidatePackageName), is(true));
  }

  @BeforeEach
  void onEachBefore() {
    subject = new PackageNameValidator();
  }
}
