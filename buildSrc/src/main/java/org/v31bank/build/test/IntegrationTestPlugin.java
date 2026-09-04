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

package org.v31bank.build.test;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.plugins.ide.idea.model.IdeaModule;

import org.v31bank.build.constant.Coordinates;
import org.v31bank.build.constant.Tasks;
import org.v31bank.build.util.SourceSets;

/**
 * Adds an {@code intTest} source set and task.
 *
 * <pre class="code">
 * plugins {
 *     id("org.v31bank.integration-test")
 * }
 * </pre>
 *
 * @author Xander Wang
 */
public class IntegrationTestPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPlugins().withType(JavaPlugin.class, (_) -> configureIntegrationTesting(project));
	}

	private void configureIntegrationTesting(Project project) {
		SourceSet intTestSourceSet = createSourceSet(project);
		TaskProvider<Test> task = createTestTask(project, intTestSourceSet);
		project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME).configure((check) -> check.dependsOn(task));
		project.getDependencies()
			.add(intTestSourceSet.getRuntimeOnlyConfigurationName(), Coordinates.JUNIT_PLATFORM_LAUNCHER);
		declareAsTestSources(project, intTestSourceSet);
	}

	private void declareAsTestSources(Project project, SourceSet intTestSourceSet) {
		project.getPluginManager().apply(IdeaPlugin.class);
		IdeaModule module = project.getExtensions().getByType(IdeaModel.class).getModule();
		module.getTestSources().from(SourceSets.of(intTestSourceSet).java().sourceDirectories());
		module.getTestResources().from(SourceSets.of(intTestSourceSet).resources().sourceDirectories());
	}

	private SourceSet createSourceSet(Project project) {
		SourceSetContainer sourceSets = SourceSets.of(project).unwrap();
		SourceSet intTestSourceSet = sourceSets.create(Tasks.INT_TEST);
		SourceSet main = SourceSets.of(project).main().unwrap();
		intTestSourceSet.setCompileClasspath(intTestSourceSet.getCompileClasspath().plus(main.getOutput()));
		intTestSourceSet.setRuntimeClasspath(intTestSourceSet.getRuntimeClasspath().plus(main.getOutput()));
		return intTestSourceSet;
	}

	private TaskProvider<Test> createTestTask(Project project, SourceSet intTestSourceSet) {
		return project.getTasks().register(Tasks.INT_TEST, Test.class, (task) -> {
			task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
			task.setDescription("Runs integration tests.");
			task.setTestClassesDirs(intTestSourceSet.getOutput().getClassesDirs());
			task.setClasspath(intTestSourceSet.getRuntimeClasspath());
			task.shouldRunAfter(JavaPlugin.TEST_TASK_NAME);
			// Gradle's native-platform loads native code, which a later JDK will refuse.
			task.jvmArgs("--enable-native-access=ALL-UNNAMED");
		});
	}

}
