/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FileProcess.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.system.proc;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Exceptions.wrongState;
import static org.pdfclown.common.util.Objects.fqn;
import static org.pdfclown.jada.core.internal.JadaMessage.P__CHANGED;
import static org.pdfclown.jada.core.internal.JadaMessage.P__PROCESSOR;
import static org.pdfclown.jada.core.internal.JadaMessage.P__UNCHANGED;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.tools.Diagnostic.Kind;
import org.apache.commons.lang3.function.Failable;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.jada.core.internal.JadaMessage;
import org.pdfclown.jada.core.system.Logger;
import org.pdfclown.jada.core.system.SystemConfig;
import org.pdfclown.jada.core.system.SystemObject;
import org.pdfclown.jada.core.util.Messages;

/**
 * File processing manager.
 *
 * @param <T>
 *          Processor type.
 * @author Stefano Chizzolini
 */
public abstract class FileProcess<T extends FileProcessor<?>> implements SystemObject {
  /**
   * {@link FileProcess} session context.
   *
   * @author Stefano Chizzolini
   */
  public static class Context {
    private static final int FILE_INDEX__DEFAULT = -1;
    private static final boolean FILE_REPEAT_REQUESTED__DEFAULT = false;
    private static final FileStatus FILE_STATUS__DEFAULT = FileStatus.DONE_UNCHANGED;
    private static final boolean REPROCESSABLE__DEFAULT = false;

    private boolean ended;
    private int fileIndex = FILE_INDEX__DEFAULT;
    private boolean fileRepeatRequested = FILE_REPEAT_REQUESTED__DEFAULT;
    private FileStatus fileStatus = FILE_STATUS__DEFAULT;
    private int iterationIndex;
    private final FileProcess<?> process;
    private boolean reprocessable = REPROCESSABLE__DEFAULT;

    public Context(FileProcess<?> process) {
      this.process = process;
    }

    /**
     * Enables re-processing, causing another iteration even if no change occurred in the current
     * one.
     * <p>
     * Useful to be invoked on value resolution, to give postponed files a chance to consume it on
     * next processing iteration.
     * </p>
     */
    public void allowReprocess() {
      reprocessable = true;
    }

    /**
     * Marks the current file as changed.
     * <p>
     * {@linkplain #getFileStatus() File status} is set to {@link FileStatus#DONE_CHANGED}, or to
     * {@link FileStatus#CHANGED} if previously {@linkplain #postponeFile() postponed}.
     * </p>
     */
    public void changeFile() {
      switch (fileStatus) {
        case DONE_UNCHANGED:
          fileStatus = FileStatus.DONE_CHANGED;
          break;
        case POSTPONED:
          fileStatus = FileStatus.CHANGED;
          break;
        default:
          // NOP
      }
    }

    /**
     * Terminates processing immediately.
     */
    public void end() {
      ended = true;
    }

    /**
     * Gets the base directory of the file within the {@linkplain FileProcess#getDirectories()
     * processing space}.
     *
     * @throws java.util.NoSuchElementException
     *           if {@code file} is outside the processing space.
     */
    public Path getBaseDir(Path file) {
      return process.getDirectories().stream()
          .filter(file::startsWith)
          .findFirst().orElseThrow();
    }

    /**
     * Current file index.
     * <p>
     * NOTE: Meaningful only within the current iteration (the same file may be assigned different
     * indices across multiple iterations).
     * </p>
     */
    public int getFileIndex() {
      return fileIndex;
    }

    /**
     * Current file status.
     */
    public FileStatus getFileStatus() {
      return fileStatus;
    }

    /**
     * Current iteration index.
     */
    public int getIterationIndex() {
      return iterationIndex;
    }

    /**
     * File process.
     */
    public FileProcess<?> getProcess() {
      return process;
    }

    /**
     * Whether processing is ended.
     */
    public boolean isEnded() {
      return ended;
    }

    /**
     * Whether the current file was marked as changed.
     */
    public boolean isFileChanged() {
      return fileStatus.isChanged();
    }

    /**
     * Whether the current file was marked as complete.
     */
    public boolean isFileComplete() {
      return fileStatus.isComplete();
    }

    /**
     * Whether the current file is requested to be reprocessed immediately.
     */
    public boolean isFileRepeatRequested() {
      return fileRepeatRequested;
    }

    /**
     * Whether processing on the current file is postponed to the next iteration.
     */
    public boolean isPostponed() {
      return fileStatus == FileStatus.POSTPONED;
    }

    /**
     * Whether reprocessing is allowed, causing another iteration even if no change occurred in the
     * current one.
     */
    public boolean isReprocessable() {
      return reprocessable;
    }

    /**
     * Marks the current file as {@link #isPostponed() postponed}.
     * <p>
     * {@linkplain #getFileStatus() File status} is set to {@link FileStatus#POSTPONED}, or to
     * {@link FileStatus#CHANGED} if previously {@linkplain #changeFile() modified}.
     * </p>
     */
    public void postponeFile() {
      switch (fileStatus) {
        case DONE_UNCHANGED:
          fileStatus = FileStatus.POSTPONED;
          break;
        case DONE_CHANGED:
          fileStatus = FileStatus.CHANGED;
          break;
        default:
          // NOP
      }
    }

