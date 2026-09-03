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

package org.v31bank.build.optional;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.JavaPlugin;

import org.v31bank.build.constant.Configurations;
import org.v31bank.build.util.SourceSets;

/**
 * Adds an {@code optional} configuration used here but not by consumers.
 *
 * <pre class="code">
 * plugins {
 *     id("org.v31bank.optional-dependencies")
 * }
 * </pre>
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class OptionalDependenciesPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		ConfigurationContainer configurations = project.getConfigurations();
		Configuration optional = configurations.dependencyScope(Configurations.OPTIONAL).get();
		project.getPlugins().withType(JavaPlugin.class, (_) -> SourceSets.of(project).unwrap().all((sourceSet) -> {
			configurations.getByName(sourceSet.getCompileClasspathConfigurationName()).extendsFrom(optional);
			configurations.getByName(sourceSet.getRuntimeClasspathConfigurationName()).extendsFrom(optional);
		}));
	}

}
