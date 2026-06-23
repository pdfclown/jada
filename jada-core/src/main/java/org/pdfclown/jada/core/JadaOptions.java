/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaOptions.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import static java.lang.String.join;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.jada.core.system.MessageManager.MISSING_KEY_PLACEHOLDER_PREFIX;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import jdk.javadoc.doclet.Doclet.Option;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.ArgumentException;
import org.pdfclown.common.util.annot.Immutable;
import org.pdfclown.jada.core.Jada.JadaCandidate;

/**
 * {@link Jada} component options builder.
 *
 * @author Stefano Chizzolini
 */
public class JadaOptions {
  @Immutable
  static class JadaOption implements Option {
    private static boolean isTextValid(String text) {
      return !text.isEmpty() && !text.startsWith(MISSING_KEY_PLACEHOLDER_PREFIX);
    }

    /**
     * Gets valid text.
     *
     * @return {@code text}, if neither empty nor missing, otherwise {@code defaultText}.
     */
    private static String text(String text, String defaultText) {
      return isTextValid(text) ? text : defaultText;
    }

    private final int argumentCount;
    private final @Nullable Option base;
    private final String description;
    private final Kind kind;
    private final List<String> names;
    private final String ownerTags;
    private final String parameters;
    private final BiFunction<String, List<String>, Boolean> processor;

    /**
     * @param owner
     *          Owner component name.
     * @param base
     *          Overridden option.
     * @param names
     *          Option names (first is the default one).
     * @param argumentCount
     *          Number of arguments expected.
     * @param kind
     *          Option kind:
     *          <ul>
     *          <li>{@link Option.Kind#STANDARD STANDARD} — Regular (standard) option associated to
     *          {@link #getOwner() owner} (it appears in the help synopsis)</li>
     *          <li>{@link Option.Kind#EXTENDED EXTENDED} — Extra (non-standard) option associated
     *          to {@link #getOwner() owner} (it appears in the help-extra synopsis)</li>
     *          <li>{@link Option.Kind#OTHER OTHER} — Hidden (implementation-reserved) option
     *          associated to {@link #getOwner() owner} (it doesn't appear in any synopsis)</li>
     *          </ul>
     * @param processor
     *          Option assignment function.
     * @param description
     *          Synoptic description of the option (empty, if {@link Option.Kind#OTHER OTHER}
     *          {@code kind}).
     * @param parameters
     *          Synoptic representation of the option parameters (empty, if {@link Option.Kind#OTHER
     *          OTHER} {@code kind}).
     */
    public JadaOption(String owner, @Nullable JadaOption base, List<String> names,
        int argumentCount, Kind kind, BiFunction<String, List<String>, Boolean> processor,
        String description, String parameters) {
      this.ownerTags = "[%s] %s".formatted(owner, base != null ? base.ownerTags : EMPTY);
      this.base = base;
      this.names = names;
      this.argumentCount = argumentCount;
      this.kind = kind;
      this.processor = processor;
      this.description = text(base != null ? base.description : EMPTY, description);
      this.parameters = text(base != null ? base.parameters : EMPTY, parameters);
    }

    /**
     * @implNote Marked as final to enforce equivalence symmetry.
     */
    @Override
    public final boolean equals(@Nullable Object o) {
      return o == this || (o instanceof Option that
          && optionName(that.getNames()).equals(optionName(this.getNames())));
    }

    @Override
    public int getArgumentCount() {
      return argumentCount;
    }

    /**
     * @implNote The description is preceded by the owners of this option.
     */
    @Override
    public String getDescription() {
      return ownerTags + description;
    }

    @Override
    public Kind getKind() {
      return kind;
    }

    @Override
    public List<String> getNames() {
      return names;
    }

    @Override
    public String getParameters() {
      return parameters;
    }

    /**
     * @implNote Marked as final to enforce equivalence symmetry.
     */
    @Override
    public final int hashCode() {
      return Objects.hashCode(names);
    }

