/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageDependencyCycle.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render;

import static java.util.stream.Collectors.joining;
import static org.pdfclown.common.util.Exceptions.wrongArg;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.RandomAccess;
import java.util.Set;
import org.jspecify.annotations.Nullable;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.dependencies.PackageDependencyCycle
/**
 * A cycle of dependencies.
 * <p>
 * Package Dependencies can form a cycle if the chain of package dependencies somehow 'return' to
 * the initial package. For example if you have three packages {@code a}, {@code b} and {@code c}
 * and the following dependencies: {@code a -> b}, {@code b -> c}, they will form a cycle if you
 * somehow create a dependency back to {@code a}, for example {@code b -> a} or {@code c -> a}.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class PackageDependencyCycle extends AbstractList<PackageDependency>
    implements RandomAccess {

  /**
   * Detect cycles in a collection of dependencies.
   *
   * @param dependencies
   *          The package dependencies to detect dependency cycles from.
   * @return A set with all found dependency cycles.
   * @implNote First the collection of dependencies is converted to a list of dependency
   *           <em>chains</em>. Next, the dependency chains are selected from all chains and
   *           returned as a new set.
   */
  public static Set<PackageDependencyCycle> detectCycles(Iterable<PackageDependency> dependencies) {
    var chains = new LinkedList<PackageDependency[]>();
    for (var dependency : dependencies) {
      var newChains = new ArrayList<PackageDependency[]>();
      for (var chain : chains) {
        if (dependency.fromPackage.equals(lastPackageName(chain))) {
          PackageDependency[] longerChain = growChain(chain, dependency);
          if (longerChain != null) {
            newChains.add(longerChain);
          }
        }
      }
      chains.addAll(newChains);
      chains.add(new PackageDependency[] { dependency });
    }
    var cycles = new LinkedHashSet<PackageDependencyCycle>();
    for (Iterator<PackageDependency[]> it = chains.iterator(); it.hasNext(); it.remove()) {
      var chain = it.next();
      if (chain.length > 1 && chain[0].fromPackage.equals(lastPackageName(chain))) {
        cycles.add(new PackageDependencyCycle(chain));
      }
    }
    return cycles;
  }

  private static PackageDependency @Nullable [] growChain(PackageDependency[] chain,
      PackageDependency dependency) {
    var longerChain = new PackageDependency[chain.length + 1];
    for (int i = 0; i < chain.length; i++) {
      if (dependency.equals(chain[i]))
        return null;
      else {
        longerChain[i] = chain[i];
      }
    }
    longerChain[chain.length] = dependency;
    return longerChain;
  }

  private static @Nullable String lastPackageName(PackageDependency[] chain) {
    return chain.length == 0 ? null : chain[chain.length - 1].toPackage;
  }

  private final PackageDependency[] cycle;

  /**
   * @param dependencies
   *          Package dependencies that form a cycle.
   * @throws IllegalArgumentException
   *           if {@code dependencies} do NOT form a cycle.
   */
  public PackageDependencyCycle(PackageDependency... dependencies) {
    if (dependencies.length < 1)
      throw wrongArg("dependencies", dependencies, "A dependency cycle CANNOT be empty");

    this.cycle = dependencies;
    // check for an actual cycle
    for (int i = 0; i < dependencies.length; i++) {
      String to = dependencies[i].toPackage;
      String from = dependencies[(i + 1) % dependencies.length].fromPackage;
      if (!to.equals(from))
        throw wrongArg("dependencies", dependencies, "Not a dependency cycle: \"{}\" != \"{}\": {}",
            to, from, this);
    }
  }

  @Override
  public PackageDependency get(int index) {
    return cycle[index];
  }

  @Override
  public int size() {
    return cycle.length;
  }

  @Override
  public String toString() {
    return cycle[0].fromPackage + " > "
        + stream().map($ -> $.toPackage).collect(joining(" > "));
  }
}
