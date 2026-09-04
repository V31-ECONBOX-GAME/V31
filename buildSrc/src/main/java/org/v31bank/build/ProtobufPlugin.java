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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.plugins.ide.idea.model.IdeaModule;

import org.v31bank.build.constant.Configurations;
import org.v31bank.build.constant.Coordinates;
import org.v31bank.build.constant.Locations;
import org.v31bank.build.constant.Projects;
import org.v31bank.build.constant.Tasks;
import org.v31bank.build.proto.BufTask;
import org.v31bank.build.proto.GenerateProtoSources;
import org.v31bank.build.proto.LintProto;
import org.v31bank.build.util.Directories;
import org.v31bank.build.util.IsolatedProjects;
import org.v31bank.build.util.SourceSets;

/**
 * Generates a project's Java sources from the {@code .proto} it is named after.
 *
 * @author Xander Wang
 */
public class ProtobufPlugin implements Plugin<Project> {

	private static final String PROTO = "proto";

	private static final List<String> API = List.of(Coordinates.PROTOBUF_JAVA, Coordinates.GRPC_PROTOBUF,
			Coordinates.GRPC_STUB);

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(JavaLibraryPlugin.class);
		project.getPluginManager().apply(DeployedPlugin.class);
		addApiDependencies(project);
		apiFor(project).ifPresent((api) -> generate(project, api));
	}

	private void addApiDependencies(Project project) {
		Provider<List<Dependency>> dependencies = project
			.provider(() -> API.stream().map(project.getDependencies()::create).toList());
		project.getConfigurations()
			.getByName(JavaPlugin.API_CONFIGURATION_NAME)
			.getDependencies()
			.addAllLater(dependencies);
	}

	private Optional<Api> apiFor(Project project) {
		List<Api> matches = apisIn(Directories.rootDirOf(project).dir(PROTO).getAsFile(),
				IsolatedProjects.rootOf(project).getName())
			.filter((api) -> project.getName().contains(api.name()))
			.toList();
		if (matches.size() > 1) {
			throw new GradleException(
					project.getPath() + " is named after more than one API " + matches.stream().map(Api::name).toList()
							+ ", so which one it wants cannot be read from its name.");
		}
		return matches.stream().findFirst();
	}

	private Stream<Api> apisIn(File root, String namespace) {
		File[] directories = new File(root, namespace).listFiles();
		return (directories != null) ? Stream.of(directories)
			.filter((file) -> file.isDirectory() && !file.getName().startsWith("."))
			.map((file) -> new Api(root, namespace, file.getName())) : Stream.empty();
	}

	private void declareAsGeneratedSources(Project project) {
		SourceSets.Directories java = SourceSets.of(project).main().java();
		java.unwrap().srcDir(SourceSets.of(project).generatedSources());
		project.getPluginManager().apply(IdeaPlugin.class);
		IdeaModule module = project.getExtensions().getByType(IdeaModel.class).getModule();
		module.getGeneratedSourceDirs()
			.add(java.directory((file) -> file.toPath().endsWith(Locations.GENERATED_SOURCES_DIRECTORY)));
	}

	private void generate(Project project, Api api) {
		declareAsGeneratedSources(project);
		Provider<Directory> toolchain = toolchain(project);
		Provider<RegularFile> buf = toolchain.map((tools) -> tools.file(Locations.BUF_EXECUTABLE));
		TaskProvider<GenerateProtoSources> generate = bufTask(project, Tasks.GENERATE_PROTO_SOURCES,
				GenerateProtoSources.class, api, buf, (task) -> {
					task.setDescription("Generates this project's sources from the " + api.name() + " proto.");
					task.getProtoc().set(toolchain.map((tools) -> tools.file(Locations.PROTOC_EXECUTABLE)));
					task.getGrpcJavaGenerator()
						.set(toolchain.map((tools) -> tools.file(Locations.GRPC_JAVA_GENERATOR_EXECUTABLE)));
					task.getDestination().set(SourceSets.of(project).generatedSources());
					task.getManifest().set(project.getLayout().getBuildDirectory().file("proto/generated-sources.txt"));
				});
		project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME).configure((compile) -> compile.dependsOn(generate));
		TaskProvider<LintProto> lint = bufTask(project, Tasks.LINT_PROTO, LintProto.class, api, buf, (task) -> {
			task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
			task.setDescription("Checks the " + api.name() + " API against the rules in buf.yaml.");
			task.getReport().set(project.getLayout().getBuildDirectory().file("proto/lint-report.txt"));
		});
		project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME).configure((check) -> check.dependsOn(lint));
	}

	private <T extends BufTask> TaskProvider<T> bufTask(Project project, String name, Class<T> type, Api api,
			Provider<RegularFile> buf, Action<? super T> configure) {
		return project.getTasks().register(name, type, (task) -> {
			task.getApi().set(api.path());
			task.getBuf().set(buf);
			task.getProtoDirectory().set(api.root());
			task.onlyIf("the " + api.name() + " API has a .proto", (_) -> api.holdsProtos());
			configure.execute(task);
		});
	}

	private Provider<Directory> toolchain(Project project) {
		ConfigurationContainer configurations = project.getConfigurations();
		Configuration declared = configurations.dependencyScope(Configurations.PROTO_TOOLCHAIN).get();
		declared.getDependencies()
			.add(project.getDependencies()
				.project(Map.of("path", Projects.ROOT, "configuration", Configurations.PROTO_TOOLCHAIN)));
		Configuration resolved = configurations
			.resolvable(Configurations.RESOLVED_PROTO_TOOLCHAIN, (configuration) -> configuration.extendsFrom(declared))
			.get();
		return project.getLayout().dir(resolved.getElements().map(ProtobufPlugin::toolchainOf));
	}

	private static File toolchainOf(Set<FileSystemLocation> resolved) {
		if (resolved.size() != 1) {
			throw new GradleException("%s offers %d proto toolchains through %s %s, so none of them is the one meant."
				.formatted(Projects.ROOT, resolved.size(), Configurations.PROTO_TOOLCHAIN, resolved));
		}
		return resolved.iterator().next().getAsFile();
	}

	private record Api(File root, String namespace, String name) {

		String path() {
			return this.namespace + "/" + this.name;
		}

		File directory() {
			return new File(this.root, path());
		}

		boolean holdsProtos() {
			try (Stream<Path> files = Files.walk(directory().toPath())) {
				return files.anyMatch((file) -> file.toString().endsWith(".proto"));
			}
			catch (IOException ex) {
				throw new UncheckedIOException("Failed to look inside " + directory(), ex);
			}
		}

	}

}
