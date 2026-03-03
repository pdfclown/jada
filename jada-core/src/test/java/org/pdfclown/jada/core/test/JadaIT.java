/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaIT.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.test;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.wrongState;
import static org.pdfclown.jada.core.test.assertion.Assertions.assertJavadoc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.pdfclown.common.build.system.ProjectDirId;
import org.pdfclown.common.build.test.IT;
import org.pdfclown.common.build.test.TestUnit;
import org.pdfclown.common.build.util.io.ResourceNames;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertArgs;
import org.pdfclown.jada.core.test.assertion.Assertions.JavadocAssertResult;

/**
 * Jada integration test.
 * <p>
 * Configurable <b>javadoc tool execution</b> is ready out-of-the-box:
 * </p>
 * <ul>
 * <li>automatic {@linkplain #singleRun() single run}</li>
 * <li>manual {@linkplain #runJavadoc(JavadocAssertArgs) multiple runs}</li>
 * </ul>
 * <p>
 * Use either {@code sourceType} or {@code sourcePackage} constructor arguments to define the source
 * fed to javadoc tool to generate the corresponding documentation.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public abstract class JadaIT extends IT {
  public class Environment extends TestUnit.Environment {
    /**
     * Gets the name of an output file relative to this test environment, defined according to
     * {@link #sourceType} (if present) or {@link #sourcePackages} (if single).
     * <p>
     * This is a convenience for frequent references to generated Javadoc files, as integration
     * tests are typically focused on a limited set of source types within a single package.
     * </p>
     *
     * @throws IllegalStateException
     *           if {@link #sourceType} is absent and {@link #sourcePackages} is not single.
     */
    public String outputName(String name) {
      if (sourceType != null)
        return ResourceNames.relBased(name, sourceType);
      else if (sourcePackages.size() == 1)
        return ResourceNames.relBased(name, sourcePackages.get(0));
      else
        throw wrongState(sourcePackages.isEmpty()
            ? "No source reference (neither `sourceType` nor `sourcePackages`)"
            : "Cannot resolve on multiple `sourcePackages` (MUST be single)");
    }

    /**
     * @param name
     *          Output file name (either absolute or relative to this test environment; in case of
     *          {@linkplain #runJavadoc() javadoc execution}, the relative base becomes its specific
     *          output directory since {@linkplain JavadocAssertArgs#getOnRunInit() run
     *          initialization}).
     */
    @Override
    public Path outputPath(String name) {
      return outputDir != null && !ResourceNames.isAbs(name)
          ? ResourceNames.toPath(name, outputDir)
          : super.outputPath(name);
    }
  }

  /**
   * Source packages fed to javadoc tool to generate the corresponding documentation.
   */
  protected final List<String> sourcePackages;
  /**
   * Source type fed to javadoc tool to generate the corresponding documentation.
   */
  protected final @Nullable Class<?> sourceType;

  /**
   * Javadoc output directory.
   */
  private @Nullable Path outputDir;
  private boolean singleRun;
  /**
   * Source base directory.
   * <p>
   * Required for {@link #sourcePackages} resolution.
   * </p>
   */
  private final @Nullable Path sourceDir;

  protected JadaIT() {
    this((Class<?>) null);
  }

  protected JadaIT(@Nullable Class<?> sourceType) {
    this(sourceType, null, List.of());
  }

  protected JadaIT(List<String> sourcePackages) {
    this(null, sourcePackages);
  }

  /**
   * @param sourceDirId
   *          (default: {@link ProjectDirId#TEST_TYPE_SOURCE})
   */
  protected JadaIT(@Nullable ProjectDirId sourceDirId, List<String> sourcePackages) {
    this(null, sourceDirId, sourcePackages);
  }

  protected JadaIT(@Nullable ProjectDirId sourceDirId, String sourcePackage) {
    this(sourceDirId, List.of(sourcePackage));
  }

  protected JadaIT(String sourcePackage) {
    this(null, sourcePackage);
  }

  private JadaIT(@Nullable Class<?> sourceType, @Nullable ProjectDirId sourceDirId,
      List<String> sourcePackages) {
    this.sourcePackages = requireNonNull(sourcePackages, "`sourcePackages`");
    if (!this.sourcePackages.isEmpty()) {
      this.sourceDir = getEnv().dir(sourceDirId != null ? sourceDirId
          : ProjectDirId.TEST_TYPE_SOURCE);

      this.sourceType = null;
    } else {
      this.sourceType = sourceType;

      this.sourceDir = null;
    }
  }

  @Override
  public synchronized Environment getEnv() {
    return (Environment) super.getEnv();
  }

  @BeforeAll
  public void onAllBefore() {
    if (singleRun) {
      var args = javadocArgs();

      onSingleRunInit(args);

      // Run javadoc tool!
      var result = runJavadoc(args);

      onSingleRunTerm(result);
    }
  }

  /**
   * Gets the text content ({@linkplain StandardCharsets#UTF_8 UTF-8} encoding) of the file
   * corresponding to the name.
   */
  public String outputContent(String name) {
    try {
      return Files.readString(getEnv().outputPath(name));
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }

  @Override
  protected TestUnit.Environment __createEnv() {
    return new Environment();
  }

  /**
   * Creates a new argument set for javadoc execution in this integration test.
   */
  protected JavadocAssertArgs javadocArgs() {
    return new JavadocAssertArgs(getEnv());
  }

  /**
   * Notifies javadoc tool initialization.
   *
   * @param args
   *          Javadoc tool arguments.
   */
  protected void onSingleRunInit(JavadocAssertArgs args) {
  }

  /**
   * Notifies javadoc tool termination.
   *
   * @param result
   *          Javadoc tool result.
   */
  protected void onSingleRunTerm(JavadocAssertResult result) {
  }

  /**
   * Executes the javadoc tool in this integration test.
   */
  protected JavadocAssertResult runJavadoc() {
    return runJavadoc(javadocArgs());
  }

  /**
   * Executes the javadoc tool in this integration test with the arguments.
   */
  protected JavadocAssertResult runJavadoc(JavadocAssertArgs args) {
    if (sourceType != null) {
      args.type(sourceType);
    } else if (!sourcePackages.isEmpty()) {
      args
          .packageDir(requireNonNull(sourceDir, "`sourceDir`"))
          .packageNames(sourcePackages);
    }

    /*
     * NOTE: `outputDir` must be reset before invoking `assertJavadoc(..)` in order not to interfere
     * with the original behavior of `outputFile(..)`.
     */
    outputDir = null;

    var userOnRunInit = args.getOnRunInit();
    args.setOnRunInit(($args, $outputDir) -> {
      /*
       * NOTE: Now the output directory has already been resolved, so we have to synchronize
       * `outputDir` accordingly in order to let `userOnRunInit` work in a consistent environment.
       */
      outputDir = $outputDir;

      if (userOnRunInit != null) {
        userOnRunInit.accept($args, $outputDir);
      }
    });

    // Run javadoc tool!
    @SuppressWarnings("UnnecessaryLocalVariable")
    var ret = assertJavadoc(args);

    return ret;
  }

  /**
   * Configures this integration test as single javadoc run.
   * <p>
   * Once configured, the javadoc tool is automatically invoked before all the test cases within the
   * derived class. Its behavior can be customized intercepting
   * {@link #onSingleRunInit(JavadocAssertArgs)}, and its result can be evaluated intercepting
   * {@link #onSingleRunTerm(JavadocAssertResult)}.
   * <p>
   * <span class="important">IMPORTANT: In order to be effective, this method MUST be invoked from
   * within the class constructor.</span>
   * </p>
   */
  protected void singleRun() {
    singleRun = true;
  }
}
