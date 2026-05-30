/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DocReuseTagletProcessor.java) is part of jada-ext module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.ext.proc;

import static java.lang.Math.min;
import static java.nio.file.Files.exists;
import static org.apache.commons.io.file.PathUtils.touch;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.HASH;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Chars.STAR;
import static org.pdfclown.common.util.Conditions.requireNonNullElseThrow;
import static org.pdfclown.common.util.Conditions.requireNotBlank;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Exceptions.unsupported;
import static org.pdfclown.common.util.Objects.anyThat;
import static org.pdfclown.common.util.Objects.found;
import static org.pdfclown.common.util.Objects.sfqnd;
import static org.pdfclown.common.util.Objects.textLiteral;
import static org.pdfclown.common.util.Objects.typeOf;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.Strings.STR_LENGTH;
import static org.pdfclown.common.util.Strings.indexOfElse;
import static org.pdfclown.common.util.function.Functions.toElse;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__JAVA;
import static org.pdfclown.jada.core.util.lang.Javadocs.TAG_NAME__LINK;
import static org.pdfclown.jada.core.util.lang.Javadocs.TAG_NAME__LINKPLAIN;
import static org.pdfclown.jada.core.util.lang.Javadocs.TAG_NAME__VALUE;
import static org.pdfclown.jada.core.util.lang.Javadocs.inlineTag;
import static org.pdfclown.jada.core.util.lang.Javadocs.inlineTagName;
import static org.pdfclown.jada.core.util.lang.Javadocs.inlineTagPattern;
import static org.pdfclown.jada.core.util.lang.Javadocs.inlineTagValue;
import static org.pdfclown.jada.ext.internal.ExtMessage.P__QUERY;
import static org.pdfclown.jada.ext.internal.ExtMessage.P__SAVE;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.TraditionalJavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic.Kind;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.jada.core.system.SystemConfig;
import org.pdfclown.jada.core.system.proc.FileProcess;
import org.pdfclown.jada.core.system.proc.src.JavaProcessor;
import org.pdfclown.jada.core.util.Messages;
import org.pdfclown.jada.core.util.lang.Javadocs;
import org.pdfclown.jada.core.util.lang.LangAsts;
import org.pdfclown.jada.ext.internal.ExtMessage;
import org.pdfclown.jada.ext.taglet.DocReuseTaglet;
import org.pdfclown.jada.ext.taglet.DocTaglet;
import org.pdfclown.jada.ext.taglet.ReuseDocTaglet;

/**
 * {@link DocReuseTaglet} source processor.
 * <p>
 * Automates Javadoc fragments reuse, keeping {@linkplain ReuseDocTaglet target} fragments
 * synchronized with corresponding {@linkplain DocTaglet source} fragments.
 * </p>
 *
 * @author Stefano Chizzolini
 * @implNote For performance reasons, file scan is delayed as much as possible, evaluating only
 *           modified files unless referenced fragment sources need to be resolved outside that file
 *           set. To detect modified files, a dumb file named after this class (and placed in the
 *           {@linkplain SystemConfig#getBuildDirectory() build directory}) is touched at the end of
 *           the processing; on following runs, its time stamp is used as a threshold against the
 *           last modification times of the files.
 */
public class DocReuseTagletProcessor extends JavaProcessor {
  /**
   * {@link DocReuseTagletProcessor} context.
   */
  private static class Context {
    String commentContent;
    /**
     * Current Javadoc fragment.
     */
    @Nullable
    Fragment fragment;
    /**
     * Source copy position.
     * <p>
     * Used to copy intermediate Javadoc fragments when updating the comment.
     * </p>
     */
    int inputStart = 0;
    int lastFragmentBegin = -1;
    @Nullable
    String lastFragmentKey;
    @Nullable
    String lastTagName;
    StringBuilder out = new StringBuilder();
    final FileProcess.Context process;

    Context(JavadocComment comment, FileProcess.Context process) {
      commentContent = comment.getContent();
      this.process = process;
    }
  }

