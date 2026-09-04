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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.v31bank.build.ConventionsPlugin;
import org.v31bank.build.DeployedPlugin;
import org.v31bank.build.constant.Configurations;
import org.v31bank.build.constant.Tasks;
import org.v31bank.build.optional.OptionalDependenciesPlugin;
import org.v31bank.build.task.TaskDependencies;
import org.v31bank.build.util.SourceSets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AutoConfigurationPlugin}.
 *
 * @author Xander Wang
 */
class AutoConfigurationPluginTests {

	private static final String DEPENDENCY_MANAGEMENT = "dependencyManagement";

	@TempDir
	private File directory;

	@Test
	void publishesTheModule() {
		assertThat(project().getPlugins().hasPlugin(DeployedPlugin.class)).isTrue();
	}

	@Test
	void decidesNothingAboutHowTheModuleIsBuilt() {
		Project project = module();
		project.getPlugins().apply(AutoConfigurationPlugin.class);
		assertThat(project.getPlugins().hasPlugin(JavaPlugin.class)).isFalse();
		assertThat(project.getPlugins().hasPlugin(DeployedPlugin.class)).isTrue();
		assertThat(project.getTasks().findByName(Tasks.CHECK_AUTO_CONFIGURATION_IMPORTS)).isNull();
		assertThat(project.getTasks().findByName(Tasks.CHECK_AUTO_CONFIGURATION_CLASSES)).isNull();
		assertThat(project.getTasks().findByName(Configurations.AUTO_CONFIGURATION_METADATA)).isNull();
	}

	@Test
	void configuresAModuleBuiltWithPlainJava() {
		Project project = moduleBuiltWith("java");
		assertThat(project.getTasks().findByName(Tasks.CHECK_AUTO_CONFIGURATION_IMPORTS)).isNotNull();
		assertThat(project.getTasks().findByName(Configurations.AUTO_CONFIGURATION_METADATA)).isNotNull();
		assertThat(project.getConfigurations().findByName(Configurations.AUTO_CONFIGURATION_REQUIRED_CLASSPATH))
			.isNotNull();
	}

	@Test
	void configuresAModuleWhoseJavaPluginArrivesAfterwards() {
		Project project = module();
		project.getPlugins().apply(AutoConfigurationPlugin.class);
		project.getPlugins().apply("java-library");
		assertThat(project.getTasks().findByName(Tasks.CHECK_AUTO_CONFIGURATION_CLASSES)).isNotNull();
	}

	@Test
	void compilesWithTheProcessorAnAutoConfigurationModuleNeedsAndNoOther() {
		Configuration annotationProcessor = project().getConfigurations()
			.getByName(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME);
		assertThat(annotationProcessor.getDependencies()).extracting(Dependency::getName)
			.contains("spring-boot-autoconfigure-processor")
			.doesNotContain("spring-boot-configuration-processor");
	}

	@Test
	void registersACheckOfTheFileAndACheckOfTheClasses() {
		Project project = project();
		assertThat(project.getTasks().findByName(Tasks.CHECK_AUTO_CONFIGURATION_IMPORTS))
			.isInstanceOf(CheckAutoConfigurationImports.class);
		assertThat(project.getTasks().findByName(Tasks.CHECK_AUTO_CONFIGURATION_CLASSES))
			.isInstanceOf(CheckAutoConfigurationClasses.class);
	}

	@Test
	void runsBothChecksAsPartOfCheck() {
		Project project = project();
		Task check = project.getTasks().getByName(LifecycleBasePlugin.CHECK_TASK_NAME);
		assertThat(TaskDependencies.namesOf(check.getDependsOn())).contains(Tasks.CHECK_AUTO_CONFIGURATION_IMPORTS,
				Tasks.CHECK_AUTO_CONFIGURATION_CLASSES);
	}

