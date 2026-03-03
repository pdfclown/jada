/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue124_PropertiesAsFieldsIT.java) is part of jada-uml module in Jada project
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
import static org.hamcrest.Matchers.not;
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__ASSOCIATES;
import static org.pdfclown.jada.uml.util.Plantumls.puml;
import static org.pdfclown.jada.uml.util.Plantumls.pumlNsFqn;

import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseIT;
import org.pdfclown.jada.uml._issues.umldoclet.beans.StandardJavaBean;

// SourceName: nl.talsmasoftware.umldoclet.features.Issue124PropertiesAsFieldsTest
/**
 * Test that properties can be rendered as fields with the option
 * {@code -umlPropertiesAsFields true}.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue124_PropertiesAsFieldsIT extends BaseIT {
  Issue124_PropertiesAsFieldsIT() {
    super(StandardJavaBean.class.getPackageName());

    singleRun();
  }

  // SourceName: testPropertiesAsFieldsForPublicClass
  @Test
  void _propertiesAsFieldsForClassDiagram() {
    String puml = outputContent(getEnv().outputName(filename(StandardJavaBean.class,
        FILE_EXTENSION__PLANTUML)));

    assertThat(puml, containsString("+stringValue: String"));
    assertThat(puml, containsString("+intValue: int"));
    assertThat(puml, containsString("+booleanValue: boolean"));
    assertThat(puml, containsString("+child: StandardJavaBean"));

    assertThat(puml, not(containsString("getStringValue(")));
    assertThat(puml, not(containsString("setStringValue(")));
    assertThat(puml, not(containsString("getIntValue(")));
    assertThat(puml, not(containsString("setIntValue(")));
    assertThat(puml, not(containsString("isBooleanValue(")));
    assertThat(puml, not(containsString("setBooleanValue(")));
    assertThat(puml, not(containsString("getChild(")));
    assertThat(puml, not(containsString("setChild(")));
  }

  // SourceName: testPropertiesAsFieldsForPackageDiagram
  @Test
  void _propertiesAsFieldsForPackageDiagram() {
    String puml = outputContent(getEnv().outputName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));
    String nsFqn = pumlNsFqn(StandardJavaBean.class);

    assertThat(puml, containsString("+stringValue: String"));
    assertThat(puml, containsString("+intValue: int"));
    assertThat(puml, containsString("+booleanValue: boolean"));
    assertThat(puml, containsString(puml()
        .join(nsFqn).join(PUML_REF__ASSOCIATES).join(nsFqn).concat(": child").toString()));

    assertThat(puml, not(containsString("getStringValue(")));
    assertThat(puml, not(containsString("setStringValue(")));
    assertThat(puml, not(containsString("getIntValue(")));
    assertThat(puml, not(containsString("setIntValue(")));
    assertThat(puml, not(containsString("isBooleanValue(")));
    assertThat(puml, not(containsString("setBooleanValue(")));
    assertThat(puml, not(containsString("getChild(")));
    assertThat(puml, not(containsString("setChild(")));
  }
}
