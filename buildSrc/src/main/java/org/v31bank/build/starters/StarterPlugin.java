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

package org.v31bank.build.starters;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import org.v31bank.build.DeployedPlugin;
import org.v31bank.build.classpath.CheckClasspathForConflicts;
import org.v31bank.build.classpath.CheckClasspathForUnconstrainedDirectDependencies;
import org.v31bank.build.classpath.CheckClasspathForUnnecessaryExclusions;
import org.v31bank.build.classpath.ClasspathCheck;
import org.v31bank.build.constant.Configurations;
import org.v31bank.build.constant.Locations;

/**
 * Publishes a starter and checks the dependency graph it hands a consumer.
 *
 * @author Xander Wang
 */
public class StarterPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(JavaLibraryPlugin.class);
		project.getPluginManager().apply(DeployedPlugin.class);
		Configuration runtimeClasspath = project.getConfigurations()
			.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
		registerMetadata(project, runtimeClasspath);
		registerClasspathCheck(project, runtimeClasspath, "ForConflicts", CheckClasspathForConflicts.class);
		registerClasspathCheck(project, runtimeClasspath, "ForUnconstrainedDirectDependencies",
				CheckClasspathForUnconstrainedDirectDependencies.class);
		registerClasspathCheck(project, runtimeClasspath, "ForUnnecessaryExclusions",
				CheckClasspathForUnnecessaryExclusions.class);
	}

	private void registerMetadata(Project project, Configuration runtimeClasspath) {
		TaskProvider<StarterMetadata> metadata = project.getTasks()
			.register(Configurations.STARTER_METADATA, StarterMetadata.class, (task) -> {
				task.setDescription("Generates metadata describing the starter.");
				task.setDependencies(runtimeClasspath);
				task.getDestination()
					.set(project.getLayout().getBuildDirectory().file(Locations.STARTER_METADATA_FILE));
			});
		project.getConfigurations().consumable(Configurations.STARTER_METADATA);
		project.getArtifacts().add(Configurations.STARTER_METADATA, metadata.map(StarterMetadata::getDestination));
	}

	private <T extends ClasspathCheck> void registerClasspathCheck(Project project, Configuration classpath,
			String suffix, Class<T> type) {
		TaskProvider<T> check = project.getTasks()
			.register(checkTaskName(classpath.getName() + suffix), type, (task) -> {
				task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
				task.setClasspath(classpath);
			});
		project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME).configure((task) -> task.dependsOn(check));
	}

	private static String checkTaskName(String subject) {
		return "check" + Character.toUpperCase(subject.charAt(0)) + subject.substring(1);
	}

}
