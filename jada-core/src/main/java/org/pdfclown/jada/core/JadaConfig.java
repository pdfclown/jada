/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaConfig.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Exceptions.wrongArgOpt;
import static org.pdfclown.common.util.Objects.fqn;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.io.Files.filename;
import static org.pdfclown.common.util.io.Files.normal;
import static org.pdfclown.common.util.reflect.Reflects.stackFrame;
import static org.pdfclown.jada.core.internal.JadaMessage.P__OPERATION;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__MODULE_SUMMARY;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_SUMMARY;

import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Log.WriterKind;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.DocumentationTool;
import javax.tools.JavaFileManager;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.doclet.Taglet;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.LazyNonNull;
import org.pdfclown.common.util.annot.UnmodifiableView;
import org.pdfclown.common.util.io.Resource;
import org.pdfclown.jada.core.internal.JadaMessage;
import org.pdfclown.jada.core.system.Logger;
import org.pdfclown.jada.core.system.SystemConfig;
import org.pdfclown.jada.core.util.lang.LangModels;
import org.pdfclown.jada.core.util.lang.LangModels.FQName;

/**
 * {@link Jada} configuration.
 *
 * @author Stefano Chizzolini
 */
public class JadaConfig extends SystemConfig implements JadaObject {
  /**
   * File attachment.
   *
   * @author Stefano Chizzolini
   */
  public static class Attachment {
    /**
     * Creates a resource attachment.
     *
     * @param groupName
     *          Group resource name, corresponding to the target folder (for example,
     *          {@code  "MyExtension"} maps to {@code  "resources/MyExtension"} folder).
     */
    public static Attachment resource(Resource resource, String groupName) {
      var basePath = Path.of("resources");
      return new Attachment(resource, groupName.isEmpty() ? basePath : basePath.resolve(groupName));
    }

    Resource source;
    Path target;

    /**
     * New attachment, whose target filename corresponds to the source filename.
     */
    public Attachment(Resource source, Path targetDir) {
      this(source, targetDir, EMPTY);
    }

    /**
     * New attachment.
     *
     * @param targetFilename
     *          (if empty, uses the source filename)
     */
    public Attachment(Resource source, Path targetDir, String targetFilename) {
      if (targetDir.isAbsolute())
        throw wrongArg("targetDir", targetDir, "MUST be relative");

      if (targetFilename.isEmpty()) {
        targetFilename = filename(source.getName());
      }

      this.source = source;
      this.target = targetDir.resolve(targetFilename);
    }

    /**
     * File source.
     */
    public Resource getSource() {
      return source;
    }

    /**
     * File target, that is relative path in the generated documentation where to copy the
     * {@link #getSource() source}.
     */
    public Path getTarget() {
      return target;
    }

    @Override
    public String toString() {
      return target.toString();
    }
  }

  /**
   * Jada logger.
   *
   * @author Stefano Chizzolini
   */
  protected static class JadaLogger extends Logger {
    public JadaLogger() {
      super();
    }

    private JadaLogger(JadaConfig config, Reporter base) {
      super(config, base);
    }

    @Override
    protected void onLogEvent(Kind kind) {
      // Problem occurred?
      if (kind.compareTo(Kind.NOTE) < 0) {
        /*
         * NOTE: In case of error, we have to automatically infer whether it was caused by a Jada
         * extension, so it can be notified accordingly.
         */
        stackFrame($ -> ((JadaConfig) JadaLogger.this.config).extensions.values().stream()
            .filter($$ -> $.getClassName().startsWith($$.getClass().getPackageName())).findFirst()
            // Found: end the call stack walk!
            .map($$ -> {
              $$.onProblem(kind);
              return true;
            })
            // Not found: keep walking the call stack!
            .orElse(false));
      }
    }
  }

  /**
   * Log interceptor.
   * <p>
   * See implementation note at {@link #println(String)}.
   * </p>
   *
   * @author Stefano Chizzolini
   */
  private static class LogWriterInterceptor extends PrintWriter {
    private final PrintWriter base;
    private final JadaConfig config;
    private @Nullable Pattern outputFileRegex;
    private boolean writable = true;

    public LogWriterInterceptor(PrintWriter base, JadaConfig config) {
      super(new ByteArrayOutputStream(0));

      this.base = base;
      this.config = config;
    }

