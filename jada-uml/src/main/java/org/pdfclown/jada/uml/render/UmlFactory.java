/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlFactory.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render;

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.ENUM;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_NS_SEPARATOR;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__ASSOCIATES;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__ENCLOSED_BY;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__ENCLOSES;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__EXTENDS;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__IMPLEMENTS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.MonotonicNonNull;
import org.pdfclown.jada.uml.UmlConfig.Visibility;
import org.pdfclown.jada.uml.UmlExtension;
import org.pdfclown.jada.uml.render.model.ClassDiagram;
import org.pdfclown.jada.uml.render.model.Diagram;
import org.pdfclown.jada.uml.render.model.Field;
import org.pdfclown.jada.uml.render.model.Method;
import org.pdfclown.jada.uml.render.model.Namespace;
import org.pdfclown.jada.uml.render.model.PackageDiagram;
import org.pdfclown.jada.uml.render.model.ParameterList;
import org.pdfclown.jada.uml.render.model.Reference;
import org.pdfclown.jada.uml.render.model.Type;
import org.pdfclown.jada.uml.render.model.TypeName;
import org.pdfclown.jada.uml.render.model.UmlLiteral;
import org.pdfclown.jada.uml.render.model.UmlNode;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.UMLFactory
/**
 * One big factory to produce UML from analyzed Javadoc elements.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 * @implNote TODO: This should be refactored into ClassDiagram and PackageDiagram visitor
 *           implementations to increase flexibility for future language features; however, it may
 *           introduce additional risk with regard to unbounded recursion (see
 *           <a href="https://github.com/talsma-ict/umldoclet/issues/75">Issue 75</a> for example).
 */
public class UmlFactory {
  private static final UmlPostProcessors POST_PROCESSORS = new UmlPostProcessors();

  static Visibility visibilityOf(Set<Modifier> modifiers) {
    return modifiers.contains(Modifier.PRIVATE) ? Visibility.PRIVATE
        : modifiers.contains(Modifier.PROTECTED) ? Visibility.PROTECTED
        : modifiers.contains(Modifier.PUBLIC) ? Visibility.PUBLIC
        : Visibility.PACKAGE_PRIVATE;
  }

  private static void addReference(Collection<Reference> collection, Reference reference) {
    Reference result = reference;
    Optional<Reference> found = collection.stream().filter(reference::equals).findFirst();
    if (found.isPresent()) {
      result = found.get();
      collection.remove(result);
      for (String note : reference.notes) {
        result = result.addNote(note);
      }
    }
    collection.add(result);
  }

  private static Stream<TypeElement> innerTypes(TypeElement type) {
    return Stream.concat(Stream.of(type), type.getEnclosedElements().stream()
        .filter(TypeElement.class::isInstance).map(TypeElement.class::cast)
        .flatMap(UmlFactory::innerTypes));
  }

  // SourceName: interfaceRefTypeFrom
  private static String interfaceReferenceTypeFrom(Type type) {
    return Type.Classification.INTERFACE.equals(type.getClassfication())
        ? PUML_REF__EXTENDS
        : PUML_REF__IMPLEMENTS;
  }

  // SourceName: IS_ABSTRACT_METHOD
  private static boolean isAbstractMethod(UmlNode node) {
    return node instanceof Method m && m.isAbstract();
  }

  private static boolean isBooleanPrimitive(TypeMirror type) {
    return "boolean".equals(TypeNameVisitor.INSTANCE.visit(type).getQualifiedName());
  }

  private static boolean isVarArgsMethod(Element element) {
    return element instanceof ExecutableElement e && e.isVarArgs();
  }

  private static @Nullable String propertyName(ExecutableElement method) {
    char[] result = null;
    final Set<Modifier> modifiers = method.getModifiers();
    if (modifiers.contains(Modifier.PUBLIC)
        && !modifiers.contains(Modifier.ABSTRACT)
        && !modifiers.contains(Modifier.STATIC)) {
      String name = method.getSimpleName().toString();
      int params = method.getParameters().size();
      if (params == 0 && name.length() > 3 && name.startsWith("get")) {
        // TODO: check non-void return type?
        result = name.substring(3).toCharArray();
      } else if (params == 1 && name.length() > 3 && name.startsWith("set")) {
        // TODO: check void return type?
        result = name.substring(3).toCharArray();
      } else if (params == 0 && name.length() > 2 && name.startsWith("is")
          && isBooleanPrimitive(method.getReturnType())) {
        result = name.substring(2).toCharArray();
      }
    }
    if (result != null) {
      result[0] = Character.toLowerCase(result[0]);
      return new String(result);
    }
    return null;
  }

