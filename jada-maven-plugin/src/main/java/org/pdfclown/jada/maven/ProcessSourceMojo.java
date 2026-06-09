/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ProcessSourceMojo.java) is part of jada-maven-plugin module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.maven;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.pdfclown.common.util.Chars.COMMA;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Exceptions.throwable;
import static org.pdfclown.common.util.Objects.fqnd;
import static org.pdfclown.common.util.ParamMessage.format;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.jada.core.util.Objects.realSubTypes;
import static org.pdfclown.jada.maven.internal.util.Mojos.parseParameterEnumValues;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.pdfclown.jada.core.system.proc.FileProcess.RunResult;
import org.pdfclown.jada.core.system.proc.FileProcess.RunResult.FileStatus;
import org.pdfclown.jada.core.system.proc.src.SrcFileProcess;
import org.pdfclown.jada.core.system.proc.src.SrcFileProcessor;

/**
 * Processes source files.
 *
 * @author Stefano Chizzolini
 */
@Mojo(name = "processSource", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
@SuppressWarnings("rawtypes")
public class ProcessSourceMojo extends AbstractMojo {
  /**
   * Information to print.
   *
   * @author Stefano Chizzolini
   */
  public enum Info {
    /**
     * Lists the class names of source code processors (extending {@link SrcFileProcess}) available
     * in the classpath.
     */
    PROCS($ -> {
      $.getLog().info(format("Available source code processors (extending {}):",
          fqnd(SrcFileProcessor.class)));
      realSubTypes(SrcFileProcessor.class).forEach($$ -> $.getLog().info("- " + fqnd($$)));
    });

    @SuppressWarnings("ImmutableEnumChecker")
    final Consumer<org.apache.maven.plugin.Mojo> printer;

    Info(Consumer<org.apache.maven.plugin.Mojo> printer) {
      this.printer = printer;
    }
  }

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  @SuppressWarnings({ "NotNullFieldNotInitialized", "unused" })
  private MavenProject project;

  /**
   * Class names of selected source code processors (extending {@link SrcFileProcess}).
   * <p>
   * The processors are executed in the given order.
   * </p>
   */
  @Parameter(property = "jada.procs")
  @SuppressWarnings({ "NotNullFieldNotInitialized", "MismatchedQueryAndUpdateOfCollection",
      "unused" })
  private List<String> processors;

  /**
   * Whether to skip source processing.
   */
  @Parameter(property = "jada.procs.skip")
  private boolean skip;

  /**
   * Information request.
   * <p>
   * A list of values among these:
   * </p>
   * <ul>
   * <li>{@code PROCS} — lists the class names of source code processors (extending
   * {@link SrcFileProcess}) available in the classpath</li>
   * </ul>
   * <p>
   * Requested information is printed to Maven output; if no selector is specified, all information
   * is printed. This operation suppresses source code processing.
   * </p>
   */
  @Parameter(property = "jada.procs.info", alias = "info")
  @SuppressWarnings({ "NotNullFieldNotInitialized", "MismatchedQueryAndUpdateOfCollection",
      "unused" })
  private Set<String> rawInfo;

  /*
   * DERIVED FIELDS
   */
  /**
   * Information request.
   * <p>
   * Strongly-typed representation of {@link #rawInfo}.
   * </p>
   *
   * @implNote When users specify the corresponding parameter without any value (resolved as "true"
   *           or empty value by the CLI parser), we want to print all the information. Since,
   *           AFAIK, Maven's built-in {@link Enum}-typed parameter conversion cannot deal with
   *           implicit parameter values, "true" or empty value cause conversion to fail; to work
   *           around this, the parameter is injected as raw list ({@link #rawInfo}), then it is
   *           processed for conversion by implicit-value-aware logic.
   */
  private final transient Set<Info> info = new HashSet<>();

  @Override
  public void execute() throws MojoExecutionException {
    if (skip) {
      getLog().info("Source processing SKIPPED");
      return;
    }

    init();

    /*
     * NOTE: Information request has priority over source processing.
     */
    if (!processors.isEmpty() && info.isEmpty()) {
      process();
    } else {
      printInfo();
    }
  }

  private void init() {
    parseParameterEnumValues(Info.class, rawInfo, info,
        $target -> $target.addAll(List.of(Info.values())));
  }

  private void printInfo() {
    if (processors.isEmpty() && info.isEmpty()) {
      getLog().warn("No source code processor selected.");

      info.add(Info.PROCS);
    }

    info.forEach($ -> {
      getLog().info(EMPTY);
      $.printer.accept(this);
    });
  }

  private void process() throws MojoExecutionException {
    var sourceDirectory = Path.of(project.getBuild().getSourceDirectory());
    if (!Files.isDirectory(sourceDirectory)) {
      getLog().debug(format("Source directory {} NOT FOUND: SKIP", sourceDirectory));
      return;
    }

    var process = new SrcFileProcess();
    {
      process.init(new PluginConfig(Locale.ROOT, getLog())
          .setBuildDirectory(Path.of(project.getBuild().getDirectory())));
      process.getDirectories().add(sourceDirectory);
    }

    for (String processor : processors) {
      Class<? extends SrcFileProcessor> processorType;
      try {
        processorType = Class.forName(processor).asSubclass(SrcFileProcessor.class);
      } catch (Exception ex) {
        throw throwable(MojoExecutionException::new,
            "Processor `{}` INVALID (MUST extend `{}`)", processor,
            SrcFileProcessor.class, ex);
      }
      try {
        process.addProcessor(processorType.getConstructor().newInstance());

        getLog().debug(format("Processor `{}` LOADED", processorType));
      } catch (Exception ex) {
        throw throwable(MojoExecutionException::new, "Processor `{}` loading FAILED",
            processorType, ex);
      }
    }

    RunResult runResult = process.run();

    {
      Map<RunResult.FileStatus, Long> fileStatusCounts = runResult.getFiles().entrySet().stream()
          .collect(groupingBy(Entry::getValue, HashMap::new, counting()));

      getLog().info(Arrays.stream(FileStatus.values())
          .map($ -> fileStatusCounts.getOrDefault($, 0L) + " files " + $)
          .collect(joining(S + COMMA + SPACE)));
    }
  }
}