	@Test
	void presentsBothChecksAsVerificationTasks() {
		Project project = project();
		for (String name : List.of(Tasks.CHECK_AUTO_CONFIGURATION_IMPORTS, Tasks.CHECK_AUTO_CONFIGURATION_CLASSES)) {
			assertThat(project.getTasks().getByName(name).getGroup()).as(name)
				.isEqualTo(LifecycleBasePlugin.VERIFICATION_GROUP);
		}
	}

	@Test
	void readsTheMainSourceSetOfTheProject() {
		Project project = project();
		SourceSet main = SourceSets.of(project).main().unwrap();
		for (String name : List.of(Tasks.CHECK_AUTO_CONFIGURATION_IMPORTS, Tasks.CHECK_AUTO_CONFIGURATION_CLASSES)) {
			AutoConfigurationImportsTask task = (AutoConfigurationImportsTask) project.getTasks().getByName(name);
			assertThat(task.getClasspath().getFiles()).as(name)
				.containsExactlyInAnyOrderElementsOf(main.getOutput().getClassesDirs().getFiles());
			assertThat(TaskDependencies.namesOf(task.getClasspath().getBuildDependencies().getDependencies(task)))
				.as(name)
				.containsExactly(main.getCompileJavaTaskName());
		}
	}

	@Test
	void takesTheRequiredClasspathFromEverythingTheModuleAlwaysResolves() {
		Project project = project();
		SourceSet main = SourceSets.of(project).main().unwrap();
		Configuration required = project.getConfigurations()
			.getByName(Configurations.AUTO_CONFIGURATION_REQUIRED_CLASSPATH);
		assertThat(required.isCanBeResolved()).isTrue();
		assertThat(required.isCanBeConsumed()).isFalse();
		assertThat(required.isCanBeDeclared()).isFalse();
		assertThat(required.getDependencies()).isEmpty();
		assertThat(parentNames(required)).contains(main.getImplementationConfigurationName(),
				main.getRuntimeOnlyConfigurationName());
	}

