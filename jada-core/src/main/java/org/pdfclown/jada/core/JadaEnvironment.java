/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaEnvironment.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.unsupported;
import static org.pdfclown.common.util.Exceptions.wrongArgOpt;
import static org.pdfclown.common.util.Objects.typeOf;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.DocTreeVisitor;
import com.sun.source.doctree.EntityTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.DocSourcePositions;
import com.sun.source.util.DocTreeFactory;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.DCTree.DCDocComment;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.internal.tool.DocEnvImpl;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.LazyNonNull;
import org.pdfclown.common.util.annot.PolyNull;
import org.pdfclown.common.util.reflect.Reflects;
import org.pdfclown.jada.core.JadaEnvironment.JadaDocFilter.DocFragmentRole;

/**
 * {@link Jada} environment.
 *
 * @author Stefano Chizzolini
 */
public class JadaEnvironment extends DocEnvImpl implements JadaObject {
  /**
   * Javadoc pre-processing filter for source files.
   * <p>
   * Transforms source files to valid Javadoc content.
   * </p>
   *
   * @author Stefano Chizzolini
   */
  public interface JadaDocFileFilter extends JadaObject {
    /**
     * Transforms the file.
     *
     * @param file
     *          Source file.
     * @return Transformed file.
     */
    FileObject filterDocFile(FileObject file);
  }

  /**
   * Javadoc pre-processing filter for source comment fragments.
   * <p>
   * Transforms source comment fragments to valid Javadoc content.
   * </p>
   *
   * @author Stefano Chizzolini
   */
  public abstract static class JadaDocFilter implements JadaObject {
    /**
     * Javadoc fragment role.
     *
     * @author Stefano Chizzolini
     */
    public enum DocFragmentRole {
      BLOCK_TAGS,
      BODY_DETAIL,
      BODY_SUMMARY
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Jada jada;

    /**
     * <span class="warning">(For internal use only)</span>
     */
    protected JadaDocFilter() {
    }

    protected JadaDocFilter(Jada jada) {
      this.jada = jada;
    }

    /**
     * Transforms the comment fragment.
     *
     * @param nodes
     *          Comment fragment.
     * @return Transformed nodes.
     */
    public abstract List<? extends DocTree> filterDocFragment(List<? extends DocTree> nodes,
        DocFragmentRole role, Object location);

    @Override
    public Jada getJada() {
      return jada;
    }

    /**
     * Transformation priority.
     * <p>
     * Useful to order a chain of filters.
     * </p>
     */
    public int getPriority() {
      return 0;
    }
  }

  /**
   * Javadoc pre-processing transformer.
   * <p>
   * Applies filters to source files and comment nodes to transform as valid Javadoc content for
   * main processing.
   * </p>
   *
   * @author Stefano Chizzolini
   */
  public static class JadaDocTransformer {
    /**
     * @implNote Weird enough (among tons of Javadoc weirdness — see also {@link Jada}
     *           implementation requirements for a load of rants), in JDK 17,
     *           {@code com.sun.tools.javac.api.JavacTrees.printMessage(Kind, CharSequence,
     *           DocTree, ..)} casts to internal {@link DCDocComment} class instead of regular
     *           {@link DocCommentTree} interface, causing {@link ClassCastException} to any other
     *           implementation of the latter (ouch!); therefore, we are forced to extend the
     *           former, alas.
     */
    private static class JadaDocComment extends DCDocComment implements DocCommentTree {
      private final DocCommentTree base;
      private final Object location;
      private final JadaDocTransformer transformer;

      /*
       * NOTE: Lots of gymnastics here: we were forced to extend `DCDocComment` because of binary
       * constraints, but now we have to shadow its fields because they are strongly typed to
       * internal classes (ouch!).
       */
      private @LazyNonNull @Nullable List<? extends DocTree> blockTags;
      private @LazyNonNull @Nullable List<? extends DocTree> body;
      private @LazyNonNull @Nullable List<? extends DocTree> firstSentence;
      private @LazyNonNull @Nullable List<DocTree> fullBody;

      public JadaDocComment(DocCommentTree base, Object location,
          JadaDocTransformer transformer) {
        super(
            /*
             * NOTE: In case of `DCDocComment`, `comment` field is REQUIRED (source comment:
             * "required for the implicit source pos table").
             */
            base instanceof DCDocComment docComment ? docComment.comment : null, null, null, null,
            null, null, null);

        this.base = base;
        this.location = location;
        this.transformer = transformer;
      }

      @Override
      public <@Nullable R, @Nullable D> R accept(DocTreeVisitor<R, D> visitor, D data) {
        return base.accept(visitor, data);
      }

