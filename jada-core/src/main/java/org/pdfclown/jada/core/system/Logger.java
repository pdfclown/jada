/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Logger.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.system;

import static java.nio.file.Files.isDirectory;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_OPEN;
import static org.pdfclown.common.util.Objects.anyThat;
import static org.pdfclown.common.util.Objects.asType;
import static org.pdfclown.common.util.Objects.sqn;
import static org.pdfclown.common.util.Objects.xflat;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.function.Functions.to;
import static org.pdfclown.common.util.io.Files.basename;
import static org.pdfclown.common.util.reflect.Reflects.stackFrame;

import com.sun.source.util.DocTreePath;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Map;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import jdk.javadoc.doclet.Reporter;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.Objects;
import org.pdfclown.common.util.io.Files;
import org.pdfclown.common.util.reflect.Reflects;
import org.pdfclown.jada.core.JadaComponent;
import org.pdfclown.jada.core.internal.Internals;

/**
 * Logger.
 *
 * @author Stefano Chizzolini
 */
public class Logger implements Reporter {
  /**
   * Filter to exclude callers by FQN.
   * <p>
   * Callers not significant for users to logically locate log entries are excluded:
   * </p>
   * <ul>
   * <li>{@link Logger}</li>
   * <li>{@code java.*} packages</li>
   * <li>{@code jdk.*} packages</li>
   * <li>{@code *.util.*} packages</li>
   * </ul>
   */
  private static final Pattern PATTERN__CALLER_EXCLUSION_FILTER = Pattern.compile(
      Logger.class.getName() + "|^java|^jdk|util|Logger");

  /**
   * Gets the name of a log source, qualified with containing component name.
   *
   * @param source
   *          Log source.
   * @return {@biblio.spec W3C-EBNF}: <pre class="lang-ebnf"><code>
   * SourceName ::= ('[' ComponentName '] ')? SourceSimpleName?</code></pre>
   *         <p>
   *         where:
   *         </p>
   *         <ul>
   *         <li>{@code ComponentName} — {@linkplain JadaComponent#getName() Name of the Jada
   *         component} {@code source} belongs to (if any), otherwise name of the artifact
   *         containing {@code source}</li>
   *         <li>{@code SourceSimpleName} — Name of the log source, corresponding to
   *         {@code source.getName()} (if available), otherwise {@code source}
   *         {@linkplain Objects#sqn(Object) simply-qualified class name}</li>
   *         </ul>
   */
  public static String sourceName(@Nullable Object source) {
    return sourceName(source, false);
  }

  /**
   * Gets the name of a log source, qualified with containing component name and call stack
   * reference.
   *
   * @param source
   *          Log source.
   * @param callerIncluded
   *          Whether also the call stack source has to be included in the name.
   * @return {@biblio.spec W3C-EBNF}: <pre class="lang-ebnf"><code>
   * SourceName ::= ('[' ComponentName '] ')? (SourceSimpleName (' (' StackCallerName ')')?)?</code></pre>
   *         <p>
   *         where:
   *         </p>
   *         <ul>
   *         <li>{@code ComponentName} — {@linkplain JadaComponent#getName() Name of the Jada
   *         component} {@code source} belongs to (if any), otherwise name of the artifact
   *         containing {@code source}</li>
   *         <li>{@code SourceSimpleName} — Name of the log source, corresponding to
   *         {@code source.getName()} (if available), otherwise {@code source}
   *         {@linkplain Objects#sqn(Object) simply-qualified class name}</li>
   *         <li>{@code StackCallerName} — Name of the class containing the log call (it differs
   *         from {@code SourceSimpleName} if {@code source} is an instance of a derived class)</li>
   *         </ul>
   */
  protected static String sourceName(@Nullable Object source, boolean callerIncluded) {
    final String sourceSimpleName;
    final String componentName;
    if (source != null) {
      var sourceType = asType(xflat(source));
      assert sourceType != null /* PolyNull */;

      String sourceSqn = sqn(sourceType);
      sourceSimpleName = requireNonNullElse(Reflects.tryGet(source, "getName"), sourceSqn);

      var fqn = sourceType.getName();
      componentName = Internals.getComponentNames().entrySet().stream()
          .filter($ -> fqn.startsWith($.getKey()))
          .map(Map.Entry::getValue)
          .findFirst().orElseGet(() -> {
            Path location = to(sourceType.getProtectionDomain().getCodeSource().getLocation(),
                Files::path);
            if (location == null)
              return null;
            else if (isDirectory(location)) {
              while (anyThat(location.getFileName().toString(), java.util.Objects::equals, "target",
                  "classes")) {
                location = location.getParent();
              }
              return location.getFileName().toString();
            } else {
              return basename(location);
            }
          });
    } else {
      sourceSimpleName = null;
      componentName = null;
    }

    String callerName = callerIncluded ? stackFrame(
        $ -> !PATTERN__CALLER_EXCLUSION_FILTER.matcher($.getClassName()).find())
            .map($ -> sqn($.getClassName()))
            .filter($ -> !$.equals(sourceSimpleName))
            .orElse(null)
        : null;

    if (sourceSimpleName == null && callerName == null)
      return EMPTY;

    var b = new StringBuilder();
    if (componentName != null) {
      b.append(SQUARE_BRACKET_OPEN).append(componentName).append(SQUARE_BRACKET_CLOSE)
          .append(SPACE);
    }
    if (sourceSimpleName != null) {
      b.append(sourceSimpleName);
      if (callerName != null) {
        b.append(SPACE).append(ROUND_BRACKET_OPEN);
      }
    }
    if (callerName != null) {
      b.append(callerName);
      if (sourceSimpleName != null) {
        b.append(ROUND_BRACKET_CLOSE);
      }
    }
    return b.toString();
  }

