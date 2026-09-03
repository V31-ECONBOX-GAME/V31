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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import org.v31bank.build.constant.Locations;
import org.v31bank.build.util.PropertiesFiles;

/**
 * What one module offers, written where the build can gather it across modules.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public abstract class AutoConfigurationMetadata extends DefaultTask {

	public AutoConfigurationMetadata() {
		getModuleName().convention(getProject().provider(getProject()::getName));
	}

	@Input
	public abstract Property<String> getModuleName();

	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract RegularFileProperty getAutoConfigurationImports();

	@Classpath
	public abstract ConfigurableFileCollection getClassesDirectories();

	@OutputFile
	public abstract RegularFileProperty getOutputFile();

	public void setSourceSet(SourceSet sourceSet) {
		getAutoConfigurationImports()
			.set(new File(sourceSet.getOutput().getResourcesDir(), Locations.AUTO_CONFIGURATION_IMPORTS_FILE));
		getClassesDirectories().from(sourceSet.getOutput().getClassesDirs());
		dependsOn(sourceSet.getOutput());
	}

	@TaskAction
	void documentAutoConfiguration() throws IOException {
		Properties metadata = new Properties();
		metadata.setProperty("module", getModuleName().get());
		metadata.setProperty("autoConfigurationClassNames", String.join(",", publicClassNames()));
		Path outputFile = getOutputFile().getAsFile().get().toPath();
		Files.createDirectories(outputFile.getParent());
		Files.write(outputFile, PropertiesFiles.render(metadata));
	}

	private Set<String> publicClassNames() {
		Set<String> publicClassNames = new LinkedHashSet<>();
		for (String className : AutoConfigurationImports.read(getAutoConfigurationImports().getAsFile().get())) {
			if (AutoConfigurationClass.isPublic(classFileOf(className))) {
				publicClassNames.add(className);
			}
		}
		return publicClassNames;
	}

	private Path classFileOf(String className) {
		return AutoConfigurationClass.classFileOf(className, getClassesDirectories().getFiles())
			.orElseThrow(
					() -> new IllegalStateException("Auto-configuration class '%s' not found.".formatted(className)));
	}

}
