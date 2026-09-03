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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.VerificationException;

import org.v31bank.build.constant.Locations;

/**
 * Fails an imports file that names a missing or unannotated class, or is not sorted.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public abstract class CheckAutoConfigurationImports extends AutoConfigurationImportsTask {

	@TaskAction
	void check() {
		File importsFile = getSource().getSingleFile();
		List<String> imports = loadImports();
		List<String> problems = new ArrayList<>();
		imports.forEach((registered) -> checkRegistered(registered).ifPresent(problems::add));
		checkSorted(imports).ifPresent(problems::add);
		File report = writeReport(report(importsFile, problems));
		if (!problems.isEmpty()) {
			throw new VerificationException("%s check failed. See '%s' for details"
				.formatted(Locations.AUTO_CONFIGURATION_IMPORTS_FILE, report));
		}
	}

	private Optional<String> checkRegistered(String className) {
		Optional<Path> classFile = AutoConfigurationClass.classFileOf(className, getClasspath().getFiles());
		if (classFile.isEmpty()) {
			return Optional.of("'%s' was not found".formatted(className));
		}
		if (AutoConfigurationClass.of(classFile.get()).isEmpty()) {
			return Optional.of("'%s' is not annotated with @AutoConfiguration".formatted(className));
		}
		return Optional.empty();
	}

	private Optional<String> checkSorted(List<String> imports) {
		List<String> sorted = imports.stream().sorted().toList();
		if (sorted.equals(imports)) {
			return Optional.empty();
		}
		File sortedFile = getOutputDirectory().file("sorted-" + Locations.AUTO_CONFIGURATION_IMPORTS_FILE_NAME)
			.get()
			.getAsFile();
		write(sortedFile,
				sorted.stream().collect(Collectors.joining(System.lineSeparator(), "", System.lineSeparator())));
		return Optional
			.of("entries should be sorted alphabetically (expected content written to %s)".formatted(sortedFile));
	}

	private String report(File importsFile, List<String> problems) {
		if (problems.isEmpty()) {
			return "";
		}
		return "Found problems in '%s':%n%s%n".formatted(importsFile,
				problems.stream().map((problem) -> "  - " + problem).collect(Collectors.joining("%n".formatted())));
	}

}