    /**
     * Requests the current file to be reprocessed immediately.
     */
    public void repeatFile() {
      fileRepeatRequested = true;
    }

    /**
     * Prepares next iteration.
     */
    protected void reset() {
      fileIndex = FILE_INDEX__DEFAULT;
      fileRepeatRequested = FILE_REPEAT_REQUESTED__DEFAULT;
      fileStatus = FILE_STATUS__DEFAULT;
      iterationIndex++;
      reprocessable = REPROCESSABLE__DEFAULT;
    }

    /**
     * Prepares next file.
     */
    protected void resetFile(boolean repeat) {
      if (!repeat) {
        fileIndex++;
      }
      fileRepeatRequested = FILE_REPEAT_REQUESTED__DEFAULT;
      fileStatus = FILE_STATUS__DEFAULT;
    }
  }

  /**
   * File processing status.
   *
   * @author Stefano Chizzolini
   */
  public enum FileStatus {
    /**
     * Processing on file complete with changes.
     */
    DONE_CHANGED(true, true),
    /**
     * Processing on file complete without changes.
     */
    DONE_UNCHANGED(false, true),
    /**
     * Processing on file incomplete, but advanced.
     */
    CHANGED(true, false),
    /**
     * Processing on file incomplete, and unchanged.
     */
    POSTPONED(false, false);

    private final boolean changed;
    private final boolean complete;

    FileStatus(boolean changed, boolean complete) {
      this.changed = changed;
      this.complete = complete;
    }

    public boolean isChanged() {
      return changed;
    }

    public boolean isComplete() {
      return complete;
    }
  }

  /**
   * {@link FileProcess} session result.
   *
   * @author Stefano Chizzolini
   */
  public static class RunResult {
    /**
     * {@link RunResult} builder.
     *
     * @author Stefano Chizzolini
     */
    public static class Builder {
      final Map<Path, FileStatus> files = new HashMap<>();

      public RunResult build() {
        return new RunResult(files);
      }

      /**
       * Notifies the completion of the processing on a file by a processor.
       *
       * @param changed
       *          Whether the file was changed.
       */
      public void complete(Path file, boolean changed) {
        /*
         * NOTE: Once a file is marked as changed, any further notification by subsequent processors
         * is irrelevant.
         */
        if (files.get(file) == FileStatus.CHANGED)
          return;

        files.put(file, changed ? FileStatus.CHANGED : FileStatus.UNCHANGED);
      }

      public void skip(Path file) {
        files.putIfAbsent(file, FileStatus.SKIPPED);
      }
    }

    /**
     * File resulting status.
     *
     * @author Stefano Chizzolini
     */
    public enum FileStatus {
      /**
       * Processed with changes.
       */
      CHANGED,
      /**
       * Processed without changes.
       */
      UNCHANGED,
      /**
       * Not processed.
       */
      SKIPPED
    }

    private final Map<Path, FileStatus> files;

    public RunResult(Map<Path, FileStatus> files) {
      this.files = unmodifiableMap(files);
    }

    /**
     * Files evaluated during this session.
     */
    public Map<Path, FileStatus> getFiles() {
      return files;
    }
  }

  protected Map<String, T> processors = new LinkedHashMap<>();

  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull SystemConfig config;
  private final List<Path> directories = new ArrayList<>();

  /**
   * Registers a processor.
   */
  public FileProcess<T> addProcessor(T processor) {
    String processorFqn = fqn(requireNonNull(processor, "`processor`"));
    if (processors.containsKey(processorFqn))
      throw wrongArg("processor", processor.getClass(), "ALREADY PRESENT");

    processor.init(config);
    processors.put(processorFqn, processor);

    getLog().print(Kind.NOTE, this, JadaMessage.ELEMENT_REGISTERED, P__PROCESSOR,
        Logger.sourceName(processor));
    return this;
  }

  @Override
  public SystemConfig getConfig() {
    return config;
  }

  /**
   * Directories containing the files to process.
   */
  public List<Path> getDirectories() {
    return directories;
  }

  @SuppressWarnings("unchecked")
  public <R extends T> @Nullable R getProcessor(Class<R> type) {
    return (R) processors.get(fqn(type));
  }

  /**
   * Processors to which generated files are fed.
   */
  public Map<String, T> getProcessors() {
    return processors;
  }

  public void init(SystemConfig config) {
    this.config = requireNonNull(config, "`config`");
  }

