/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageDependencyCycleIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  Changes: Adaptation and redesign for Jada.
 */
/*
 * Copyright 2016-2026 Talsma ICT
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pdfclown.jada.uml.render;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.pdfclown.jada.core.test.assertion.Assertions.assertJavadoc;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__CYCLIC_PACKAGE_DEPENDENCIES_CHECKED;

import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertArgs.ArgGroups;
import org.pdfclown.jada.uml.__test.BaseIT;
import org.pdfclown.jada.uml.render._2.CyclicDependencyClass;

// SourceName: nl.talsmasoftware.umldoclet.features.Feature182CyclicDependencyTest
/*
 * See {@biblio.ref UML-DOCLET-BUGS 182}.
 */
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class PackageDependencyCycleIT extends BaseIT {
  /**
   * @implNote Method intentionally causing a cyclic package dependency.
   */
  public void cycle(CyclicDependencyClass dummy) {
  }

  // SourceName: testCyclicDependencyWarning
  @Test
  void _cyclicDependencyWarning() {
    String myPackage = getClass().getPackageName();
    String cyclicPackage = CyclicDependencyClass.class.getPackageName();

    var result = assertJavadoc(javadocArgs()
        .outputStreamErr()
        .arg(OPTION__CYCLIC_PACKAGE_DEPENDENCIES_CHECKED)
        .arg("--show-members", "public")
        .args(ArgGroups.get(ArgGroups.KEY__JSR335_TAGS))
        .packageDir(getEnv().dir(ProjectDirId.TEST_TYPE_SOURCE))
        .packageName(myPackage)
        .packageName(cyclicPackage));

    //noinspection DataFlowIssue
    assertThat(result.err, containsString(myPackage + " > " + cyclicPackage + " > " + myPackage));
  }
}