  /**
   * Reusable Javadoc fragment.
   * <p>
   * There are two major problems to solve when dealing with Javadoc fragment reuse:
   * </p>
   * <ol>
   * <li><b>change detection</b>: target fragments (marked by <code>@jada.reuseDoc</code> tags) are
   * expected to be updated only when the corresponding source fragments (marked by
   * <code>@jada.doc</code> tags) changes. A simple comparison between them wouldn't work, as
   * identical contents may be formatted differently (for example, they could be at different
   * indentation levels, which in turn could cause the formatter to wrap their contents over shorter
   * comment lines). The solution is to normalize both source and targets to neutralize whitespace
   * noise.</li>
   * <li><b>inline links resolution</b>: source fragments (marked by <code>@jada.doc</code> tags)
   * may contain link tags (<code>@link</code>, <code>@linkplain</code>, <code>@value</code>) which
   * are based on their source container (that is, the type syntactically containing the Javadoc
   * fragment); when copied to their target fragments (marked by <code>@jada.reuseDoc</code> tags),
   * such links are broken by the target container, thus requiring a proper adjustment. The solution
   * is to resolve the links in the target fragments to their fully-qualified form: this way, all
   * the occurrences of the same fragment outside its source container will have valid links
   * whatever their container; furthermore, they will be all identical, speeding up their comparison
   * during updates.</li>
   * </ol>
   */
  private static class Fragment {
    private static final Pattern PATTERN__LINK_TAGS = inlineTagPattern(Set.of(
        TAG_NAME__LINK, TAG_NAME__LINKPLAIN, TAG_NAME__VALUE));

    /**
     * Gets the source container of a node.
     *
     * @return Either {@code node} (if it is a {@linkplain TypeDeclaration type} or
     *         {@linkplain CompilationUnit compilation unit}), or its parent.
     */
    private static Node base(Node node) {
      return node instanceof TypeDeclaration || node instanceof CompilationUnit
          ? node
          : node.getParentNode().orElseThrow(() -> unexpected(node,
              "{} not handled as source container type", typeOf(node)));
    }

    /**
     * Resolves a Javadoc link to its fully-qualified form.
     *
     * @param link
     *          Link to resolve relatively to {@code base}.
     * @param base
     *          Containing node.
     * @param file
     *          Containing file.
     */
    private static String resolveLink(String link, Node base, Path file) {
      requireNotBlank(link, "link");

      // [Link resolution 0] Local own member (hash name; class scope).
      if (link.charAt(0) == HASH)
        return fragmentRef(base) + link;

      int linkPartSeparatorIndex = indexOfElse(link, DOT, STR_LENGTH);
      String linkPart = link.substring(0, linkPartSeparatorIndex);

      if (base instanceof TypeDeclaration<?> typeDeclaration) {
        // [Link resolution 1] Local type (simple name; class scope).
        if (linkPart.equals(typeDeclaration.getNameAsString()))
          return fragmentRef(base) + link.substring(linkPartSeparatorIndex);

        // [Link resolution 2] Local inner type (simple name; class scope).
        for (var member : typeDeclaration.getMembers()) {
          NodeWithSimpleName<?> namedNode;
          if (member instanceof CallableDeclaration || member instanceof FieldDeclaration) {
            /*
             * NOTE: Local members have already been evaluated (MUST be prefixed with hash -- see
             * [Link resolution 0]).
             */
            continue;
          } else if (member instanceof NodeWithSimpleName<?> nodeWithSimpleName) {
            namedNode = nodeWithSimpleName;
          } else {
            continue;
          }

          if (namedNode.getNameAsString().equals(linkPart))
            return fragmentRef((Node) namedNode) + link.substring(linkPartSeparatorIndex);
        }
      }

      var compilationUnit = requireNonNullElseThrow(LangAsts.compilationUnit(base),
          () -> runtime("{}: Compilation unit MISSING for {}", file, fragmentLocation(base)));

      // [Link resolution 3] Import (simple name; module scope).
      for (var import_ : compilationUnit.getImports()) {
        if (import_.getName().getId().equals(linkPart))
          return import_.getName().getQualifier().orElseThrow().toString() + DOT + link;
      }

      // [Link resolution 4] Package-level type (simple name; package scope).
      Path path = file.getParent().resolve(linkPart + FILE_EXTENSION__JAVA);
      if (exists(path))
        return compilationUnit.getPackageDeclaration()
            .map($$ -> $$.getNameAsString() + DOT).orElse(EMPTY) + link;

      // [Link resolution 5] Module-level type (fully-qualified name; module scope).
      return link;
    }