    @Override
    public PrintWriter append(CharSequence csq) {
      if (isWritable()) {
        base.append(csq);
      }
      return this;
    }

    @Override
    public PrintWriter append(CharSequence csq, int start, int end) {
      if (isWritable()) {
        base.append(csq, start, end);
      }
      return this;
    }

    @Override
    public PrintWriter append(char c) {
      if (isWritable()) {
        base.append(c);
      }
      return this;
    }

    @Override
    public boolean checkError() {
      return base.checkError();
    }

    @Override
    public void close() {
      base.close();
    }

    @Override
    public void flush() {
      base.flush();
    }

    @Override
    public PrintWriter format(Locale l, String format, Object... args) {
      if (isWritable()) {
        base.format(l, format, args);
      }
      return this;
    }

    @Override
    public PrintWriter format(String format, Object... args) {
      if (isWritable()) {
        base.format(format, args);
      }
      return this;
    }

    @Override
    public void print(Object obj) {
      if (isWritable()) {
        base.print(obj);
      }
    }

    @Override
    public void print(String s) {
      if (isWritable()) {
        base.print(s);
      }
    }

    @Override
    public void print(boolean b) {
      if (isWritable()) {
        base.print(b);
      }
    }

    @Override
    public void print(char c) {
      if (isWritable()) {
        base.print(c);
      }
    }

    @Override
    public void print(char[] s) {
      if (isWritable()) {
        base.print(s);
      }
    }

    @Override
    public void print(double d) {
      if (isWritable()) {
        base.print(d);
      }
    }

    @Override
    public void print(float f) {
      if (isWritable()) {
        base.print(f);
      }
    }

    @Override
    public void print(int i) {
      if (isWritable()) {
        base.print(i);
      }
    }

    @Override
    public void print(long l) {
      if (isWritable()) {
        base.print(l);
      }
    }

    @Override
    public PrintWriter printf(Locale l, String format, Object... args) {
      if (isWritable()) {
        base.printf(l, format, args);
      }
      return this;
    }

    @Override
    public PrintWriter printf(String format, Object... args) {
      if (isWritable()) {
        base.printf(format, args);
      }
      return this;
    }

    @Override
    public void println() {
      if (isWritable()) {
        base.println();
      }
    }

    @Override
    public void println(Object x) {
      if (isWritable()) {
        base.println(x);
      }
    }

    /**
     * @implNote Since Javadoc API apparently doesn't expose its output context (that is,
     *           currently-generated documentation file path), we are forced to infer it from log
     *           entries (this is crucial, for example, on
     *           {@linkplain Taglet#toString(List, Element) tag rendering}, to build relative paths
     *           for linking).
     *           <p>
     *           <span class="important">IMPORTANT: As generated documentation file paths are logged
     *           at {@link Kind#NOTE} level, in order to intercept the corresponding log
     *           notifications the quiet mode MUST be suppressed (that is, the
     *           {@linkplain JadaConfig#OPTION__QUIET quiet option} MUST NOT be passed to the base
     *           doclet); once intercepted, the {@link #isWritable() writable} flag (which,
     *           conversely, is {@linkplain JadaConfig#setQuiet(boolean) aware of the quiet option})
     *           will be queried to decide whether to actually log or drop it.</span>
     *           </p>
     */
    @Override
    public void println(String x) {
      if (outputFileRegex == null) {
        //noinspection ConstantValue
        if (config.getJada().getEnv() != null) {
          Path overviewOutputFile = null;
          try {
            overviewOutputFile = config.getOverviewOutputFile();
          } catch (Exception ex) {
            /*
             * NOP: During doclet bootstrap, output paths cannot be resolved.
             */
          }
          if (overviewOutputFile != null) {
            outputFileRegex = Pattern.compile("("
                /*
                 * Documentation root path.
                 *
                 * NOTE: Documentation paths are assumed to be rooted at the output directory.
                 */
                + Pattern.quote(overviewOutputFile.getParent().toString())
                /*
                 * Documentation sub-path.
                 *
                 * NOTE: Documentation sub-paths (that is, under the output directory) are assumed
                 * not to have whitespace inside.
                 */
                + "\\S*?"
                + ")"
                /*
                 * Trailing waste.
                 *
                 * NOTE: In the log entry, the documentation path can be followed by whitespace or
                 * punctuation, or any other character, provided that it is separated from the path
                 * by whitespace.
                 */
                + "(?:[\\s\\p{Punct}]*|\\s+.+)$");
          }
        }
      }
      // Detect current documentation file path!
      if (outputFileRegex != null) {
        Matcher m = outputFileRegex.matcher(x);
        if (m.find()) {
          config.currentOutputFile = Paths.get(m.group(1));
        }
      }

      if (isWritable()) {
        base.println(x);
      }
    }

