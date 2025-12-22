/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FileOptimizer.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.proc;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.pdfclown.common.util.Chars.PIPE;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Objects.any;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__CSS;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__JAVASCRIPT;
import static org.pdfclown.common.util.io.Files.baseName;
import static org.pdfclown.common.util.io.Files.extension;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.LightweightMessageFormatter;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceExcerptProvider.SourceExcerpt;
import com.google.javascript.jscomp.SourceFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.tools.Diagnostic.Kind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.LazyNonNull;
import org.pdfclown.common.util.regex.Patterns;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.internal.JadaMessage;
import org.pdfclown.jada.core.system.SystemConfig;
import org.pdfclown.jada.core.system.proc.FileProcess;
import org.pdfclown.jada.core.system.proc.FileProcess.Context;
import org.pdfclown.jada.core.system.proc.TextSerializer;

/**
 * Optimizes Javadoc tool output files.
 * <p>
 * Supported file types:
 * </p>
 * <ul>
 * <li>CSS (*.css)</li>
 * <li>Javascript (*.js)</li>
 * </ul>
 * <p>
 * Files whose basename ends with {@code ".min"} are ignored, as they are assumed to have already
 * been optimized.
 * </p>
 * <p>
 * <span class="important">IMPORTANT: In order to retain the header comment with essential
 * information such as copyright notice and license, it MUST be formatted as in the following
 * example (note the exclamation mark in the opening comment marker):</span>
 * </p>
 * <pre class="lang-javascript" data-line=""><code>
 * &#47;*!
 *  * Copyright 2023-2025 Foo Inc.
 *  * MIT License
 *  *&#47;</code></pre>
 * <p>
 * NOTE: In {@linkplain JadaConfig#isDebug() debug mode}, optimization is disabled to ease source
 * code inspection and stepping.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class FileOptimizer extends JadaFileProcessor<String> {
  private @LazyNonNull @Nullable Pattern excludedFilesPattern;

  /**
   */
  public FileOptimizer() {
    super(new TextSerializer());

    /*
     * Disable Java logging!
     *
     * NOTE: Java logging backs Closure Compiler, causing compilation events to pollute the Javadoc
     * diagnostics; instead, such events are intercepted by Jada and smoothly integrated into the
     * Javadoc logging system.
     */
    LogManager.getLogManager().reset();
    var globalLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    globalLogger.setLevel(Level.OFF);
  }

  /**
   * @return {@code 1_000}
   * @implNote This processor is expected to transform files whose contents are in their final
   *           state, after being fed to all the other processors.
   */
  @Override
  public int getPriority() {
    return 1_000;
  }

  @Override
  public void init(SystemConfig config) {
    super.init(config);

    ((TextSerializer) serializer).setCharset(config.getOutputCharset());
  }

  @Override
  public boolean isProcessable(Path file, FileProcess.Context context) {
    return !getConfig().isDebug()
        && any(extension(file), String::equalsIgnoreCase,
            FILE_EXTENSION__CSS, FILE_EXTENSION__JAVASCRIPT)
        && !isFileExcluded(file);
  }

  @Override
  protected @Nullable String processContent(@NonNull String content, Path file, Context context) {
    String ret = null;
    var problems = new StringBuilder();
    String extension = extension(file).toLowerCase();
    switch (extension) {
      case FILE_EXTENSION__CSS: {
        var out = new ByteArrayOutputStream();
        try (var writer = new OutputStreamWriter(out, UTF_8)) {
          var reader = new StringReader(content);
          var compressor = new CssCompressor(reader);

          compressor.compress(writer, 500);
        } catch (IOException ex) {
          problems.append(getStackTrace(ex));
        }
        if (out.size() > 0) {
          ret = out.toString(UTF_8);
        }
      }
        break;
      case FILE_EXTENSION__JAVASCRIPT: {
        var externs = List.<SourceFile>of();
        var interns = List.of(SourceFile.fromPath(file, UTF_8));
        var options = new CompilerOptions();
        {
          options.setEnvironment(CompilerOptions.Environment.BROWSER);
          /*
           * NOTE: Strict mode becomes problematic in case of loosen third-party code, so we opt for
           * relaxed syntax.
           */
          options.setEmitUseStrict(false);
          /*
           * NOTE: The compiler emits a JSC_BAD_JSDOC_ANNOTATION warning when encounters a
           * `@callback` JSDoc tag, as it's not recognised; this option suppresses such noisy
           * warning.
           */
          options.setExtraAnnotationNames(Set.of("callback"));
          CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        }
        Compiler.setLoggingLevel(Level.WARNING);
        var compiler = new Compiler();

        Result result = compiler.compile(externs, interns, options);
        {
          var formatter = new LightweightMessageFormatter(compiler, SourceExcerpt.FULL);
          if (result.warnings != null) {
            for (JSError warning : result.warnings) {
              problems.append(formatter.formatWarning(warning));
            }
          }
          if (result.errors != null) {
            for (JSError error : result.errors) {
              problems.append(formatter.formatError(error));
            }
          }
        }
        if (result.success) {
          ret = compiler.toSource();
        }
      }
        break;
      default:
        throw unexpected(extension);
    }
    if (ret != null) {
      if (problems.length() > 0) {
        getLog().print(Kind.WARNING, this, JadaMessage.OPTIMIZATION_ISSUES, file, problems);
      }
      context.changeFile();
    } else {
      getLog().print(Kind.ERROR, this, JadaMessage.OPTIMIZATION_FAILED, file, problems);
    }
    return ret;
  }

  private boolean isFileExcluded(Path file) {
    /*
     * NOTE: By definition, already optimized files are excluded.
     */
    if (baseName(file).endsWith(".min"))
      return true;

    if (excludedFilesPattern == null) {
      excludedFilesPattern = Pattern.compile(
          getConfig().getExcludedOptimizationFiles().stream()
              .map(Patterns::globToRegex)
              .collect(joining(S + PIPE)));
    }
    /*
     * NOTE: Match is done against a subsequence rather than the entire region in order to permit
     * simple filters like "myScript.js" (otherwise, they would never match the full path!).
     */
    return !excludedFilesPattern.pattern().isEmpty()
        && excludedFilesPattern.matcher(file.toUri().toString()).find();
  }
}