  @SuppressWarnings("NotNullFieldNotInitialized")
  protected SystemConfig config;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private Reporter base;

  private boolean enabled = true;

  public Logger(SystemConfig config, Reporter base) {
    this.config = requireNonNull(config, "`config`");
    this.base = requireNonNull(base, "`base`");
  }

  /**
   * <span class="warning">(For internal use only)</span>
   */
  protected Logger() {
  }

  public Reporter getBase() {
    return base;
  }

  @Override
  public @Nullable PrintWriter getDiagnosticWriter() {
    return base.getDiagnosticWriter();
  }

  @Override
  public @Nullable PrintWriter getStandardWriter() {
    return base.getStandardWriter();
  }

  /**
   * Whether the log entries are printed.
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Logs a message.
   *
   * @param kind
   *          Message kind.
   * @param path
   *          Reported path.
   * @param source
   *          Log source.
   * @param message
   *          Message formatted according to {@link MessageFormat}. For simplicity, generic argument
   *          placeholders (<code>{}</code>) can be used instead of indexed ones.
   * @param args
   *          Message arguments. Special argument types:
   *          <ul>
   *          <li>{@link Message} — automatically resolved</li>
   *          <li>{@link Class} — converted to FQN</li>
   *          <li>{@link Throwable} — if last argument, its string representation is automatically
   *          appended to the format (DO NOT specify an explicit argument placeholder in
   *          {@code message}!).</li>
   *          </ul>
   */
  public final void print(Kind kind, DocTreePath path, @Nullable Object source, Message message,
      Object... args) {
    print(kind, path, source, format(message, args));
  }

  /**
   * Logs a message.
   *
   * @param kind
   *          Message kind.
   * @param path
   *          Reported path.
   * @param source
   *          Log source.
   * @param message
   *          Message to print.
   */
  public void print(Kind kind, DocTreePath path, @Nullable Object source, String message) {
    if (!isPrintable(kind))
      return;

    kind = checkKind(kind);

    base.print(kind, path, prepareMessage(source, message));

    onLogEvent(kind);
  }

  /**
   * Logs a message.
   *
   * @param kind
   *          Message kind.
   * @param path
   *          Reported path.
   * @param source
   *          Log source.
   * @param message
   *          Message formatted according to {@link MessageFormat}. For simplicity, generic argument
   *          placeholders (<code>{}</code>) can be used instead of indexed ones.
   * @param args
   *          Message arguments. Special argument types:
   *          <ul>
   *          <li>{@link Message} — automatically resolved</li>
   *          <li>{@link Class} — converted to FQN</li>
   *          <li>{@link Throwable} — if last argument, its string representation is automatically
   *          appended to the format (DO NOT specify an explicit argument placeholder in
   *          {@code message}!).</li>
   *          </ul>
   */
  public final void print(Kind kind, DocTreePath path, @Nullable Object source, String message,
      Object... args) {
    print(kind, path, source, format(message, args));
  }

  @Override
  public final void print(Kind kind, DocTreePath path, String message) {
    print(kind, path, null, message);
  }

  /**
   * Logs a message.
   *
   * @param kind
   *          Message kind.
   * @param element
   *          Reported element.
   * @param source
   *          Log source.
   * @param message
   *          Message formatted according to {@link MessageFormat}. For simplicity, generic argument
   *          placeholders (<code>{}</code>) can be used instead of indexed ones.
   * @param args
   *          Message arguments. Special argument types:
   *          <ul>
   *          <li>{@link Message} — automatically resolved</li>
   *          <li>{@link Class} — converted to FQN</li>
   *          <li>{@link Throwable} — if last argument, its string representation is automatically
   *          appended to the format (DO NOT specify an explicit argument placeholder in
   *          {@code message}!).</li>
   *          </ul>
   */
  public final void print(Kind kind, Element element, @Nullable Object source, Message message,
      Object... args) {
    print(kind, element, source, format(message, args));
  }

