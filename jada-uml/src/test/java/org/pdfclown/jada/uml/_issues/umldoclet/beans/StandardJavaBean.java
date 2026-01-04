/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (StandardJavaBean.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet.beans;

// SourceName: nl.talsmasoftware.umldoclet.features.beans.StandardJavaBean
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
@SuppressWarnings("NotNullFieldNotInitialized")
public class StandardJavaBean {
  private String stringValue;
  private int intValue;
  private boolean booleanValue;
  private StandardJavaBean child;

  public StandardJavaBean getChild() {
    return child;
  }

  public int getIntValue() {
    return intValue;
  }

  public String getStringValue() {
    return stringValue;
  }

  public boolean isBooleanValue() {
    return booleanValue;
  }

  public void setBooleanValue(boolean booleanValue) {
    this.booleanValue = booleanValue;
  }

  public void setChild(StandardJavaBean child) {
    this.child = child;
  }

  public void setIntValue(int intValue) {
    this.intValue = intValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }
}