    @Override
    public boolean process(String opt, List<String> args) {
      return !processor.apply(opt, args) || base == null || base.process(opt, args);
    }

    @Override
    public String toString() {
      return join(",", names);
    }
  }

  private static class OptionException extends ArgumentException {
    @Serial
    private static final long serialVersionUID = 1L;

    public OptionException(String optionName, String optionValue, String description,
        Throwable cause) {
      super(optionName, optionValue, "INVALID -- " + description, cause);
    }
  }

  private static String optionName(List<String> names) {
    return names.get(0);
  }

  private final JadaConfig config;
  private final TreeMap<String, JadaOption> options = new TreeMap<>();
  private @Nullable JadaCandidate<?> owner;

  public JadaOptions(JadaConfig config) {
    this.config = config;
  }

  /**
   * Adds a custom option.
   * <p>
   * The option description is resolved according to {@link #getText(String, String, Object...)
   * getText(..)}, where {@code optionName} corresponds to {@code names[0]}, and {@code subKey} is
   * empty.
   * </p>
   *
   * @param names
   *          List of names that may be used to identify the option.
   * @param parameters
   *          Synoptic representation of the option parameters.
   * @param processor
   *          Consumes the option.
   * @return Self.
   */
  public JadaOptions add(List<String> names, List<String> parameters,
      Consumer<List<String>> processor) {
    return add(names, Option.Kind.STANDARD, parameters, processor);
  }

  /**
   * Adds a custom option.
   * <p>
   * The option description is resolved according to {@link #getText(String, String, Object...)
   * getText(..)}, where {@code optionName} corresponds to {@code names[0]}, and {@code subKey} is
   * empty.
   * </p>
   *
   * @param names
   *          List of names that may be used to identify the option.
   * @param kind
   *          Option kind:
   *          <ul>
   *          <li>{@link Option.Kind#STANDARD STANDARD} — Regular (standard) option associated to
   *          {@link #getOwner() owner} (it appears in the help synopsis)</li>
   *          <li>{@link Option.Kind#EXTENDED EXTENDED} — Extra (non-standard) option associated to
   *          {@link #getOwner() owner} (it appears in the help-extra synopsis)</li>
   *          <li>{@link Option.Kind#OTHER OTHER} — Hidden (implementation-reserved) option
   *          associated to {@link #getOwner() owner} (it doesn't appear in any synopsis)</li>
   *          </ul>
   * @param parameters
   *          Synoptic representation of the option parameters.
   * @param processor
   *          Consumes the option.
   * @return Self.
   */
  public JadaOptions add(List<String> names, Option.Kind kind, List<String> parameters,
      Consumer<List<String>> processor) {
    var optionName = optionName(names);
    var overriddenOption = options.get(optionName);
    return add(names,
        kind,
        getText(
            /*
             * NOTE: In case of hidden or overridden option, the description is not required (in the
             * former case, the option doesn't appear in the synopsis, whilst in the latter the
             * description is inherited from the overridden option).
             */
            kind != Option.Kind.OTHER && (overriddenOption == null
                || !JadaOption.isTextValid(overriddenOption.description)),
            optionName, EMPTY),
        parameters, processor);
  }

