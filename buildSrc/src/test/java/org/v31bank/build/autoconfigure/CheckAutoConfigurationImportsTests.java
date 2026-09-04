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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.api.tasks.VerificationException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.v31bank.build.constant.Locations;
import org.v31bank.build.constant.Tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link CheckAutoConfigurationImports}.
 *
 * @author Xander Wang
 */
class CheckAutoConfigurationImportsTests {

	private static final String A = "com.example.AaAutoConfiguration";

	private static final String B = "com.example.BbAutoConfiguration";

	@TempDir
	private Path directory;

	@Test
	void passesWhenEveryEntryNamesAnAnnotatedClass() {
		ClassFiles.autoConfiguration(A).writeTo(classes());
		ClassFiles.autoConfiguration(B).writeTo(classes());
		writeImports(A, B);
		CheckAutoConfigurationImports task = task();
		assertThatCode(task::check).doesNotThrowAnyException();
		assertThat(report(task)).isEmpty();
	}

	@Test
	void failsWhenAnEntryNamesAClassThatIsNotThere() {
		ClassFiles.autoConfiguration(A).writeTo(classes());
		writeImports(A, B);
		CheckAutoConfigurationImports task = task();
		assertThatExceptionOfType(VerificationException.class).isThrownBy(task::check)
			.withMessageContaining(Locations.AUTO_CONFIGURATION_IMPORTS_FILE)
			.withMessageContaining(Locations.FAILURE_REPORT_FILE);
		assertThat(report(task)).contains("'%s' was not found".formatted(B));
	}

	@Test
	void failsWhenAnEntryNamesAClassThatLostItsAnnotation() {
		ClassFiles.plainClass(A).writeTo(classes());
		writeImports(A);
		CheckAutoConfigurationImports task = task();
		assertThatExceptionOfType(VerificationException.class).isThrownBy(task::check);
		assertThat(report(task)).contains("'%s' is not annotated with @AutoConfiguration".formatted(A));
	}

	@Test
	void failsWhenTheEntriesAreOutOfOrderAndWritesOutTheOrderToUse() {
		ClassFiles.autoConfiguration(A).writeTo(classes());
		ClassFiles.autoConfiguration(B).writeTo(classes());
		writeImports(B, A);
		CheckAutoConfigurationImports task = task();
		assertThatExceptionOfType(VerificationException.class).isThrownBy(task::check);
		assertThat(report(task)).contains("sorted alphabetically");
		assertThat(read(outputDirectory(task).resolve("sorted-" + Locations.AUTO_CONFIGURATION_IMPORTS_FILE_NAME)))
			.containsSubsequence(A, B);
	}

	@Test
	void readsTheFileTheWaySpringBootDoes() {
		ClassFiles.autoConfiguration(A).writeTo(classes());
		writeImports("# the one this module registers", "", A + "   ", "");
		assertThatCode(task()::check).doesNotThrowAnyException();
	}

	@Test
	void hasNothingToReadWhenTheModuleHasNoImportsFile() {
		ClassFiles.autoConfiguration(A).writeTo(classes());
		assertThat(task().getSource().isEmpty()).isTrue();
	}

	private CheckAutoConfigurationImports task() {
		Project project = ProjectBuilder.builder().withProjectDir(this.directory.toFile()).build();
		CheckAutoConfigurationImports task = project.getTasks()
			.register(Tasks.CHECK_AUTO_CONFIGURATION_IMPORTS, CheckAutoConfigurationImports.class)
			.get();
		task.getResources().from(resources().toFile());
		task.getClasspath().from(classes().toFile());
		return task;
	}

	private String report(AutoConfigurationImportsTask task) {
		return read(outputDirectory(task).resolve(Locations.FAILURE_REPORT_FILE));
	}

	private Path outputDirectory(AutoConfigurationImportsTask task) {
		return task.getOutputDirectory().get().getAsFile().toPath();
	}

	private Path classes() {
		return this.directory.resolve("classes");
	}

	private Path resources() {
		return this.directory.resolve("resources");
	}

	private String read(Path file) {
		try {
			return Files.readString(file);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to read " + file, ex);
		}
	}

	private void writeImports(String... lines) {
		Path importsFile = resources().resolve(Locations.AUTO_CONFIGURATION_IMPORTS_FILE);
		try {
			Files.createDirectories(importsFile.getParent());
			Files.writeString(importsFile, String.join(System.lineSeparator(), lines) + System.lineSeparator());
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to write " + importsFile, ex);
		}
	}

}
