/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.v31bank.build.classpath;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.tasks.TaskAction;

/**
 * Fails when a starter declares a dependency nothing constrains.
 *
 * @author Xander Wang
 */
public abstract class CheckClasspathForUnconstrainedDirectDependencies extends ClasspathCheck {

	@TaskAction
	void checkForUnconstrainedDirectDependencies() {
		ResolvedComponentResult root = getRootComponent().get();
		Set<String> unconstrained = moduleIds(root.getDependencies().stream());
		unconstrained.removeAll(moduleIds(everyEdgeFrom(root).filter(DependencyResult::isConstraint)));
		if (!unconstrained.isEmpty()) {
			throw new GradleException("Found unconstrained direct dependencies: " + unconstrained);
		}
	}

	private Stream<? extends DependencyResult> everyEdgeFrom(ResolvedComponentResult root) {
		Set<ResolvedComponentResult> seen = new HashSet<>();
		List<DependencyResult> edges = new ArrayList<>();
		Deque<ResolvedComponentResult> queue = new ArrayDeque<>(List.of(root));
		while (!queue.isEmpty()) {
			ResolvedComponentResult component = queue.removeFirst();
			if (!seen.add(component)) {
				continue;
			}
			for (DependencyResult edge : component.getDependencies()) {
				edges.add(edge);
				if (edge instanceof ResolvedDependencyResult resolved) {
					queue.addLast(resolved.getSelected());
				}
			}
		}
		return edges.stream();
	}

	private Set<String> moduleIds(Stream<? extends DependencyResult> dependencies) {
		return dependencies.map(DependencyResult::getRequested)
			.filter(ModuleComponentSelector.class::isInstance)
			.map(ModuleComponentSelector.class::cast)
			.map((selector) -> selector.getGroup() + ":" + selector.getModule())
			.collect(Collectors.toCollection(TreeSet::new));
	}

}
