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

package org.v31bank.build;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;

import org.v31bank.build.util.IsolatedProjects;

/**
 * Plugin for projects that need deploying.
 *
 * @author Xander Wang
 */
public class DeployedPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(MavenRepositoryPlugin.class);
		PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
		MavenPublication publication = publishing.getPublications()
			.create(IsolatedProjects.rootOf(project).getName(), MavenPublication.class);
		publishComponent(project, publication, JavaPlugin.class, "java");
		publishComponent(project, publication, JavaPlatformPlugin.class, "javaPlatform");
		allowDependenciesWithoutVersions(project);
	}

	private void allowDependenciesWithoutVersions(Project project) {
		project.getTasks()
			.withType(GenerateModuleMetadata.class)
			.configureEach((task) -> task.getSuppressedValidationErrors().add("dependencies-without-versions"));
	}

	private void publishComponent(Project project, MavenPublication publication,
			Class<? extends Plugin<Project>> pluginType, String componentName) {
		project.getPlugins()
			.withType(pluginType,
					(_) -> project.getComponents()
						.matching((component) -> componentName.equals(component.getName()))
						.all(publication::from));
	}

}