    private final Node element;
    private final Path file;
    /**
     * Fragment content for reuse within the {@linkplain #getBase() source container}.
     * <p>
     * Original content representation, whose links (that is, <code>@link</code>,
     * <code>@linkplain</code>, <code>@value</code> tags) relate to the source container; therefore,
     * in order to be reused outside the source container, it must be normalized into
     * {@link #content}.
     * </p>
     */
    private final String sourceContent;

    /*
     * DERIVED FIELDS
     */
    private transient @Nullable Node base;
    /**
     * Fragment content for reuse outside the {@linkplain #getBase() source container}.
     * <p>
     * Canonical representation of {@link #sourceContent}, whose links (that is, <code>@link</code>,
     * <code>@linkplain</code>, <code>@value</code> tags) are resolved to their fully-qualified
     * names for reuse outside the source container.
     * </p>
     */
    private transient @Nullable String content;
    private transient @Nullable String normalContent;
    private transient @Nullable String normalSourceContent;

    Fragment(Path file, Node element, String sourceContent) {
      this.file = file;
      this.element = element;
      this.sourceContent = sourceContent;
    }

    /**
     * Source container, that is type containing this fragment.
     *
     * @return Either {@link #getElement() element} (if it is a {@linkplain TypeDeclaration type} or
     *         {@linkplain CompilationUnit compilation unit}), or its parent.
     */
    public Node getBase() {
      if (base == null) {
        base = base(element);
      }
      return base;
    }

    /**
     * Gets the fragment content appropriate to the target.
     * <p>
     * There are two representations of a fragment: <b>source</b> (reusable within the source
     * container, for example by method overloads) and <b>target</b> (reusable outside the source
     * container, for example by another package).
     * </p>
     *
     * @param target
     *          Target node where this fragment has to be placed ({@code null}, to return the source
     *          representation).
     */
    public String getContent(@Nullable Node target) {
      if (isSameBase(target))
        return sourceContent;
      else {
        if (content == null) {
          /*
           * Flattening all links to their fully-qualified form for reuse outside the source
           * container.
           */
          StringBuilder b = null;
          Matcher m = PATTERN__LINK_TAGS.matcher(sourceContent);
          while (m.find()) {
            if (b == null) {
              b = new StringBuilder();
            }
            String tagName = inlineTagName(m);
            String tagValue = inlineTagValue(m);
            int labelSeparatorIndex = indexOfElse(tagValue, SPACE, STR_LENGTH);
            String link = tagValue.substring(0, labelSeparatorIndex);
            String fullLink = resolveLink(link, getBase(), file);
            String label = EMPTY;
            if (labelSeparatorIndex < tagValue.length()) {
              label = tagValue.substring(labelSeparatorIndex);
            } else if (anyThat(tagName, Objects::equals, TAG_NAME__LINK, TAG_NAME__LINKPLAIN)) {
              /*
               * NOTE: Since links are fully-qualified, it is important to provide a short label to
               * avoid their expansion on display (`@value` tags don't need it, as they are rendered
               * directly with the corresponding literal).
               */
              int memberSeparatorIndex = fullLink.lastIndexOf(HASH);
              label = SPACE + fullLink.substring((found(memberSeparatorIndex) ? memberSeparatorIndex
                  : fullLink.lastIndexOf(DOT)) + 1);
            }
            m.appendReplacement(b, inlineTag(tagName, fullLink + label));
          }
          if (b != null) {
            m.appendTail(b);
            content = b.toString();
          } else {
            content = sourceContent;
          }
        }
        return content;
      }
    }

    /**
     * Node this fragment belongs to.
     */
    public Node getElement() {
      return element;
    }

    /**
     * Source file.
     */
    public Path getFile() {
      return file;
    }

    /**
     * Gets whether another node belongs to the same source container as this one.
     */
    public boolean isSameBase(@Nullable Node other) {
      return other == null || getBase().equals(base(other));
    }

    /**
     * Gets whether another fragment content is equivalent to this one.
     *
     * @implNote Because of source code formatting, arbitrary whitespace may be interspersed in
     *           Javadoc content, so this method {@linkplain Javadocs#normal(String) normalizes} it
     *           for clean comparison.
     */
    public boolean isSameContent(String other, @Nullable Node target) {
      String normalOther = Javadocs.normal(other);
      String normalThis;
      if (isSameBase(target)) {
        if (normalSourceContent == null) {
          normalSourceContent = Javadocs.normal(sourceContent);
        }
        normalThis = normalSourceContent;
      } else {
        if (normalContent == null) {
          normalContent = Javadocs.normal(getContent(target));
        }
        normalThis = normalContent;
      }
      return normalThis.equals(normalOther);
    }
  }

