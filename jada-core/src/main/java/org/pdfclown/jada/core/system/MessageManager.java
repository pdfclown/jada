/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (MessageManager.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.system;

import static org.pdfclown.common.util.Chars.ANGLE_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Objects.fqn;
import static org.pdfclown.common.util.Strings.S;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.tools.Diagnostic.Kind;
import jdk.javadoc.doclet.StandardDoclet;
import org.jspecify.annotations.Nullable;

/**
 * Message manager.
 * <p>
 * Messages are based on {@link ResourceBundle}.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class MessageManager {
  /**
   * Placeholder for missing key, as defined by {@link StandardDoclet} implementation.
   */
  public static final String MISSING_KEY_PLACEHOLDER_PREFIX = "<MISSING KEY";

  private final SystemConfig config;

  public MessageManager(SystemConfig config) {
    this.config = config;
  }

  /**
   * Formats the message.
   *
   * @param message
   *          Message formatted according to {@link MessageFormat}.
   * @param args
   *          Message arguments. Special argument types:
   *          <ul>
   *          <li>{@link Message} — automatically resolved</li>
   *          <li>{@link Class} — converted to FQN</li>
   *          <li>{@link Throwable} — if last argument, its string representation is automatically
   *          appended to the format (DO NOT specify an explicit parameter placeholder in
   *          {@code message}!).</li>
   *          </ul>
   */
  public String format(String message, @Nullable Object... args) {
    // Arguments resolution.
    for (int i = 0; i < args.length; i++) {
      if (args[i] instanceof Message m) {
        args[i] = getText(m);
      } else if (args[i] instanceof Class) {
        args[i] = fqn(args[i]);
      }
    }

    // Message formatting.
    var ret = MessageFormat.format(message, args);
    if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
      ret += S + SPACE + ROUND_BRACKET_OPEN + args[args.length - 1] + ROUND_BRACKET_CLOSE;
    }
    return ret;
  }

  /**
   * Resolves the message.
   * <p>
   * The resource bundle is resolved according to the algorithm specified by
   * {@link ResourceBundle#getBundle(String, java.util.Locale, ClassLoader)
   * ResourceBundle.getBundle(..)}, where {@code baseName} is
   * {@code message}.{@link Message#getBundleName() getBundleName()}.
   * </p>
   * <p>
   * In case of missing resource entry, its key is returned without throwing exceptions (a warning
   * is logged instead), in order not to interfere with ongoing operations.
   * </p>
   * <p>
   * Message formatting follows {@link MessageFormat} conventions.
   * </p>
   *
   * @param message
   *          Message resource.
   * @param args
   *          Message arguments. Special argument types:
   *          <ul>
   *          <li>{@link Message} — automatically resolved</li>
   *          <li>{@link Class} — converted to FQN</li>
   *          <li>{@link Throwable} — if last argument, its string representation is automatically
   *          appended to the format (DO NOT specify an explicit parameter placeholder in
   *          {@code message}!).</li>
   *          </ul>
   * @return Resolved message, or message key ({@code <MISSING KEY:%key%>}) if resource missing.
   */
  public String getText(Message message, @Nullable Object... args) {
    return getText(message.getBundleName(), message.getKey(), args);
  }

  /**
   * Resolves the message corresponding to the coordinates.
   * <p>
   * The resource bundle is resolved according to the algorithm specified by
   * {@link ResourceBundle#getBundle(String, java.util.Locale, ClassLoader)
   * ResourceBundle.getBundle(..)}, where {@code baseName} is {@code bundleName}.
   * </p>
   * <p>
   * In case of missing resource entry, its key is returned without throwing exceptions (a warning
   * is logged instead), in order not to interfere with ongoing operations.
   * </p>
   * <p>
   * Message formatting follows {@link MessageFormat} conventions.
   * </p>
   *
   * @param bundleName
   *          Message resource bundle.
   * @param key
   *          Message resource key.
   * @param args
   *          Message arguments. Special argument types:
   *          <ul>
   *          <li>{@link Message} — automatically resolved</li>
   *          <li>{@link Class} — converted to FQN</li>
   *          <li>{@link Throwable} — if last argument, its string representation is automatically
   *          appended to the format (DO NOT specify an explicit parameter placeholder in
   *          {@code message}!).</li>
   *          </ul>
   * @return Resolved message, or message key ({@code <MISSING KEY:%key%>}) if resource missing.
   */
  public String getText(String bundleName, String key, @Nullable Object... args) {
    try {
      /*
       * NOTE: `ResourceBundle.getBundle(..)` caches returned instances.
       */
      var text = ResourceBundle.getBundle(bundleName, config.getLocale(),
          getClass().getClassLoader()).getString(key);
      return args.length > 0 ? format(text, args) : text;
    } catch (MissingResourceException ex) {
      /*
       * IMPORTANT: DO NOT use a message resource here (error loop hazard!).
       */
      config.getLog().print(Kind.WARNING, this, format("Missing resource key: {0}@{1}", key,
          bundleName));

      /*
       * IMPORTANT: DO NOT use curly braces ('{', '}') to wrap the key (they are special characters
       * conflicting with `MessageFormat`!).
       */
      return MISSING_KEY_PLACEHOLDER_PREFIX + COLON + key + ANGLE_BRACKET_CLOSE;
    }
  }
}