  private static TypeMirror propertyType(ExecutableElement method) {
    if (method.getSimpleName().toString().startsWith("set") && !method.getParameters().isEmpty())
      return method.getParameters().get(0).asType();
    return method.getReturnType();
  }

  private static Type.Classification typeClassificationOf(TypeElement type) {
    ElementKind kind = type.getKind();
    Set<Modifier> modifiers = type.getModifiers();
    return ENUM.equals(kind) ? Type.Classification.ENUM
        : ElementKind.INTERFACE.equals(kind) ? Type.Classification.INTERFACE
        : ElementKind.ANNOTATION_TYPE.equals(kind) ? Type.Classification.ANNOTATION
        : modifiers.contains(Modifier.ABSTRACT) ? Type.Classification.ABSTRACT_CLASS
        : Type.Classification.CLASS;
  }

  private final UmlExtension extension;
  private @MonotonicNonNull @Nullable Collection<ExecutableElement> methodsFromExcludedSuperclasses;
  private final Function<TypeMirror, TypeNameWithCardinality> typeNameWithCardinality;

  public UmlFactory(UmlExtension extension) {
    this.extension = requireNonNull(extension, "`extension`");

    typeNameWithCardinality = TypeNameWithCardinality.function(extension.getEnv().getTypeUtils());
  }