  private static final Pattern PATTERN__DOC_REUSE_TAG = inlineTagPattern(Set.of(
      DocTaglet.NAME, ReuseDocTaglet.NAME));

  /**
   * Fragment end pseudo-key.
   */
  private static final String PSEUDO_KEY__END = "END";

  /**
   * Gets the fully-qualified name corresponding to a node.
   * <p>
   * To use outside the algorithm (for example, in failure messages) to reference a fragment.
   * </p>
   */
  protected static String fragmentLocation(Node node) {
    return LangAsts.fqn(node);
  }

  /**
   * Gets the fragment reference corresponding to a node.
   * <p>
   * To use as tag value of <code>@jada.reuseDoc</code> to reference a source fragment.
   * </p>
   */
  protected static String fragmentRef(Node node) {
    return LangAsts.fqn(node, false);
  }

  private static UnsupportedOperationException unexpectedTag(String tagName) {
    return unsupported("Tag UNEXPECTED: @" + tagName);
  }

  private final Map<String, Fragment> fragments = new HashMap<>();
  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull Path thresholdFile;
  private long thresholdFileTime;
  private final Set<Path> unsolvedFragmentFiles = new LinkedHashSet<>();
  private final Set<String> unsolvedFragmentKeys = new TreeSet<>();

  @Override
  public String createStatusMessage() {
    return "- Javadoc fragment keys NOT FOUND:" + Messages.list(unsolvedFragmentKeys, 1);
  }

  @Override
  public void end(boolean success) {
    super.end(success);

    if (success) {
      // Save threshold time for next processing!
      try {
        touch(thresholdFile);
      } catch (IOException ex) {
        getLog().print(Kind.WARNING, this, ExtMessage.LAST_MOD_TIME_ACTION_FAILED, P__SAVE,
            thresholdFile, ex);
      }
    }
  }

  /**
   * @return {@code -1_000}
   * @implNote This processor is expected to synchronize obsolete contents before feeding them to
   *           any other processor.
   */
  @Override
  public int getPriority() {
    return -1_000;
  }

  @Override
  public void init(SystemConfig config) {
    super.init(config);

    // Load threshold time!
    thresholdFile = getConfig().getBuildDirectory().resolve(getClass().getName());
    if (exists(thresholdFile)) {
      try {
        thresholdFileTime = Files.getLastModifiedTime(thresholdFile).toMillis();
      } catch (IOException ex) {
        getLog().print(Kind.WARNING, this, ExtMessage.LAST_MOD_TIME_ACTION_FAILED, P__QUERY,
            thresholdFile, ex);
      }
    }
  }

  @Override
  public boolean isProcessable(Path path, FileProcess.Context context) {
    if (!super.isProcessable(path, context))
      return false;
    // End processing if fragment resolution is complete across the file set!
    else if (context.getIterationIndex() > 0 && unsolvedFragmentFiles.isEmpty()) {
      context.end();
      return false;
    }
    // Process if threshold time is not available!
    else if (thresholdFileTime == 0 || unsolvedFragmentFiles.contains(path))
      return true;

    // Process if file time is not available!
    long fileTime;
    try {
      fileTime = Files.getLastModifiedTime(path).toMillis();
    } catch (IOException ex) {
      getLog().print(Kind.WARNING, this, ExtMessage.LAST_MOD_TIME_ACTION_FAILED, P__QUERY, path,
          ex);

      return true;
    }

    /*
     * Postpone unmodified file!
     *
     * NOTE: For the sake of efficiency, unmodified files are set aside until all modified files are
     * resolved -- this way, they can be evaluated later in case the resolution of modified files
     * requires to process them too.
     */
    if (fileTime < thresholdFileTime) {
      context.postponeFile();
      return false;
    }

    // Process modified file!
    return true;
  }

  protected void logFragmentEvent(Kind kind, String tagName, String key, String event, Node node) {
    getLog().print(kind, this, getFragmentEventMessage(tagName, key, event, node));
  }

