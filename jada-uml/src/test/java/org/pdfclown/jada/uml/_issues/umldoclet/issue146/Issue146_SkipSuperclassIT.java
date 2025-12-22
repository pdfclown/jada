/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue146_SkipSuperclassIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
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
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertResult;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.bug146.Bug146SkipSuperclassTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue146_SkipSuperclassIT extends BaseIT {
  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull String classPuml;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull String packagePuml;

  Issue146_SkipSuperclassIT() {
    super(PublicTestClass.class);

    singleRun();
  }

  @Override
  protected void onSingleRunTerm(JavadocAssertResult result) {
    assert sourceType != null;
    classPuml = outputContent(getEnv().basedName(filename(sourceType, FILE_EXTENSION__PLANTUML)));
    packagePuml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));
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