	@Test
	void reachesTheExportedDependenciesThroughImplementation() {
		Project project = project();
		Configuration implementation = project.getConfigurations()
			.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME);
		assertThat(parentNames(implementation)).contains(JavaPlugin.API_CONFIGURATION_NAME);
	}

	@Test
	void namesTheDerivedClasspathsSoThatTheyCarryThePlatform() {
		Project project = project();
		project.getPlugins().apply(OptionalDependenciesPlugin.class);
		for (String name : List.of(Configurations.AUTO_CONFIGURATION_REQUIRED_CLASSPATH,
				Configurations.AUTO_CONFIGURATION_OPTIONAL_CLASSPATH)) {
			assertThat(name).endsWith("Classpath");
			assertThat(parentNames(project.getConfigurations().getByName(name))).as(name)
				.contains(DEPENDENCY_MANAGEMENT);
		}
	}

	@Test
	void asksAboutOptionalDependenciesOnlyWhenTheModuleHasSome() {
		Project project = project();
		assertThat(project.getConfigurations().findByName(Configurations.AUTO_CONFIGURATION_OPTIONAL_CLASSPATH))
			.isNull();
		project.getPlugins().apply(OptionalDependenciesPlugin.class);
		Configuration optional = project.getConfigurations()
			.getByName(Configurations.AUTO_CONFIGURATION_OPTIONAL_CLASSPATH);
		assertThat(optional.isCanBeResolved()).isTrue();
		assertThat(parentNames(optional)).contains(Configurations.OPTIONAL);
	}

	@Test
	void feedsTheOptionalDependenciesToTheCheckThatJudgesThem() {
		Project project = project();
		project.getPlugins().apply(OptionalDependenciesPlugin.class);
		CheckAutoConfigurationClasses check = (CheckAutoConfigurationClasses) project.getTasks()
			.getByName(Tasks.CHECK_AUTO_CONFIGURATION_CLASSES);
		assertThat(check.getOptionalDependencies().getFrom()).isNotEmpty();
	}

	@Test
	void registersTheMetadataTask() {
		assertThat(project().getTasks().findByName(Configurations.AUTO_CONFIGURATION_METADATA))
			.isInstanceOf(AutoConfigurationMetadata.class);
	}

	@Test
	void offersTheMetadataThroughAConfigurationOfItsOwn() {
		Project project = project();
		assertThat(project.getConfigurations().findByName(Configurations.AUTO_CONFIGURATION_METADATA)).isNotNull();
		assertThat(project.getConfigurations().getByName(Configurations.AUTO_CONFIGURATION_METADATA).getArtifacts())
			.singleElement()
			.satisfies((artifact) -> assertThat(
					TaskDependencies.namesOf(artifact.getBuildDependencies().getDependencies(null)))
				.contains(Configurations.AUTO_CONFIGURATION_METADATA));
	}

	@Test
	void keepsTheMetadataOutOfTheModuleItDescribes() {
		Project project = project();
		AutoConfigurationMetadata metadata = (AutoConfigurationMetadata) project.getTasks()
			.getByName(Configurations.AUTO_CONFIGURATION_METADATA);
		assertThat(metadata.getOutputFile().get().getAsFile()).hasName("auto-configuration-metadata.properties")
			.hasParent(project.getLayout().getBuildDirectory().get().getAsFile());
	}

	@Test
	void describesTheModuleThatShipsRatherThanTheOneThatIsWritten() {
		Project project = project();
		SourceSet main = SourceSets.of(project).main().unwrap();
		AutoConfigurationMetadata metadata = (AutoConfigurationMetadata) project.getTasks()
			.getByName(Configurations.AUTO_CONFIGURATION_METADATA);
		AutoConfigurationImportsTask check = (AutoConfigurationImportsTask) project.getTasks()
			.getByName(Tasks.CHECK_AUTO_CONFIGURATION_IMPORTS);
		assertThat(metadata.getAutoConfigurationImports().get().getAsFile())
			.hasParent(new File(main.getOutput().getResourcesDir(), "META-INF/spring"));
		assertThat(check.getResources().getFrom()).containsExactly(main.getResources());
	}

	@Test
	void saysWhatTheMetadataIsSoThatItCanBeCollected() {
		Configuration metadata = project().getConfigurations().getByName(Configurations.AUTO_CONFIGURATION_METADATA);
		assertThat(metadata.isCanBeConsumed()).isTrue();
		assertThat(metadata.isCanBeResolved()).isFalse();
		assertThat(metadata.getAttributes().getAttribute(Category.CATEGORY_ATTRIBUTE)).extracting(Category::getName)
			.isEqualTo(Category.DOCUMENTATION);
		assertThat(metadata.getAttributes().getAttribute(Usage.USAGE_ATTRIBUTE)).extracting(Usage::getName)
			.isEqualTo("auto-configuration-metadata");
	}

	private static Set<String> parentNames(Configuration configuration) {
		return configuration.getExtendsFrom().stream().map(Configuration::getName).collect(Collectors.toSet());
	}

	private Project project() {
		return moduleBuiltWith("java-library");
	}

	private Project moduleBuiltWith(String javaPlugin) {
		Project project = module();
		project.getPlugins().apply(javaPlugin);
		project.getPlugins().apply(AutoConfigurationPlugin.class);
		return project;
	}

	private Project module() {
		Project root = ProjectBuilder.builder().withName("v31").withProjectDir(this.directory).build();
		Project platform = ProjectBuilder.builder().withName("platform").withParent(root).build();
		ProjectBuilder.builder().withName("v31-internal-dependencies").withParent(platform).build();
		Project project = ProjectBuilder.builder().withName("v31-example-spring-boot").withParent(root).build();
		project.getExtensions().getExtraProperties().set("buildJavaVersion", "25");
		project.getExtensions().getExtraProperties().set("runtimeJavaVersion", "25");
		project.getExtensions().getExtraProperties().set("checkstyleToolVersion", "12.3.1");
		project.getPlugins().apply(ConventionsPlugin.class);
		return project;
	}

}