      @Override
      public List<? extends DocTree> getBlockTags() {
        if (blockTags == null) {
          blockTags = transformer.filterDocFragment(base.getBlockTags(), DocFragmentRole.BLOCK_TAGS,
              location);
        }
        return blockTags;
      }

      @Override
      public List<? extends DocTree> getBody() {
        if (body == null) {
          body = transformer.filterDocFragment(base.getBody(), DocFragmentRole.BODY_DETAIL,
              location);
        }
        return body;
      }

      @Override
      public List<? extends DocTree> getFirstSentence() {
        if (firstSentence == null) {
          firstSentence = transformer.filterDocFragment(base.getFirstSentence(),
              DocFragmentRole.BODY_SUMMARY, location);
        }
        return firstSentence;
      }

      @Override
      public List<? extends DocTree> getFullBody() {
        if (fullBody == null) {
          fullBody = new ArrayList<>();
          fullBody.addAll(getFirstSentence());
          fullBody.addAll(getBody());
        }
        return fullBody;
      }

      @Override
      public Kind getKind() {
        return base.getKind();
      }

      @Override
      public List<? extends DocTree> getPostamble() {
        return base.getPostamble();
      }

      @Override
      public List<? extends DocTree> getPreamble() {
        return base.getPreamble();
      }
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private DocTrees docTrees;
    private JadaDocFileFilter fileFilter = new JadaDocFileFilter() {
      @Override
      public FileObject filterDocFile(FileObject file) {
        return file;
      }

      @Override
      public Jada getJada() {
        throw unsupported();
      }
    };
    private final Queue<JadaDocFilter> filters = new PriorityQueue<>(
        Comparator.comparingInt(JadaDocFilter::getPriority));

    public JadaDocTransformer(DocTrees docTrees) {
      this.docTrees = docTrees;
    }

    /**
     * <span class="warning">(For internal use only)</span>
     */
    protected JadaDocTransformer() {
    }

    /**
     * Adds a filter.
     */
    public JadaDocTransformer addFilter(JadaDocFilter filter) {
      filters.add(filter);
      return this;
    }

    /**
     */
    public @Nullable DocCommentTree filterDocComment(FileObject location) {
      return filterDocComment(docTrees.getDocCommentTree(fileFilter.filterDocFile(location)),
          location);
    }

    /**
     */
    public @Nullable DocCommentTree filterDocComment(TreePath location) {
      return filterDocComment(docTrees.getDocCommentTree(location), location);
    }

    /**
     */
    public List<? extends DocTree> filterDocFragment(List<? extends DocTree> nodes,
        DocFragmentRole role, Object location) {
      switch (filters.size()) {
        case 0:
          break;
        case 1:
          nodes = filters.element().filterDocFragment(nodes, role, location);
          break;
        default:
          for (var filter : filters) {
            nodes = filter.filterDocFragment(nodes, role, location);
          }
      }
      return nodes;
    }

    /**
     * File pre-processing filter.
     */
    public JadaDocFileFilter getFileFilter() {
      return fileFilter;
    }

    /**
     * Javadoc pre-processing filter.
     */
    public Queue<JadaDocFilter> getFilters() {
      return filters;
    }

    /**
     * Removes a filter.
     */
    public JadaDocTransformer remove(JadaDocFilter filter) {
      filters.remove(filter);
      return this;
    }

    /**
     * Sets {@link #getFileFilter() fileFilter}.
     */
    public JadaDocTransformer setFileFilter(JadaDocFileFilter value) {
      fileFilter = value;
      return this;
    }

    protected @PolyNull @Nullable DocCommentTree filterDocComment(
        @PolyNull @Nullable DocCommentTree comment, Object location) {
      return comment != null
          ? (filters.isEmpty()
              ? comment
              : new JadaDocComment(comment, location, this))
          : null;
    }
  }

  /**
   * @author Stefano Chizzolini
   */
  public static class JadaDocTrees extends DocTrees {
    @SuppressWarnings("NotNullFieldNotInitialized")
    private DocTrees base;
    private @LazyNonNull @Nullable JadaDocTransformer transformer;

    /**
     * <span class="warning">(For internal use only)</span>
     */
    protected JadaDocTrees() {
    }

    private JadaDocTrees(JadaEnvironment environment) {
      base = environment.base.getDocTrees();
    }

    @Override
    public @Nullable BreakIterator getBreakIterator() {
      return base.getBreakIterator();
    }