  /**
   * Adds a custom option.
   *
   * @param names
   *          List of names that may be used to identify the option.
   * @param kind
   *          Option kind:
   *          <ul>
   *          <li>{@link Option.Kind#STANDARD STANDARD} — Regular (standard) option associated to
   *          {@link #getOwner() owner} (it appears in the help synopsis)</li>
   *          <li>{@link Option.Kind#EXTENDED EXTENDED} — Extra (non-standard) option associated to
   *          {@link #getOwner() owner} (it appears in the help-extra synopsis)</li>
   *          <li>{@link Option.Kind#OTHER OTHER} — Hidden (implementation-reserved) option
   *          associated to {@link #getOwner() owner} (it doesn't appear in any synopsis)</li>
   *          </ul>
   * @param description
   *          Synoptic description of the option.
   * @param parameters
   *          Synoptic representation of the option parameters.
   * @param processor
   *          Consumes the option.
   * @return Self.
   */
  public JadaOptions add(List<String> names, Option.Kind kind, String description,
      List<String> parameters, Consumer<List<String>> processor) {
    return doAdd(names, parameters.size(), kind, ($opt, $args) -> {
      try {
        processor.accept($args);
        return true;
      } catch (OptionException ex) {
        /*
         * DO NOT REMOVE (this catch is purposely declared to intercept option exceptions already
         * thrown by overridden options, avoiding to recursively nest them).
         */
        throw ex;
      } catch (Exception ex) {
        throw new OptionException(optionName(names),
            join(S + SPACE, $args.subList(0, parameters.size())), description, ex);
      }
    }, description, String.join(S + SPACE, parameters));
  }

  /**
   * Adds a custom option.
   *
   * @param names
   *          List of names that may be used to identify the option.
   * @param description
   *          Synoptic description of the option.
   * @param parameters
   *          Synoptic representation of the option parameters.
   * @param processor
   *          Consumes the option.
   * @return Self.
   */
  public JadaOptions add(List<String> names, String description, List<String> parameters,
      Consumer<List<String>> processor) {
    return add(names, Option.Kind.STANDARD, description, parameters, processor);
  }

  /**
   * Adds a custom option.
   * <p>
   * The option description is resolved according to {@link #getText(String, String, Object...)
   * getText(..)}, where {@code optionName} corresponds to {@code names[0]}, and {@code subKey} is
   * empty.
   * </p>
   *
   * @param name
   *          Name used to identify the option.
   * @param parameters
   *          Synoptic representation of the option parameters.
   * @param processor
   *          Consumes the option.
   * @return Self.
   */
  public JadaOptions add(String name, List<String> parameters,
      Consumer<List<String>> processor) {
    return add(name, Option.Kind.STANDARD, parameters, processor);
  }

  /**
   * Adds a custom option.
   * <p>
   * The option description is resolved according to {@link #getText(String, String, Object...)
   * getText(..)}, where {@code optionName} corresponds to {@code names[0]}, and {@code subKey} is
   * empty.
   * </p>
   *
   * @param name
   *          Name used to identify the option.
   * @param kind
   *          Option kind:
   *          <ul>
   *          <li>{@link Option.Kind#STANDARD STANDARD} — Regular (standard) option associated to
   *          {@link #getOwner() owner} (it appears in the help synopsis)</li>
   *          <li>{@link Option.Kind#EXTENDED EXTENDED} — Extra (non-standard) option associated to
   *          {@link #getOwner() owner} (it appears in the help-extra synopsis)</li>
   *          <li>{@link Option.Kind#OTHER OTHER} — Hidden (implementation-reserved) option
   *          associated to {@link #getOwner() owner} (it doesn't appear in any synopsis)</li>
   *          </ul>
   * @param parameters
   *          Synoptic representation of the option parameters.
   * @param processor
   *          Consumes the option.
   * @return Self.
   */
  public JadaOptions add(String name, Option.Kind kind, List<String> parameters,
      Consumer<List<String>> processor) {
    return add(List.of(name), kind, parameters, processor);
  }

  /**
   * Adds a custom option.
   *
   * @param name
   *          Name used to identify the option.
   * @param kind
   *          Option kind:
   *          <ul>
   *          <li>{@link Option.Kind#STANDARD STANDARD} — Regular (standard) option associated to
   *          {@link #getOwner() owner} (it appears in the help synopsis)</li>
   *          <li>{@link Option.Kind#EXTENDED EXTENDED} — Extra (non-standard) option associated to
   *          {@link #getOwner() owner} (it appears in the help-extra synopsis)</li>
   *          <li>{@link Option.Kind#OTHER OTHER} — Hidden (implementation-reserved) option
   *          associated to {@link #getOwner() owner} (it doesn't appear in any synopsis)</li>
   *          </ul>
   * @param description
   *          Option description.
   * @param parameters
   *          Parameters description.
   * @param processor
   *          Consumes the option.
   * @return Self.
   */
  public JadaOptions add(String name, Option.Kind kind, String description,
      List<String> parameters, Consumer<List<String>> processor) {
    return add(List.of(name), kind, description, parameters, processor);
  }

