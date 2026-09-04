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

package org.v31bank.build.properties;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import org.v31bank.build.constant.Configurations;
import org.v31bank.build.constant.Coordinates;
import org.v31bank.build.constant.Locations;
import org.v31bank.build.constant.Tasks;
import org.v31bank.build.util.Directories;
import org.v31bank.build.util.SourceSets;

/**
 * Generates configuration metadata with {@code spring-boot-configuration-processor},
 * checks it and offers it onward.
 *
 * <pre class="code">
 * plugins {
 *     `java-library`
 *     id("org.v31bank.configuration-properties")
 * }
 * </pre>
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class ConfigurationPropertiesPlugin implements Plugin<Project> {

	private static final String ADDITIONAL_LOCATIONS_ARGUMENT = "-Aorg.springframework.boot.configurationprocessor.additionalMetadataLocations=";

	@Override
	public void apply(Project project) {
		project.getPlugins().withType(JavaPlugin.class, (_) -> configure(project));
	}

	private void configure(Project project) {
		SourceSet main = SourceSets.of(project).main().unwrap();
		addAnnotationProcessor(project);
		compileWholeSourceSet(project, main);
		readAdditionalMetadata(project, main);
		registerChecks(project, main);
		offerMetadata(project, main);
	}

	private void addAnnotationProcessor(Project project) {
		project.getDependencies()
			.add(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME, Coordinates.CONFIGURATION_PROCESSOR);
	}

	/**
	 * Incremental compilation loses the descriptions of unchanged classes.
	 * @param project the project to configure
	 * @param main the main source set
	 */
	private void compileWholeSourceSet(Project project, SourceSet main) {
		project.getTasks()
			.named(main.getCompileJavaTaskName(), JavaCompile.class)
			.configure((compile) -> compile.getOptions().setIncremental(false));
	}

	private void readAdditionalMetadata(Project project, SourceSet main) {
		project.getTasks().named(main.getCompileJavaTaskName(), JavaCompile.class).configure((compile) -> {
			compile.getInputs()
				.files(project.getTasks().named(main.getProcessResourcesTaskName()))
				.withPathSensitivity(PathSensitivity.RELATIVE)
				.withPropertyName("processed resources");
			compile.getOptions().getCompilerArgs().add(ADDITIONAL_LOCATIONS_ARGUMENT + locations(project, main));
		});
	}

	private String locations(Project project, SourceSet main) {
		return String.join(",", SourceSets.of(main).resources().relativeTo(Directories.rootDirOf(project)));
	}

	private void registerChecks(Project project, SourceSet main) {
		TaskProvider<CheckAdditionalSpringConfigurationMetadata> checkAdditional = project.getTasks()
			.register(Tasks.CHECK_ADDITIONAL_CONFIGURATION_METADATA, CheckAdditionalSpringConfigurationMetadata.class,
					(task) -> {
						task.setDescription("Checks the hand-written configuration metadata of the main source set.");
						task.setSource(SourceSets.of(main).resources().unwrap());
						task.include(Locations.ADDITIONAL_CONFIGURATION_METADATA_FILE);
						task.getReportLocation().set(report(project, "additional-spring-configuration-metadata"));
					});
		TaskProvider<CheckSpringConfigurationMetadata> checkGenerated = project.getTasks()
			.register(Tasks.CHECK_CONFIGURATION_METADATA, CheckSpringConfigurationMetadata.class, (task) -> {
				task.setDescription("Checks the generated configuration metadata of the main source set.");
				task.getMetadataLocation().set(generatedMetadata(project, main));
				task.getReportLocation().set(report(project, "spring-configuration-metadata"));
			});
		project.getTasks()
			.named(LifecycleBasePlugin.CHECK_TASK_NAME)
			.configure((check) -> check.dependsOn(checkAdditional, checkGenerated));
	}

	private void offerMetadata(Project project, SourceSet main) {
		ObjectFactory objects = project.getObjects();
		project.getConfigurations()
			.consumable(Configurations.CONFIGURATION_PROPERTIES_METADATA,
					(configuration) -> configuration.attributes((attributes) -> {
						attributes.attribute(Category.CATEGORY_ATTRIBUTE,
								objects.named(Category.class, Category.DOCUMENTATION));
						attributes.attribute(Usage.USAGE_ATTRIBUTE,
								objects.named(Usage.class, Configurations.CONFIGURATION_PROPERTIES_METADATA_USAGE));
					}));
		project.getArtifacts().add(Configurations.CONFIGURATION_PROPERTIES_METADATA, generatedMetadata(project, main));
	}

	private Provider<RegularFile> generatedMetadata(Project project, SourceSet main) {
		return project.getTasks()
			.named(main.getCompileJavaTaskName(), JavaCompile.class)
			.flatMap((compile) -> compile.getDestinationDirectory().file(Locations.CONFIGURATION_METADATA_FILE));
	}

	private Provider<RegularFile> report(Project project, String name) {
		return project.getLayout().getBuildDirectory().file("reports/%s/check.txt".formatted(name));
	}

}
