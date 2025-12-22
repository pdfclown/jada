/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Access.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet.issue148;

// SourceName: nl.talsmasoftware.umldoclet.features.Access
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
@SuppressWarnings({ "FieldMayBeFinal", "unused" })
public class Access {
  protected static class ProtectedClass {
    private String privateField;
    String packageProtectedField;
    protected String protectedField;
    public String publicField;

    protected ProtectedClass() {
      this.privateField = packageProtectedField = protectedField = publicField = null;
    }

    public String getPublicValue() {
      return publicField;
    }

    protected String getProtectedValue() {
      return protectedField;
    }

    String getPackageProtectedValue() {
      return packageProtectedField;
    }

    private String getPrivateValue() {
      return privateField;
    }
  }

  private static class PrivateClass {
    private String privateField;
    String packageProtectedField;
    protected String protectedField;
    public String publicField;

    private PrivateClass() {
      this.privateField = packageProtectedField = protectedField = publicField = null;
    }

    public String getPublicValue() {
      return publicField;
    }

    protected String getProtectedValue() {
      return protectedField;
    }

    String getPackageProtectedValue() {
      return packageProtectedField;
    }

    private String getPrivateValue() {
      return privateField;
    }
  }
}
