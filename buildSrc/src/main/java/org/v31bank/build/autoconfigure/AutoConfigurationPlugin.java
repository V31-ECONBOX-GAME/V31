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

package org.v31bank.build.autoconfigure;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import org.v31bank.build.DeployedPlugin;
import org.v31bank.build.constant.Configurations;
import org.v31bank.build.constant.Coordinates;
import org.v31bank.build.constant.Locations;
import org.v31bank.build.constant.Tasks;
import org.v31bank.build.optional.OptionalDependenciesPlugin;
import org.v31bank.build.util.SourceSets;

/**
 * Publishes an auto-configuration module and wires up its processor, metadata and checks.
 *
 * <pre class="code">
 * plugins {
 *     `java-library`
 *     id("org.v31bank.auto-configuration")
 * }
 * </pre>
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class AutoConfigurationPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(DeployedPlugin.class);
		project.getPlugins().withType(JavaPlugin.class, (_) -> configure(project));
	}

	private void configure(Project project) {
		SourceSet main = SourceSets.of(project).main().unwrap();
		addAnnotationProcessors(project);
		registerMetadata(project, main);
		registerChecks(project, main);
	}

	private void addAnnotationProcessors(Project project) {
		project.getDependencies()
			.add(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME, Coordinates.AUTO_CONFIGURATION_PROCESSOR);
	}

	private void registerMetadata(Project project, SourceSet main) {
		project.getConfigurations()
			.consumable(Configurations.AUTO_CONFIGURATION_METADATA,
					(configuration) -> configuration.attributes((attributes) -> {
						attributes.attribute(Category.CATEGORY_ATTRIBUTE,
								project.getObjects().named(Category.class, Category.DOCUMENTATION));
						attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects()
							.named(Usage.class, Configurations.AUTO_CONFIGURATION_METADATA_USAGE));
					}));
		TaskProvider<AutoConfigurationMetadata> metadata = project.getTasks()
			.register(Configurations.AUTO_CONFIGURATION_METADATA, AutoConfigurationMetadata.class, (task) -> {
				task.setDescription("Generates metadata describing the module's auto-configurations.");
				task.setSourceSet(main);
				task.dependsOn(main.getClassesTaskName());
				task.getOutputFile()
					.set(project.getLayout().getBuildDirectory().file(Locations.AUTO_CONFIGURATION_METADATA_FILE));
			});
		project.getArtifacts()
			.add(Configurations.AUTO_CONFIGURATION_METADATA, metadata.map(AutoConfigurationMetadata::getOutputFile));
	}

	private void registerChecks(Project project, SourceSet main) {
		TaskProvider<CheckAutoConfigurationImports> checkImports = project.getTasks()
			.register(Tasks.CHECK_AUTO_CONFIGURATION_IMPORTS, CheckAutoConfigurationImports.class, (task) -> {
				task.setDescription("Checks the %s file of the main source set."
					.formatted(Locations.AUTO_CONFIGURATION_IMPORTS_FILE));
				readMainSourceSet(task, main);
			});
		TaskProvider<CheckAutoConfigurationClasses> checkClasses = project.getTasks()
			.register(Tasks.CHECK_AUTO_CONFIGURATION_CLASSES, CheckAutoConfigurationClasses.class, (task) -> {
				task.setDescription("Checks the auto-configuration classes of the main source set.");
				readMainSourceSet(task, main);
				task.getRequiredDependencies().from(requiredClasspath(project, main));
			});
		project.getPlugins()
			.withType(OptionalDependenciesPlugin.class, (_) -> checkClasses
				.configure((task) -> task.getOptionalDependencies().from(optionalClasspath(project))));
		project.getTasks()
			.named(LifecycleBasePlugin.CHECK_TASK_NAME)
			.configure((check) -> check.dependsOn(checkImports, checkClasses));
	}

	private void readMainSourceSet(AutoConfigurationImportsTask task, SourceSet main) {
		task.getResources().from(SourceSets.of(main).resources().unwrap());
		task.getClasspath().from(main.getOutput().getClassesDirs());
	}

	private Configuration requiredClasspath(Project project, SourceSet main) {
		ConfigurationContainer configurations = project.getConfigurations();
		return configurations.resolvable(Configurations.AUTO_CONFIGURATION_REQUIRED_CLASSPATH,
				(configuration) -> configuration.extendsFrom(
						configurations.getByName(main.getImplementationConfigurationName()),
						configurations.getByName(main.getRuntimeOnlyConfigurationName())))
			.get();
	}

	private Configuration optionalClasspath(Project project) {
		ConfigurationContainer configurations = project.getConfigurations();
		Configuration optional = configurations.getByName(Configurations.OPTIONAL);
		return configurations
			.resolvable(Configurations.AUTO_CONFIGURATION_OPTIONAL_CLASSPATH,
					(configuration) -> configuration.extendsFrom(optional))
			.get();
	}

}
