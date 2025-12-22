/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue164_PackageDependenciesIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_DEPS;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_SUMMARY;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__ASSOCIATES;
import static org.pdfclown.jada.uml.util.Plantumls.puml;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.common.build.util.io.ResourceNames;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertArgs;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertArgs.ArgGroups;
import org.pdfclown.jada.uml.UmlExtension;
import org.pdfclown.jada.uml.__test.BaseIT;
import org.pdfclown.jada.uml.internal.util.io.DelegateWriter;
import org.pdfclown.jada.uml.internal.util.io.Files;
import org.pdfclown.jada.uml.proc.PageProcessor;
import org.pdfclown.jada.uml.render.UmlFactory;
import org.pdfclown.jada.uml.render.model.UmlNode;

// SourceName: nl.talsmasoftware.umldoclet.features.Issue164PackageDependenciesTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue164_PackageDependenciesIT extends BaseIT {
  Issue164_PackageDependenciesIT() {
    super(ProjectDirId.MAIN_TYPE_SOURCE, List.of(
        UmlExtension.class.getPackageName(),
        PageProcessor.class.getPackageName(),
        UmlFactory.class.getPackageName(),
        DelegateWriter.class.getPackageName(),
        UmlNode.class.getPackageName(),
        Files.class.getPackageName()));

    singleRun();
  }

  @Override
  protected void onSingleRunInit(JavadocAssertArgs args) {
    args.args(ArgGroups.get(ArgGroups.KEY__JSR335_TAGS));
  }

  // SourceName: testPackageDependencies
  @Test
  void _main() {
    String puml = outputContent(FILENAME__PACKAGE_DEPS + FILE_EXTENSION__PLANTUML);
    var umlExtensionPackage = UmlExtension.class.getPackageName();

    assertThat("Jada doclet extension superclass dependency", puml,
        containsString(puml()
            .join(umlExtensionPackage)
            .join(PUML_REF__ASSOCIATES)
            .join(Jada.class.getPackageName()).toString()));
    assertThat("package-summary links", puml,
        containsString("\"%s\" [[%s]]".formatted(
            umlExtensionPackage, ResourceNames.based(
                FILENAME__PACKAGE_SUMMARY + FILE_EXTENSION__HTML,
                UmlExtension.class))));
  }
}
