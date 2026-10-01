/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue146_SkipSuperclassIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  Changes: Adaptation and redesign for Jada.
 */
/*
 * Copyright 2016-2026 Talsma ICT
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pdfclown.jada.uml._issues.umldoclet.issue146;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.common.util.Objects.fqnd;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__EXTENDED_BY;
import static org.pdfclown.jada.uml.util.Plantumls.puml;
import static org.pdfclown.jada.uml.util.Plantumls.pumlNsFqn;
import static org.pdfclown.jada.uml.util.Plantumls.pumlNsSqn;

import java.util.AbstractList;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.annot.Initializer;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertResult;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.bug146.Bug146SkipSuperclassTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue146_SkipSuperclassIT extends BaseIT {
  @SuppressWarnings("NotNullFieldNotInitialized")
  private String classPuml;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private String packagePuml;

  Issue146_SkipSuperclassIT() {
    super(PublicTestClass.class);

    singleRun();
  }

  @Initializer
  @Override
  protected void onSingleRunTerm(JavadocAssertResult result) {
    assert sourceType != null;
    classPuml = outputContent(getEnv().outputName(filename(sourceType, FILE_EXTENSION__PLANTUML)));
    packagePuml = outputContent(getEnv().outputName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));
  }

  // SourceName: testPackageProtectedSuperclassShouldBeSkipped
  @Test
  void _main() {
    assertThat(packagePuml, allOf(
        containsString(puml()
            .join(pumlNsFqn(AbstractList.class))
            .join(PUML_REF__EXTENDED_BY)
            .join(pumlNsFqn(sourceType)).toString()),
        not(containsString(pumlNsSqn(PackageProtectedSuperclass.class)))));
    assertThat(classPuml, allOf(
        containsString(puml()
            .join(fqnd(AbstractList.class))
            .join(PUML_REF__EXTENDED_BY)
            .join(fqnd(sourceType)).toString()),
        not(containsString(puml()
            .join(fqnd(PackageProtectedSuperclass.class))
            .join(PUML_REF__EXTENDED_BY)
            .join(fqnd(PublicTestClass.class)).toString()))));
  }
}