  /**
   * Logs a message.
   *
   * @param kind
   *          Message kind.
   * @param element
   *          Reported element.
   * @param source
   *          Log source.
   * @param message
   *          Message to print.
   */
  public void print(Kind kind, Element element, @Nullable Object source, String message) {
    if (!isPrintable(kind))
      return;

    kind = checkKind(kind);

    base.print(kind, element, prepareMessage(source, message));

    onLogEvent(kind);
  }

  /**
   * Logs a message.
   *
   * @param kind
   *          Message kind.
   * @param element
   *          Reported element.
   * @param source
   *          Log source.
   * @param message
   *          Message formatted according to {@link MessageFormat}. For simplicity, generic argument
   *          placeholders (<code>{}</code>) can be used instead of indexed ones.
   * @param args
   *          Message arguments. Special argument types:
   *          <ul>
   *          <li>{@link Message} — automatically resolved</li>
   *          <li>{@link Class} — converted to FQN</li>
   *          <li>{@link Throwable} — if last argument, its string representation is automatically
   *          appended to the format (DO NOT specify an explicit argument placeholder in
   *          {@code message}!).</li>
   *          </ul>
   */
  public final void print(Kind kind, Element element, @Nullable Object source, String message,
      Object... args) {
    print(kind, element, source, format(message, args));
  }

  @Override
  public final void print(Kind kind, Element element, String message) {
    print(kind, element, null, message);
  }

  /**
   * Logs a message.
   *
   * @param kind
   *          Message kind.
   * @param file
   *          Reported file.
   * @param start
   *          Start position of the reported file content.
   * @param pos
   *          Current position of the reported file content.
   * @param end
   *          End position of the reported file content.
   * @param source
   *          Log source.
   * @param message
   *          Message formatted according to {@link MessageFormat}. For simplicity, generic argument
   *          placeholders (<code>{}</code>) can be used instead of indexed ones.
   * @param args
   *          Message arguments. Special argument types:
   *          <ul>
   *          <li>{@link Message} — automatically resolved</li>
   *          <li>{@link Class} — converted to FQN</li>
   *          <li>{@link Throwable} — if last argument, its string representation is automatically
   *          appended to the format (DO NOT specify an explicit argument placeholder in
   *          {@code message}!).</li>
   *          </ul>
   */
  public final void print(Kind kind, FileObject file, int start, int pos, int end,
      @Nullable Object source, Message message, Object... args) {
    print(kind, file, start, pos, end, source, format(message, args));
  }

  /**
   * Logs a message.
   *
   * @param kind
   *          Message kind.
   * @param file
   *          Reported file.
   * @param start
   *          Start position of the reported file content.
   * @param pos
   *          Current position of the reported file content.
   * @param end
   *          End position of the reported file content.
   * @param source
   *          Log source.
   * @param message
   *          Message.
   */
  public void print(Kind kind, FileObject file, int start, int pos, int end,
      @Nullable Object source, String message) {
    if (!isPrintable(kind))
      return;

    kind = checkKind(kind);
    message = prepareMessage(source, message);

    base.print(kind, file, start, pos, end, message);

    onLogEvent(kind);
  }

  /**
   * Logs a message.
   *
   * @param kind
   *          Message kind.
   * @param file
   *          Reported file.
   * @param start
   *          Start position of the reported file content.
   * @param pos
   *          Current position of the reported file content.
   * @param end
   *          End position of the reported file content.
   * @param source
   *          Log source.
   * @param message
   *          Message formatted according to {@link MessageFormat}. For simplicity, generic argument
   *          placeholders (<code>{}</code>) can be used instead of indexed ones.
   * @param args
   *          Message arguments. Special argument types:
   *          <ul>
   *          <li>{@link Message} — automatically resolved</li>
   *          <li>{@link Class} — converted to FQN</li>
   *          <li>{@link Throwable} — if last argument, its string representation is automatically
   *          appended to the format (DO NOT specify an explicit argument placeholder in
   *          {@code message}!).</li>
   *          </ul>
   */
  public final void print(Kind kind, FileObject file, int start, int pos, int end,
      @Nullable Object source, String message, Object... args) {
    print(kind, file, start, pos, end, source, format(message, args));
  }

  @Override
  public final void print(Kind kind, FileObject file, int start, int pos, int end, String message) {
    print(kind, file, start, pos, end, null, message);
  }

