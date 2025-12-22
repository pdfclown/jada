/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlPostProcessorsTest.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.util.Strings.EMPTY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseTest;
import org.pdfclown.jada.uml.render.model.Field;
import org.pdfclown.jada.uml.render.model.Method;
import org.pdfclown.jada.uml.render.model.Namespace;
import org.pdfclown.jada.uml.render.model.Type;
import org.pdfclown.jada.uml.render.model.Type.Classification;
import org.pdfclown.jada.uml.render.model.TypeName;

// SourceName: nl.talsmasoftware.umldoclet.uml.util.UmlPostProcessorsTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class UmlPostProcessorsTest extends BaseTest {
  private static final Namespace UNNAMED = new Namespace(null, EMPTY, null);

  private static TypeName typeName(String qualified) {
    int lastDot = qualified.lastIndexOf('.');
    String simpleName = lastDot >= 0 ? qualified.substring(lastDot + 1) : qualified;
    String packagename = lastDot > 0 ? qualified.substring(0, lastDot) : null;
    return new TypeName(packagename, simpleName, qualified);
  }

  private UmlPostProcessors postProcessors;

  @BeforeEach
  void onEachBefore() {
    postProcessors = new UmlPostProcessors();
  }

  // SourceName: testJavaBeanPropertiesAsFieldsPostProcessorAcceptsEmptyType
  @Test
  void propertiesToFields__acceptsEmptyType() {
    var emptyType = new Type(UNNAMED, Classification.CLASS, typeName("EmptyType"));
    postProcessors.propertiesToFields().accept(emptyType);

    assertThat(emptyType.getPackageName(), equalTo(""));
    assertThat(emptyType.getClassfication(), is(Classification.CLASS));
    assertThat(emptyType.getName(), equalTo(typeName("EmptyType")));
    assertThat(emptyType.getChildren(), is(empty()));
  }

  // SourceName: testJavaBeanPropertiesAsFieldsPostProcessorAcceptsNull
  @Test
  void propertiesToFields__acceptsNull() {
    try {
      postProcessors.propertiesToFields().accept(null);
    } catch (NullPointerException ex) {
      fail("postprocessor should just accept null.");
    }
  }

  // SourceName: testJavaBeanPropertiesAsFielsPostProcessorSimpleAccessors
  @Test
  void propertiesToFiels__simpleAccessors() {
    var simpleBean = new Type(UNNAMED, Classification.CLASS, typeName("SimpleBean"));
    var businessMethod = new Method(simpleBean, "someBusinessMethod", null);
    var getter = new Method(simpleBean, "getStringValue", typeName("java.lang.String"));
    var setter = new Method(simpleBean, "setStringValue", null);
    setter.addParameter("value", typeName("java.lang.String"));
    simpleBean.addChild(getter);
    simpleBean.addChild(setter);
    simpleBean.addChild(businessMethod);

    assertThat(simpleBean.getChildren(Method.class), hasSize(3));
    assertThat(simpleBean.getChildren(Field.class), is(empty()));

    postProcessors.propertiesToFields().accept(simpleBean);

    assertThat(simpleBean.getChildren(Method.class), hasSize(1));
    assertThat(simpleBean.getChildren(Method.class).get(0).getName(),
        equalTo("someBusinessMethod"));
    assertThat(simpleBean.getChildren(Field.class), hasSize(1));
    assertThat(simpleBean.getChildren(Field.class).get(0).getName(), equalTo("stringValue"));
  }
}
