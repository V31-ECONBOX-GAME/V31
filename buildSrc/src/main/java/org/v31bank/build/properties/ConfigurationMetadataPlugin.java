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

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.jvm.tasks.ProcessResources;

import org.v31bank.build.constant.Configurations;
import org.v31bank.build.constant.Locations;
import org.v31bank.build.constant.Tasks;
import org.v31bank.build.util.SourceSets;

/**
 * Checks hand-written configuration metadata and offers it onward.
 *
 * <pre class="code">
 * plugins {
 *     `java-library`
 *     id("org.v31bank.configuration-metadata")
 * }
 * </pre>
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class ConfigurationMetadataPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPlugins().withType(JavaPlugin.class, (_) -> configure(project));
	}

	private void configure(Project project) {
		SourceSet main = SourceSets.of(project).main().unwrap();
		Provider<File> metadata = metadata(project, main);
		TaskProvider<CheckManualSpringConfigurationMetadata> check = project.getTasks()
			.register(Tasks.CHECK_MANUAL_CONFIGURATION_METADATA, CheckManualSpringConfigurationMetadata.class,
					(task) -> {
						task.setDescription("Checks the hand-written configuration metadata of the main source set.");
						task.getMetadataLocation().set(metadata);
						task.getReportLocation()
							.set(project.getLayout()
								.getBuildDirectory()
								.file("reports/manual-spring-configuration-metadata/check.txt"));
					});
		offerMetadata(project, metadata);
		project.getTasks()
			.named(LifecycleBasePlugin.CHECK_TASK_NAME)
			.configure((lifecycle) -> lifecycle.dependsOn(check));
	}

	private Provider<File> metadata(Project project, SourceSet main) {
		return project.getTasks()
			.named(main.getProcessResourcesTaskName(), ProcessResources.class)
			.map((processResources) -> new File(processResources.getDestinationDir(),
					Locations.CONFIGURATION_METADATA_FILE));
	}

	private void offerMetadata(Project project, Provider<File> metadata) {
		ObjectFactory objects = project.getObjects();
		project.getConfigurations()
			.consumable(Configurations.CONFIGURATION_PROPERTIES_METADATA,
					(configuration) -> configuration.attributes((attributes) -> {
						attributes.attribute(Category.CATEGORY_ATTRIBUTE,
								objects.named(Category.class, Category.DOCUMENTATION));
						attributes.attribute(Usage.USAGE_ATTRIBUTE,
								objects.named(Usage.class, Configurations.CONFIGURATION_PROPERTIES_METADATA_USAGE));
					}));
		project.getArtifacts().add(Configurations.CONFIGURATION_PROPERTIES_METADATA, metadata);
	}

}