  /**
   * Creates the class diagram corresponding to an element.
   */
  public Diagram createClassDiagram(TypeElement classElement) {
    Type type = createAndPopulateType(null, classElement);
    var classDiagram = new ClassDiagram(extension.getExtConfig(), type);

    List<TypeName> foundTypeVariables = new ArrayList<>();
    List<Reference> references = new ArrayList<>();
    UmlLiteral sep = UmlLiteral.NEWLINE;

    // Add superclass
    TypeMirror superclassType = classElement.getSuperclass();
    Element superclassElement = extension.getEnv().getTypeUtils().asElement(superclassType);
    while (superclassElement instanceof TypeElement e && !includeSuperclass(e)) {
      superclassType = e.getSuperclass();
      superclassElement = extension.getEnv().getTypeUtils().asElement(superclassType);
    }
    if (superclassElement instanceof TypeElement) {
      final TypeName superclassName = TypeNameVisitor.INSTANCE.visit(superclassType);
      if (superclassName.getGenerics().length > 0) {
        foundTypeVariables.add(superclassName);
      }
      if (!extension.getExtConfig().getExcludedTypeReferences()
          .contains(superclassName.getQualifiedName())) {
        classDiagram.addChild(sep);
        Type superType = createAndPopulateType(null, (TypeElement) superclassElement);
        // Only keep abstract methods of supertype.
        superType.removeChildren(not(UmlFactory::isAbstractMethod));
        classDiagram.addChild(superType);
        sep = UmlLiteral.EMPTY;
        references.add(new Reference(
            Reference.from(type.getName().getQualifiedName(), null),
            PUML_REF__EXTENDS,
            Reference.to(superclassName.getQualifiedName(), null))
                .canonical());
      }
    }

    // Add interfaces
    for (TypeMirror interfaceType : classElement.getInterfaces()) {
      TypeName ifName = TypeNameVisitor.INSTANCE.visit(interfaceType);
      if (ifName.getGenerics().length > 0) {
        foundTypeVariables.add(ifName);
      }
      if (!extension.getExtConfig().getExcludedTypeReferences()
          .contains(ifName.getQualifiedName())) {
        if (extension.getEnv().getTypeUtils().asElement(interfaceType) instanceof TypeElement e) {
          classDiagram.addChild(sep);
          Type implementedType = createAndPopulateType(null, e);
          implementedType.removeChildren(not(UmlFactory::isAbstractMethod));
          classDiagram.addChild(implementedType);
          sep = UmlLiteral.EMPTY;
        }
        references.add(new Reference(
            Reference.from(type.getName().getQualifiedName(), null),
            interfaceReferenceTypeFrom(type),
            Reference.to(ifName.getQualifiedName(), null))
                .canonical());
      }
    }

    // Add containing class reference
    ElementKind enclosingKind = classElement.getEnclosingElement().getKind();
    if (enclosingKind.isClass() || enclosingKind.isInterface()) {
      TypeName enclosingTypeName =
          TypeNameVisitor.INSTANCE.visit(classElement.getEnclosingElement().asType());
      if (enclosingTypeName.getGenerics().length > 0) {
        foundTypeVariables.add(enclosingTypeName);
      }
      if (!extension.getExtConfig().getExcludedTypeReferences()
          .contains(enclosingTypeName.getQualifiedName())) {
        if (classElement.getEnclosingElement() instanceof TypeElement e) {
          classDiagram.addChild(sep);
          Type enclosingType = createAndPopulateType(null, e);
          enclosingType.removeChildren(not(UmlFactory::isAbstractMethod));
          classDiagram.addChild(enclosingType);
          //noinspection UnusedAssignment
          sep = UmlLiteral.EMPTY;
        }
        references.add(new Reference(
            Reference.from(type.getName().getQualifiedName(), null),
            PUML_REF__ENCLOSED_BY,
            Reference.to(enclosingTypeName.getQualifiedName(), null))
                .canonical());
      }
    }

    // Add inner classes
    classElement.getEnclosedElements().stream()
        .filter($child -> $child.getKind().isInterface() || $child.getKind().isClass())
        .filter(TypeElement.class::isInstance).map(TypeElement.class::cast)
        .filter(extension.getEnv()::isIncluded)
        .forEach($innerclassElement -> {
          Type innerType = createType(null, $innerclassElement);
          classDiagram.addChild(innerType);
          references.add(new Reference(
              Reference.from(type.getName().getQualifiedName(), null),
              PUML_REF__ENCLOSES,
              Reference.to(innerType.getName().getQualifiedName(), null))
                  .canonical());
        });

    if (!references.isEmpty()) {
      classDiagram.addChild(UmlLiteral.NEWLINE);
      references.forEach(classDiagram::addChild);
    }

    foundTypeVariables.forEach($foundTypeVariable -> classDiagram.getChildren().stream()
        .filter(Type.class::isInstance).map(Type.class::cast)
        .filter($ -> $foundTypeVariable.equals($.getName()))
        .forEach($ -> $.updateGenericTypeVariables($foundTypeVariable)));

    if (!extension.getExtConfig().getMethodConfig().isPropertiesFlattened()) {
      POST_PROCESSORS.propertiesToFields().accept(type);
    }

    return classDiagram;
  }

  /**
   * Creates the package diagram corresponding to an element.
   */
  public Diagram createPackageDiagram(PackageElement packageElement) {
    final ModuleElement module = extension.getEnv().getElementUtils().getModuleOf(packageElement);
    var packageDiagram = new PackageDiagram(extension.getExtConfig(),
        packageElement.getQualifiedName().toString(),
        module != null ? module.getQualifiedName().toString() : null);
    var foreignTypes = new LinkedHashMap<String, Collection<Type>>();
    var references = new ArrayList<Reference>();
    Namespace namespace = createPackage(packageDiagram, packageElement, foreignTypes, references,
        PUML_NS_SEPARATOR);
    packageDiagram.addChild(namespace);

    // Filter "java.lang" or "java.util" references that occur >= 3 times
    // Maybe somehow make this configurable as well?
    foreignTypes.entrySet().stream()
        .filter($ -> "java.lang".equals($.getKey()) || "java.util".equals($.getKey()))
        .map(Map.Entry::getValue)
        .forEach($types -> {
          for (Iterator<Type> it = $types.iterator(); it.hasNext();) {
            var type = it.next();
            if (references.stream().filter($ -> $.contains(type.getName())).limit(3).count() > 2) {
              references.removeIf($ -> $.contains(type.getName()));
              it.remove();
            }
          }
        });

    // Add all remaining foreign types to the diagram.
    foreignTypes.entrySet().stream()
        .filter($ -> !$.getValue().isEmpty())
        .map($ -> {
          String foreignPackage = $.getKey();
          var foreignNamespace = new Namespace(packageDiagram, foreignPackage, null);
          $.getValue().forEach(foreignNamespace::addChild);
          return foreignNamespace;
        })
        .flatMap($foreignNamespace -> Stream.of(UmlLiteral.NEWLINE, $foreignNamespace))
        .forEach(packageDiagram::addChild);

    namespace.addChild(UmlLiteral.NEWLINE);

    if (!extension.getExtConfig().getMethodConfig().isPropertiesFlattened()) {
      namespace.getChildren().stream()
          .filter(Type.class::isInstance).map(Type.class::cast)
          .forEach(POST_PROCESSORS.propertiesToFields());
    }

    if (!references.isEmpty()) {
      packageDiagram.addChild(UmlLiteral.NEWLINE);
    }
    references.stream().map(Reference::canonical).forEach(packageDiagram::addChild);

    return packageDiagram;
  }

