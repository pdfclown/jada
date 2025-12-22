/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DependencyDiagramTest.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render.model;

import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_DEPS;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.UmlConfig;
import org.pdfclown.jada.uml.UmlConfig.ImageConfig;
import org.pdfclown.jada.uml.__test.BaseTest;
import org.pdfclown.jada.uml.render.PackageDependency;
import org.pdfclown.jada.uml.util.Plantumls;

// SourceName: nl.talsmasoftware.umldoclet.uml.DependencyDiagramTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class DependencyDiagramTest extends BaseTest {
  private DependencyDiagram diagram;
  private List<String> excluded;
  private UmlConfig mockConfig;
  private ImageConfig mockImages;

  // SourceName: testDefaultExcludedPackageDependencies
  /**
   * @implNote Contrary to the original implementation, JadaUML matches package dependencies against
   *           wildcard patterns; as a consequence, the following cases against the
   *           {@linkplain UmlConfig#EXCLUDED_PACKAGE_DEPENDENCIES__DEFAULT default excluded package
   *           dependencies} don't make sense anymore:<pre class="lang-java"><code>
   *diagram.addPackageDependency("foo.bar", "java");
   *diagram.addPackageDependency("foo.bar", "javax");</code></pre>
   */
  @Test
  void _defaultExcludedPackageDependencies() {
    diagram.setDependencies(Set.of(
        new PackageDependency("foo.bar", "java.lang"),
        new PackageDependency("foo.bar", "javax.activation"),
        new PackageDependency("foo.bar", "foo.bar.baz")));

    assertThat(diagram.getChildren(), hasSize(1));
    assertThat(diagram.getChildren(),
        contains(hasToString(containsString("foo.bar --> foo.bar.baz"))));
  }

  // SourceName: testExcludedPackageDependenciesFalsePositives
  @Test
  void _excludedPackageDependenciesFalsePositives() {
    diagram.setDependencies(Set.of(
        new PackageDependency("foo.bar", "javas"),
        new PackageDependency("foo.bar", "javas.lang"),
        new PackageDependency("foo.bar", "javaxi"),
        new PackageDependency("foo.bar", "javaxi.activation")));

    assertThat(diagram.getChildren(), hasSize(4));
  }

  // SourceName: testExcludedPackageDependenciesUnnamed
  @Test
  void _excludedPackageDependenciesUnnamed() {
    excluded.add(Plantumls.PUML_NS__EMPTY);
    diagram.setDependencies(Set.of(
        new PackageDependency("foo.bar", ""),
        new PackageDependency("foo.bar", "java.lang"),
        new PackageDependency("foo.bar", "foo.bar.baz")));

    assertThat(diagram.getChildren(), hasSize(1));
    assertThat(diagram.getChildren(),
        contains(hasToString(containsString("foo.bar --> foo.bar.baz"))));
  }

  // SourceName: testUnnamedPackageIsIncludedByDefault
  @Test
  void _unnamedPackageIsIncludedByDefault() {
    diagram.setDependencies(Set.of(
        new PackageDependency("foo.bar", "")));

    assertThat(diagram.getChildren(), hasSize(1));

    String dependency = diagram.getChildren().get(0).toString().trim();

    assertThat(dependency, is(equalTo("foo.bar --> unnamed")));
  }

  @AfterEach
  void onEachAfter() {
    verify(mockImages, atLeastOnce()).getFormats();
    verify(mockConfig, atLeastOnce()).getImageConfig();
    verify(mockConfig, atLeastOnce()).getPlantumlServerUrl();
    verify(mockConfig).getExcludedPackageDependencies();
    verify(mockConfig).getPackageDependenciesMaxCount();
    verifyNoMoreInteractions(mockConfig, mockImages);
  }

  @BeforeEach
  void onEachBefore() {
    excluded = new ArrayList<>(UmlConfig.EXCLUDED_PACKAGE_DEPENDENCIES__DEFAULT);

    mockImages = mock(ImageConfig.class);
    {
      when(mockImages.getFormats()).thenReturn(singleton(ImageConfig.Format.SVG));
    }

    mockConfig = mock(UmlConfig.class);
    {
      when(mockConfig.getImageConfig()).thenReturn(mockImages);
      when(mockConfig.getExcludedPackageDependencies()).thenReturn(excluded);
      when(mockConfig.getPackageDependenciesMaxCount()).thenReturn(
          UmlConfig.PACKAGE_DEPENDENCIES_MAX_COUNT__DEFAULT);
    }

    diagram = new DependencyDiagram(mockConfig, null,
        FILENAME__PACKAGE_DEPS + FILE_EXTENSION__PLANTUML);
  }
}