  @Override
  protected @Nullable CompilationUnit processContent(CompilationUnit content, Path file,
      FileProcess.Context context) {
    var baseDir = context.getBaseDir(file);

    content.walk($ -> {
      JavadocComment comment = $.getComment()
          .filter(Comment::isJavadocComment)
          .map(JavadocComment.class::cast)
          .orElse(null);
      if (comment == null)
        return;

      var c = new Context(comment, context);
      Matcher tagMatcher = PATTERN__DOC_REUSE_TAG.matcher(c.commentContent);
      while (tagMatcher.find()) {
        String tagName = inlineTagName(tagMatcher);
        String tagValue = inlineTagValue(tagMatcher);
        switch (tagValue) {
          // Fragment end tag.
          case PSEUDO_KEY__END: {
            if (!tagName.equals(c.lastTagName))
              throw runtime("@{} end tag INVALID at {}: {}",
                  tagName, fragmentLocation($), c.lastTagName != null
                      ? "@" + c.lastTagName + " tag expected"
                      : "@" + tagName + " begin tag missing");

            String tagFragmentContent = c.commentContent.substring(c.lastFragmentBegin,
                tagMatcher.start());
            switch (tagName) {
              case DocTaglet.NAME: {
                assert c.lastFragmentKey != null;

                // Store the fragment for later use by @jada.reuseDoc tags!
                storeFragment(c.lastFragmentKey, tagFragmentContent, $, file, c);
                break;
              }
              case ReuseDocTaglet.NAME: {
                assert c.fragment != null;

                // Tag fragment needs update?
                if (!c.fragment.isSameContent(tagFragmentContent, $)) {
                  c.out
                      // Content BEFORE the end tag.
                      .append(c.commentContent, c.inputStart, c.lastFragmentBegin)
                      // @jada.reuseDoc content (update).
                      .append(c.fragment.getContent($))
                      // @jada.reuseDoc end tag.
                      .append(tagMatcher.group());
                  c.inputStart = tagMatcher.end();

                  assert c.lastFragmentKey != null;

                  logFragmentEvent(Kind.NOTE, tagName, c.lastFragmentKey, "UPDATED", $);
                }
                break;
              }
              default:
                throw unexpectedTag(tagName);
            }
            c.lastTagName = null;
            c.lastFragmentKey = null;
            c.lastFragmentBegin = -1;
            c.fragment = null;
            break;
          }
          // Fragment begin tag.
          default: {
            // Fragment end tag missing (unbalanced tags)?
            if (c.lastFragmentKey != null) {
              normalizeTag(tagMatcher.start(), $, file, c);
            }

            // Fragment key resolution.
            final String fragmentKey;
            final Path fragmentFile;
            fragmentKeySwitch: switch (tagName) {
              // Source key.
              case DocTaglet.NAME:
                if (!tagValue.isEmpty() && tagValue.charAt(0) == COLON)
                  throw runtime("{}: @{} with INVALID local key {} (colon prefix NOT ALLOWED)",
                      file, DocTaglet.NAME, textLiteral(tagValue));

                fragmentKey = fragmentRef($) + (!tagValue.isEmpty() ? COLON + tagValue : EMPTY);
                fragmentFile = null;
                break;
              // Target key.
              case ReuseDocTaglet.NAME: {
                /*
                 * Element name (that is, reference to the Javadoc comment associated to a syntactic
                 * element, such as a type or a type member).
                 *
                 * Corresponds to `tagValue` without `localKey` (for example, if `tagValue` is
                 * "MyClass:myId", `elementKey` is "MyClass").
                 */
                String elementKey;
                /*
                 * Fragment identifier local to a Javadoc comment.
                 *
                 * Corresponds to the trailing part of `tagValue`, prefixed by colon (for example,
                 * if `tagValue` is "MyClass:myId", `localKey` is ":myId").
                 */
                String localKey;
                {
                  int localKeySeparatorIndex = indexOfElse(tagValue, COLON, STR_LENGTH);
                  elementKey = tagValue.substring(0, localKeySeparatorIndex);
                  localKey = tagValue.substring(localKeySeparatorIndex);
                }

                int elementKeyPartSeparatorIndex = min(indexOfElse(elementKey, DOT, STR_LENGTH),
                    indexOfElse(elementKey, HASH, STR_LENGTH));
                String elementKeyPart = elementKey.substring(0, elementKeyPartSeparatorIndex);
                var ownMember = false /*
                                       * Whether the key part represents a type's own member (field
                                       * or callable, NOT inner type)
                                       */;
                if (elementKeyPart.isEmpty()) {
                  if (elementKey.isEmpty()) {
                    /*
                     * [Name resolution 0] Local callable member overload.
                     *
                     * NOTE: Implicit element key is applicable to callable member overloads only.
                     */
                    if ($ instanceof CallableDeclaration) {
                      fragmentKey = fragmentRef($) + localKey;
                      fragmentFile = null;
                      break;
                    } else
                      throw runtime("""
                          {}: @{} with implicit element key (valid only within callable member \
                          overloads)""", file, ReuseDocTaglet.NAME);
                  }
                  // HASH prefix (local member)?
                  else if (elementKey.charAt(0) == HASH) {
                    ownMember = true;
                    elementKeyPartSeparatorIndex = indexOfElse(elementKey, DOT, 1, STR_LENGTH);
                    elementKeyPart = elementKey.substring(1, elementKeyPartSeparatorIndex);
                  }
                  // DOT prefix.
                  else
                    throw runtime("""
                        {}: @{} with INVALID reference key {} (dot prefix NOT ALLOWED)""", file,
                        ReuseDocTaglet.NAME, textLiteral(tagValue));
                }

                var callable = false;
                if (ownMember) {
                  int callableMemberParamsSeparatorIndex =
                      elementKeyPart.indexOf(ROUND_BRACKET_OPEN);
                  // Callable member?
                  if (found(callableMemberParamsSeparatorIndex)) {
                    /*
                     * NOTE: Callable member references are expected to be terminal (that is, they
                     * MUST end `elementKey`), to end with ')', and to represent parameters with
                     * '*'.
                     */
                    if (!elementKey.endsWith(elementKeyPart)
                        || elementKeyPart.charAt(elementKeyPart.length() - 1) != ROUND_BRACKET_CLOSE
                        || !elementKeyPart.substring(callableMemberParamsSeparatorIndex + 1,
                            elementKeyPart.length() - 1).equals(S + STAR))
                      throw runtime("""
                          {}: {} reference to callable member MALFORMED (should be "{}(*)")""",
                          file, textLiteral(elementKeyPart),
                          elementKeyPart.substring(0, callableMemberParamsSeparatorIndex));

                    callable = true;
                    elementKeyPart = elementKeyPart.substring(0,
                        callableMemberParamsSeparatorIndex);
                  }
                }

                // [Name resolution 1] Local member (simple name; class scope).
                {
                  TypeDeclaration<?> containerNode = null;
                  if ($ instanceof TypeDeclaration) {
                    // [Name resolution 1.1] Child member.
                    containerNode = (TypeDeclaration<?>) $;
                  } else if ($ instanceof BodyDeclaration) {
                    // [Name resolution 1.2] Sibling member.
                    containerNode = (TypeDeclaration<?>) $.getParentNode().orElseThrow();
                  } else if (callable)
                    throw unexpected(typeOf($), """
                        {}: structure UNEXPECTED as context of callable member reference \
                        "{}(*)\"""", file, elementKeyPart);

                  if (containerNode != null) {
                    for (var member : containerNode.getMembers()) {
                      if (member instanceof NodeWithJavadoc) {
                        NodeWithSimpleName<?> namedNode;
                        if (member instanceof NodeWithSimpleName<?> nodeWithSimpleName) {
                          if ((!callable == member instanceof CallableDeclaration)
                              || (!callable && ownMember)) {
                            continue;
                          }

                          namedNode = nodeWithSimpleName;
                        } else if (member instanceof FieldDeclaration fieldDeclaration) {
                          if (callable || !ownMember) {
                            continue;
                          }

                          namedNode = fieldDeclaration.getVariable(0);
                        } else {
                          continue;
                        }

                        if (namedNode.getNameAsString().equals(elementKeyPart)) {
                          fragmentKey = fragmentRef((Node) namedNode)
                              + elementKey.substring(elementKeyPartSeparatorIndex) + localKey;
                          fragmentFile = null;
                          break fragmentKeySwitch;
                        }
                      }
                    }
                  }
                  if (ownMember)
                    throw runtime("{}: member \"{}(*)\" NOT FOUND{}", file, elementKeyPart,
                        toElse(containerNode, $$ -> " inside " + fragmentLocation($$), EMPTY));
                }

                // [Name resolution 2] Import (simple name; module scope).
                for (var import_ : content.getImports()) {
                  if (import_.getName().getId().equals(elementKeyPart)) {
                    fragmentKey = import_.getName().getQualifier().orElseThrow().toString()
                        + DOT + tagValue;
                    fragmentFile = resolveFragmentFile(fragmentKey, baseDir, file);
                    break fragmentKeySwitch;
                  }
                }

                // [Name resolution 3] Package-level type (simple name; package scope).
                Path path = file.getParent().resolve(elementKeyPart + FILE_EXTENSION__JAVA);
                if (exists(path)) {
                  fragmentKey = content.getPackageDeclaration()
                      .map($$ -> $$.getNameAsString() + DOT).orElse(EMPTY) + tagValue;
                  fragmentFile = path;
                  break;
                }

                // [Name resolution 4] Module-level type (fully-qualified name; module scope).
                fragmentKey = tagValue;
                fragmentFile = resolveFragmentFile(fragmentKey, baseDir, file);
                break;
              }
              default:
                throw unexpectedTag(tagName);
            }

            // Store information for fragment end tag!
            c.lastTagName = tagName;
            c.lastFragmentKey = fragmentKey;
            c.lastFragmentBegin = tagMatcher.end();
            switch (c.lastTagName) {
              case DocTaglet.NAME:
                c.fragment = null;

                if (thresholdFileTime > 0) {
                  /*
                   * Force ALL the files to be processed!
                   *
                   * NOTE: Normally, due to `thresholdFileTime`, only changed files are processed;
                   * in case a changed file contains a source fragment though, the processing MUST
                   * be extended to ALL the files to ensure that every possible target is
                   * synchronized, since currently there is no way to detect whether that source
                   * fragment itself has changed or not (ideally, a caching mechanism should be
                   * implemented to keep track of fragment checksums in order to process only
                   * relevant files).
                   *
                   * TODO: implement checksum caching to optimize fragment change detection.
                   */
                  thresholdFileTime = 0;
                }
                break;
              case ReuseDocTaglet.NAME:
                c.fragment = fragments.get(c.lastFragmentKey);
                if (c.fragment != null) {
                  unsolvedFragmentKeys.remove(c.lastFragmentKey);
                } else {
                  // If no fragment is available yet, postpone!
                  context.postponeFile();
                  unsolvedFragmentFiles.add(file);
                  unsolvedFragmentKeys.add(c.lastFragmentKey);
                  if (fragmentFile != null) {
                    unsolvedFragmentFiles.add(fragmentFile);
                  }
                  logFragmentEvent(Kind.OTHER, c.lastTagName, c.lastFragmentKey, "POSTPONED", $);
                  return;
                }
                break;
              default:
                throw unexpectedTag(c.lastTagName);
            }
          }
        }
      }

      // Fragment end tag missing (unbalanced tags)?
      if (c.lastFragmentKey != null) {
        normalizeTag(c.commentContent.length(), $, file, c);
      }
      // Content changed (balanced tags)?
      else if (c.out.length() > 0) {
        // Trailing content.
        c.out.append(c.commentContent.substring(c.inputStart));
      }

      // Update the source code!
      if (c.out.length() > 0) {
        //noinspection UnusedAssignment
        $.setComment(comment = new TraditionalJavadocComment(c.out.toString()));
        context.changeFile();
      }
    });

    if (context.isFileComplete()) {
      unsolvedFragmentFiles.remove(file);
    }
    return content;
  }

