/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlConfig_ExternalLinkIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.uml;

import static java.nio.file.Files.readString;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML;
import static org.pdfclown.common.util.io.Files.relativize;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_LIST;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_SUMMARY;
import static org.pdfclown.jada.uml.__test.Utils.writeText;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import org.apache.commons.lang3.function.Failable;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.Ref;
import org.pdfclown.common.util.io.ResourceNames;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.features.ExternalLinksTest
/**
 * Tests the 'external links' feature
 * <p>
 * This feature is tracked by <a href="https://github.com/talsma-ict/umldoclet/issues/96">issue
 * 96</a>.
 * </p>
 * <p>
 * For later reference, some JDK links:
 * </p>
 * <dl>
 * <dt>JDK 9</dt>
 * <dd><a href=
 * "https://docs.oracle.com/javase/9/docs/api">https://docs.oracle.com/javase/9/docs/api</a></dd>
 * <dt>JDK 10</dt>
 * <dd><a href=
 * "https://docs.oracle.com/javase/10/docs/api">https://docs.oracle.com/javase/10/docs/api</a></dd>
 * <dt>JDK 11</dt>
 * <dd><a href=
 * "https://docs.oracle.com/en/java/javase/11/docs/api">https://docs.oracle.com/en/java/javase/11/docs/api</a></dd>
 * </dl>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class UmlConfig_ExternalLinkIT extends BaseIT {
  public static class TestClass implements Serializable {
    private static final long serialVersionUID = 1L;
  }

  private static final String SUBDIR__EXTERNAL_API = "externalApidocs";

  UmlConfig_ExternalLinkIT() {
    super(UmlConfig_ExternalLinkIT.class);
  }

  // SourceName: testOfflineExternalLink
  @Test
  void _offlineExternalLink() {
    runJavadoc(javadocArgs()
        .setOnRunInit(Failable.asBiConsumer(($args, $outputDir) -> {
          Path packageListFile = getEnv().outputPath(ResourceNames.name(SUBDIR__EXTERNAL_API,
              FILENAME__PACKAGE_LIST));
          writeText(packageListFile, Serializable.class.getPackageName());

          $args.arg("-linkoffline", "https://docs.oracle.com/javase/9/docs/api",
              packageListFile.getParent());
        })));
    String puml = outputContent(getEnv().outputName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

    assertThat(puml, stringContainsInOrder(asList("interface", "Serializable",
        "[[https://docs.oracle.com/javase/9/docs/api/java/io/Serializable.html?is-external=true]]")));
  }

  // SourceName: testOnlineExternalLink
  @Test
  void _onlineExternalLink() {
    runJavadoc(javadocArgs()
        .arg("-link", "https://docs.oracle.com/javase/9/docs/api"));
    String puml = outputContent(getEnv().outputName(FILENAME__PACKAGE + FILE_EXTENSION__PLANTUML));

    assertThat(puml, stringContainsInOrder(asList("interface", "Serializable",
        "[[https://docs.oracle.com/javase/9/docs/api/java/io/Serializable.html?is-external=true]]")));
  }

  // SourceName: testRelativeExternalLink
  @Test
  void _relativeExternalLink() throws IOException {
    final var externalDirRef = new Ref<Path>();
    runJavadoc(javadocArgs()
        .setOnRunInit(Failable.asBiConsumer(($args, $outputDir) -> {
          externalDirRef.set(getEnv().outputPath(SUBDIR__EXTERNAL_API));

          writeText(externalDirRef.get().resolve(FILENAME__PACKAGE_LIST),
              Serializable.class.getPackageName());
          writeText(externalDirRef.get().resolve(ResourceNames.relBased(
              FILENAME__PACKAGE_SUMMARY + FILE_EXTENSION__HTML, Serializable.class)),
              "<html></html>");

          $args.arg("-link", relativize($outputDir, externalDirRef.get()));
        })));
    Path packagePumlFile = getEnv().outputPath(getEnv().outputName(FILENAME__PACKAGE
        + FILE_EXTENSION__PLANTUML));
    String puml = readString(packagePumlFile);

    assertThat(puml, stringContainsInOrder(asList("interface", "Serializable",
        "[[" + relativize(packagePumlFile, externalDirRef.get())
            + "/java/io/Serializable.html]]")));
  }
}