    @Override
    public void println(boolean x) {
      if (isWritable()) {
        base.println(x);
      }
    }

    @Override
    public void println(char x) {
      if (isWritable()) {
        base.println(x);
      }
    }

    @Override
    public void println(char[] x) {
      if (isWritable()) {
        base.println(x);
      }
    }

    @Override
    public void println(double x) {
      if (isWritable()) {
        base.println(x);
      }
    }

    @Override
    public void println(float x) {
      if (isWritable()) {
        base.println(x);
      }
    }

    @Override
    public void println(int x) {
      if (isWritable()) {
        base.println(x);
      }
    }

    @Override
    public void println(long x) {
      if (isWritable()) {
        base.println(x);
      }
    }

    @Override
    public void write(String s) {
      if (isWritable()) {
        base.write(s);
      }
    }

    @Override
    public void write(String s, int off, int len) {
      if (isWritable()) {
        base.write(s, off, len);
      }
    }

    @Override
    public void write(char[] buf) {
      if (isWritable()) {
        base.write(buf);
      }
    }

    @Override
    public void write(char[] buf, int off, int len) {
      if (isWritable()) {
        base.write(buf, off, len);
      }
    }

    @Override
    public void write(int c) {
      if (isWritable()) {
        base.write(c);
      }
    }

    private boolean isWritable() {
      return writable;
    }
  }

  /**
   * @see #isDebug()
   */
  public static final String OPTION__DEBUG =
      "--debug";
  /**
   * @see Jada#getBase() getJada().getBase()
   */
  public static final String OPTION__BASE_DOCLET =
      "--jada-doclet";
  /**
   * @see #getExtensions()
   */
  public static final String OPTION__DOCLET_EXTENSIONS =
      "--jada-exts";
  /**
   * @see #getExcludedOptimizationFiles()
   */
  public static final String OPTION__EXCLUDED_OPTIMIZATION_FILES =
      "--jada-file-optimize-exclude";
  /**
   * @see #isHelp()
   */
  public static final String OPTION__HELP =
      "--help";
  /**
   * @see #isHelp()
   */
  public static final String OPTION__HELP_EXTRA =
      "--help-extra";
  /**
   * @see #getInputEncoding()
   */
  public static final String OPTION__INPUT_ENCODING =
      "-encoding";
  /**
   * @see #getLogLevel()
   */
  public static final String OPTION__LOG_LEVEL =
      "--log-level";
  /**
   * @see #getOutputDirectory()
   */
  public static final String OPTION__OUTPUT_DIR =
      "-d";
  /**
   * @see #getOutputEncoding()
   */
  public static final String OPTION__OUTPUT_ENCODING =
      "-docencoding";
  /**
   * @see #isQuiet()
   */
  public static final String OPTION__QUIET =
      "-quiet";
  /**
   * @see #getResourceDirectories()
   */
  public static final String OPTION__RESOURCE_DIR =
      "--jada-dir";
  /**
   * Packages source path (short name).
   */
  public static final String OPTION__SOURCE_PATH =
      "-sourcepath";
  /**
   * Packages source path (long name).
   */
  public static final String OPTION__SOURCE_PATH__L =
      "--source-path";

  /**
   * @see #isVerbose()
   */
  public static final String OPTION__VERBOSE =
      "-verbose";

  /**
   * @see #isWarningRejected()
   */
  static final String OPTION__REJECT_WARNINGS =
      "-Xwerror";

  @SuppressWarnings("NotNullFieldNotInitialized")
  private List<Attachment> attachments;
  private @Nullable Path currentOutputFile;
  private boolean debug;
  private final List<String> excludedOptimizationFiles = new ArrayList<>();
  @SuppressWarnings("NotNullFieldNotInitialized")
  private Map<String, JadaExtension> extensions;
  private boolean help;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private Jada jada;
  private @Nullable LogWriterInterceptor logInterceptor;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private Map<String, JadaOperation<?>> operations;
  private @LazyNonNull @Nullable Path overviewOutputFile;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private Map<String, List<String>> pageContents;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private List<Path> resourceDirectories;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private Map<String, Taglet> taglets;
  private boolean warningRejected;