  private String getFragmentEventMessage(String tagName, String key, String event, Node node) {
    String role = switch (tagName) {
      case DocTaglet.NAME -> "source";
      case ReuseDocTaglet.NAME -> "target";
      default -> throw unexpectedTag(tagName);
    };
    return ExtMessage.DOC_REUSE_FRAGMENT_EVENT.toString(getConfig(), role, tagName, key,
        sfqnd(fragmentLocation(node)), event);
  }

  /**
   * Expands the current fragment reuse tag to its normal form.
   * <p>
   * Fragment reuse tags ({@link DocTaglet @jada.doc} and {@link ReuseDocTaglet @jada.reuseDoc})
   * normally surround a comment fragment with a start tag and end tag. However, for leniency, users
   * are allowed to leave such fragments open/unbalanced (that is, without the end tag): this method
   * is responsible to normalize these fragments balancing them with the corresponding end tag (in
   * case of {@code @jada.reuseDoc}, the fragment is also automatically copied from its source).
   * </p>
   */
  private void normalizeTag(int lastFragmentEnd, Node node, Path file, Context c) {
    assert c.lastFragmentKey != null;
    assert c.lastTagName != null;

    // Append content UP TO the fragment begin tag (inclusive)!
    c.out.append(c.commentContent, c.inputStart, c.lastFragmentBegin);
    // Content BETWEEN the fragment begin tag (exclusive) and the current position.
    String midContent = c.commentContent.substring(c.lastFragmentBegin, lastFragmentEnd);
    String endTag = inlineTag(c.lastTagName, PSEUDO_KEY__END);
    switch (c.lastTagName) {
      case DocTaglet.NAME: {
        // Store the fragment for later use by jada.reuseDoc tags!
        c.fragment = storeFragment(c.lastFragmentKey, midContent, node, file, c);

        /*
         * NOTE: @jada.doc normalization implies that its content is followed by a new balancing end
         * tag.
         */
        c.out
            // @jada.doc content.
            .append(c.fragment.getContent(node))
            // @jada.doc end tag.
            .append(endTag);

        logFragmentEvent(Kind.NOTE, c.lastTagName, c.lastFragmentKey, "NORMALIZED", node);
        break;
      }
      case ReuseDocTaglet.NAME: {
        assert c.fragment != null;

        /*
         * NOTE: @jada.reuseDoc normalization implies that its content expands between the existing
         * start tag and a new balancing end tag.
         */
        c.out
            // @jada.reuseDoc content.
            .append(c.fragment.getContent(node))
            // @jada.reuseDoc end tag.
            .append(endTag)
            // Content AFTER @jada.reuseDoc end tag.
            .append(midContent);

        logFragmentEvent(Kind.NOTE, c.lastTagName, c.lastFragmentKey, "UPDATED+NORMALIZED", node);
        break;
      }
      default:
        throw unexpectedTag(c.lastTagName);
    }
    c.inputStart = lastFragmentEnd;
  }

