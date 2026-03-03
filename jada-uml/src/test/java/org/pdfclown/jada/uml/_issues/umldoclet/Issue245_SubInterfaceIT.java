/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue245_SubInterfaceIT.java) is part of jada-uml module in Jada project
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
import static org.pdfclown.jada.uml.__test.Utils.filename;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__EXTENDED_BY;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__IMPLEMENTED_BY;
import static org.pdfclown.jada.uml.util.Plantumls.puml;

import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Bug245SubInterfaceTest
/**
 * This is a test for <a href="https://github.com/talsma-ict/umldoclet/issues/245">bug 245</a> where
 * a sub-interface relationship is incorrectly rendered with a dotted line, which should be a solid
 * line instead.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue245_SubInterfaceIT extends BaseIT {
  public static final class Implementation implements ParentInterface {
  }

  public interface ParentInterface {
  }

  public interface SubInterface extends ParentInterface {
  }

  Issue245_SubInterfaceIT() {
    super(Issue245_SubInterfaceIT.class);

    singleRun();
  }

  // SourceName: testExtensionOfInterfaseWithSolidLine
  /**
   * Test that the bug is fixed; interface extension with solid line, not dotted.
   */
  @Test
  void _extendsWithSolidLine() {
    String puml = outputContent(getEnv().outputName(filename(SubInterface.class,
        FILE_EXTENSION__PLANTUML)));

    assertThat("SubInterface extends ParentInterface", puml,
        containsString(puml()
            .join(fqnd(ParentInterface.class))
            .join(PUML_REF__EXTENDED_BY)
            .join(fqnd(SubInterface.class)).toString()));
  }

  // SourceName: testImplementationOfInterfaceWithDottedLine
  /**
   * Tests that there is no new regression bug from this fix.
   */
  @Test
  void _implementsWithDottedLine() {
    String puml = outputContent(getEnv().outputName(filename(Implementation.class,
        FILE_EXTENSION__PLANTUML)));

    assertThat("Implementation implements ParentInterface", puml,
        containsString(puml()
            .join(fqnd(ParentInterface.class))
            .join(PUML_REF__IMPLEMENTED_BY)
            .join(fqnd(Implementation.class)).toString()));
  }
}