  /**
   * <span class="warning">(For internal use only)</span>
   */
  protected JadaConfig() {
  }

  protected JadaConfig(Jada jada, Locale locale, Reporter reporter) {
    super(locale, reporter);

    this.jada = jada;

    attachments = new ArrayList<>();
    resourceDirectories = new ArrayList<>();

    pageContents = new HashMap<>();
    {
      pageContents.put("head", new ArrayList<>());
      pageContents.put("body", new ArrayList<>());
    }

    extensions = new LinkedHashMap<>();
    operations = new LinkedHashMap<>();
    taglets = new HashMap<>();

    // Log interceptor.
    {
      String reporterFqn = fqn(reporter);
      WriterKind writerKind = switch (reporterFqn) {
        case "jdk.javadoc.internal.tool.Messager" ->
            /*
             * NOTE: JDK 11 writes diagnostic notes to NOTICE writer.
             */
            WriterKind.NOTICE;
        case "jdk.javadoc.internal.tool.JavadocLog" ->
            /*
             * NOTE: JDK 17 writes diagnostic notes to STDERR writer, despite what `JavadocLog` API
             * states ("javadoc API, and `Reporter` in particular, does not specify the use of
             * streams, and provides no support for identifying or specifying streams. JDK-8267204.
             * The current implementation/workaround is to write errors and warnings to error stream
             * and notes to output stream").
             */
            WriterKind.STDERR;
        default -> throw unexpected("Reporter type UNKNOWN: {}", reporterFqn);
      };

      var log = (Log) reporter;
      log.setWriter(writerKind, logInterceptor = new LogWriterInterceptor(log.getWriter(writerKind),
          this));
    }
  }

  /**
   * Adds a {@linkplain #getAttachments() file attachment}.
   */
  public JadaConfig addAttachment(Attachment attachment) {
    attachments.add(attachment);
    return this;
  }

  /**
   * Adds {@linkplain #getPageContents() page content} to the generated pages.
   * <p>
   * NOTE: Graphical contents, which require dedicated placement onto the page, should be added via
   * post-processing instead, for example:
   * </p>
   * <pre class="lang-java"><code>
   * getOperation(JadaFileProcess.class).addProcessor(new MyPostProcessor());</code></pre>
   * <p>
   * where {@code MyPostProcessor} stands in for a custom processor injecting content into processed
   * pages.
   * </p>
   *
   * @param location
   *          Either {@code "head"} or {@code "body"}.
   * @see #addScriptAttachment(Attachment)
   * @see #addStylesheetAttachment(Attachment)
   */
  public JadaConfig addPageContent(String location, String content) {
    List<String> contents = pageContents.get(location);
    if (contents == null)
      throw wrongArgOpt("location", location, null, pageContents.keySet());

    contents.add(content);
    return this;
  }

  /**
   * Adds a script attachment.
   *
   * @see #getAttachments() attachments
   */
  public JadaConfig addScriptAttachment(Attachment script) {
    addAttachment(script);
    addPageContent("head", """
        <script type="text/javascript" src="{@docRoot}/%s"></script>"""
        .formatted(script));
    return this;
  }

  /**
   * Adds a stylesheet attachment.
   *
   * @see #getAttachments() attachments
   */
  public JadaConfig addStylesheetAttachment(Attachment stylesheet) {
    addAttachment(stylesheet);
    addPageContent("head", """
        <link rel="stylesheet" type="text/css" href="{@docRoot}/%s" title="Style">"""
        .formatted(stylesheet));
    return this;
  }

  /**
   * Additional resources to attach to Javadoc output.
   *
   * @see #getResourceDirectories()
   */
  public List<Attachment> getAttachments() {
    return attachments;
  }

  @Override
  public JadaConfig getConfig() {
    return this;
  }

  /**
   * Latest documentation output file processed.
   *
   * @implNote Unfortunately, the {@link Doclet} architecture doesn't seem to provide a way to
   *           associate processed documentation elements to their respective output files: this is
   *           extremely inconvenient, especially to resolve relative links between files (such as
   *           on {@linkplain Taglet#toString(List, Element) tag rendering}). As a workaround, the
   *           diagnostic log is intercepted for paths of generated files, relaying them here.
   */
  public @Nullable Path getCurrentOutputFile() {
    return currentOutputFile;
  }