  Method createConstructor(Type containingType, ExecutableElement executableElement) {
    Set<Modifier> modifiers = executableElement.getModifiers();
    var ret = new Method(containingType, containingType.getName().getSimpleName(), null);
    {
      ret.setVisibility(visibilityOf(modifiers));
      ret.setAbstract(modifiers.contains(Modifier.ABSTRACT));
      ret.setStatic(modifiers.contains(Modifier.STATIC));
      ret.setDeprecated(extension.getEnv().getElementUtils().isDeprecated(executableElement));
      ret.addChild(createParameters(executableElement.getParameters()));
    }
    return ret;
  }

  Field createField(Type containingType, VariableElement variable) {
    Set<Modifier> modifiers = requireNonNull(variable, "`variable`").getModifiers();
    var ret = new Field(containingType, variable.getSimpleName().toString(),
        TypeNameVisitor.INSTANCE.visit(variable.asType()));
    {
      ret.setVisibility(visibilityOf(modifiers));
      ret.setStatic(modifiers.contains(Modifier.STATIC));
      ret.setDeprecated(extension.getEnv().getElementUtils().isDeprecated(variable));
    }
    return ret;
  }

  Method createMethod(Type containingType, ExecutableElement executableElement) {
    Set<Modifier> modifiers = requireNonNull(executableElement, "`executableElement`")
        .getModifiers();
    var ret = new Method(containingType,
        executableElement.getSimpleName().toString(),
        TypeNameVisitor.INSTANCE.visit(executableElement.getReturnType()));
    {
      ret.setVisibility(visibilityOf(modifiers));
      ret.setAbstract(modifiers.contains(Modifier.ABSTRACT));
      ret.setStatic(modifiers.contains(Modifier.STATIC));
      ret.setDeprecated(extension.getEnv().getElementUtils().isDeprecated(executableElement));
      ret.addChild(createParameters(executableElement.getParameters()));
    }
    return ret;
  }

  Namespace createPackage(Diagram diagram, PackageElement packageElement,
      Map<String, Collection<Type>> foreignTypes, List<Reference> references,
      String referenceSeparator) {
    final ModuleElement module = extension.getEnv().getElementUtils().getModuleOf(packageElement);
    var pkg = new Namespace(diagram, packageElement.getQualifiedName().toString(),
        module != null ? module.getQualifiedName().toString() : null);

    // Add all types contained in this package.
    packageElement.getEnclosedElements().stream()
        .filter(TypeElement.class::isInstance).map(TypeElement.class::cast)
        .flatMap(UmlFactory::innerTypes)
        .filter(extension.getEnv()::isIncluded)
        .map($typeElement -> {
          Type type = createAndPopulateType(pkg, $typeElement);
          references.addAll(findPackageReferences(pkg, foreignTypes, $typeElement, type,
              referenceSeparator));
          return type;
        })
        .flatMap($type -> Stream.of(UmlLiteral.NEWLINE, $type))
        .forEach(pkg::addChild);

    return pkg;
  }