  /**
   * Walks across the {@linkplain #getDirectories() directories}, offering their files to the
   * {@linkplain #getProcessors() processors}, ordered by {@linkplain FileProcessor#getPriority()
   * priority}.
   * <p>
   * At the end of a walk, incomplete files (that is, those whose
   * {@linkplain FileProcessor#process(Object, Path, Context) processing} status is not
   * {@linkplain FileStatus#isComplete() complete}) are walked again until they are all complete —
   * this allows multi-pass resolution. Finally, all the processors are
   * {@linkplain AutoCloseable#close() closed}.
   * </p>
   *
   * @throws IllegalStateException
   *           if processing entered an infinite loop, due to reiterated
   *           {@linkplain FileStatus#POSTPONED postpone} on all incomplete files.
   */
  public RunResult run() {
    var resultBuilder = new RunResult.Builder();

    processors.values().stream()
        .sorted(Comparator.comparing(FileProcessor::getPriority))
        .forEachOrdered($ -> run($, resultBuilder));

    return resultBuilder.build();
  }

  /**
   * Walks across the {@linkplain #getDirectories() directories}, offering their files to the
   * processor.
   * <p>
   * (see {@linkplain #run() main overload} for further information)
   * </p>
   *
   * @throws IllegalStateException
   *           if processing entered an infinite loop, due to reiterated
   *           {@linkplain FileStatus#POSTPONED postpone} on all incomplete files.
   */
  private <A> void run(T processor, RunResult.Builder resultBuilder) {
    /*
     * NOTE: Unfortunately, javac (tried on OpenJDK 11 and 17) erratically refuses, from time to
     * time (sic!), to compile this method signature (while Eclipse compiler is totally fine):
     *
     * `private <A> void run(FileProcessor<A> processor)`
     *
     * prompting this error message:
     *
     * >> cannot infer type-variable(s) A
     *
     * >> [ERROR] (argument mismatch; T cannot be converted to
     * org.pdfclown.jada.core.proc.FileProcessor<A>)
     *
     * despite generic parameters for multi-level wildcard arguments are a well-known practice (see,
     * for example, "How do I implement a method that takes a multi-level wildcard argument?" at
     * <https://angelikalanger.com/GenericsFAQ/FAQSections/ProgrammingIdioms.html#FAQ305>).
     *
     * This is one of those weird things about java which drive me nuts...
     */
    @SuppressWarnings("unchecked")
    var p = (FileProcessor<A>) processor;

    Stream<Path> files = directories.stream()
        .filter($ -> {
          if (!Files.exists($)) {
            getLog().print(Kind.WARNING, this, JadaMessage.INPUT_DIR_NOT_FOUND, $);
            return false;
          }
          return true;
        })
        .flatMap(Failable.asFunction(Files::walk))
        .filter(Files::isRegularFile);
    var context = new Context(this);
    /*
     * NOTE: Re-processing is initially activated to ensure at least another iteration in case the
     * first one builds state without applying changes (POSTPONED only).
     */
    context.allowReprocess();
    var success = true;
    try {
      var incompleteFiles = new LinkedHashSet<Path>();
      while (true) {
        files
            .takeWhile($ -> !context.isEnded())
            .forEach($ -> {
              context.resetFile(false);

              var processable = false;
              if (p.isProcessable($, context)) {
                processable = true;

                var content = p.load($);

                boolean contentChanged = false;
                {
                  int postponeRepeatCount = 1;
                  while (true) {
                    content = p.process(content, $, context);

                    if (context.isFileChanged()) {
                      contentChanged = true;

                      context.allowReprocess();
                    }

                    /*
                     * NOTE: File processing can be repeated immediately on request if the last
                     * processing was not postponed or was within the tolerated number of
                     * postponements (further postponements are rejected to avoid infinite loops).
                     */
                    if (!context.isFileRepeatRequested()) {
                      break;
                    } else if (context.isPostponed()) {
                      if (--postponeRepeatCount < 0) {
                        break;
                      }
                    } else {
                      postponeRepeatCount = 1;
                    }

                    context.resetFile(true);
                  }
                }

                if (contentChanged) {
                  p.save(content, $);
                }
              }

              if (context.isFileComplete()) {
                incompleteFiles.remove($);

                /*
                 * NOTE: Files not supported by the current processor are silently ignored.
                 */
                if (processable) {
                  resultBuilder.complete($, context.isFileChanged());

                  getLog().print(context.isFileChanged() ? Kind.NOTE : Kind.OTHER, this,
                      JadaMessage.FILE_PROCESS_ITEM_COMPLETE,
                      Logger.sourceName(p), $, context.isFileChanged() ? P__CHANGED : P__UNCHANGED);
                }
              } else {
                incompleteFiles.add($);
              }
            });
        if (context.isEnded() || incompleteFiles.isEmpty()) {
          incompleteFiles.forEach(resultBuilder::skip);
          break;
        } else if (!context.isReprocessable())
          throw wrongState(JadaMessage.FILE_PROCESS_INFINITE_LOOP.toString(config,
              Logger.sourceName(p), Messages.list(incompleteFiles, 1), p.createStatusMessage()));

        files.close();

        /*
         * NOTE: `incompleteFiles` copied to new list to avoid `ConcurrentModificationException`.
         */
        files = new ArrayList<>(incompleteFiles).stream();
        context.reset();
      }
    } catch (RuntimeException ex) {
      success = false;
      throw ex;
    } finally {
      files.close();

      p.end(success);
    }
  }
}
