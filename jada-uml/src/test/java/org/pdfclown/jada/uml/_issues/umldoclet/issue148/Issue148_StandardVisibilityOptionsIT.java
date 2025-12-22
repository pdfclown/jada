/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue148_StandardVisibilityOptionsIT.java) is part of jada-uml module in Jada project
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

import static java.nio.file.Files.exists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__PROPERTIES_FLATTENED;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.features.Issue148StandardIncludeOptionsTest
/**
 * Test the include options by the Standard doclet.
 * <p>
 * Verify that the visibility is interpreted the same by the UML doclet.
 * </p>
 * <p>
 * The options to be supported:
 * </p>
 * <ul>
 * <li>{@code -package}</li>
 * <li>{@code -private}</li>
 * <li>{@code -protected}</li>
 * <li>{@code -public}</li>
 * <li>{@code --show-types [private|protected|package|all]}</li>
 * <li>{@code --show-members [private|protected|package|all]}</li>
 * </ul>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue148_StandardVisibilityOptionsIT extends BaseIT {
  Issue148_StandardVisibilityOptionsIT() {
    super(Issue148_StandardVisibilityOptionsIT.class.getPackageName());
  }

  // SourceName: testOptionPackage
  @Test
  void _option__package() {
    runJavadoc("-package");
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat(puml, allOf(
          not(containsString("[[Access.PrivateClass.html]]")),
          containsString("[[PackageProtectedClass.html]]"),
          containsString("[[Access.ProtectedClass.html]]"),
          containsString("[[PublicClass.html]]")));
      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, containsString("~packageProtectedField"));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, containsString("~getPackageProtectedValue()"));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
    {
      String puml = outputContent(getEnv().basedName("PackageProtectedClass"
          + FILE_EXTENSION__PLANTUML));

      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, containsString("~packageProtectedField"));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, containsString("~getPackageProtectedValue()"));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
  }

  // SourceName: testOptionPrivate
  @Test
  void _option__private() {
    runJavadoc("-private");
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat(puml, allOf(
          containsString("[[Access.PrivateClass.html]]"),
          containsString("[[PackageProtectedClass.html]]"),
          containsString("[[Access.ProtectedClass.html]]"),
          containsString("[[PublicClass.html]]")));
      assertThat(puml, containsString("-privateField"));
      assertThat(puml, containsString("~packageProtectedField"));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, containsString("-getPrivateValue()"));
      assertThat(puml, containsString("~getPackageProtectedValue()"));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
    {
      String puml = outputContent(getEnv().basedName("Access.PrivateClass"
          + FILE_EXTENSION__PLANTUML));

      assertThat(puml, containsString("-privateField"));
      assertThat(puml, containsString("~packageProtectedField"));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, containsString("-getPrivateValue()"));
      assertThat(puml, containsString("~getPackageProtectedValue()"));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
  }

  // SourceName: testOptionProtected
  @Test
  void _option__protected() {
    runJavadoc("-protected");
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat(puml, allOf(
          not(containsString("[[Access.PrivateClass.html]]")),
          not(containsString("[[PackageProtectedClass.html]]")),
          containsString("[[Access.ProtectedClass.html]]"),
          containsString("[[PublicClass.html]]")));
      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
    {
      String puml = outputContent(getEnv().basedName("Access.ProtectedClass"
          + FILE_EXTENSION__PLANTUML));

      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
  }

  // SourceName: testOptionPublic
  @Test
  void _option__public() {
    runJavadoc("-public");
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat(puml, allOf(
          not(containsString("[[Access.PrivateClass.html]]")),
          not(containsString("[[PackageProtectedClass.html]]")),
          not(containsString("[[Access.ProtectedClass.html]]")),
          containsString("[[PublicClass.html]]")));
      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, not(containsString("#protectedField")));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, not(containsString("#getProtectedValue()")));
      assertThat(puml, containsString("+getPublicValue()"));
    }
    {
      String puml = outputContent(getEnv().basedName("PublicClass" + FILE_EXTENSION__PLANTUML));

      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, not(containsString("#protectedField")));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, not(containsString("#getProtectedValue()")));
      assertThat(puml, containsString("+getPublicValue()"));
    }
  }

  // SourceName: testOptionShowMembersPackage
  @Test
  void _option__showMembers_package() {
    runJavadoc("--show-members", "package");
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat(puml, allOf(
          not(containsString("[[Access.PrivateClass.html]]")),
          not(containsString("[[PackageProtectedClass.html]]")),
          containsString("[[Access.ProtectedClass.html]]"),
          containsString("[[PublicClass.html]]")));
      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, containsString("~packageProtectedField"));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, containsString("~getPackageProtectedValue()"));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
    {
      String puml = outputContent(getEnv().basedName("PublicClass" + FILE_EXTENSION__PLANTUML));

      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, containsString("~packageProtectedField"));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, containsString("~getPackageProtectedValue()"));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
  }

  // SourceName: testOptionShowMembersPrivate
  @Test
  void _option__showMembers_private() {
    runJavadoc("--show-members", "private");
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat(puml, allOf(
          not(containsString("[[Access.PrivateClass.html]]")),
          not(containsString("[[PackageProtectedClass.html]]")),
          containsString("[[Access.ProtectedClass.html]]"),
          containsString("[[PublicClass.html]]")));
      assertThat(puml, containsString("-privateField"));
      assertThat(puml, containsString("~packageProtectedField"));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, containsString("-getPrivateValue()"));
      assertThat(puml, containsString("~getPackageProtectedValue()"));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
    {
      String puml = outputContent(getEnv().basedName("PublicClass" + FILE_EXTENSION__PLANTUML));

      assertThat(puml, containsString("-privateField"));
      assertThat(puml, containsString("~packageProtectedField"));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, containsString("-getPrivateValue()"));
      assertThat(puml, containsString("~getPackageProtectedValue()"));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
  }

  // SourceName: testOptionShowMembersProtected
  @Test
  void _option__showMembers_protected() {
    runJavadoc("--show-members", "protected");
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat(puml, allOf(
          not(containsString("[[Access.PrivateClass.html]]")),
          not(containsString("[[PackageProtectedClass.html]]")),
          containsString("[[Access.ProtectedClass.html]]"),
          containsString("[[PublicClass.html]]")));
      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
    {
      String puml = outputContent(getEnv().basedName("Access.ProtectedClass"
          + FILE_EXTENSION__PLANTUML));

      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
  }

  // SourceName: testOptionShowMembersPublic
  @Test
  void _option__showMembers_public() {
    runJavadoc("--show-members", "public");
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat(puml, allOf(
          not(containsString("[[Access.PrivateClass.html]]")),
          not(containsString("[[PackageProtectedClass.html]]")),
          containsString("[[Access.ProtectedClass.html]]"),
          containsString("[[PublicClass.html]]")));
      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, not(containsString("#protectedField")));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, not(containsString("#getProtectedValue()")));
      assertThat(puml, containsString("+getPublicValue()"));
    }
    {
      String puml = outputContent(getEnv().basedName("PublicClass" + FILE_EXTENSION__PLANTUML));

      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, not(containsString("#protectedField")));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, not(containsString("#getProtectedValue()")));
      assertThat(puml, containsString("+getPublicValue()"));
    }
  }

  // SourceName: testOptionShowTypesPackage
  @Test
  void _option__showTypes_package() {
    runJavadoc("--show-types", "package");
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat(puml, allOf(
          not(containsString("[[Access.PrivateClass.html]]")),
          containsString("[[PackageProtectedClass.html]]"),
          containsString("[[Access.ProtectedClass.html]]"),
          containsString("[[PublicClass.html]]")));
      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
    {
      String puml = outputContent(getEnv().basedName("PackageProtectedClass"
          + FILE_EXTENSION__PLANTUML));

      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
  }

  // SourceName: testOptionShowTypesPrivate
  @Test
  void _option__showTypes_private() {
    runJavadoc("--show-types", "private");
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat(puml, allOf(
          containsString("[[Access.PrivateClass.html]]"),
          containsString("[[PackageProtectedClass.html]]"),
          containsString("[[Access.ProtectedClass.html]]"),
          containsString("[[PublicClass.html]]")));
      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
    {
      String puml = outputContent(getEnv().basedName("Access.PrivateClass"
          + FILE_EXTENSION__PLANTUML));

      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
  }

  // SourceName: testOptionShowTypesProtected
  @Test
  void _option__showTypes_protected() {
    runJavadoc("--show-types", "protected");
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat(puml, allOf(
          not(containsString("[[Access.PrivateClass.html]]")),
          not(containsString("[[PackageProtectedClass.html]]")),
          containsString("[[Access.ProtectedClass.html]]"),
          containsString("[[PublicClass.html]]")));
      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
    {
      String puml = outputContent(getEnv().basedName("Access.ProtectedClass"
          + FILE_EXTENSION__PLANTUML));

      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
  }

  // SourceName: testOptionShowTypesPublic
  @Test
  void _option__showTypes_public() {
    runJavadoc("--show-types", "public");
    {
      String puml = outputContent(getEnv().basedName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

      assertThat(puml, allOf(
          not(containsString("[[Access.PrivateClass.html]]")),
          not(containsString("[[PackageProtectedClass.html]]")),
          not(containsString("[[Access.ProtectedClass.html]]")),
          containsString("[[PublicClass.html]]")));
      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
    {
      String puml = outputContent(getEnv().basedName("PublicClass" + FILE_EXTENSION__PLANTUML));

      assertThat(puml, not(containsString("-privateField")));
      assertThat(puml, not(containsString("~packageProtectedField")));
      assertThat(puml, containsString("#protectedField"));
      assertThat(puml, containsString("+publicField"));

      assertThat(puml, not(containsString("-getPrivateValue()")));
      assertThat(puml, not(containsString("~getPackageProtectedValue()")));
      assertThat(puml, containsString("#getProtectedValue()"));
      assertThat(puml, containsString("+getPublicValue()"));
    }
  }

  private void runJavadoc(String... options) {
    runJavadoc(javadocArgs()
        .arg(OPTION__PROPERTIES_FLATTENED)
        .args(options));

    Stream.of("Access.PrivateClass",
        "PackageProtectedClass",
        "Access.ProtectedClass",
        "PublicClass")
        .forEach($ -> assertThat("File " + $ + FILE_EXTENSION__PLANTUML + " exists",
            exists(getEnv().outputPath($ + FILE_EXTENSION__PLANTUML)),
            is(exists(getEnv().outputPath($ + FILE_EXTENSION__HTML)))));
  }
}
