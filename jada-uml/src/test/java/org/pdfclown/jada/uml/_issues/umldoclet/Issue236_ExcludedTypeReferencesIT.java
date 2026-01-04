/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue236_ExcludedTypeReferencesIT.java) is part of jada-uml module in Jada project
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.pdfclown.common.util.Objects.fqnd;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__EXCLUDED_TYPE_REFERENCES;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__EXTENDED_BY;
import static org.pdfclown.jada.uml.util.Plantumls.puml;

import org.junit.jupiter.api.Test;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertArgs;
import org.pdfclown.jada.uml.__test.BaseIT;
import org.pdfclown.jada.uml._issues.umldoclet.beans.StandardJavaBean;

// SourceName: nl.talsmasoftware.umldoclet.features.Feature236ExcludedTypeReferencesTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue236_ExcludedTypeReferencesIT extends BaseIT {
  Issue236_ExcludedTypeReferencesIT() {
    super(StandardJavaBean.class.getPackageName());

    singleRun();
  }

  @Override
  protected void onSingleRunInit(JavadocAssertArgs args) {
    args.arg(OPTION__EXCLUDED_TYPE_REFERENCES, "none");
  }

  // SourceName: testImplicitSuperclassObjectIsNotExcluded
  @Test
  void _implicitSuperclassObjectIsNotExcluded() {
    String puml = outputContent(getEnv().basedName(filename(StandardJavaBean.class,
        FILE_EXTENSION__PLANTUML)));

    assertThat(puml, containsString(puml()
        .join(fqnd(Object.class))
        .join(PUML_REF__EXTENDED_BY)
        .join(fqnd(StandardJavaBean.class)).toString()));
  }
}
