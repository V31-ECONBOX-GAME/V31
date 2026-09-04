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
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import org.v31bank.build.constant.Locations;

/**
 * A project's source sets.
 *
 * @author Xander Wang
 */
public final class SourceSets {

	private final Project project;

	private SourceSets(Project project) {
		this.project = project;
	}

	public static SourceSets of(Project project) {
		return new SourceSets(project);
	}

	public static Sources of(SourceSet sourceSet) {
		return new Sources(sourceSet);
	}

	public SourceSetContainer unwrap() {
		return this.project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
	}

	public Sources main() {
		return named(SourceSet.MAIN_SOURCE_SET_NAME);
	}

	public Directory generatedSources() {
		return this.project.getLayout().getProjectDirectory().dir(Locations.GENERATED_SOURCES_DIRECTORY);
	}

	public Sources named(String name) {
		return new Sources(unwrap().getByName(name));
	}

	public static final class Sources {

		private final SourceSet sourceSet;

		private Sources(SourceSet sourceSet) {
			this.sourceSet = sourceSet;
		}

		public SourceSet unwrap() {
			return this.sourceSet;
		}

		public Directories java() {
			return new Directories(this.sourceSet.getJava());
		}

		public Directories resources() {
			return new Directories(this.sourceSet.getResources());
		}

		public Directories allJava() {
			return new Directories(this.sourceSet.getAllJava());
		}

		public Directories allSource() {
			return new Directories(this.sourceSet.getAllSource());
		}

	}

	public static final class Directories {

		private final SourceDirectorySet sources;

		private Directories(SourceDirectorySet sources) {
			this.sources = sources;
		}

		public SourceDirectorySet unwrap() {
			return this.sources;
		}

		public Set<File> srcDirs() {
			return this.sources.getSrcDirs();
		}

		public FileCollection sourceDirectories() {
			return this.sources.getSourceDirectories();
		}

		public List<String> relativeTo(Directory base) {
			Path root = base.getAsFile().toPath();
			return srcDirs().stream().map((directory) -> root.relativize(directory.toPath()).toString()).toList();
		}

		public File directory() {
			return directory((_) -> true);
		}

		public File directory(Predicate<File> filter) {
			List<File> matches = srcDirs().stream().filter(filter).toList();
			if (matches.size() != 1) {
				throw new GradleException(this.sources.getName() + " has " + matches.size()
						+ " source directories to choose from " + matches + ", so none of them is the one meant.");
			}
			return matches.getFirst();
		}

	}

}