  private Path resolveFragmentFile(String fragmentKey, Path baseDir, Path file) {
    var elementKey = fragmentKey.substring(0, indexOfElse(fragmentKey, COLON, 0, STR_LENGTH));
    var path = baseDir;
    var elementKeyPartSeparatorIndex = -1;
    while (true) {
      var startIndex = elementKeyPartSeparatorIndex + 1;
      var elementKeyPart = elementKey.substring(startIndex,
          elementKeyPartSeparatorIndex = indexOfElse(elementKey, DOT, startIndex, STR_LENGTH));
      // Package level reached?
      if (!exists(path = path.resolve(elementKeyPart))) {
        /*
         * Compilation unit MISSING?
         *
         * NOTE: When the incremental directory is not found, its parent is the package directory
         * and its filename is resolved to Java file.
         */
        if (!exists(path = path.getParent().resolve(path.getFileName().toString()
            + FILE_EXTENSION__JAVA)))
          throw runtime("{}: compilation unit of {} NOT FOUND", file, textLiteral(fragmentKey));

        return path;
      }
    }
  }

  /**
   * Stores the fragment for later use by {@code @jada.reuseDoc} tags.
   *
   * @return Stored fragment.
   */
  private Fragment storeFragment(String key, String value, Node node, Path file, Context c) {
    String event;
    var fragment = fragments.get(key);
    if (fragment != null) {
      /*
       * Duplicated key?
       *
       * NOTE: A fragment may have already been assigned if either an additional processing
       * iteration occurred or a duplicated key exists associated to another element.
       */
      if (!fragment.getElement().equals(node))
        throw runtime(getFragmentEventMessage(DocTaglet.NAME, key,
            "key already existing at " + fragment.getElement(), node));

      /*
       * NOTE: This condition should always be true, since already-assigned source fragments are
       * expected not to change across processing iterations.
       */
      if (fragment.getContent(node).equals(value))
        return fragment;

      event = "RELOADED";
    } else {
      event = "RETRIEVED";
    }

    fragments.put(key, fragment = new Fragment(file, node, value));

    /*
     * Force reprocessing!
     *
     * NOTE: After all the fileset is processed, processing starves to infinite loop exception if no
     * changes occurred despite incomplete files left. To prevent such failure, we have to
     * explicitly notify the process that we expect another processing cycle so stored source
     * fragments can be used to resolve targets and eventually complete those incomplete files.
     */
    c.process.allowReprocess();

    logFragmentEvent(Kind.OTHER, DocTaglet.NAME, key, event, node);

    return fragment;
  }
}