    /**
     * <a href=
     * "https://docs.oracle.com/en/java/javase/17/docs/api/jdk.compiler/com/sun/source/util/DocTrees.html#getCharacters(com.sun.source.doctree.EntityTree)"
     * >JDK 17</a>
     */
    // @Override
    public @Nullable String getCharacters(EntityTree tree) {
      return Reflects.call(base, "getCharacters", new Class[] { EntityTree.class },
          new Object[] { tree });
    }

    @Override
    public @Nullable String getDocComment(TreePath path) {
      return base.getDocComment(path);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The result is filtered via {@link #getTransformer() transformer}.
     * </p>
     */
    @Override
    public @Nullable DocCommentTree getDocCommentTree(Element e) {
      TreePath path = getPath(e);
      return path != null ? getTransformer().filterDocComment(path) : null;
    }

    /**
     * @implNote For simplicity (is there any actual use case for this method?),
     *           {@linkplain #getTransformer() filtering} is currently not supported.
     */
    @Override
    public @Nullable DocCommentTree getDocCommentTree(Element e, String relativePath)
        throws IOException {
      return base.getDocCommentTree(e, relativePath);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The result is filtered via {@link #getTransformer() transformer}.
     * </p>
     */
    @Override
    public @Nullable DocCommentTree getDocCommentTree(FileObject fileObject) {
      return getTransformer().filterDocComment(fileObject);
    }

    /**
     * Returns the doc comment tree of the HTML content.
     * <p>
     * The result is filtered via {@link #getTransformer() transformer}.
     * </p>
     *
     * @param content
     *          HTML content.
     * @return {@code null}, if no doc comment was found.
     */
    public @Nullable DocCommentTree getDocCommentTree(String content) {
      String commentContent = !content.contains("<html")
          ? "<html><body>" + content + "</body></html>"
          : content;
      return getDocCommentTree(new SimpleJavaFileObject(URI.create("dummy.html"),
          JavaFileObject.Kind.HTML) {
        @Override
        public CharSequence getCharContent(boolean ignoreEncoding) {
          return commentContent;
        }
      });
    }

    /**
     * {@inheritDoc}
     * <p>
     * The result is filtered via {@link #getTransformer() transformer}.
     * </p>
     */
    @Override
    public @Nullable DocCommentTree getDocCommentTree(TreePath path) {
      return getTransformer().filterDocComment(path);
    }

    /**
     * Returns the doc comment fragment of the HTML content.
     * <p>
     * The result is filtered via {@link #getTransformer() transformer}.
     * </p>
     *
     * @param content
     *          HTML content.
     */
    public List<? extends DocTree> getDocFragment(String content) {
      var comment = getDocCommentTree(content);
      return comment != null ? comment.getFullBody() : List.of();
    }

    @Override
    public DocTreeFactory getDocTreeFactory() {
      return base.getDocTreeFactory();
    }

    @Override
    public @Nullable DocTreePath getDocTreePath(FileObject fileObject,
        PackageElement packageElement) {
      return base.getDocTreePath(fileObject, packageElement);
    }

    @Override
    public @Nullable Element getElement(DocTreePath path) {
      return base.getElement(path);
    }

    @Override
    public @Nullable Element getElement(TreePath path) {
      return base.getElement(path);
    }

    @Override
    public List<DocTree> getFirstSentence(List<? extends DocTree> list) {
      return base.getFirstSentence(list);
    }

    @Override
    public TypeMirror getLub(CatchTree tree) {
      return base.getLub(tree);
    }

    @Override
    public TypeMirror getOriginalType(ErrorType errorType) {
      return base.getOriginalType(errorType);
    }

    @Override
    public TreePath getPath(CompilationUnitTree unit, Tree node) {
      return base.getPath(unit, node);
    }

    @Override
    public @Nullable TreePath getPath(Element e) {
      return base.getPath(e);
    }

    @Override
    public @Nullable TreePath getPath(Element e, AnnotationMirror a) {
      return base.getPath(e, a);
    }

    @Override
    public @Nullable TreePath getPath(Element e, AnnotationMirror a, AnnotationValue v) {
      return base.getPath(e, a, v);
    }

    @Override
    public @Nullable Scope getScope(TreePath path) {
      return base.getScope(path);
    }

    @Override
    public DocSourcePositions getSourcePositions() {
      return base.getSourcePositions();
    }

    /**
     * Javadoc documentation transformer for content pre-processing.
     */
    public JadaDocTransformer getTransformer() {
      if (transformer == null) {
        transformer = new JadaDocTransformer(base);
      }
      return transformer;
    }

    @Override
    public @Nullable Tree getTree(Element element) {
      return base.getTree(element);
    }

    @Override
    public @Nullable Tree getTree(Element e, AnnotationMirror a) {
      return base.getTree(e, a);
    }

    @Override
    public @Nullable Tree getTree(Element e, AnnotationMirror a, AnnotationValue v) {
      return base.getTree(e, a, v);
    }

    @Override
    public @Nullable MethodTree getTree(ExecutableElement method) {
      return base.getTree(method);
    }

    @Override
    public @Nullable ClassTree getTree(TypeElement element) {
      return base.getTree(element);
    }

    /**
     * <a href=
     * "https://docs.oracle.com/en/java/javase/17/docs/api/jdk.compiler/com/sun/source/util/DocTrees.html#getType(com.sun.source.util.DocTreePath)"
     * >JDK 15</a>
     */
    // @Override
    public @Nullable TypeMirror getType(DocTreePath path) {
      return Reflects.call(base, "getType", new Class[] { DocTreePath.class },
          new Object[] { path });
    }

    @Override
    public @Nullable TypeMirror getTypeMirror(TreePath path) {
      return base.getTypeMirror(path);
    }

    @Override
    public boolean isAccessible(Scope scope, Element member, DeclaredType type) {
      return base.isAccessible(scope, member, type);
    }

    @Override
    public boolean isAccessible(Scope scope, TypeElement type) {
      return base.isAccessible(scope, type);
    }

    @Override
    public void printMessage(Kind kind, CharSequence msg, DocTree t, DocCommentTree c,
        CompilationUnitTree root) {
      base.printMessage(kind, msg, t, c, root);
    }

    @Override
    public void printMessage(Kind kind, CharSequence msg, Tree t, CompilationUnitTree root) {
      base.printMessage(kind, msg, t, root);
    }

    @Override
    public void setBreakIterator(BreakIterator breakIterator) {
      base.setBreakIterator(breakIterator);
    }

    /**
     * Sets {@link #getTransformer() transformer}.
     */
    public void setTransformer(JadaDocTransformer value) {
      transformer = value;
    }
  }

  @SuppressWarnings("NotNullFieldNotInitialized")
  private DocletEnvironment base;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private JadaDocTrees docTrees;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private Jada jada;

  /**
   */
  public JadaEnvironment(DocletEnvironment base, Jada jada) {
    super(((DocEnvImpl) base).toolEnv, ((DocEnvImpl) base).etable);

    this.base = base;
    this.jada = jada;

    docTrees = new JadaDocTrees(this);
  }

  /**
   * <span class="warning">(For internal use only)</span>
   */
  protected JadaEnvironment() {
    super(null, null);
  }

  @Override
  public JadaDocTrees getDocTrees() {
    return docTrees;
  }

  @Override
  public Elements getElementUtils() {
    return base.getElementUtils();
  }

  @Override
  public JadaEnvironment getEnv() {
    return this;
  }

  @Override
  public JavaFileObject.Kind getFileKind(TypeElement type) {
    return base.getFileKind(type);
  }

  /**
   */
  public Path getFilePath(Element location) {
    return getFilePath(docTrees.base.getPath(location));
  }

  /**
   */
  public Path getFilePath(FileObject location) {
    return Path.of(location.toUri());
  }

  /**
   */
  public Path getFilePath(Object location) {
    requireNonNull(location, "`location`");
    if (location instanceof Element element)
      return getFilePath(element);
    else if (location instanceof TreePath treePath)
      return getFilePath(treePath);
    else if (location instanceof FileObject fileObject)
      return getFilePath(fileObject);
    else
      throw wrongArgOpt("location", typeOf(location), null, Set.of(Element.class, TreePath.class,
          FileObject.class));
  }

  /**
   */
  public Path getFilePath(TreePath location) {
    return Path.of(location.getCompilationUnit().getSourceFile().toUri());
  }

  @Override
  public Set<? extends Element> getIncludedElements() {
    return base.getIncludedElements();
  }

  @Override
  public Jada getJada() {
    return jada;
  }

  @Override
  public JavaFileManager getJavaFileManager() {
    return base.getJavaFileManager();
  }

  @Override
  public DocletEnvironment.ModuleMode getModuleMode() {
    return base.getModuleMode();
  }

  @Override
  public SourceVersion getSourceVersion() {
    return base.getSourceVersion();
  }

  @Override
  public Set<? extends Element> getSpecifiedElements() {
    return base.getSpecifiedElements();
  }

  @Override
  public Types getTypeUtils() {
    return base.getTypeUtils();
  }

  @Override
  public boolean isIncluded(Element e) {
    return base.isIncluded(e);
  }

  @Override
  public boolean isSelected(Element e) {
    return base.isSelected(e);
  }
}
