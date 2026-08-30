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

package org.v31bank.build.proto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskProvider;

import org.v31bank.build.constant.Configurations;
import org.v31bank.build.constant.Coordinates;
import org.v31bank.build.constant.GradleProperties;
import org.v31bank.build.constant.Locations;
import org.v31bank.build.constant.Projects;
import org.v31bank.build.constant.Tasks;
import org.v31bank.build.util.Bom;

/**
 * Installs buf and the generators it runs, once for the whole build.
 * <p>
 * Declared by the root project:
 *
 * <pre class="code">
 * plugins {
 *     id("org.v31bank.proto-toolchain")
 * }
 * </pre>
 *
 * The three executables weigh more than everything else the build downloads put together,
 * so there is one install and every API project shares it. A project cannot register a
 * task in another one, so the install belongs to the project that holds it and is handed
 * out through {@link Configurations#PROTO_TOOLCHAIN} the way a jar is.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class ProtoToolchainPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		// A BOM answers only a project that resolves Java libraries, and the generator
		// versions are read from one.
		project.getPluginManager().apply(JavaBasePlugin.class);
		Bom platform = Bom.of(project, project.getDependencies().project(Projects.INTERNAL_DEPENDENCIES));
		TaskProvider<Install> install = project.getTasks()
			.register(Tasks.INSTALL_PROTO_TOOLCHAIN, Install.class, (task) -> {
				task.setDescription("Fetches buf and the generators it runs.");
				task.getBuf()
					.set(fromMaven(project, Coordinates.BUF,
							project.property(GradleProperties.BUF_VERSION).toString()));
				task.getProtoc()
					.set(fromMaven(project, Coordinates.PROTOC, platform.version(Coordinates.PROTOBUF_JAVA)));
				task.getGrpcJavaGenerator()
					.set(fromMaven(project, Coordinates.GRPC_JAVA_GENERATOR,
							platform.version(Coordinates.GRPC_PROTOBUF)));
				task.getDestination()
					.set(project.getLayout().getBuildDirectory().dir(Locations.PROTO_TOOLCHAIN_DIRECTORY));
			});
		project.getConfigurations().consumable(Configurations.PROTO_TOOLCHAIN);
		project.getArtifacts().add(Configurations.PROTO_TOOLCHAIN, install.flatMap(Install::getDestination));
	}

	/**
	 * Resolves one executable from Maven.
	 * @param project the project the install belongs to
	 * @param coordinate the module the executable is published as
	 * @param version the version to fetch
	 * @return the file Maven serves
	 */
	private Provider<RegularFile> fromMaven(Project project, String coordinate, String version) {
		// buf's own registry answered resource_exhausted once every build called it.
		String full = coordinate + ":" + version + ":" + platform() + "@exe";
		return project.getLayout()
			.file(project.provider(() -> project.getConfigurations()
				.detachedConfiguration(project.getDependencies().create(full))
				.getSingleFile()));
	}

	private static String platform() {
		String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
		String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
		boolean arm = arch.contains("aarch64") || arch.contains("arm64");
		if (os.contains("mac") || os.contains("darwin")) {
			return arm ? "osx-aarch_64" : "osx-x86_64";
		}
		if (os.contains("windows")) {
			return "windows-x86_64";
		}
		return arm ? "linux-aarch_64" : "linux-x86_64";
	}

	/**
	 * Copies the three executables into one directory. Maven serves each of them as an
	 * ordinary file, so the copy is also where they are made executable.
	 */
	public abstract static class Install extends DefaultTask {

		@InputFile
		@PathSensitive(PathSensitivity.NONE)
		public abstract RegularFileProperty getBuf();

		@InputFile
		@PathSensitive(PathSensitivity.NONE)
		public abstract RegularFileProperty getProtoc();

		@InputFile
		@PathSensitive(PathSensitivity.NONE)
		public abstract RegularFileProperty getGrpcJavaGenerator();

		@OutputDirectory
		public abstract DirectoryProperty getDestination();

		@TaskAction
		void install() throws IOException {
			Path destination = getDestination().get().getAsFile().toPath();
			Files.createDirectories(destination);
			install(getBuf(), destination.resolve(Locations.BUF_EXECUTABLE));
			install(getProtoc(), destination.resolve(Locations.PROTOC_EXECUTABLE));
			install(getGrpcJavaGenerator(), destination.resolve(Locations.GRPC_JAVA_GENERATOR_EXECUTABLE));
		}

		private void install(RegularFileProperty tool, Path target) throws IOException {
			File resolved = tool.get().getAsFile();
			Files.copy(resolved.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
			if (!target.toFile().setExecutable(true)) {
				throw new GradleException("Copied " + target + " but could not make it executable");
			}
		}

	}

}