  /**
   * Paths of files excluded from {@linkplain org.pdfclown.jada.core.proc.FileOptimizer
   * optimization}.
   * <p>
   * Paths can be expressed as globs (with {@code '*'} and {@code '?'} wildcards), matched against
   * paths expressed as URIs to ensure a filesystem-independent format.
   * </p>
   * <p>
   * CLI option: {@value #OPTION__EXCLUDED_OPTIMIZATION_FILES} (repeatable)
   * </p>
   */
  public List<String> getExcludedOptimizationFiles() {
    return excludedOptimizationFiles;
  }

  @SuppressWarnings("unchecked")
  public <T extends JadaExtension> T getExtension(Class<T> type) {
    return (T) extensions.get(fqn(type));
  }

  /**
   * Selected doclet extensions.
   * <p>
   * CLI option: {@value #OPTION__DOCLET_EXTENSIONS}
   * </p>
   */
  public @UnmodifiableView Map<String, JadaExtension> getExtensions() {
    return unmodifiableMap(extensions);
  }

  /**
   * Source directories, which the doclet will scan to generate the documentation.
   */
  @Override
  public @UnmodifiableView List<Path> getInputDirectories() {
    return unmodifiableList(super.getInputDirectories());
  }

  /**
   * {@inheritDoc}
   * <p>
   * CLI option: {@value #OPTION__INPUT_ENCODING}
   * </p>
   */
  @Override
  public @Nullable String getInputEncoding() {
    return super.getInputEncoding();
  }

  @Override
  public Jada getJada() {
    return jada;
  }

  /**
   * {@inheritDoc}
   * <p>
   * CLI option: {@value #OPTION__LOG_LEVEL}
   * </p>
   */
  @Override
  public LogLevel getLogLevel() {
    return super.getLogLevel();
  }

  @SuppressWarnings("unchecked")
  public <T extends JadaOperation<?>> T getOperation(Class<T> type) {
    return (T) operations.get(fqn(type));
  }

  public Map<String, JadaOperation<?>> getOperations() {
    return operations;
  }

  /**
   * Destination directory, in which the doclet will generate the documentation.
   * <p>
   * CLI option: {@value #OPTION__OUTPUT_DIR}
   * </p>
   * <p>
   * Default: current directory.
   * </p>
   */
  @Override
  public Path getOutputDirectory() {
    return super.getOutputDirectory();
  }

  /**
   * {@inheritDoc}
   * <p>
   * CLI option: {@value #OPTION__OUTPUT_ENCODING}
   * </p>
   */
  @Override
  public @Nullable String getOutputEncoding() {
    return super.getOutputEncoding();
  }

