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

import static java.lang.Boolean.parseBoolean;
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
public class ProcessSourceMojo extends AbstractMojo {
  /**
   * Information to print.
   */
  public enum Info {
    PROCS($ -> {
      $.getLog().info(format("Available source code processors (extending `{}`):",
          fqnd(SrcFileProcessor.class)));
      realSubTypes(SrcFileProcessor.class).forEach($$ -> $.getLog().info("- " + fqnd($$)));
    });

    final Consumer<org.apache.maven.plugin.Mojo> printer;

    Info(Consumer<org.apache.maven.plugin.Mojo> printer) {
      this.printer = printer;
    }
  }

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  /**
   * Class names of selected {@linkplain SrcFileProcessor source file processors}.
   * <p>
   * The processors are executed in the given order.
   * </p>
   */
  @Parameter(property = "jada.procs")
  private List<String> processors;

  /**
   * Whether to skip source processing.
   */
  @Parameter(property = "jada.procs.skip")
  private boolean skip;

  /**
   * Information request.
   * <p>
   * A list of values, among these:
   * </p>
   * <ul>
   * <li>{@code PROCS} — lists the class names of {@linkplain SrcFileProcessor source-code
   * processors} available in the classpath</li>
   * </ul>
   * <p>
   * Requested information is printed to Maven output; if no selector is specified, all information
   * is printed. This operation suppresses source code processing.
   * </p>
   */
  @Parameter(property = "jada.procs.info")
  private Set<String> info;

  private final transient Set<Info> info_ = new HashSet<>();

  @Override
  public void execute() throws MojoExecutionException {
    if (isSkip()) {
      getLog().info("Source processing SKIPPED");
      return;
    }

    init();

    if (!processors.isEmpty() && info_.isEmpty()) {
      process();
    } else {
      printInfo();
    }
  }

  public boolean isSkip() {
    return skip;
  }

  public void setSkip(boolean value) {
    skip = value;
  }

  private void init() {
    /*
     * NOTE: AFAIK, Maven's built-in parameter conversion capabilities are quite rudimentary; here
     * we want to print all the information when users input the parameter without specifying any
     * value (in such case, CLI parser assigns "true").
     */
    for (var e : info) {
      if (parseBoolean(e)) {
        info_.addAll(List.of(Info.values()));
        break;
      } else {
        info_.add(Info.valueOf(e));
      }
    }
  }

  private void printInfo() {
    if (processors.isEmpty()) {
      getLog().warn("No source code processor selected.");

      info_.add(Info.PROCS);
    }

    info_.forEach($ -> {
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
      @SuppressWarnings("rawtypes")
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
