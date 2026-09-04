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

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.v31bank.build.constant.Configurations;
import org.v31bank.build.util.SourceSets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OptionalDependenciesPlugin}.
 *
 * @author Xander Wang
 */
class OptionalDependenciesPluginTests {

	private static final String OPTIONAL = Configurations.OPTIONAL;

	private static final String DEPENDENCY = "widget";

	private static final String EXPORTED = "core";

	@TempDir
	private File directory;

	@Test
	void addsTheConfigurationToAProjectThatCompilesNoJava() {
		assertThat(project().getConfigurations().findByName(OPTIONAL)).isNotNull();
	}

	@Test
	void declaresDependenciesWithoutResolvingThem() {
		assertThat(optional(javaProject()).isCanBeResolved()).isFalse();
	}

	@Test
	void declaresDependenciesAndNothingElse() {
		Configuration optional = optional(javaProject());
		assertThat(optional.isCanBeResolved()).isFalse();
		assertThat(optional.isCanBeConsumed()).isFalse();
	}

	@Test
	void compilesAndTestsAgainstAnOptionalDependency() {
		Project project = javaProjectDeclaring(DEPENDENCY);
		for (String classpath : List.of("compileClasspath", "runtimeClasspath", "testCompileClasspath",
				"testRuntimeClasspath")) {
			assertThat(dependencyNames(project, classpath)).as(classpath).contains(DEPENDENCY);
		}
	}

	@Test
	void reachesASourceSetCreatedAfterwards() {
		Project project = javaProjectDeclaring(DEPENDENCY);
		SourceSet added = SourceSets.of(project).unwrap().create("intTest");
		assertThat(dependencyNames(project, added.getCompileClasspathConfigurationName())).contains(DEPENDENCY);
		assertThat(dependencyNames(project, added.getRuntimeClasspathConfigurationName())).contains(DEPENDENCY);
	}

	@Test
	void wiresUpWhenTheJavaPluginArrivedFirst() {
		Project project = ProjectBuilder.builder().withProjectDir(this.directory).build();
		project.getPlugins().apply("java-library");
		project.getPlugins().apply(OptionalDependenciesPlugin.class);
		project.getDependencies().add(OPTIONAL, "com.example:" + DEPENDENCY + ":1.0");
		assertThat(dependencyNames(project, "compileClasspath")).contains(DEPENDENCY);
	}

	@Test
	void keepsOptionalDependenciesOutOfWhatAConsumerResolves() {
		Project project = javaProjectDeclaring(DEPENDENCY);
		for (String configuration : List.of("apiElements", "runtimeElements", "api", "implementation")) {
			assertThat(dependencyNames(project, configuration)).as(configuration)
				.contains(EXPORTED)
				.doesNotContain(DEPENDENCY);
		}
	}

	private static Configuration optional(Project project) {
		return project.getConfigurations().getByName(OPTIONAL);
	}

	private static Set<String> dependencyNames(Project project, String configuration) {
		return project.getConfigurations()
			.getByName(configuration)
			.getAllDependencies()
			.stream()
			.map(Dependency::getName)
			.collect(Collectors.toSet());
	}

	private Project project() {
		Project project = ProjectBuilder.builder().withProjectDir(this.directory).build();
		project.getPlugins().apply(OptionalDependenciesPlugin.class);
		return project;
	}

	private Project javaProject() {
		Project project = project();
		project.getPlugins().apply("java-library");
		return project;
	}

	private Project javaProjectDeclaring(String artifact) {
		Project project = javaProject();
		project.getDependencies().add(OPTIONAL, "com.example:" + artifact + ":1.0");
		project.getDependencies().add("api", "com.example:" + EXPORTED + ":1.0");
		return project;
	}

}