  /**
   * Gets the documentation file path associated to the coordinates.
   */
  public Path getOutputFile(String fileName, String packageName, @Nullable String moduleName) {
    try {
      var fm = jada.getEnv().getJavaFileManager();

      JavaFileManager.Location location = moduleName != null
          ? fm.getLocationForModule(DocumentationTool.Location.DOCUMENTATION_OUTPUT, moduleName)
          : DocumentationTool.Location.DOCUMENTATION_OUTPUT;

      if (!fm.hasLocation(location))
        throw runtime("Location unavailable: {}", location);

      return Path.of(fm.getFileForOutput(location, packageName, fileName, null).toUri());
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Rendering format.
   * <p>
   * Corresponds to the filename extension without leading dot.
   * </p>
   *
   * @return {@code "html"}
   */
  public String getOutputFormat() {
    return "html";
  }

  /**
   * Gets the documentation page path associated to the element.
   */
  public Path getOutputPage(Element element) {
    /*
     * NOTE: Since the public Javadoc API doesn't expose `DocPath`-related facilities, we have to
     * hack our way to manually infer file paths associated to documented elements.
     */
    String moduleName;
    String packageName = null;
    String pageName = null;
    {
      Element container = element;
      if (element instanceof ModuleElement) {
        packageName = EMPTY;
        pageName = FILENAME__MODULE_SUMMARY;
      } else if (element instanceof PackageElement) {
        pageName = FILENAME__PACKAGE_SUMMARY;
      } else {
        container = LangModels.currentType(element);
      }

      FQName fqName = LangModels.fqName(container);
      moduleName = fqName.moduleName;
      if (packageName == null) {
        packageName = fqName.packageName;
      }
      if (pageName == null) {
        pageName = fqName.localName;
      }
    }
    assert pageName != null;
    assert packageName != null;
    return getOutputPage(pageName, packageName, moduleName);
  }

  /**
   * Gets the documentation page path associated to the coordinates.
   */
  public Path getOutputPage(String pageName, String packageName, @Nullable String moduleName) {
    return getOutputFile(pageName + DOT + getOutputFormat(), packageName, moduleName);
  }

  /**
   * Overview file path.
   */
  public Path getOverviewOutputFile() {
    if (overviewOutputFile == null) {
      overviewOutputFile = getOutputPage("index", EMPTY, null);
    }
    return overviewOutputFile;
  }

  /**
   * Page contents to insert into the generated pages.
   *
   * @see #addPageContent(String, String)
   */
  public Map<String, List<String>> getPageContents() {
    return pageContents;
  }

  /**
   * Gets the Jada resource for the name.
   * <p>
   * In case multiple instances are associated to the name, the first one is returned.
   * </p>
   *
   * @param name
   *          Resource name (that is, relative path under {@linkplain #getResourceDirectories()
   *          resource directories}).
   * @see #getResources(String)
   */
  public Optional<Path> getResource(String name) {
    return getResources(name).findFirst();
  }

  /**
   * Jada resource directories.
   * <p>
   * These resources are meant to be consumed during the javadoc tool execution.
   * </p>
   * <p>
   * Each resource directory is expected to contain these subdirectories:
   * </p>
   * <ul>
   * <li>{@code attach} — resources automatically attached to Javadoc output (additional resources
   * can be dynamically specified as {@linkplain #getAttachments() attachments}). Subdirectories:
   * <ul>
   * <li>{@code common} — resources common to any output</li>
   * <li>{@code java%JAVA_VERSION%} — resources bound to specific javadoc tool versions. Supported
   * version identifiers:
   * <ul>
   * <li>{@code java11} — for Java 11-16</li>
   * <li>{@code java17} — for Java 17+</li>
   * </ul>
   * </li>
   * </ul>
   * </li>
   * <li>{@code ext} — {@linkplain JadaExtension extension}-specific resources under respective
   * subdirectories {@linkplain JadaExtension#getName() named after the corresponding extension}
   * (for example, <code>ext/JadaBiblio</code> for JadaBiblio extension)</li>
   * <li>{@code scripts} — script hooks for Jada execution phases ({@code onMainProcess},
   * {@code onPostProcess}); their files are named after the corresponding phase (for example,
   * {@code onMainProcess.groovy}); their language has to be Groovy</li>
   * </ul>
   * <p>
   * Resources under these directories are identified by their relative paths (<b>resource
   * name</b>); as a consequence, <i>a resource name may match multiple resource instances, ordered
   * by decreasing priority corresponding to the position of their respective resource directory in
   * this list</i>. This definition provides sort of inheritance line, useful in case of shared
   * resource artifacts: depending on resource type-specific semantics, a higher-priority resource
   * instance may replace (for example, project logo image) or augment (for example, a collection of
   * bibliographic entries may expand a shared collection or even override its entries) a
   * lower-priority one; in the context of script hooks, the ancestor script can be invoked via
   * {@link JadaScriptContext#callSuper() self.callSuper()} method.
   * </p>
   * <p>
   * CLI option: {@value #OPTION__RESOURCE_DIR} (repeatable)
   * </p>
   *
   * @see #getAttachments()
   */
  public List<Path> getResourceDirectories() {
    return resourceDirectories;
  }

  /**
   * Gets the Jada resources for the name.
   *
   * @param name
   *          Resource name (that is, relative path under {@linkplain #getResourceDirectories()
   *          resource directories}).
   * @see #getResource(String)
   */
  public Stream<Path> getResources(String name) {
    return resourceDirectories.stream()
        .map($ -> $.resolve(name))
        .filter(Files::exists);
  }

  @SuppressWarnings("unchecked")
  public <T extends Taglet> T getTaglet(Class<T> type) {
    return (T) taglets.get(fqn(type));
  }

  /**
   * {@linkplain #registerTaglet(Taglet) Registered} taglets.
   */
  public @UnmodifiableView Map<String, Taglet> getTaglets() {
    return unmodifiableMap(taglets);
  }

  /**
   * Whether executing in debug mode.
   * <p>
   * Useful to generate diagnostic data and preserve temporary files which would be cleaned up on
   * normal execution.
   * </p>
   * <p>
   * CLI option: {@value #OPTION__DEBUG}
   * </p>
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * Whether the doclet synopsis is requested to display (instead of the regular documentation
   * generation).
   */
  public boolean isHelp() {
    return help;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This flag can be enabled only if {@link #isHelp() help} and {@link #isDebug() debug} are
   * disabled.
   * </p>
   * <p>
   * CLI option: {@value #OPTION__QUIET}
   * </p>
   */
  @Override
  public boolean isQuiet() {
    return super.isQuiet();
  }

  /**
   * {@inheritDoc}
   * <p>
   * This flag is locked by {@linkplain #isDebug() debug mode} (that is, whenever {@code debug} is
   * enabled, this is too).
   * </p>
   * <p>
   * CLI option: {@value #OPTION__VERBOSE}
   * </p>
   */
  @Override
  public boolean isVerbose() {
    return super.isVerbose();
  }

  /**
   * Registers a post-processing operation.
   */
  public JadaConfig registerOperation(JadaOperation<?> operation) {
    operations.put(fqn(operation), operation);
    operation.init(this);

    getLog().print(Kind.NOTE, this, JadaMessage.ELEMENT_REGISTERED, P__OPERATION,
        Logger.sourceName(operation));
    return this;
  }

  /**
   * Registers a taglet.
   */
  public JadaConfig registerTaglet(Taglet taglet) {
    taglets.put(fqn(taglet), taglet);
    return this;
  }

  /**
   * Sets {@link #isDebug() debug}.
   */
  public JadaConfig setDebug(boolean value) {
    debug = value;
    if (debug) {
      setVerbose(true);
    }
    return this;
  }

  /**
   * @implNote Functionalities like {@linkplain #isHelp() command synopsis display} or
   *           {@linkplain #isDebug() debug mode} are dependent on log level; as a consequence, such
   *           functionalities take priority over arbitrary level tuning, limiting its applicability
   *           — for leniency, inapplicable values are silently ignored.
   */
  @Override
  public SystemConfig setLogLevel(LogLevel value) {
    if ((!debug || value.compareTo(LOG_LEVEL__VERBOSE) <= 0 /*
                                                             * NOTE: In order to display all the
                                                             * messages for debugging, log level
                                                             * MUST be at most verbose
                                                             */)
        && (!help || value.compareTo(LOG_LEVEL__NORMAL) <= 0 /*
                                                              * NOTE: In order to let the javadoc
                                                              * command synopsis being emitted, log
                                                              * level MUST be at most normal
                                                              */)) {
      super.setLogLevel(value);

      if (logInterceptor != null) {
        logInterceptor.writable = !isQuiet();
      }
    }
    return this;
  }

  @Override
  protected Logger creareLogger(Reporter reporter) {
    return new JadaLogger(this, reporter);
  }

  void addExtension(JadaExtension e) {
    extensions.put(fqn(e), e);
  }

  void addInputDirectory(File e) {
    super.getInputDirectories().add(e.toPath());
  }

  /**
   * @implNote Resource directories are prioritized in inverse order of insertion (the later, the
   *           higher).
   */
  void addResourceDirectory(Path e) {
    resourceDirectories.add(0, normal(e));
  }

  void clearExtensions() {
    extensions.clear();
  }

  /**
   * Whether logged warnings are assimilated to errors, thus representing execution failure.
   */
  boolean isWarningRejected() {
    return warningRejected;
  }

  boolean removeExtension(JadaExtension e) {
    return extensions.remove(fqn(e)) != null;
  }

  /**
   * Sets {@link #isHelp() help}.
   *
   * @implNote In order to let the javadoc command synopsis being emitted, {@code help} has priority
   *           over {@link #isQuiet() quiet}: if {@code help} is requested, {@code quiet} is
   *           disabled.
   */
  void setHelp(boolean value) {
    help = value;
    if (help && isQuiet()) {
      setQuiet(false);
    }
  }

  /**
   * Sets {@link #isWarningRejected () warningRejected}.
   */
  void setWarningsRejected(boolean value) {
    warningRejected = value;
  }
}
