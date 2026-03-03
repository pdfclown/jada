/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue30_PropertiesIT.java) is part of jada-uml module in Jada project
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
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.common.util.Objects.sqnd;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__ASSOCIATES;
import static org.pdfclown.jada.uml.util.Plantumls.puml;
import static org.pdfclown.jada.uml.util.Plantumls.pumlNsFqn;

import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Issue30JavaBeanPropertiesTest
/**
 * Test correct substitution of JavaBean properties by UML references.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue30_PropertiesIT extends BaseIT {
  Issue30_PropertiesIT() {
    super(Issue30_PropertiesIT.class);

    singleRun();
  }

  // Method that SHOULD be seen as a bean property.
  public Issue30_PropertiesIT getSomeProperty() {
    return this;
  }

  // Method that should not be seen as a bean property.
  public Issue30_PropertiesIT getSomeValue(Boolean withArgument) {
    return this;
  }

  public void setSomeProperty(Issue30_PropertiesIT someProperty) {
    // Empty body, just to simulate a setter method
  }

  // SourceName: testIssue30
  @Test
  void _main() {
    String puml = outputContent(getEnv().outputName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));
    String simpleName = sqnd(sourceType);
    String nsFqn = pumlNsFqn(sourceType);

    // someProperty should have been replaced by reference:
    assertThat(puml, not(containsString("+getSomeProperty()")));
    assertThat(puml, not(containsString("+setSomeProperty")));
    assertThat(puml, either(containsString(puml()
        .join(simpleName).join(PUML_REF__ASSOCIATES).join(simpleName)
        .concat(": someProperty").toString()))
            .or(containsString(puml()
                .join(nsFqn).join(PUML_REF__ASSOCIATES).join(nsFqn)
                .concat(": someProperty").toString())));

    // someValue must not be replaced by reference:
    assertThat(puml, containsString("+getSomeValue(Boolean): " + simpleName));
    assertThat(puml, not(either(containsString(puml()
        .join(simpleName).join(PUML_REF__ASSOCIATES).join(simpleName)
        .concat(": someValue").toString()))
            .or(containsString(puml()
                .join(nsFqn).join(PUML_REF__ASSOCIATES).join(nsFqn)
                .concat(": someValue").toString()))));
  }
}