  /**
   * Logs a message.
   *
   * @param kind
   *          Message kind.
   * @param source
   *          Log source.
   * @param message
   *          Message formatted according to {@link MessageFormat}. For simplicity, generic argument
   *          placeholders (<code>{}</code>) can be used instead of indexed ones.
   * @param args
   *          Message arguments. Special argument types:
   *          <ul>
   *          <li>{@link Message} — automatically resolved</li>
   *          <li>{@link Class} — converted to FQN</li>
   *          <li>{@link Throwable} — if last argument, its string representation is automatically
   *          appended to the format (DO NOT specify an explicit argument placeholder in
   *          {@code message}!).</li>
   *          </ul>
   */
  public final void print(Kind kind, @Nullable Object source, Message message, Object... args) {
    print(kind, source, format(message, args));
  }

  /**
   * Logs a message.
   *
   * @param kind
   *          Message kind.
   * @param source
   *          Log source.
   * @param message
   *          Message to print.
   */
  public void print(Kind kind, @Nullable Object source, String message) {
    if (!isPrintable(kind))
      return;

    kind = checkKind(kind);

    base.print(kind, prepareMessage(source, message));

    onLogEvent(kind);
  }

  /**
   * Logs a message.
   *
   * @param kind
   *          Message kind.
   * @param source
   *          Log source.
   * @param message
   *          Message formatted according to {@link MessageFormat}. For simplicity, generic argument
   *          placeholders (<code>{}</code>) can be used instead of indexed ones.
   * @param args
   *          Message arguments. Special argument types:
   *          <ul>
   *          <li>{@link Message} — automatically resolved</li>
   *          <li>{@link Class} — converted to FQN</li>
   *          <li>{@link Throwable} — if last argument, its string representation is automatically
   *          appended to the format (DO NOT specify an explicit argument placeholder in
   *          {@code message}!).</li>
   *          </ul>
   */
  public final void print(Kind kind, @Nullable Object source, String message, Object... args) {
    print(kind, source, format(message, args));
  }

  @Override
  public final void print(Kind kind, String message) {
    print(kind, (Object) null, message);
  }

  /**
   * Sets {@link #isEnabled() enabled}.
   */
  public void setEnabled(boolean value) {
    enabled = value;
  }

  /**
   * Checks the diagnostic type.
   *
   * @param kind
   *          Message kind.
   * @implNote Unfortunately, {@code com.sun.tools.javac.util.Log.DefaultDiagnosticHandler} doesn't
   *           support {@link Kind#OTHER} (OpenJDK 17); this method ensures safe alternatives to
   *           such cases.
   */
  protected Kind checkKind(Kind kind) {
    return kind == Kind.OTHER ? Kind.NOTE : kind;
  }

  /**
   * Formats a {@linkplain MessageFormat parameterized} message.
   *
   * @param message
   *          Message (either {@link String} (literal) or {@link Message} (resource)). For
   *          simplicity, generic argument placeholders (<code>{}</code>) can be used instead of
   *          indexed ones.
   * @param args
   *          Message arguments. Special argument types:
   *          <ul>
   *          <li>{@link Message} — automatically resolved</li>
   *          <li>{@link Class} — converted to FQN</li>
   *          <li>{@link Throwable} — if last argument, its string representation is automatically
   *          appended to the format (DO NOT specify an explicit argument placeholder in
   *          {@code message}!).</li>
   *          </ul>
   */
  protected String format(Object message, Object... args) {
    return message instanceof Message m
        ? m.toString(config, args)
        : config.getMessageManager().format((String) message, args);
  }

  /**
   * Gets whether the given message kind is loggable.
   *
   * @param kind
   *          Message kind.
   * @implNote Despite the default {@link Reporter} implementation
   *           ({@code jdk.javadoc.internal.tool.JavadocLog}) supports logging thresholds (see
   *           {@code com.sun.tools.javac.util.Log.DefaultDiagnosticHandler}), apparently that
   *           mechanism doesn't work (OpenJDK 17 — all entries are logged, no matter whether
   *           {@code -quiet} or {@code -verbose} CLI options are set); as a consequence, this
   *           method enforces it.
   */
  protected boolean isPrintable(Kind kind) {
    return enabled && kind.compareTo(config.getLogLevel().getCode()) <= 0;
  }

  protected void onLogEvent(Kind kind) {
  }

  /**
   * Gets the qualified message.
   *
   * @param source
   *          Log source.
   * @param message
   *          Raw message.
   * @return {@biblio.spec W3C-EBNF}: <pre class="lang-ebnf"><code>
   * QualifiedMessage ::= (SourceName ': ')? Message</code></pre>
   *         <p>
   *         where:
   *         </p>
   *         <ul>
   *         <li>{@code SourceName} — {@linkplain #sourceName(Object, boolean) Qualified source
   *         name}</li>
   *         <li>{@code Message} — Corresponds to {@code message}</li>
   *         </ul>
   */
  protected String prepareMessage(@Nullable Object source, String message) {
    String sourceName = sourceName(source, true);
    return !sourceName.isEmpty() ? sourceName + COLON + SPACE + message : message;
  }
}
