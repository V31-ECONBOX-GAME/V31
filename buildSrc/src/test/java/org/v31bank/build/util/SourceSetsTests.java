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

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.v31bank.build.task.TaskDependencies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link SourceSets}.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
class SourceSetsTests {

	private static final String GENERATED = "build/generated/source/jooq";

	@TempDir
	private File directory;

	private Project project;

	@BeforeEach
	void createJavaProject() {
		this.project = ProjectBuilder.builder().withProjectDir(this.directory).build();
		this.project.getPlugins().apply("java");
	}

	@Test
	void findsTheSourceSetsOfAJavaProject() {
		assertThat(SourceSets.of(this.project).unwrap().getNames()).contains("main", "test");
	}

	@Test
	void findsTheMainSourceSet() {
		assertThat(SourceSets.of(this.project).main().unwrap().getName()).isEqualTo("main");
	}

	@Test
	void startsFromASourceSetAlreadyInHand() {
		SourceSet main = SourceSets.of(this.project).main().unwrap();
		assertThat(SourceSets.of(main).resources().srcDirs()).containsExactly(path("src/main/resources"));
	}

	@Test
	void separatesJavaFromResources() {
		assertThat(SourceSets.of(this.project).main().java().srcDirs()).containsExactly(path("src/main/java"));
		assertThat(SourceSets.of(this.project).main().resources().srcDirs())
			.containsExactly(path("src/main/resources"));
	}

	@Test
	void gathersEveryKindOfSourceTogether() {
		assertThat(SourceSets.of(this.project).main().allSource().srcDirs()).contains(path("src/main/java"),
				path("src/main/resources"));
		assertThat(SourceSets.of(this.project).main().allJava().srcDirs()).contains(path("src/main/java"));
	}

	@Test
	void saysWhereItIsRootedRelativeToWhateverIsAsked() {
		mainJava().unwrap().srcDir(GENERATED);
		assertThat(mainJava().relativeTo(this.project.getLayout().getProjectDirectory()))
			.containsExactly("src/main/java", GENERATED);
	}

	@Test
	void keepsHoldOfWhateverBuildsADirectoryItWasGiven() {
		mainJava().unwrap()
			.srcDir(this.project.getTasks()
				.register("generateSources", DefaultTask.class,
						(task) -> task.getOutputs().dir(this.project.getLayout().getBuildDirectory().dir(GENERATED))));
		assertThat(mainJava().srcDirs()).contains(path("build/" + GENERATED));
		assertThat(
				TaskDependencies.namesOf(mainJava().sourceDirectories().getBuildDependencies().getDependencies(null)))
			.containsExactly("generateSources");
	}

	@Test
	void takesTheOnlyDirectoryWithoutBeingTold() {
		assertThat(mainJava().directory()).isEqualTo(path("src/main/java"));
	}

	@Test
	void refusesToChooseBetweenSeveral() {
		mainJava().unwrap().srcDir(GENERATED);
		assertThatExceptionOfType(GradleException.class).isThrownBy(() -> mainJava().directory())
			.withMessageContaining("2 source directories");
	}

	@Test
	void takesTheDirectoryTheFilterLeaves() {
		mainJava().unwrap().srcDir(GENERATED);
		assertThat(mainJava().directory((dir) -> !dir.getPath().contains("generated")))
			.isEqualTo(path("src/main/java"));
		assertThat(mainJava().directory((dir) -> dir.getPath().contains("generated"))).isEqualTo(path(GENERATED));
	}

	@Test
	void refusesAFilterThatLeavesNothing() {
		assertThatExceptionOfType(GradleException.class)
			.isThrownBy(() -> mainJava().directory((dir) -> dir.getName().equals("kotlin")))
			.withMessageContaining("0 source directories");
	}

	@Test
	void locatesAKindOfSourceOtherThanJava() {
		assertThat(SourceSets.of(this.project).main().resources().directory()).isEqualTo(path("src/main/resources"));
	}

	private SourceSets.Directories mainJava() {
		return SourceSets.of(this.project).main().java();
	}

	private File path(String relative) {
		return new File(this.project.getProjectDir(), relative);
	}

}
