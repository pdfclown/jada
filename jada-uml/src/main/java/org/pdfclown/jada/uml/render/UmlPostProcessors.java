/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlPostProcessors.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.uml.render;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.uml.render.model.Field;
import org.pdfclown.jada.uml.render.model.Method;
import org.pdfclown.jada.uml.render.model.Parameters;
import org.pdfclown.jada.uml.render.model.Type;
import org.pdfclown.jada.uml.render.model.TypeMember;
import org.pdfclown.jada.uml.render.model.TypeName;
import org.pdfclown.jada.uml.render.model.UmlNode;

// SourceName: nl.talsmasoftware.umldoclet.uml.util.UmlPostProcessors
/**
 * Post-processing functionality for generated UML models.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class UmlPostProcessors {
  // SourceName: nl.talsmasoftware.umldoclet.uml.util.JavaBeanProperty
  /**
   * Representation of a property.
   * <p>
   * A JavaBeans property {@biblio.spec JAVA-BEANS} comprises read and write methods to access
   * object's state (either a field or dynamic information). This class represents a superset of
   * JavaBeans properties, as any method syntactically compatible with the JavaBeans specification,
   * no matter its visibility or concreteness, is detected as property.
   * </p>
   *
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   * @implNote Contrary to the upstream project, non-public and abstract accessors are considered
   *           properties in order to make UML diagrams as terse and logically consistent as
   *           possible.
   */
  static class Property {
    /**
     * Detects the properties from the UML {@link Type} of a Java class.
     * <p>
     * The following members will be matched:
     * </p>
     * <ul>
     * <li>any {@linkplain Field field}</li>
     * <li>any getter {@linkplain Method method}</li>
     * <li>any setter {@linkplain Method method}</li>
     * </ul>
     *
     * @implNote Contrary to the upstream project (which requires properties to be public), members
     *           are considered properties no matter their visibility.
     */
    public static Collection<Property> detectFrom(@Nullable Type type) {
      if (type == null)
        return emptySet();

      final Map<String, Property> propertiesByName = new LinkedHashMap<>();
      type.getChildren().stream()
          .filter(TypeMember.class::isInstance)
          .map(TypeMember.class::cast)
          .forEach($typeMember -> propertyNameOf($typeMember).ifPresent(
              $propertyName -> propertiesByName
                  .computeIfAbsent($propertyName, Property::new)
                  .add($typeMember)));
      return propertiesByName.values();
    }

    private static boolean isBooleanGetterMethod(Method method) {
      return isBooleanType(method.getType()) && method.getName().startsWith("is")
          && parameterCount(method) == 0;
    }

    /**
     * Determines whether the type is boolean (either primitive {@code boolean} or {@link Boolean}
     * wrapper).
     */
    private static boolean isBooleanType(@Nullable TypeName type) {
      if (type == null)
        return false;

      var qName = type.getQualifiedName();
      return "boolean".equals(qName) || "java.lang.Boolean".equals(qName);
    }

    private static boolean isGetterMethod(Method method) {
      return method.getType() != null && method.getName().startsWith("get")
          && parameterCount(method) == 0;
    }

    private static boolean isSetterMethod(Method method) {
      return method.getName().startsWith("set") && parameterCount(method) == 1;
    }

    /**
     * Gets the number of parameters of the method.
     */
    private static int parameterCount(Method method) {
      return method.getChildren().stream()
          .filter(Parameters.class::isInstance)
          .map(UmlNode::getChildren).mapToInt(Collection::size)
          .sum();
    }

    /**
     * Gets the property name corresponding to the type member, if any.
     * <p>
     * {@linkplain Field} names are returned as-is; getter/setter {@linkplain Method methods} are
     * stripped of their prefix (that is, {@code "get"}, {@code "is"}, or {@code "set"}) and the
     * initial character is converted to lower-case.
     * </p>
     *
     * @implNote Contrary to the upstream project (which requires properties not to be abstract),
     *           abstract accessors are considered properties.
     */
    private static Optional<String> propertyNameOf(TypeMember member) {
      var ret = Optional.<String>empty();
      if (member instanceof Field) {
        ret = Optional.of(member.getName());
      } else if (member instanceof Method method) {
        // SourceName: propertyNameOfAccessor(Method)
        if (!method.isStatic()) {
          if (isGetterMethod(method) || isSetterMethod(method)) {
            ret = Optional.of(uncapitalize(
                method.getName().substring(3) /* Removes "get"/"set" prefix */));
          } else if (isBooleanGetterMethod(method)) {
            ret = Optional.of(uncapitalize(
                method.getName().substring(2) /* Removes "is" prefix */));
          }
        }
      }
      return ret;
    }

    private @Nullable Field field;
    private @Nullable Method getter;
    private final String name;
    @SuppressWarnings({ "FieldCanBeLocal", "unused" })
    private @Nullable Method setter;

    private Property(String name) {
      this.name = name;
    }

    /**
     * Replaces the getter and setter methods with the corresponding field in the parent type.
     * <p>
     * <span class="important">IMPORTANT: This method modifies the parent {@linkplain Type type}
     * in-place, therefore it is NOT thread-safe.</span>
     * </p>
     *
     * @implNote Contrary to the upstream project (which requires both getter and setter to be
     *           present), replacement occurs even if only the getter is present; moreover,
     *           {@link Field#isDeprecated() deprecated} is set.
     */
    void replaceGetterAndSetterByField() {
      if (getter != null) {
        // Convert the getter into a field for UML rendering purposes.
        final var type = (Type) requireNonNull(getter.getParent(), "`getter.getParent()`");
        field = new Field(type, name, requireNonNull(getter.getType(), "`getter.getType()`"));
        {
          field.setVisibility(getter.getVisibility());
          field.setDeprecated(getter.isDeprecated());
        }
        type.removeChildren(this::isSameProperty);
        type.addChild(field);
      }
    }

    /**
     * Adds a detected {@link Field} or {@link Method} to this property.
     * <p>
     * This method assumes that the member conforms to the correct naming convention for JavaBeans,
     * no additional checks are performed.
     * </p>
     */
    private void add(TypeMember member) {
      if (member instanceof Field) {
        field = (Field) member;
      } else if (member instanceof Method) {
        if (member.getName().startsWith("set")) {
          setter = (Method) member;
        } else {
          getter = (Method) member;
        }
      }
    }

    /**
     * Tests whether the {@linkplain #propertyNameOf(TypeMember) property name of} the UML node
     * matches the name of this property.
     *
     * @implNote Although this method accepts any {@link UmlNode}, only {@link Field} and
     *           {@link Method} instances can test positive.
     */
    private boolean isSameProperty(UmlNode node) {
      return node instanceof TypeMember typeMember
          && propertyNameOf(typeMember).filter(name::equals).isPresent();
    }
  }

  // SourceName: javaBeanPropertiesAsFieldsPostProcessor
  /**
   * A post-processor for a UML {@link Type} to replace accessor {@linkplain Method methods} (that
   * is, getters and setters) with corresponding {@linkplain Field fields}.
   *
   * @implNote Contrary to the upstream project, non-public and abstract accessors are considered
   *           properties in order to make UML diagrams as terse and logically consistent as
   *           possible.
   */
  public Consumer<Type> propertiesToFields() {
    return $type -> Property.detectFrom($type)
        .forEach(Property::replaceGetterAndSetterByField);
  }
}
