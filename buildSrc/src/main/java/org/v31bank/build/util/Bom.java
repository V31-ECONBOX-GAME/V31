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

package org.v31bank.build.util;

import java.util.Objects;
import java.util.stream.Stream;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.plugins.JavaBasePlugin;

/**
 * The version a BOM settles a module at:
 *
 * <pre class="code">
 * Bom.of(project, SpringBootPlugin.BOM_COORDINATES).version("com.google.protobuf:protobuf-java")
 * Bom.of(project, project.project(":platform:V31-dependencies")).version("io.grpc:grpc-protobuf")
 * </pre>
 *
 * The BOM is asked rather than read: the module is resolved without a version of its own,
 * beside the BOM, so the version it comes back with is the one it would have on a
 * consumer's classpath. Nothing here knows what a pom looks like, so a BOM that renames a
 * property, spells one in terms of another or inherits one from a parent needs no
 * allowance made for it.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class Bom {

	private final Project project;

	private final Object notation;

	private final String name;

	private Bom(Project project, Object notation, String name) {
		this.project = project;
		this.notation = notation;
		this.name = name;
	}

	/**
	 * The versions a BOM settles.
	 * @param project the project to resolve with, which has to be one that resolves Java
	 * libraries: matching a platform's variant takes the attributes {@code java-base}
	 * brings, and without them a pom answers with its default variant, which carries none
	 * of its constraints
	 * @param notation what names the BOM, in any notation
	 * {@link org.gradle.api.artifacts.dsl.DependencyHandler#platform(Object) platform}
	 * takes: a {@code group:artifact:version} string, a project that publishes one, a
	 * dependency already in hand
	 * @return the versions it settles
	 * @throws GradleException if the project resolves no Java libraries
	 */
	public static Bom of(Project project, Object notation) {
		if (!project.getPlugins().hasPlugin(JavaBasePlugin.class)) {
			throw new GradleException(
					"%s resolves no Java libraries, so a BOM asked there answers with nothing settled."
						.formatted(project.getDisplayName()));
		}
		return new Bom(project, notation, nameOf(project.getDependencies().create(notation)));
	}

	/**
	 * The version this BOM settles a module at.
	 * <p>
	 * Answered on the spot rather than through a {@link org.gradle.api.provider.Provider
	 * Provider}: a provider handed to a task is queried as the configuration cache is
	 * written, and a configuration cannot be resolved there. Call it while configuring,
	 * where resolving is allowed.
	 * @param module the module, as {@code group:name}
	 * @return its version
	 * @throws GradleException if the BOM settles no version for the module
	 */
	public String version(String module) {
		try {
			return settled(resolve(module), module);
		}
		catch (ResolveException ex) {
			throw settlesNothing(module, ex);
		}
	}

	/**
	 * The module asked for without a version of its own, so that the only version it can
	 * come back with is the one the BOM beside it hands out.
	 * @param module the module, as {@code group:name}
	 * @return what the pair resolves to
	 */
	private ResolutionResult resolve(String module) {
		DependencyHandler dependencies = this.project.getDependencies();
		Configuration against = this.project.getConfigurations()
			.detachedConfiguration(dependencies.platform(this.notation), dependencies.create(module));
		return against.getIncoming().getResolutionResult();
	}

	private String settled(ResolutionResult resolution, String module) {
		for (DependencyResult dependency : resolution.getRoot().getDependencies()) {
			if (dependency instanceof ResolvedDependencyResult resolved) {
				ModuleVersionIdentifier selected = resolved.getSelected().getModuleVersion();
				if (selected != null && module.equals(selected.getGroup() + ":" + selected.getName())) {
					return selected.getVersion();
				}
			}
		}
		throw settlesNothing(module, null);
	}

	private GradleException settlesNothing(String module, Throwable cause) {
		return new GradleException("%s settles no version for %s.".formatted(this.name, module), cause);
	}

	/**
	 * What to call the BOM when it has to be named in a failure, since a notation can
	 * arrive as anything.
	 * @param bom the dependency the notation was read as
	 * @return the coordinate it stands for
	 */
	private static String nameOf(Dependency bom) {
		return Stream.of(bom.getGroup(), bom.getName(), bom.getVersion())
			.filter(Objects::nonNull)
			.reduce((left, right) -> left + ":" + right)
			.orElseGet(bom::getName);
	}

}