  Namespace packageOf(TypeElement typeElement) {
    final ModuleElement module = extension.getEnv().getElementUtils().getModuleOf(typeElement);
    return new Namespace(null, extension.getEnv().getElementUtils().getPackageOf(typeElement)
        .getQualifiedName().toString(),
        module != null ? module.getQualifiedName().toString() : null);
  }

  private void addForeignType(@Nullable Map<String, Collection<Type>> foreignTypes,
      Element typeElement) {
    if (foreignTypes != null && typeElement instanceof TypeElement e) {
      Type type = createAndPopulateType(null, e);
      if (e.getKind().isClass()) {
        type.removeChildren($ -> $ instanceof Method method && !method.isAbstract());
      }
      foreignTypes.computeIfAbsent(type.getPackageName(), $k -> new LinkedHashSet<>()).add(type);
    }
  }

  private Type createAndPopulateType(@Nullable Namespace containingPackage, TypeElement type) {
    return populateType(createType(containingPackage, type), type);
  }

  private ParameterList createParameters(List<? extends VariableElement> params) {
    ParameterList result = new ParameterList(null);
    Boolean varargs = null;
    for (VariableElement param : params) {
      if (varargs == null) {
        result = result.varargs(varargs = isVarArgsMethod(param.getEnclosingElement()));
      }
      result = result.add(param.getSimpleName().toString(),
          TypeNameVisitor.INSTANCE.visit(param.asType()));
    }
    return result;
  }

  /**
   * Creates an 'empty' type (that is, without any fields, constructors or methods).
   *
   * @param containingPackage
   *          The containing package of the type (optional, will be obtained from typeElement if
   *          {@code null}).
   * @param type
   *          The type element to create a Type object for.
   */
  private Type createType(@Nullable Namespace containingPackage, TypeElement type) {
    if (containingPackage == null) {
      containingPackage = packageOf(type);
    }
    requireNonNull(type, "`type`");
    return new Type(containingPackage, typeClassificationOf(type),
        TypeNameVisitor.INSTANCE.visit(type.asType()));
  }

  private Collection<Reference> findPackageReferences(
      Namespace namespace, Map<String, Collection<Type>> foreignTypes, TypeElement typeElement,
      Type type, String separator) {
    Collection<Reference> references = new LinkedHashSet<>();

    // Superclass reference.
    TypeMirror superclassType = typeElement.getSuperclass();
    Element superclassElement = extension.getEnv().getTypeUtils().asElement(superclassType);
    while (superclassElement instanceof TypeElement
        && !includeSuperclass((TypeElement) superclassElement)) {
      superclassType = ((TypeElement) superclassElement).getSuperclass();
      superclassElement = extension.getEnv().getTypeUtils().asElement(superclassType);
    }
    if (superclassElement instanceof TypeElement) {
      TypeName superclassName = TypeNameVisitor.INSTANCE.visit(superclassType);
      if (!extension.getExtConfig().getExcludedTypeReferences()
          .contains(superclassName.getQualifiedName())) {
        references.add(new Reference(
            Reference.from(type.getName().getQualifiedName(separator), null),
            PUML_REF__EXTENDS,
            Reference.to(superclassName.getQualifiedName(separator), null)));
        if (!namespace.contains(superclassName)) {
          addForeignType(foreignTypes, superclassElement);
        }
      }
    }

    // Implemented interfaces.
    typeElement.getInterfaces().forEach($interfaceType -> {
      TypeName interfaceName = TypeNameVisitor.INSTANCE.visit($interfaceType);
      if (!extension.getExtConfig().getExcludedTypeReferences()
          .contains(interfaceName.getQualifiedName())) {
        references.add(new Reference(
            Reference.from(type.getName().getQualifiedName(separator), null),
            interfaceReferenceTypeFrom(type),
            Reference.to(interfaceName.getQualifiedName(separator), null)));
        // TODO Figure out what to do IF the interface is found BUT has a different typename
        if (!namespace.contains(interfaceName)) {
          addForeignType(foreignTypes, extension.getEnv().getTypeUtils().asElement($interfaceType));
        }
      }
    });

    // Add reference to containing class from inner classes.
    ElementKind enclosingKind = typeElement.getEnclosingElement().getKind();
    if (enclosingKind.isClass() || enclosingKind.isInterface()) {
      TypeName parentType =
          TypeNameVisitor.INSTANCE.visit(typeElement.getEnclosingElement().asType());
      references.add(new Reference(
          Reference.from(parentType.getQualifiedName(separator), null),
          PUML_REF__ENCLOSES,
          Reference.to(type.getName().getQualifiedName(separator), null)));
      // No check needed whether parent type lives in our namespace.
    }

    // Add 'uses' reference by replacing visible getters/setters
    typeElement.getEnclosedElements().stream()
        .filter($ -> ElementKind.METHOD.equals($.getKind()))
        .filter(ExecutableElement.class::isInstance).map(ExecutableElement.class::cast)
        .filter($method -> extension.getExtConfig().getMethodConfig()
            .includes(visibilityOf($method.getModifiers())))
        .forEach($method -> {
          String propertyName = propertyName($method);
          if (propertyName != null) {
            TypeNameWithCardinality returnType =
                typeNameWithCardinality.apply(propertyType($method));
            if (namespace.contains(returnType.typeName)) {
              addReference(references, new Reference(
                  Reference.from(type.getName().getQualifiedName(separator), null),
                  PUML_REF__ASSOCIATES,
                  Reference.to(returnType.typeName.getQualifiedName(separator),
                      returnType.cardinality),
                  propertyName));
              type.removeChildren($ -> $ instanceof Method m
                  && m.getName().equals($method.getSimpleName().toString()));
            }
          }
        });

    return references;
  }