  /**
   * Adds a custom option.
   *
   * @param name
   *          Name used to identify the option.
   * @param description
   *          Option description.
   * @param parameters
   *          Parameters description.
   * @param processor
   *          Consumes the option.
   * @return Self.
   */
  public JadaOptions add(String name, String description, List<String> parameters,
      Consumer<List<String>> processor) {
    return add(name, Option.Kind.STANDARD, description, parameters, processor);
  }

  /**
   * Component whose options this builder is collecting.
   */
  public JadaCandidate<?> getOwner() {
    return requireNonNull(owner);
  }

  /**
   * Resolves the string resource associated to the coordinates.
   * <p>
   * Useful in case a resource string expects arguments; in any other case, there is no need to call
   * this method, since {@code add(..)} overloads support bundled resources natively.
   * </p>
   * <h4>Resolution</h4>
   * <p>
   * The resource bundle is resolved according to the algorithm specified by
   * {@link ResourceBundle#getBundle(String, java.util.Locale, ClassLoader)
   * ResourceBundle.getBundle(..)}, where {@code baseName} is {@link #getOwner() owner}'s FQN.
   * </p>
   * <p>
   * The key associated to the string resource is {@code "%optionName%"} in case of empty
   * {@code subKey}, {@code "%optionName%__%subKey%"} in any other case.
   * </p>
   *
   * @param optionName
   *          Option name.
   * @param subKey
   *          Key suffix of the string resource.
   * @param args
   *          Arguments applied to the string resource.
   */
  public String getText(String optionName, String subKey, Object... args) {
    return getText(true, optionName, subKey, args);
  }

  /**
   * Adds third-party options.
   *
   * @return Self.
   */
  JadaOptions addAll(Collection<? extends Option> options) {
    options.forEach($ -> doAdd($.getNames(), $.getArgumentCount(), $.getKind(), $::process,
        $.getDescription(), $.getParameters()));
    return this;
  }

  Set<? extends Option> build() {
    Set<Option> ret = new TreeSet<>(comparing($ -> optionName($.getNames())));
    ret.addAll(this.options.values());
    return ret;
  }

  /**
   * Sets {@link #getOwner() owner}.
   *
   * @return Self.
   */
  JadaOptions setOwner(JadaCandidate<?> value) {
    owner = value;
    return this;
  }

  private JadaOptions doAdd(List<String> names, int argumentCount, Option.Kind kind,
      BiFunction<String, List<String>, Boolean> processor, String description, String parameters) {
    var optionName = optionName(names);
    options.put(optionName, new JadaOption(getOwner().getName(), options.remove(optionName), names,
        argumentCount, kind, processor, description, parameters));
    return this;
  }

  /**
   * @param required
   *          Whether the text resource is required. If not required, its absence isn't logged in
   *          order not to pollute the output with unnecessary noise.
   * @return Resolved message, or message key ({@code <MISSING KEY:%key%>}) if resource missing.
   */
  private String getText(boolean required, String optionName, String subKey, Object... args) {
    var bundleName = getOwner().getType().getName();
    String key;
    {
      var b = new StringBuilder(optionName);
      if (!subKey.isEmpty()) {
        b.append("__").append(subKey);
      }
      key = b.toString();
    }
    try {
      if (!required) {
        config.getLog().setEnabled(false);
      }
      return config.getMessageManager().getText(bundleName, key, args);
    } finally {
      if (!required) {
        config.getLog().setEnabled(true);
      }
    }
  }
}