  // SourceName: methodsFromExcludedSuperclasses()
  private Collection<ExecutableElement> getMethodsFromExcludedSuperclasses() {
    if (methodsFromExcludedSuperclasses == null) {
      methodsFromExcludedSuperclasses =
          extension.getExtConfig().getExcludedTypeReferences().stream()
              .map(extension.getEnv().getElementUtils()::getTypeElement).filter(Objects::nonNull)
              .map(TypeElement::getEnclosedElements).flatMap(Collection::stream)
              .filter($element -> ElementKind.METHOD.equals($element.getKind()))
              .filter(ExecutableElement.class::isInstance).map(ExecutableElement.class::cast)
              .filter($method -> !$method.getModifiers().contains(Modifier.ABSTRACT))
              .filter($method -> visibilityOf($method.getModifiers())
                  .compareTo(Visibility.PRIVATE) > 0)
              .collect(toCollection(LinkedHashSet::new));
    }
    return methodsFromExcludedSuperclasses;
  }

  /**
   * Determine whether a superclass is included in the documentation.
   * <p>
   * Introduced to fix <a href="https://github.com/talsma-ict/umldoclet/issues/146">issue 146</a>:
   * skip superclass if not included in the documentation.
   * </p>
   *
   * @param superclass
   *          The superclass to test.
   * @return {@code true} if the superclass is within the documented javadoc part, or if its
   *         modifiers have the 'right' accesibility. See {#148} for accessibility details.
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean includeSuperclass(TypeElement superclass) {
    if (extension.getEnv().isIncluded(superclass))
      return true;
    // TODO Make configurable:
    // See https://github.com/talsma-ict/umldoclet/issues/148
    return superclass.getModifiers().contains(Modifier.PUBLIC)
        || superclass.getModifiers().contains(Modifier.PROTECTED);
  }

  private boolean isExcludedEnumMethod(ExecutableElement method) {
    if (extension.getExtConfig().getExcludedTypeReferences().contains(Enum.class.getName())
        && ElementKind.ENUM.equals(method.getEnclosingElement().getKind())
        && method.getModifiers().contains(Modifier.STATIC)) {
      if ("values".equals(method.getSimpleName().toString()) && method.getParameters().isEmpty())
        return true;
      else if ("valueOf".equals(method.getSimpleName().toString())
          && method.getParameters().size() == 1) {
        String paramType =
            TypeNameVisitor.INSTANCE.visit(method.getParameters().get(0).asType())
                .getQualifiedName();
        return String.class.getName().equals(paramType);
      }
    }
    return false;
  }

  private boolean isMethodFromExcludedSuperclass(ExecutableElement method) {
    boolean result = false;
    Element containingClass = method.getEnclosingElement();
    if (containingClass.getKind().isClass() || containingClass.getKind().isInterface()) {
      result = getMethodsFromExcludedSuperclasses().stream().anyMatch(
          $ -> similarMethodSignatures($, method)
              && extension.getEnv().getTypeUtils().isAssignable(containingClass.asType(),
                  $.getEnclosingElement().asType()));
    }
    result = result || isExcludedEnumMethod(method);
    return result;
  }

  private boolean isOnlyDefaultConstructor(Collection<ExecutableElement> constructors) {
    return constructors.size() == 1 && constructors.iterator().next().getParameters().isEmpty();
  }

  private Type populateType(Type type, TypeElement typeElement) {
    // Add the various parts of the class UML, order matters here, obviously!
    List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
    if (Type.Classification.ENUM.equals(type.getClassfication())) {
      enclosedElements.stream()
          .filter($ -> ElementKind.ENUM_CONSTANT.equals($.getKind()))
          .filter(VariableElement.class::isInstance).map(VariableElement.class::cast)
          .map($enumConst -> createField(type, $enumConst))
          .forEach(type::addChild);
    }

    // Static fields.
    {
      /*
       * NOTE: As static fields may count in the tens or even hundreds, uselessly bloating their UML
       * representation, for convenience they are truncated to the first few, followed by an
       * ellipsis.
       */
      final int staticMaxCount = extension.getExtConfig().getStaticFieldsMaxCount();
      var staticCounter = new MutableInt();
      enclosedElements.stream()
          .filter($ -> ElementKind.FIELD.equals($.getKind()))
          .filter(VariableElement.class::isInstance).map(VariableElement.class::cast)
          .filter($ -> $.getModifiers().contains(Modifier.STATIC))
          .sorted(Comparator.comparing($ -> $.getSimpleName().toString()))
          .limit(staticMaxCount + 1)
          .map($field -> staticCounter.getAndIncrement() < staticMaxCount
              ? createField(type, $field)
              : UmlLiteral.ELLIPSIS)
          .forEach(type::addChild);
    }

    // Instance fields.
    enclosedElements.stream()
        .filter($ -> ElementKind.FIELD.equals($.getKind()))
        .filter(VariableElement.class::isInstance).map(VariableElement.class::cast)
        .filter($ -> !$.getModifiers().contains(Modifier.STATIC))
        .sorted(Comparator.comparing($ -> $.getSimpleName().toString()))
        .map($field -> createField(type, $field))
        .forEach(type::addChild);

    List<ExecutableElement> constructors = enclosedElements.stream()
        .filter($ -> ElementKind.CONSTRUCTOR.equals($.getKind()))
        .filter(ExecutableElement.class::isInstance).map(ExecutableElement.class::cast)
        .collect(toList());
    if (!isOnlyDefaultConstructor(constructors)) {
      constructors.stream()
          .map($ -> createConstructor(type, $))
          .forEach(type::addChild);
    }

    enclosedElements.stream()
        .filter($ -> ElementKind.METHOD.equals($.getKind()))
        .filter(ExecutableElement.class::isInstance).map(ExecutableElement.class::cast)
        .filter($method -> !isMethodFromExcludedSuperclass($method))
        .map($method -> createMethod(type, $method))
        .forEach(type::addChild);

    return extension.getEnv().getElementUtils().isDeprecated(typeElement) ? type.deprecate()
        : type;
  }

  private boolean similarMethodSignatures(ExecutableElement method1, ExecutableElement method2) {
    if (!method1.getSimpleName().equals(method2.getSimpleName()))
      return false;
    int paramCount = method1.getParameters().size();
    if (paramCount != method2.getParameters().size())
      return false;

    Types typeUtils = extension.getEnv().getTypeUtils();
    boolean assignable1 = true;
    boolean assignable2 = true;
    for (int i = 0; i < paramCount && (assignable1 || assignable2); i++) {
      TypeMirror param1 = method1.getParameters().get(i).asType();
      TypeMirror param2 = method2.getParameters().get(i).asType();
      assignable1 = assignable1 && typeUtils.isAssignable(param1, param2);
      assignable2 = assignable2 && typeUtils.isAssignable(param2, param1);
    }
    return assignable1 || assignable2;
  }
}
